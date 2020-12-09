package co.tpcreative.supersafe.common.extension
import android.os.Environment
import android.util.Base64
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.util.Utils
import java.io.File


/*This is for key pin*/
fun String.createPin(content : String?) : Boolean?{
    return EncryptDecryptFilesHelper.getInstance()?.createFile(this,content = content)
}

fun String.encryptTextByIdPKCS7(mode : Int) : String?{
    return EncryptDecryptFilesHelper.getInstance()?.encryptTextPKCS7(this,mode)
}

fun String.createdTextByDefaultPKCS7(mode : Int) : String?{
    return EncryptDecryptPinHelper.getInstance()?.createdTextPKCS7(this,mode)
}

fun String.readPin() : String? {
    return EncryptDecryptFilesHelper.getInstance()?.readTextFile(this)
}

fun String.decode(): String {
    return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
}

fun String.encode(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
}

fun String.readFile() : ByteArray?{
    return EncryptDecryptFilesHelper.getInstance()?.readFile(this)
}

fun String.isFileExist(): Boolean {
    return File(this).exists()
}

fun String.createDirectory() : Boolean{
    val directory = File(this)
    if (directory.exists()) {
        Utils.Log("TAG", "Directory $this already exists")
        return false
    }
    return directory.mkdirs()
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
