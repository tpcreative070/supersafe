package co.tpcreative.supersafe.common.services.upload
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

class MultiUploadService : MultipartEntity {
    private val listener: MultiUploadService.ProgressListener?

    constructor(listener: MultiUploadService.ProgressListener?) : super() {
        this.listener = listener
    }

    constructor(mode: HttpMultipartMode?, listener: MultiUploadService.ProgressListener?) : super(mode) {
        this.listener = listener
    }

    constructor(mode: HttpMultipartMode?, boundary: String?,
                charset: Charset?, listener: MultiUploadService.ProgressListener?) : super(mode, boundary, charset) {
        this.listener = listener
    }

    @Throws(IOException::class)
    override fun writeTo(outstream: OutputStream?) {
        super.writeTo(MultiUploadService.CountingOutputStream(outstream, listener))
    }

    interface ProgressListener {
        fun transferred(num: Long)
    }

    class CountingOutputStream(out: OutputStream?,
                               private val listener: MultiUploadService.ProgressListener?) : FilterOutputStream(out) {
        private var transferred: Long = 0

        @Throws(IOException::class)
        override fun write(b: ByteArray?, off: Int, len: Int) {
            out.write(b, off, len)
            transferred += len.toLong()
            listener?.transferred(transferred)
        }

        @Throws(IOException::class)
        override fun write(b: Int) {
            out.write(b)
            transferred++
            listener?.transferred(transferred)
        }

    }
}