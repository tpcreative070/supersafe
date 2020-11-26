package co.tpcreative.supersafe.common.extension
import android.os.Environment
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

fun String.deleteDirectory() : Boolean? {
    return EncryptDecryptFilesHelper.getInstance()?.deleteDirectory(this)
}

fun String.getExternalStorageDirectory(): String? {
    return Environment.getExternalStorageDirectory().absolutePath
}

fun String.getExternalStorageDirectory(publicDirectory: String?): String? {
    return Environment.getExternalStoragePublicDirectory(publicDirectory).absolutePath
}

fun String.isDirectoryExists(): Boolean {
    return File(this).exists()
}
