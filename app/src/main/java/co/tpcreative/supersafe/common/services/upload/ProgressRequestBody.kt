package co.tpcreative.supersafe.common.services.upload
import android.os.Handler
import android.os.Looper
import co.tpcreative.supersafe.common.extension.isFileExist
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ProgressRequestBody(private val mFile: File?,private val mContentType : String?, private val mListener: UploadCallbacks?) : RequestBody() {
    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError()
        fun onFinish()
    }

    override fun contentType(): MediaType? {
        return if (mContentType == null) {
            "image/*".toMediaTypeOrNull()
        } else this.mContentType.toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mFile?.length() ?:0
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        var mInPutStream : FileInputStream? = null
        try {
            val fileLength = mFile?.length()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            mInPutStream = FileInputStream(mFile)
            var uploaded: Long = 0
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (mInPutStream.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength ?:0))
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
        finally {
            mInPutStream?.close()
        }
    }

    private inner class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) : Runnable {
        override fun run() {
            val percent = (100 * mUploaded / mTotal).toInt()
            if (percent == 100) {
                mListener?.onFinish()
            } else {
                mListener?.onProgressUpdate(percent)
            }
        }
    }
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}