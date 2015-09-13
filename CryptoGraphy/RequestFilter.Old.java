package com.intercepter;

import CryptUtils;
import Cryption;
import lombok.extern.slf4j.Slf4j;
import org.mortbay.log.Log;
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

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        InputStream is = servletRequest.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int letti=-1;
        while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

        String str=new String(baos.toByteArray());
        if(!StringUtils.isEmpty(str)){
            log.info("Request : " + str);
            try {
                str=Cryption.Decrypt(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Decrypt "+str);
        }
        BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest,str);

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

        byte[] bytes = pw.toByteArray();
        String data = new String(bytes);
        if(!StringUtils.isEmpty(data)){
            try {
                response.getOutputStream().flush();
//                String encrypted = CryptUtils.Encrypt(data);
                String encrypted = CryptUtils.Encrypt("{\"password\":\"000000000000000\",\"logitude\":0,\"latitude\":0,\"macAddress\":\"000000000000000\"}");
                response.getOutputStream().write(encrypted.getBytes());
//                response.getOutputStream().write("tYVuVxTUCKhz7s3FTAK2v3SbIRRkv6N825tWfaIkUc/wzLY14OUEdZzEPhFek6NxX3h0ropTxPTshFDzCcnSwguxYo4p762CFNW2wYGM5NkLURpVwxf/BI2pepgZGMIWgygySv1YQR1PYY6Tz1nGU8h4VQWutS1otZY97ZBQx/IPmcIVLssDUJSjiyfkqM+o9LQC3Mg49uRTSd1QwR7VlQS4Q3R4gq4CxIfHkJMUUbOazMan+qhWivi9KYjVy0/oO2f/1aHB26/u48qDXmen7XmnLHhweVO2UOBP/YvfPIIjADPbVXX8naL8fzMQXnNSyxMuuxkMrxqASI+ItoLPVBptGs7gQtoXAGdSWD584anxAUySuWKQlhQyvy0X1YoPrYsZFk/fd/RSsO7Yq9tgU7JNqFaJFf3YMSszyiqyXSZ72e7H6eMLvzltc+CQsF4jpNTn17Yrys9Rxl+IIdPIts7MX1I7XnEY3XFaPCcL5Xq8R7JasNecvvV6gwIcA0E4IAcCp/8Rzza5tZtoy86rVEQ8YHO8GpkDB30zXmSNk6AUizZ5Duq86TbEGfKzRYi2M0kVklhf8ck3IVIqTpSVBduOe8NaN4xT4mrjkW8GBIj/XyT72FY+t7oM6rSP4I531jjYpVuksMxHXnT4wSq/hhkREc/X97KqGnzeu/6Tb6Y7eBwdVfDRs8PT86CuxNpP+N0+AaQ1T/U+tj+Y4IgGZ9tju62VwUrY52nWa1OxSmIYaeBc+le2V7wGg8W30C/hkYW++X9qWtia7YZpqqndwvVdEDKvG2IP1PZ4ubpjZv/BTn2iqeMNzQHXBwDwx/HmmXzNsWwQ6FYpysEC5A6IICoHEP2B73ai90Tv54kXmwYzb5Ic/ZR2TSv37+T1pwOO66qaQ0EkXMQGeF+01Ga7x6A3fdRHX7CrT70n+7WRrU9L2el2YC9eNtweDAk4gttR9+brdpRg4AEBAfitKY17Nx4yZMBSvrBcDIj60yesCA8xuk0c4mqHQAQs4AKNzjqdGba7jxkzspuO5290M4OE0q5qz42001zvn8JmVm+MdvVc/uwAXVZ57ok3LcQkTKXpdwQl/jP+hleG9aIdw4Fgitf4AkoVPFAX1noqEWWCnVmlaz+pyAXEL7227m0asAQu5AtVzxQwPjn5g6dJfIfKa/yDX1qZvO73mXgncGZ5uSxsVg3xcl9w9r2EsDX+IHys/6Bs6/WoXzVEFFekov7z4JjpxerivXCtZCm4cckiwtjXjR+PLsyn+3iBK9lZdxODQPfSWMXs+jgb2dyRdXWZXKC83em2W6IDKqw7gHK3GsA2RJK1O0ZBC5vQnmSFG7rw+JiVerQKsbvLV80BNYikRymNRUCnbmNd98KHWTe5Y50Ge5hXl2H/zcd2ARzcuQNu+h5Lkrpwkc56KS0uSqoha+zsEop86UWAFKwsI+GqZYRK/en4OEn6mXujpI+vuR88NHK24+eWHHefQEOCQXRVDwbW+osMh/QZB57zyoAaYaEvoxIimoZ1DShdLL8kMPzj".getBytes());
                response.getOutputStream().flush();
                log.info(encrypted);
                log.info(Cryption.Decrypt(encrypted));
            } catch (Exception e) {
                log.error(e.getMessage());
                response.getOutputStream().write("".getBytes());
                response.getOutputStream().flush();
            }
        }
    }

    public void destroy() {
    }

}