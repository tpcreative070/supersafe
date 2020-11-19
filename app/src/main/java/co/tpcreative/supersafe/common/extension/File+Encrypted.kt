package co.tpcreative.supersafe.common.extension
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import java.io.File

fun File.createFile(output: File, input: File, mode: Int) : Boolean?{
    return try {
        EncryptDecryptFilesHelper.getInstance()?.createFile(output, input, mode)
    }catch (e: Exception){
        false
    }
}

fun File.isFileExist(path: String?): Boolean {
    return File(path).exists()
}