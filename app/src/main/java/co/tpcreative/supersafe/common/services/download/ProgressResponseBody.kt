package co.tpcreative.supersafe.common.services.download
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressResponseBody(responseBody: ResponseBody?, progressListener: ProgressResponseBodyListener?) : ResponseBody() {
    private val responseBody: ResponseBody? = responseBody
    private val progressListener: ProgressResponseBodyListener? = progressListener
    private var bufferedSource: BufferedSource? = null
    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    override fun contentLength(): Long {
        return responseBody!!.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody!!.source())?.buffer()
        }
        return bufferedSource!!
    }

    @Synchronized
    private fun source(source: Source): Source? {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var allBytes: Long = responseBody!!.contentLength()
            var startTime: Long? = System.currentTimeMillis()

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead: Long = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                val percent = if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / responseBody?.contentLength()!!.toFloat() * 100
                if (progressListener != null) {
                    try {
                        if (percent > 1) {
                            if (percent > 99) {
                                progressListener.onAttachmentDownloadUpdate(percent.toInt())
                                progressListener.onAttachmentDownloadedSuccess()
                            } else {
                                progressListener.onAttachmentDownloadUpdate(percent.toInt())
                            }
                            progressListener.onAttachmentTotalDownload(allBytes, totalBytesRead)
                            val elapsedTime = System.currentTimeMillis() - startTime!!
                            progressListener.onAttachmentElapsedTime(elapsedTime)
                            val allTimeForDownloading = elapsedTime * allBytes / totalBytesRead
                            progressListener.onAttachmentAllTimeForDownloading(allTimeForDownloading)
                            val remainingTime = allTimeForDownloading - elapsedTime
                            progressListener.onAttachmentRemainingTime(remainingTime)
                            var speedInKBps = 0.0
                            val timeInSecs = elapsedTime / 1000 //converting millis to seconds as 1000m in 1 second
                            if (timeInSecs != 0L) {
                                speedInKBps = totalBytesRead / timeInSecs / 1024.0
                                progressListener.onAttachmentSpeedPerSecond(speedInKBps)
                            }
                        }
                    } catch (ae: Exception) {
                        progressListener.onAttachmentDownloadedError(ae.message)
                    }
                }
                return bytesRead
            }
        }
    }

    interface ProgressResponseBodyListener {
        fun onAttachmentDownloadedSuccess()
        fun onAttachmentDownloadedError(message: String?)
        fun onAttachmentDownloadUpdate(percent: Int)
        fun onAttachmentElapsedTime(elapsed: Long)
        fun onAttachmentAllTimeForDownloading(all: Long)
        fun onAttachmentRemainingTime(all: Long)
        fun onAttachmentSpeedPerSecond(all: Double)
        fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long)
    }

    companion object {
        val TAG = ProgressResponseBody::class.java.simpleName
    }

}