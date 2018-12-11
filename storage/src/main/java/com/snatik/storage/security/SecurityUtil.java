package com.snatik.storage.security;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Security utils for encryption, xor and more
 *
 * @author Roman Kushnarenko - sromku (sromku@gmail.com)
 */
public class SecurityUtil {

    private static final String TAG = "SecurityUtil";
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";
    public static final String SHA1_8BIT = "PBKDF2WithHmacSHA1And8bit";
    public static final String SHA1 = "PBKDF2WithHmacSHA1";
    public static final int iterationCount = 1000; // recommended by PKCS#5
    public static final int keyLength = 128;

    public static final String url_developer = "http://192.168.1.2:8081";
    public static final String url_live = "http://tpcreative.me:8081";

    public static final String key_password_default = "tpcreative.co";
    public static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRwY3JlYXRpdmUuY29AZ21haWwuY29tIiwibmFtZSI6IkZyZWUiLCJyb2xlIjoiMCIsImNyZWF0ZWRfZGF0ZSI6IjExLzEzLzIwMTggMTA6NDg6MDAgUE0iLCJfaWQiOiI1YmVhZjIzMDQ2NGEwNzNmNjUzNDVjYmUiLCJpYXQiOjE1NDIxMjQwODB9.oEfdmeOTYxGnJtl1ZJtC71AELyLcNz6w6FhlTizVJdE";

    /*Encrypt key*/
    public static final String IVX = "1234567891234567"; // 16 lenght - not secret
    public static final String SECRET_KEY = "secret@123456789"; // 16 lenght - secret
    public static final byte[] SALT = "0000111100001111".getBytes(); // random 16 bytes array


    /**
     * Encrypt or Descrypt the content. <br>
     *
     * @param content        The content to encrypt or descrypt.
     * @param encryptionMode Use: {@link Cipher#ENCRYPT_MODE} or
     *                       {@link Cipher#DECRYPT_MODE}
     * @param secretKey      Set the secret key for encryption of file content.
     *                       <b>Important: The length must be 16 long</b>. <i>Uses SHA-256
     *                       to generate a hash from your key and trim the result to 128
     *                       bit (16 bytes)</i>
     * @param ivx            This is not have to be secret. It used just for better
     *                       randomizing the cipher. You have to use the same IV parameter
     *                       within the same encrypted and written files. Means, if you
     *                       want to have the same content after descryption then the same
     *                       IV must be used. <i>About this parameter from wiki:
     *                       https://en.wikipedia.org/wiki/Block_cipher_modes_of_operation
     *                       #Initialization_vector_.28IV.29</i> <b>Important: The length
     *                       must be 16 long</b>
     * @return
     */
    public static byte[] encrypt(byte[] content, int encryptionMode, final byte[] secretKey, final byte[] ivx) {
        if (secretKey.length != 16 || ivx.length != 16) {
            Log.w(TAG, "Set the encryption parameters correctly. The must be 16 length long each");
            return null;
        }

        try {
            SecretKey secretkey = new SecretKeySpec(secretKey, AES_ALGORITHM);
            IvParameterSpec IV = new IvParameterSpec(ivx);
            Cipher decipher = Cipher.getInstance(AES_TRANSFORMATION);
            decipher.init(encryptionMode, secretkey, IV);
            return decipher.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Unknown Algorithm", e);
            return null;
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "Failed to encrypt/descrypt- Unknown Padding", e);
            return null;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Invalid Key", e);
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Invalid Algorithm Parameter", e);
            return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "Failed to encrypt/descrypt", e);
            return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "Failed to encrypt/descrypt", e);
            return null;
        }
    }

    /**
     * Do xor operation on the string with the key
     *
     * @param msg The string to xor on
     * @param key The key by which the xor will work
     * @return The string after xor
     */
    public String xor(String msg, String key) {
        try {
            final String UTF_8 = "UTF-8";
            byte[] msgArray;

            msgArray = msg.getBytes(UTF_8);

            byte[] keyArray = key.getBytes(UTF_8);

            byte[] out = new byte[msgArray.length];
            for (int i = 0; i < msgArray.length; i++) {
                out[i] = (byte) (msgArray[i] ^ keyArray[i % keyArray.length]);
            }
            return new String(out, UTF_8);
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }



}
