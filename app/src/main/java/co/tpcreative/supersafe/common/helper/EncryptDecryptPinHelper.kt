package co.tpcreative.supersafe.common.helper
import android.content.Context
import android.os.Environment
import co.tpcreative.supersafe.common.encypt.EncryptConfiguration
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.decodeBase64
import co.tpcreative.supersafe.common.extension.encodeBase64
import co.tpcreative.supersafe.common.util.ImmutablePair
import co.tpcreative.supersafe.common.util.SizeUnit
import co.tpcreative.supersafe.common.util.Utils
import java.io.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptDecryptPinHelper {
    fun createdTextPKCS7(value: String, mode: Int) : String? {
        if (checkConfig()){
            return null
        }
        return if (mode==Cipher.ENCRYPT_MODE){
            encryptPKCS7(value.toByteArray(), mode)?.encodeBase64()
        }else{
            encryptPKCS7(value.toByteArray().decodeBase64(), mode)?.let {
                String(it)
            }
        }
    }

    fun createFile(path: String?, content: String?): Boolean {
        if (checkConfig()){
            return false
        }
        return createFile(path, content?.toByteArray())
    }
    fun readTextFile(path: String?): String? {
        if (checkConfig()){
            return null
        }
        val bytes = readFile(path)
        return bytes?.let { String(it)}
    }

    private fun getConfigurationFile(): EncryptConfiguration? {
        configurationFile = EncryptConfiguration.Builder()
                .setChuckSize(1024 * 2)
                .setEncryptContent(SecurityUtil.IVX, SecurityUtil.SECRET_KEY, SecurityUtil.SALT)
                .build()
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

    fun createFile(output: File?, input: File?, mode: Int): Boolean {
        if (checkConfig()){
            return false
        }
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(input)
            var length = 0
            val fOutputStream = FileOutputStream(
                    output)
            //note the following line
            var buffer: ByteArray? = ByteArray(1024 * 1024)
            while (inputStream.read(buffer).also { length = it } > 0) {
                if (configurationFile != null && configurationFile?.isEncrypted!!) {
                    buffer = encrypt(buffer!!, mode)
                }
                fOutputStream.write(buffer, 0, length)
            }
            fOutputStream.flush()
            fOutputStream.close()
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
    }

    fun createLargeFile(output: File?, input: File?, cipher: Cipher): Boolean {
        if (checkConfig()){
            return false
        }
        if (configurationFile == null || !(configurationFile?.isEncrypted)!!) {
            return false
        }
        var inputStream: FileInputStream? = null
        val cipherOutputStream: CipherOutputStream
        try {
            inputStream = FileInputStream(input)
            val outputStream = FileOutputStream(output)
            cipherOutputStream = CipherOutputStream(outputStream, cipher)
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
    }

    fun createFileByteDataNoEncrypt(context: Context, data: ByteArray?)  : File {
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
        if (configurationFile != null && configurationFile?.isEncrypted!!) {
            try {
                val mSecretKeySpec = SecretKeySpec(configurationFile?.secretKey, SecurityUtil.AES_ALGORITHM)
                val mIvParameterSpec = IvParameterSpec(configurationFile?.ivParameter)
                mCipher = Cipher.getInstance(SecurityUtil.AES_TRANSFORMATION)
                mCipher?.init(mode, mSecretKeySpec, mIvParameterSpec)
                return mCipher
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            if (configurationFile != null && configurationFile!!.isEncrypted) {
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
        return if (configurationFile != null && configurationFile?.isEncrypted!!) {
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

    fun deleteDirectoryImpl(path: String): Boolean {
        val directory = File(path)

        // If the directory exists then delete
        if (directory.exists()) {
            val files = directory.listFiles() ?: return true
            // Run on all sub files and folders and delete them
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    deleteDirectoryImpl(files[i].absolutePath)
                } else {
                    files[i].delete()
                }
            }
        }
        return directory.delete()
    }

    fun createDirectory(path: String): Boolean {
        val directory = File(path)
        if (directory.exists()) {
            Utils.Log(TAG, "Directory $path already exists")
            return false
        }
        return directory.mkdirs()
    }

    fun createDirectory(path: String, override: Boolean): Boolean {
        // Check if directory exists. If yes, then delete all directory
        if (override && isDirectoryExists(path)) {
            deleteDirectory(path)
        }
        // Create new directory
        return createDirectory(path)
    }

    fun deleteDirectory(path: String?): Boolean {
        return deleteDirectoryImpl(path!!)
    }

    fun isDirectoryExists(path: String?): Boolean {
        return File(path).exists()
    }

    fun cleanUp(){
        configurationFile = null
    }

    companion object {
        private var instance: EncryptDecryptPinHelper? = null
        private val TAG = EncryptDecryptPinHelper::class.java.simpleName
        private var mCipher: Cipher? = null
        var configurationFile : EncryptConfiguration? = null
        fun getInstance(): EncryptDecryptPinHelper? {
            if (instance == null) {
                instance = EncryptDecryptPinHelper()
            }
            return instance
        }
    }

    init {
        configurationFile = getConfigurationFile()
    }
}