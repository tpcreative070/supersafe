package co.tpcreative.supersafe.common.services.upload
import org.apache.http.Header
import org.apache.http.HttpEntity
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ProgressiveEntity(entity: HttpEntity?, progressListener: ProgressListener?) : HttpEntity {
    private val listener: ProgressListener?
    private val yourEntity: HttpEntity?

    @Throws(IOException::class)
    override fun consumeContent() {
        //EntityUtils.consume(yourEntity);
        yourEntity?.consumeContent()
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun getContent(): InputStream? {
        return yourEntity?.getContent()
    }

    override fun getContentEncoding(): Header? {
        return yourEntity?.getContentEncoding()
    }

    override fun getContentLength(): Long {
        return yourEntity!!.getContentLength()
    }

    override fun getContentType(): Header? {
        return yourEntity?.getContentType()
    }

    override fun isChunked(): Boolean {
        return yourEntity!!.isChunked()
    }

    override fun isRepeatable(): Boolean {
        return yourEntity!!.isRepeatable()
    }

    override fun isStreaming(): Boolean {
        return yourEntity!!.isStreaming()
    } // CONSIDER put a _real_ delegator into here!

    @Throws(IOException::class)
    override fun writeTo(outstream: OutputStream?) {
        yourEntity!!.writeTo(ProxyOutputStream(outstream, listener))
    }

    inner class ProxyOutputStream(proxy: OutputStream?, private val progressListener: co.tpcreative.supersafe.common.services.upload.ProgressiveEntity.ProgressListener?) : FilterOutputStream(proxy) {
        private var transferred: Long = 0
        var startTime: Long? = System.currentTimeMillis()

        @Throws(IOException::class)
        override fun write(bts: ByteArray?) {
            out.write(bts)
        }

        @Throws(IOException::class)
        override fun write(bts: ByteArray?, st: Int, end: Int) {
            out.write(bts, st, end)
            transferred += end.toLong()
            val elapsedTime = System.currentTimeMillis() - startTime!!
            var speedInKBps = 0.0
            try {
                val timeInSecs = elapsedTime / 1000 //converting millis to seconds as 1000m in 1 second
                speedInKBps = transferred / timeInSecs / 1024.0
                progressListener?.transferSpeed(speedInKBps)
            } catch (ae: ArithmeticException) {
            }
            progressListener?.transferred(transferred)
        }

        @Throws(IOException::class)
        override fun write(idx: Int) {
            out.write(idx)
            transferred++
            val elapsedTime = System.currentTimeMillis() - startTime!!
            var speedInKBps = 0.0
            try {
                val timeInSecs = elapsedTime / 1000 //converting millis to seconds as 1000m in 1 second
                speedInKBps = transferred / timeInSecs / 1024.0
                progressListener?.transferSpeed(speedInKBps)
            } catch (ae: ArithmeticException) {
            }
            progressListener?.transferred(transferred)
        }

        @Throws(IOException::class)
        override fun flush() {
            out.flush()
        }

        @Throws(IOException::class)
        override fun close() {
            out.close()
        }

    } // CONSIDER import this class (and risk more Jar File Hell)

    interface ProgressListener {
        open fun transferred(num: Long)
        open fun transferSpeed(speed: Double)
    }

    companion object {
        val TAG = ProgressiveEntity::class.java.simpleName
    }

    init {
        yourEntity = entity
        listener = progressListener
    }
}