ciperDemo#cat CipherDemo.java

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
public class CipherDemo {
public static void main(String [] args) {
try {
String[] algorithm = {“AES”,”Blowfish”,”DES”};//”3DES”,”RSA”
String[] modes = {“ECB”,”CBC”,”CFB”,”OFB”,”OFB32?,”CFB8?};
String[] padding = {“PKCS5Padding”};//”NoPadding”
KeyGenerator kg = null;
Cipher cipher = null;
for(int i=0; i<algorithm.length; i++) {
SecretKey sk = KeyGenerator.getInstance(algorithm[i]).generateKey();
for(int j=0; j<modes.length; j++) {
for(int k=0; k<padding.length; k++) {
cipher = Cipher.getInstance(algorithm[i]+”/”+modes[j]+”/”+padding[k]);
cipher.init(Cipher.ENCRYPT_MODE, sk);
printeParam(cipher);
System.out.println();
byte[] encryptedText = cipher.doFinal(“data”.getBytes());
if(cipher.getIV()!=null) {
cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(cipher.getIV()));
}else{
cipher.init(Cipher.DECRYPT_MODE, sk);
}

System.out.println(“Decrypted data:”+new String(cipher.doFinal(encryptedText)));
cipher = Cipher.getInstance(algorithm[i]+”/”+modes[j]+”/”+padding[k]);
System.out.println();
}

}
}
KeyGenerator kg1 = KeyGenerator.getInstance(“AES”);//3DES, Blowfish
Cipher cipher1 = Cipher.getInstance(“AES/ECB/PKCS5Padding”);//CBC,CFB,OFB

} catch (NoSuchAlgorithmException e) {
e.printStackTrace();
} catch (NoSuchPaddingException e) {
e.printStackTrace();
} catch (InvalidKeyException e) {
e.printStackTrace();
} catch (IllegalBlockSizeException e) {
e.printStackTrace();
} catch (BadPaddingException e) {
e.printStackTrace();
} catch (InvalidAlgorithmParameterException e) {
e.printStackTrace();
}
}

private static void printeParam(Cipher ciper) {
System.out.println(“ciper.getProvider(): “+ciper.getProvider());
System.out.println(“ciper.getAlgorithm(): “+ciper.getAlgorithm());
System.out.println(“ciper.getBlockSize(): “+ciper.getBlockSize());
System.out.println(“ciper.getParameters(): “+ciper.getParameters());
System.out.println(“ciper.getIV(): “+ciper.getIV());
}

}