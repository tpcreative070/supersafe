package co.tpcreative.supersafe.common.services.upload
import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ProgressRequestBody(private val mFile: File?, private val mListener: UploadCallbacks?) : RequestBody() {
    private val TAG = ProgressRequestBody::class.java.simpleName
    private var type: String? = null
    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError()
        fun onFinish()
    }

    override fun contentType(): MediaType? {
        // i want to upload only images
        return if (type == null) {
            "image/*".toMediaTypeOrNull()
        } else this.type!!.toMediaTypeOrNull()
    }

    fun setContentType(type: String?) {
        this.type = type
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mFile!!.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile!!.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(mFile)
        var uploaded: Long = 0
        try {
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (`in`.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength))
            }
        } finally {
            `in`.close()
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