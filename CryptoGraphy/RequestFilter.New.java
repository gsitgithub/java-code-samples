package com.intercepter;

import CryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * Created by kuldeep on 2/26/14.
 */
@Slf4j
public class RequestFilter implements Filter {

    private static class ByteArrayServletResponseStream extends ServletOutputStream {
        ByteArrayOutputStream baos;

        ByteArrayServletResponseStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }
        public void write(int param) throws IOException {
            baos.write(param);
        }
    }

    private static class ByteArrayPrintWriter {
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private PrintWriter pw = new PrintWriter(baos);
        private ServletOutputStream sos = new ByteArrayServletResponseStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }
        public ServletOutputStream getStream() {
            return sos;
        }
        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private class BufferedServletRequestStream extends ServletInputStream {
        ByteArrayInputStream bais;

        public BufferedServletRequestStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }
        public int available() {
            return bais.available();
        }
        public int read() {
            return bais.read();
        }
        public int read(byte[] buf, int off, int len) {
            return bais.read(buf, off, len);
        }
    }

    private class BufferedRequestWrapper extends HttpServletRequestWrapper {
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        BufferedServletRequestStream bsis;
        byte[] buffer;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
            InputStream is = req.getInputStream();
            baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int letti;
            while ((letti = is.read(buf)) > 0) {
                baos.write(buf, 0, letti);
            }
            buffer = baos.toByteArray();
        }
        public BufferedRequestWrapper(HttpServletRequest req, String body) throws IOException {
            super(req);
            InputStream is = req.getInputStream();
            baos = new ByteArrayOutputStream();
            baos.write(body.getBytes());
            buffer = baos.toByteArray();
        }
        public ServletInputStream getInputStream() {
            try {
                bais = new ByteArrayInputStream(buffer);
                bsis = new BufferedServletRequestStream(bais);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bsis;
        }

        public byte[] getBuffer() {
            return buffer;
        }
    }

    private boolean dumpRequest;
    private boolean dumpResponse;

    public void init(FilterConfig filterConfig) throws ServletException {
//        dumpRequest = Boolean.valueOf(filterConfig.getInitParameter("requestFilter"));
//        dumpResponse = Boolean.valueOf(filterConfig.getInitParameter("responseFilter"));
    }

    private void processEncrypt(final ServletRequest servletRequest ,final HttpServletRequest httpRequest,final ServletResponse servletResponse,
                          final FilterChain filterChain)throws IOException, ServletException{


        InputStream inputStream = servletRequest.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int letti=-1;
        while ((letti = inputStream.read(buf)) > 0){
            byteArrayOutputStream.write(buf, 0, letti);
        }
        final BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest,doEncrypt(byteArrayOutputStream));

        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
        HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
            public PrintWriter getWriter() {
                return pw.getWriter();
            }
            public ServletOutputStream getOutputStream() {
                return pw.getStream();
            }
        };

        filterChain.doFilter(bufferedRequest, wrappedResp);


    }

    private String doEncrypt(ByteArrayOutputStream byteArrayOutputStream){
        String byteString=new String(byteArrayOutputStream.toByteArray());
        if(!StringUtils.isEmpty(byteString)){
            log.info("Request : " + byteString);
            try {
                byteString= CryptionUtils.Decrypt(byteString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("decrypt "+byteString);
        }
        return byteString;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String isEncryptHeader =  httpRequest.getHeader("data-encrypt");
        if(!StringUtils.isEmpty(isEncryptHeader)){
            Boolean isEncrypt =  Boolean.parseBoolean(isEncryptHeader);
            if(isEncrypt){
                processEncrypt(servletRequest, httpRequest, servletResponse, filterChain);
            }

        }else{
            throw new ServletException("Encrypt key not found");
        }


    }

    public void destroy() {
    }

}