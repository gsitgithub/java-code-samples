package com.lib.auth;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class Cryption {
	
	public static String Encrypt(String text)
		throws Exception {
		return Encrypt(text, GetKey());
	}
	
	public static String Encrypt(String text, String key)
        throws Exception {
        
        byte[] keyBytes= new byte[16];
        byte[] b= key.getBytes("UTF-8");
        int len= b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        String result = Base64.encodeToString(results, Base64.DEFAULT);

        return result;
        }

	public static String GetKey()
	{
		String key = "LKINKDESF8_UqjpyNh?5ZJWjKDaSTp59413700K0Kf@0cQUv34E3FXP5lkRmA7KLYJPh>8;D53N8fdhhoiRu80CA<F<3EE<F;2=E56:48A:0CDC";
		return key;
	}
	
	public static String Decrypt(String text)
			throws Exception {
			return Decrypt(text, GetKey());
	}
		
	public static String Decrypt(String text, String key)
	        throws Exception {

		byte[] keyBytes= new byte[16];
        byte[] b= key.getBytes("UTF-8");
        int len= b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);

        byte[] textBytes = Base64.decode(text, Base64.DEFAULT);
        byte[] results = cipher.doFinal(textBytes);
        String result = new String(results);

        return result;
        }
}
