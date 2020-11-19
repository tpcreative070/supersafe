package co.tpcreative.supersafe.common.extension
import android.content.Context
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun ByteArray.createFileByteDataNoEncrypt(context:Context) : File?{
    return withContext(Dispatchers.IO){
        EncryptDecryptFilesHelper.getInstance()?.createFileByteDataNoEncrypt(context,this@createFileByteDataNoEncrypt)
    }
}

suspend fun ByteArray.createFile(path : String) : Boolean?{
    return withContext(Dispatchers.IO){
        EncryptDecryptFilesHelper.getInstance()?.createFile(path,this@createFile)
    }
}