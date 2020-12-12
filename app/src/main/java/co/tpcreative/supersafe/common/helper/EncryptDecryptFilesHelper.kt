package co.tpcreative.supersafe.common.helper
import android.content.Context
import android.os.Environment
import co.tpcreative.supersafe.common.encypt.EncryptConfiguration
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.encodeBase64
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.util.ImmutablePair
import co.tpcreative.supersafe.common.util.SizeUnit
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.User
import java.io.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptDecryptFilesHelper {
    fun encryptTextPKCS7(value : String, mode : Int) : String? {
        try {
            if (checkConfig()){
                return null
            }
            return encryptPKCS7(value.toByteArray(),mode)?.encodeBase64()
        }
        catch (e : Exception){
            e.printStackTrace()
            return null
        }
    }

    fun createFile(path: String?, content: String?): Boolean {
        try {
            if (checkConfig()){
                return false
            }
            return createFile(path, content?.toByteArray())
        }catch (e : Exception){
            e.printStackTrace()
            return false
        }
    }

    fun readTextFile(path: String?): String? {
        try {
            if (checkConfig()){
                return null
            }
            val bytes = readFile(path)
            return bytes?.let { String(it) }
        }
        catch (e : Exception){
            e.printStackTrace()
            return null
        }
    }

    private fun getSecretKey(): String? {
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            if (user._id != null) {
                Utils.Log(TAG, "Get secret key " + user._id)
               return user._id
            }
            Utils.Log(TAG, "secret id is null")
        } else {
            Utils.Log(TAG, "Get secret key null")
        }
        return null
    }

    private fun getConfigurationFile(): EncryptConfiguration? {
        getSecretKey()?.let {
            configurationFile = EncryptConfiguration.Builder()
                    .setChuckSize(1024 * 2)
                    .setEncryptContent(SecurityUtil.IVX, it, SecurityUtil.SALT)
                    .build()
            Utils.Log(TAG, "config files")
            return configurationFile
        }
        Utils.Log(TAG, "config files")
        return configurationFile
    }

    private fun checkConfig() : Boolean{
        if (configurationFile==null){
            configurationFile = getConfigurationFile()
        }
        if (configurationFile==null){
            return true
        }
        return false
    }

    fun createFile(output: File?, input: File?, mode: Int): Boolean{
        try {
            if (checkConfig()){
                return false
            }
            try {
                val fis = FileInputStream(input)
                val bufferLength = 1024*1024
                val buffer = ByteArray(bufferLength)
                val fos = FileOutputStream(output)
                val bos = BufferedOutputStream(fos, bufferLength)
                var read = 0
                read = fis.read(buffer, 0, read)
                while (read != -1) {
                    val mData = encrypt(buffer, mode)
                    bos.write(mData, 0, read)
                    read = fis.read(buffer) // if read value is -1, it escapes loop.
                }
                fis.close()
                bos.flush()
                bos.close()
                return true
            } catch (exception: IOException) {
                exception.printStackTrace()
                return false
            }
        }catch (e : Exception){
            e.printStackTrace()
            return false
        }
    }

    fun createCipherFile(output: File?, input: File?, cipher: Int): Boolean {
        try {
            if (checkConfig()){
                return false
            }
            if (configurationFile?.isEncrypted != true) {
                return false
            }
            var inputStream: FileInputStream? = null
            val cipherOutputStream: CipherOutputStream
            try {
                inputStream = FileInputStream(input)
                val outputStream = FileOutputStream(output)
                cipherOutputStream = CipherOutputStream(outputStream, getCipher(cipher))
                //note the following line
                val buffer = ByteArray(1024 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead)
                }
                cipherOutputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                return false
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (ignored: IOException) {
                        ignored.printStackTrace()
                    }
                }
            }
            return true
        }catch (e : Exception){
            e.printStackTrace()
            return false
        }
    }

    /*Create temporary file*/
    fun createFileByteDataNoEncrypt(context: Context, data: ByteArray?)  : File{
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "picture.jpg")
        var os: OutputStream? = null
        try {
            os = FileOutputStream(file)
            os.write(data)
        } catch (e: IOException) {
            Utils.Log(TAG, "Cannot write to $file")
        } finally {
            if (os != null) {
                try {
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return file
    }

    fun getCipher(mode: Int): Cipher? {
        if (checkConfig()){
            return null
        }
        try {
            return if (Cipher.ENCRYPT_MODE == mode){
                val mSecretKeySpec = SecretKeySpec(configurationFile?.secretKey,SecurityUtil.AES_ALGORITHM)
                val mIvParameterSpec = IvParameterSpec(configurationFile?.ivParameter)
                val mCipherEncrypted = Cipher.getInstance(SecurityUtil.AES_TRANSFORMATION)
                mCipherEncrypted?.init(mode, mSecretKeySpec, mIvParameterSpec)
                mCipherEncrypted
            }else{
                val mSecretKeySpec = SecretKeySpec(configurationFile?.secretKey,SecurityUtil.AES_ALGORITHM)
                val mIvParameterSpec = IvParameterSpec(configurationFile?.ivParameter)
                val mCipherDecrypted = Cipher.getInstance(SecurityUtil.AES_TRANSFORMATION)
                mCipherDecrypted?.init(mode, mSecretKeySpec, mIvParameterSpec)
                mCipherDecrypted
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getSize(file: File, unit: SizeUnit): Double {
        val length = file.length()
        return length.toDouble() / unit.inBytes().toDouble()
    }

    fun readFile(path: String?): ByteArray? {
        val stream: FileInputStream
        return try {
            stream = FileInputStream(File(path))
            readFile(stream)
        } catch (e: FileNotFoundException) {
            Utils.Log(TAG, "Failed to read file to input stream ${e.message}")
            null
        }
    }

    fun createFile(path: String?, content: ByteArray?): Boolean {
        if (checkConfig()){
            return false
        }
        var mContent = content
        var stream: OutputStream? = null
        try {
            stream = FileOutputStream(File(path))
            if (configurationFile != null && configurationFile?.isEncrypted==true) {
                mContent = encrypt(mContent!!, Cipher.ENCRYPT_MODE)
            }
            stream.write(mContent)
        } catch (e: IOException) {
            Utils.Log(TAG, "Failed create file $e")
            return false
        } finally {
            if (stream != null) {
                try {
                    stream.flush()
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    fun readFile(stream: FileInputStream): ByteArray? {
        if (checkConfig()){
            return null
        }
        open class Reader : Thread() {
            var array: ByteArray? = null
        }
        val reader: Reader = object : Reader() {
            override fun run() {
                val chunks = LinkedList<ImmutablePair<ByteArray, Int>>()
                // read the file and build chunks
                var size = 0
                var globalSize = 0
                do {
                    try {
                        val chunkSize = if (contextClassLoader != null) configurationFile?.chuckSize else 8192
                        // read chunk
                        val buffer = ByteArray(chunkSize!!)
                        size = stream.read(buffer, 0, chunkSize)
                        if (size > 0) {
                            globalSize += size

                            // add chunk to list
                            chunks.add(ImmutablePair(buffer, size))
                        }
                    } catch (e: java.lang.Exception) {
                        // very bad
                    }
                } while (size > 0)
                try {
                    stream.close()
                } catch (e: java.lang.Exception) {
                    // very bad
                }
                array = ByteArray(globalSize)
                // append all chunks to one array
                var offset = 0
                for (chunk in chunks) {
                    // flush chunk to array
                    chunk.element2?.let { System.arraycopy(chunk.element1, 0, array, offset, it) }
                    offset += chunk.element2!!
                }
            }
        }
        reader.start()
        try {
            reader.join()
        } catch (e: InterruptedException) {
            Utils.Log(TAG, "Failed on reading file from storage while the locking Thread ${e.message}")
            return null
        }
        return if (configurationFile != null && configurationFile?.isEncrypted==true) {
            encrypt(reader.array!!, Cipher.DECRYPT_MODE)
        } else {
            reader.array
        }
    }

    @Synchronized
    private fun encrypt(content: ByteArray, encryptionMode: Int): ByteArray? {
        val secretKey: ByteArray = configurationFile?.secretKey!!
        val ivx: ByteArray = configurationFile?.ivParameter!!
        return SecurityUtil.encrypt(content, encryptionMode, secretKey, ivx)
    }

    @Synchronized
    private fun encryptPKCS7(content: ByteArray, encryptionMode: Int): ByteArray? {
        val secretKey: ByteArray = configurationFile?.secretKey!!
        val ivx: ByteArray = configurationFile?.ivParameter!!
        return SecurityUtil.encryptPKCS7(content, encryptionMode, secretKey, ivx)
    }

    fun cleanUp(){
        configurationFile = null
    }

    companion object {
        private var instance: EncryptDecryptFilesHelper? = null
        private val TAG = EncryptDecryptFilesHelper::class.java.simpleName
        var configurationFile : EncryptConfiguration? = null
        fun getInstance(): EncryptDecryptFilesHelper? {
            if (instance == null) {
                instance = EncryptDecryptFilesHelper()
            }
            return instance
        }
    }

    init {
        configurationFile = getConfigurationFile()
    }
}