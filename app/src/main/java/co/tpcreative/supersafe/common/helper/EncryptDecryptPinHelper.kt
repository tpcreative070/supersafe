package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.encypt.EncryptConfiguration
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.decodeBase64
import co.tpcreative.supersafe.common.extension.encodeBase64
import co.tpcreative.supersafe.common.util.ImmutablePair
import co.tpcreative.supersafe.common.util.Utils
import java.io.*
import java.util.*
import javax.crypto.Cipher

class EncryptDecryptPinHelper {
    fun createdTextPKCS7(value: String, mode: Int) : String? {
        try {
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
        }catch (e : Exception){
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
            return bytes?.let { String(it)}
        }catch (e : Exception){
            e.printStackTrace()
            return null
        }
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
        try {
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
        }catch (e : Exception){
            e.printStackTrace()
            return false
        }
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

    fun cleanUp(){
        configurationFile = null
    }

    companion object {
        private var instance: EncryptDecryptPinHelper? = null
        private val TAG = EncryptDecryptPinHelper::class.java.simpleName
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