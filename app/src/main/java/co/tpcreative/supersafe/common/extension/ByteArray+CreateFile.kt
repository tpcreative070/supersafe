package co.tpcreative.supersafe.common.extension
import android.content.Context
import android.util.Base64
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

fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.DEFAULT);

fun ByteArray.toByteArray(): ByteArray = Base64.decode(this, Base64.DEFAULT);