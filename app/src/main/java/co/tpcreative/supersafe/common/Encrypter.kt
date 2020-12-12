package co.tpcreative.supersafe.common
import android.net.Uri
import android.util.Base64
import java.io.*
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encrypter {
    var paramSpec: AlgorithmParameterSpec?
    fun encryptFile(path: String?, encrypted: Uri?): String? {
        val inFile = File(path)
        var sKey: String? = ""
        val outFile = File(encrypted?.getPath())
        try {
            val key = KeyGenerator.getInstance(ALGO_SECRET_KEY_GENERATOR).generateKey()
            val keyData = key.encoded
            val key2: SecretKey = SecretKeySpec(keyData, 0, keyData.size, ALGO_SECRET_KEY_GENERATOR)
            sKey = Base64.encodeToString(key2.encoded, Base64.DEFAULT)
            encrypt(key, paramSpec, FileInputStream(inFile), FileOutputStream(outFile))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sKey
    }

    fun decryptFile(uri: Uri?, uriOut: String?, secretKey: String?) {
        val inFile = File(uri?.getPath())
        val outFile = File(uriOut)
        val encodedKey = Base64.decode(secretKey, Base64.DEFAULT)
        val key2: SecretKey = SecretKeySpec(encodedKey, 0, encodedKey.size, "AES")
        try {
            decrypt(key2, paramSpec, FileInputStream(inFile), FileOutputStream(outFile))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val IV_LENGTH = 16 // Default length with Default 128
        private val ALGO_RANDOM_NUM_GENERATOR: String? = "SHA1PRNG"
        private val ALGO_SECRET_KEY_GENERATOR: String? = "AES"
        private const val DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE = 1024
        private val ALGO_VIDEO_ENCRYPTOR: String? = "AES/CBC/PKCS5Padding"

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IOException::class)
        fun encrypt(key: SecretKey?,
                    paramSpec: AlgorithmParameterSpec?, `in`: InputStream?, out: OutputStream?) {
            var out = out
            try {
                val c = Cipher.getInstance(ALGO_VIDEO_ENCRYPTOR)
                c.init(Cipher.ENCRYPT_MODE, key, paramSpec)
                out = CipherOutputStream(out, c)
                var count = 0
                val buffer = ByteArray(DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE)
                while (`in`?.read(buffer).also {
                            if (it != null) {
                                count = it
                            }
                        }!! >= 0) {
                    out.write(buffer, 0, count)
                }
            } finally {
                out?.close()
            }
        }

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IOException::class)
        fun decrypt(key: SecretKey?, paramSpec: AlgorithmParameterSpec?,
                    `in`: InputStream?, out: OutputStream?) {
            var out = out
            try {
                val c = Cipher.getInstance(ALGO_VIDEO_ENCRYPTOR)
                c.init(Cipher.DECRYPT_MODE, key, paramSpec)
                out = CipherOutputStream(out, c)
                var count = 0
                val buffer = ByteArray(DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE)
                while (`in`?.read(buffer).also {
                            if (it != null) {
                                count = it
                            }
                        }!! >= 0) {
                    out.write(buffer, 0, count)
                }
            } finally {
                out?.close()
            }
        }
    }

    init {
        val iv = ByteArray(IV_LENGTH)
        SecureRandom.getInstance(ALGO_RANDOM_NUM_GENERATOR).nextBytes(iv)
        paramSpec = IvParameterSpec(iv)
    }
}