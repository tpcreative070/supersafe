package co.tpcreative.supersafe.common
import android.util.Base64
import java.io.*
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

//class JealousSky protected constructor() {
//    private val TAG = JealousSky::class.java.simpleName
//    private val INIT_ILLEGAL_ARG_KEY: String? = "Invalid Key Argument"
//    private val INIT_ILLEGAL_ARG_SALT: String? = "Invalid Salt Argument"
//    private val DEFAULT_ITERATIONS = 1024
//    private val DEFAULT_KEY_LENGTH = 128
//    private val SECURE_RANDOM_ALGORITHM: String? = "SHA1PRNG"
//    private val SECRETKEY_DIGEST_SHA1: String? = "SHA1"
//    private val SECRETKEY_ALGORITHM_SHA1: String? = "PBKDF2WithHmacSHA1"
//    private val SECRETKEY_ALGORITHM_CBC: String? = "PBEWithSHA256And256BitAES-CBC-BC"
//    private val ENCRYPTION_ALGORITHM_PCKS5: String? = "AES/CBC/PKCS5Padding"
//    private val ENCRYPTION_ALGORITHM_NOPAD: String? = "AES/CBC/NoPadding"
//    private var key: String? = null
//    private var salt: ByteArray? = null
//    private var iv: ByteArray? = null
//    private var secureRandom: SecureRandom? = null
//    private var ivParameterSpec: IvParameterSpec? = null
//
//    /**
//     * Initialization for SecureRandom, IVParamSpec, Key (Password) and Salt
//     * @param key Password used for Encryption/Decryption
//     * @param salt Salt used Password derivation
//     * @throws IllegalArgumentException
//     * @throws NoSuchAlgorithmException
//     */
//    @Throws(IllegalArgumentException::class, NoSuchAlgorithmException::class)
//    fun initialize(key: String?, salt: String?) {
//        require(isValidArgKey(key)) { INIT_ILLEGAL_ARG_KEY }
//        this.key = key
//        require(isValidArg(salt)) { INIT_ILLEGAL_ARG_SALT }
//        this.salt = hexStringToByteArray(salt)
//        iv = ByteArray(16)
//        secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM)
//        ivParameterSpec = IvParameterSpec(iv)
//    }
//
//    /**
//     * Encryption Method
//     * @param clearString String to Encrypt
//     * @return Byte Array of Encrypted String
//     * @throws IOException
//     * @throws NoSuchAlgorithmException
//     * @throws NoSuchPaddingException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeyException
//     * @throws InvalidKeySpecException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     */
//    @Throws(IOException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, InvalidKeySpecException::class, BadPaddingException::class, IllegalBlockSizeException::class)
//    fun encrypt(clearString: String?): ByteArray? {
//        if (clearString == null || clearString.isEmpty()) {
//            return null
//        }
//        val cipher = getCipher(Cipher.ENCRYPT_MODE)
//        return cipher.doFinal(clearString.toByteArray(charset("UTF8")))
//    }
//
//    /**
//     * Encryption Method Wrapper
//     * @param clearString String to Encrypt
//     * @return Encrypted String
//     * @throws NoSuchPaddingException
//     * @throws InvalidKeyException
//     * @throws NoSuchAlgorithmException
//     * @throws IOException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeySpecException
//     */
//    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, InvalidKeySpecException::class)
//    fun encryptToString(clearString: String?): String? {
//        return Base64.encodeToString(encrypt(clearString), Base64.DEFAULT)
//    }
//
//    /**
//     * Encryption Method Wrapper
//     * @param fileInput InputStream of File to Encrypt
//     * @return Byte Array of Encryped InputStream
//     * @throws IOException
//     * @throws NoSuchAlgorithmException
//     * @throws NoSuchPaddingException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeyException
//     * @throws InvalidKeySpecException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     */
//    @Throws(IOException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, InvalidKeySpecException::class, BadPaddingException::class, IllegalBlockSizeException::class)
//    fun encrypt(fileInput: InputStream?): ByteArray? {
//        val cipher = getCipher(Cipher.ENCRYPT_MODE)
//        return getEncryptInputStream(fileInput, cipher)
//    }
//
//    /**
//     * Encryption Method Wrapper
//     * @param fileInput InputStream of File to Encrypt
//     * @param fileNameOutput File thumbnailName of Encrypted File
//     * @return Encrypted File
//     * @throws NoSuchPaddingException
//     * @throws InvalidKeyException
//     * @throws NoSuchAlgorithmException
//     * @throws IOException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeySpecException
//     */
//    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, InvalidKeySpecException::class)
//    fun encryptToFile(fileInput: InputStream?, fileNameOutput: String?): File? {
//        if (fileNameOutput == null || fileNameOutput.isEmpty()) {
//            return null
//        }
//        val fileOutput = File(fileNameOutput)
//        val out = FileOutputStream(fileOutput)
//        val encrypted = encrypt(fileInput)
//        out.write(encrypted)
//        out.flush()
//        out.close()
//        return fileOutput
//    }
//
//    /**
//     * Decryption Method
//     * @param encryptedString String to Decrypt
//     * @return Byte Array of Decrypted String
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     * @throws IOException
//     * @throws NoSuchPaddingException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeyException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     */
//    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, IOException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
//    fun decrypt(encryptedString: String?): ByteArray? {
//        if (encryptedString == null || encryptedString.isEmpty()) {
//            return null
//        }
//        val cipher = getCipher(Cipher.DECRYPT_MODE)
//        return cipher.doFinal(Base64.decode(encryptedString, Base64.DEFAULT))
//    }
//
//    /**
//     * Decryption Method Wrapper
//     * @param encryptedString String to Decrypt
//     * @return Decrypted String
//     * @throws NoSuchPaddingException
//     * @throws InvalidKeyException
//     * @throws NoSuchAlgorithmException
//     * @throws IOException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeySpecException
//     */
//    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, InvalidKeySpecException::class)
//    fun decryptToString(encryptedString: String?): String? {
//        return String(decrypt(encryptedString))
//    }
//
//    /**
//     * Decryption Method Wrapper
//     * @param fileInput InputStream to Decrypt
//     * @return Byte Array of Decrypted InputStream
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     * @throws IOException
//     * @throws NoSuchPaddingException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeyException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     */
//    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, IOException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
//    fun decrypt(fileInput: InputStream?): ByteArray? {
//        if (fileInput == null || fileInput.available() == 0) {
//            return null
//        }
//        val cipher = getCipher(Cipher.DECRYPT_MODE)
//        return getDecryptFromCipherInputStream(fileInput, cipher)
//    }
//
//    /**
//     * Decryption Method Wrapper
//     * @param fileInput InputStream to Decrypt
//     * @param fileNameOutput File thumbnailName of Decrypted File
//     * @return Decrypted File
//     * @throws NoSuchPaddingException
//     * @throws InvalidKeyException
//     * @throws NoSuchAlgorithmException
//     * @throws IOException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeySpecException
//     */
//    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, InvalidKeySpecException::class)
//    fun decryptToFile(fileInput: InputStream?, fileNameOutput: String?): File? {
//        if (fileNameOutput == null || fileNameOutput.isEmpty()) {
//            return null
//        }
//        val fileOutput = File(fileNameOutput)
//        val outdec = FileOutputStream(fileOutput)
//        val decryptedStream = decrypt(fileInput)
//        outdec.write(decryptedStream)
//        outdec.flush()
//        outdec.close()
//        return fileOutput
//    }
//
//    /**
//     * Decryption Method Wrapper
//     * @param fileInput InputStream to Decrypt
//     * @return Bitmap of Decrypted InputStream
//     * @throws NoSuchPaddingException
//     * @throws InvalidKeyException
//     * @throws NoSuchAlgorithmException
//     * @throws IOException
//     * @throws BadPaddingException
//     * @throws IllegalBlockSizeException
//     * @throws InvalidAlgorithmParameterException
//     * @throws InvalidKeySpecException
//     */
//    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, InvalidKeySpecException::class)
//    fun decryptToBitmap(fileInput: InputStream?): Bitmap? {
//        return BitmapFactory.decodeStream(ByteArrayInputStream(decrypt(fileInput)))
//    }
//
//    //----------------------------------------------------------------------------------------------
//    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class, InvalidKeySpecException::class)
//    private fun getSecretKey(secretKeyAlgorithm: String?): SecretKey? {
//        val factory = SecretKeyFactory.getInstance(secretKeyAlgorithm)
//        val spec: KeySpec = PBEKeySpec(hashTheKey(key), salt, DEFAULT_ITERATIONS, DEFAULT_KEY_LENGTH)
//        val tmp = factory.generateSecret(spec)
//        return SecretKeySpec(tmp.encoded, "AES")
//    }
//
//    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
//    private fun hashTheKey(key: String?): CharArray? {
//        val messageDigest = MessageDigest.getInstance(SECRETKEY_DIGEST_SHA1)
//        messageDigest.update(key.toByteArray(charset("UTF8")))
//        return Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP).toCharArray()
//    }
//
//    private fun isValidArgKey(arg: String?): Boolean {
//        return arg != null && !arg.isEmpty()
//    }
//
//    private fun isValidArg(arg: String?): Boolean {
//        return arg != null && !arg.isEmpty() && arg.length % 2 == 0 && arg.length >= 16
//    }
//
//    private fun hexStringToByteArray(s: String?): ByteArray? {
//        val len = s.length
//        val data = ByteArray(len / 2)
//        var i = 0
//        while (i < len) {
//            data[i / 2] = ((Character.digit(s.get(i), 16) shl 4)
//                    + Character.digit(s.get(i + 1), 16)) as Byte
//            i += 2
//        }
//        return data
//    }
//
//    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, UnsupportedEncodingException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class)
//    private fun getCipher(operationMode: Int): Cipher? {
//        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_PCKS5)
//        cipher.init(
//                operationMode,
//                getSecretKey(SECRETKEY_ALGORITHM_SHA1),
//                ivParameterSpec,
//                secureRandom)
//        return cipher
//    }
//
//    @Throws(IOException::class)
//    private fun getDecryptFromCipherInputStream(fis: InputStream?, cipher: Cipher?): ByteArray? {
//        val bos = ByteArrayOutputStream()
//        val cis = CipherInputStream(fis, cipher)
//        val data = ByteArray(16)
//        var read: Int
//        while (cis.read(data).also { read = it } != -1) {
//            bos.write(data, 0, read)
//            bos.flush()
//        }
//        if (bos != null) {
//            bos.flush()
//            bos.close()
//        }
//        fis?.close()
//        cis?.close()
//        return bos.toByteArray()
//    }
//
//    @Throws(IOException::class)
//    private fun getByteArrayFromFile(fileInput: InputStream?): ByteArray? {
//        val bos = ByteArrayOutputStream()
//        while (fileInput.available() > 0) {
//            bos.write(fileInput.read())
//        }
//        if (bos != null) {
//            bos.flush()
//            bos.close()
//        }
//        fileInput?.close()
//        return bos.toByteArray()
//    }
//
//    @Throws(IOException::class)
//    private fun getByteArrayFromFile(fileInput: File?): ByteArray? {
//        val fis: InputStream = BufferedInputStream(FileInputStream(fileInput))
//        val bos = ByteArrayOutputStream()
//        while (fis.available() > 0) {
//            bos.write(fis.read())
//        }
//        if (bos != null) {
//            bos.flush()
//            bos.close()
//        }
//        fis?.close()
//        return bos.toByteArray()
//    }
//
//    @Throws(IOException::class)
//    private fun getEncryptInputStream(fis: InputStream?, cipher: Cipher?): ByteArray? {
//        val bos = ByteArrayOutputStream()
//        val cos = CipherOutputStream(bos, cipher)
//        val data = ByteArray(16)
//        var read: Int
//        while (fis.read(data).also { read = it } != -1) {
//            cos.write(data, 0, read)
//            cos.flush()
//        }
//        cos?.close()
//        bos?.close()
//        fis?.close()
//        return bos.toByteArray()
//    }
//
//    companion object {
//        private var instance: JealousSky? = null
//        fun getInstance(): JealousSky? {
//            if (instance == null) {
//                instance = JealousSky()
//            }
//            return instance
//        }
//    }
//}