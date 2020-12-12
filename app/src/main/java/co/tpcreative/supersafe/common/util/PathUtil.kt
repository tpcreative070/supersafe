package co.tpcreative.supersafe.common.util
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URISyntaxException
/**
 * Created by Aki on 1/7/2017.
 */
object PathUtil {
    /*
     * Gets the file path of the given Uri.
     */
    private val TAG = PathUtil::class.java.simpleName

    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri: Boolean = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String?>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: Array<String?> = docId.split(":".toRegex()).toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id: String = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: Array<String?> = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf<String?>(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor?.let {
                    if (it.moveToFirst()) {
                        column_index?.let { index ->
                            return it.getString(index)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return uri.getPath()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri?.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri?.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri?.getAuthority()
    }

    fun onWorkingOnOreo(data: Uri): String? {
        try {
            val file = File(data.getPath()) //create path from uri
            val split: Array<String?> = file.path.split(":".toRegex()).toTypedArray() //split the path.
            return split[1] //assign it to a string(your choice).
        } catch (e: Exception) {
        }
        return null
    }

    fun getRealPathFromUri(context: Context?, contentUri: Uri?): String? {
        var path: String? = contentUri.toString()
        if (path != null) {
            if (path.contains("file://")) {
                if (contentUri != null) {
                    path = contentUri.getPath()
                }
                return path
            }
        }
        var cursor: Cursor? = null
        return try {
            /*    /external/video/media/3586    */
            val proj = arrayOf<String?>(MediaStore.Images.Media.DATA)
            cursor = contentUri?.let { context?.contentResolver?.query(it, proj, null, null, null) }
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            column_index?.let {
               return cursor?.getString(it)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
            null
        } finally {
            cursor?.close()
        }
    }

    /*New way to get file into Gallery*/
    fun getFilePathFromURI(context: Context, contentUri: Uri): String? {
        //copy file and send new file path
        val fileName = getFileName(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val rootDataDir: File? = SuperSafeApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Utils.Log(TAG, "root path $rootDataDir")
            val copyFile = File(rootDataDir.toString() + File.separator + fileName)
            copy(context, contentUri, copyFile)
            return copyFile.absolutePath
        }
        return null
    }

    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path = uri.path
        val cut = path?.lastIndexOf('/')
        if (cut != -1) {
            if (cut != null) {
                fileName = path.substring(cut + 1)
            }
        }
        return fileName
    }

    fun copy(context: Context?, srcUri: Uri, dstFile: File) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(srcUri) ?: return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            IOUtils.copy(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}