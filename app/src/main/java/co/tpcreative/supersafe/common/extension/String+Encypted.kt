package co.tpcreative.supersafe.common.extension
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import java.io.File

fun String.readFile() : ByteArray?{
    return EncryptDecryptFilesHelper.getInstance()?.readFile(this)
}

fun String.isFileExist(): Boolean {
    return File(this).exists()
}

fun String.createDirectory(){
    EncryptDecryptFilesHelper.getInstance()?.createDirectory(this)
}

fun String.deleteFile(){
    val file = File(this)
    file.delete()
}