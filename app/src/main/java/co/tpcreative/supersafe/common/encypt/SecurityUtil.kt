package co.tpcreative.supersafe.common.encypt
import co.tpcreative.supersafe.common.util.Utils
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class SecurityUtil {
    /**
     * Do xor operation on the string with the key
     *
     * @param msg The string to xor on
     * @param key The key by which the xor will work
     * @return The string after xor
     */
    fun xor(msg: String, key: String): String? {
        try {
            val UTF_8 = "UTF-8"
            val msgArray: ByteArray
            msgArray = msg.toByteArray(charset(UTF_8))
            val keyArray = key.toByteArray(charset(UTF_8))
            val out = ByteArray(msgArray.size)
            for (i in msgArray.indices) {
                out[i] = (msgArray[i] xor keyArray[i % keyArray.size]) as Byte
            }
            return String(out, charset(UTF_8))
        } catch (e: UnsupportedEncodingException) {
        }
        return null
    }

    companion object {
        private const val TAG = "SecurityUtil"
        const val AES_ALGORITHM = "AES"
        const val AES_TRANSFORMATION = "AES/CTR/NoPadding"
        const val AES_TRANSFORMATION_PKCS7= "AES/CTR/PKCS7Padding"
        const val SHA1_8BIT = "PBKDF2WithHmacSHA1And8bit"
        const val SHA1 = "PBKDF2WithHmacSHA1"
        const val iterationCount = 1000 // recommended by PKCS#5
        const val keyLength = 128
        const val url_developer = "http://192.168.1.3:8081"
        const val url_live = "http://tpcreative.me:8081"
        const val key_password_default = "tpcreative.co"
        const val key_password_default_encrypted = "qF6xTl5bcYOVtf4A9RND6g=="
        const val DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRwY3JlYXRpdmUuY29AZ21haWwuY29tIiwibmFtZSI6IkZyZWUiLCJyb2xlIjoiMCIsImNyZWF0ZWRfZGF0ZSI6IjExLzEzLzIwMTggMTA6NDg6MDAgUE0iLCJfaWQiOiI1YmVhZjIzMDQ2NGEwNzNmNjUzNDVjYmUiLCJpYXQiOjE1NDIxMjQwODB9.oEfdmeOTYxGnJtl1ZJtC71AELyLcNz6w6FhlTizVJdE"

        /*Encrypt key*/
        const val IVX = "1234567891234567" // 16 lenght - not secret
        const val SECRET_KEY = "secret@123456789" // 16 lenght - secret
        const val MAIL = "care@tpcreative.me"
        val SALT = "0000111100001111".toByteArray() // random 16 bytes array
        const val old_encrypt_key = ".encrypt_key"
        const val old_encrypt_fake_Key = ".encrypt_fake_key"

        const val new_encrypt_key = ".new_encrypt_key"
        const val new_encrypt_fake_Key = ".new_encrypt_fake_key"

        /**
         * Encrypt or Descrypt the content. <br></br>
         *
         * @param content        The content to encrypt or descrypt.
         * @param encryptionMode Use: [Cipher.ENCRYPT_MODE] or
         * [Cipher.DECRYPT_MODE]
         * @param secretKey      Set the secret key for encryption of file content.
         * **Important: The length must be 16 long**. *Uses SHA-256
         * to generate a hash from your key and trim the result to 128
         * bit (16 bytes)*
         * @param ivx            This is not have to be secret. It used just for better
         * randomizing the cipher. You have to use the same IV parameter
         * within the same encrypted and written files. Means, if you
         * want to have the same content after descryption then the same
         * IV must be used. *About this parameter from wiki:
         * https://en.wikipedia.org/wiki/Block_cipher_modes_of_operation
         * #Initialization_vector_.28IV.29* **Important: The length
         * must be 16 long**
         * @return
         */
        fun encrypt(content: ByteArray?, encryptionMode: Int, secretKey: ByteArray, ivx: ByteArray): ByteArray? {
            if (secretKey.size != 16 || ivx.size != 16) {
                Utils.Log(TAG, "Set the encryption parameters correctly. The must be 16 length long each")
                return null
            }
            return try {
                val mSecretKey: SecretKey = SecretKeySpec(secretKey, AES_ALGORITHM)
                val mIV = IvParameterSpec(ivx)
                val decipher = Cipher.getInstance(AES_TRANSFORMATION)
                decipher.init(encryptionMode, mSecretKey, mIV)
                decipher.doFinal(content)
            } catch (e: NoSuchAlgorithmException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Unknown Algorithm ${e.message}")
                null
            } catch (e: NoSuchPaddingException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt- Unknown Padding ${e.message}")
                null
            } catch (e: InvalidKeyException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Invalid Key ${e.message}")
                null
            } catch (e: InvalidAlgorithmParameterException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Invalid Algorithm Parameter ${e.message}")
                null
            } catch (e: IllegalBlockSizeException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt ${e.message}")
                null
            } catch (e: BadPaddingException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt ${e.message}")
                null
            }
        }

        fun encryptPKCS7(content: ByteArray?, encryptionMode: Int, secretKey: ByteArray, ivx: ByteArray): ByteArray? {
            if (secretKey.size != 16 || ivx.size != 16) {
                Utils.Log(TAG, "Set the encryption parameters correctly. The must be 16 length long each")
                return null
            }
            return try {
                val mSecretKey: SecretKey = SecretKeySpec(secretKey, AES_ALGORITHM)
                val mIV = IvParameterSpec(ivx)
                val decipher = Cipher.getInstance(AES_TRANSFORMATION_PKCS7)
                decipher.init(encryptionMode, mSecretKey, mIV)
                decipher.doFinal(content)
            } catch (e: NoSuchAlgorithmException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Unknown Algorithm ${e.message}")
                null
            } catch (e: NoSuchPaddingException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt- Unknown Padding ${e.message}")
                null
            } catch (e: InvalidKeyException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Invalid Key ${e.message}")
                null
            } catch (e: InvalidAlgorithmParameterException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt - Invalid Algorithm Parameter ${e.message}")
                null
            } catch (e: IllegalBlockSizeException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt ${e.message}")
                null
            } catch (e: BadPaddingException) {
                Utils.Log(TAG, "Failed to encrypt/descrypt ${e.message}")
                null
            }
        }
    }
}
