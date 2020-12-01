package co.tpcreative.supersafe.common.extension
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.util.SizeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun File.createFile(output: File, input: File, mode: Int) : Boolean?{
   return withContext(Dispatchers.IO){
        try {
           EncryptDecryptFilesHelper.getInstance()?.createLargeFile(output, input, mode)
       }catch (e: Exception){
           false
       }
   }
}

fun File.isFileExist(path: String?): Boolean {
    return File(path).exists()
}

fun File.getSize(type : SizeUnit) : Double{
    return EncryptDecryptFilesHelper.getInstance()?.getSize(this,type) ?: 0.0
}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024