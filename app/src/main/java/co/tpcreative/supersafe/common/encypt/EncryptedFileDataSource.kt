package co.tpcreative.supersafe.common.encyptimport
import android.net.Uri
import co.tpcreative.supersafe.common.util.Utils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import java.io.*
import java.math.BigInteger
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by michaeldunn on 3/13/17.
 */
class EncryptedFileDataSource(cipher: Cipher?, secretKeySpec: SecretKeySpec?, ivParameterSpec: IvParameterSpec?, listener: TransferListener<in EncryptedFileDataSource?>?) : DataSource {
    private val mTransferListener: TransferListener<in EncryptedFileDataSource?>?
    private var mInputStream: StreamingCipherInputStream? = null
    private var mUri: Uri? = null
    private var mBytesRemaining: Long = 0
    private var mOpened = false
    private val mCipher: Cipher?
    private val mSecretKeySpec: SecretKeySpec?
    private val mIvParameterSpec: IvParameterSpec?

    @Throws(EncryptedFileDataSourceException::class)
    override fun open(dataSpec: DataSpec?): Long {
        // if we're open, we shouldn't need to open again, fast-fail
        if (mOpened) {
            return mBytesRemaining
        }
        // #getUri is part of the contract...
        mUri = dataSpec?.uri
        // put all our throwable work in a single block, wrap the error in a custom Exception
        try {
            setupInputStream()
            skipToPosition(dataSpec)
            computeBytesRemaining(dataSpec)
        } catch (e: IOException) {
            throw EncryptedFileDataSourceException(e)
        }
        // if we made it this far, we're open
        mOpened = true
        // notify
        if (mTransferListener != null) {
            mTransferListener.onTransferStart(this, dataSpec)
        }
        // report
        return mBytesRemaining
    }

    @Throws(FileNotFoundException::class)
    private fun setupInputStream() {
        val encryptedFile = File(mUri?.getPath())
        val fileInputStream = FileInputStream(encryptedFile)
        mInputStream = StreamingCipherInputStream(fileInputStream, mCipher, mSecretKeySpec, mIvParameterSpec)
    }

    @Throws(IOException::class)
    private fun skipToPosition(dataSpec: DataSpec?) {
        dataSpec?.position?.let { mInputStream?.forceSkip(it) }
    }

    @Throws(IOException::class)
    private fun computeBytesRemaining(dataSpec: DataSpec?) {
        if (dataSpec?.length != C.LENGTH_UNSET.toLong()) {
            dataSpec?.let {
                mBytesRemaining=   it.length
            }
        } else {
            mInputStream?.available()?.let {
                mBytesRemaining  = it.toLong()
            }
            if (mBytesRemaining == Int.MAX_VALUE.toLong()) {
                mBytesRemaining = C.LENGTH_UNSET.toLong()
            }
        }
    }

    @Throws(EncryptedFileDataSourceException::class)
    override fun read(buffer: ByteArray?, offset: Int, readLength: Int): Int {
        // fast-fail if there's 0 quantity requested or we think we've already processed everything
        Utils.Log(TAG, "reading...")
        if (readLength == 0) {
            return 0
        } else if (mBytesRemaining == 0L) {
            return C.RESULT_END_OF_INPUT
        }
        // constrain the read length and try to read from the cipher input stream
        val bytesToRead = getBytesToRead(readLength)
        val bytesRead: Int
        bytesRead = try {
            mInputStream!!.read(buffer, offset, bytesToRead)
        } catch (e: IOException) {
            throw EncryptedFileDataSourceException(e)
        }
        // if we get a -1 that means we failed to read - we're either going to EOF error or broadcast EOF
        if (bytesRead == -1) {
            if (mBytesRemaining != C.LENGTH_UNSET.toLong()) {
                throw EncryptedFileDataSourceException(EOFException())
            }
            return C.RESULT_END_OF_INPUT
        }
        // we can't decrement bytes remaining if it's just a flag representation (as opposed to a mutable numeric quantity)
        if (mBytesRemaining != C.LENGTH_UNSET.toLong()) {
            mBytesRemaining -= bytesRead.toLong()
        }
        // notify
        if (mTransferListener != null) {
            mTransferListener.onBytesTransferred(this, bytesRead)
        }
        // report
        return bytesRead
    }

    private fun getBytesToRead(bytesToRead: Int): Int {
        return if (mBytesRemaining == C.LENGTH_UNSET.toLong()) {
            bytesToRead
        } else Math.min(mBytesRemaining, bytesToRead.toLong()) as Int
    }

    override fun getUri(): Uri? {
        return mUri
    }

    @Throws(EncryptedFileDataSourceException::class)
    override fun close() {
        mUri = null
        try {
            mInputStream?.close()
        } catch (e: IOException) {
            throw EncryptedFileDataSourceException(e)
        } finally {
            mInputStream = null
            if (mOpened) {
                mOpened = false
                if (mTransferListener != null) {
                    mTransferListener.onTransferEnd(this)
                }
            }
        }
    }

    class EncryptedFileDataSourceException(cause: IOException?) : IOException(cause)
    class StreamingCipherInputStream(inputStream: InputStream?, cipher: Cipher?, secretKeySpec: SecretKeySpec?, ivParameterSpec: IvParameterSpec?) : CipherInputStream(inputStream, cipher) {
        private val mUpstream: InputStream?
        private val mCipher: Cipher?
        private val mSecretKeySpec: SecretKeySpec?
        private val mIvParameterSpec: IvParameterSpec?

        @Throws(IOException::class)
        override fun read(b: ByteArray?, off: Int, len: Int): Int {
            return super.read(b, off, len)
        }

        @Throws(IOException::class)
        fun forceSkip(bytesToSkip: Long): Long {
            val skipped: Long = mUpstream!!.skip(bytesToSkip)
            try {
                val skip = (bytesToSkip % AES_BLOCK_SIZE).toInt()
                val blockOffset = bytesToSkip - skip
                val numberOfBlocks = blockOffset / AES_BLOCK_SIZE
                // from here to the next inline comment, i don't understand
                val ivForOffsetAsBigInteger: BigInteger = BigInteger(1, mIvParameterSpec?.getIV()).add(BigInteger.valueOf(numberOfBlocks))
                val ivForOffsetByteArray: ByteArray = ivForOffsetAsBigInteger.toByteArray()
                val computedIvParameterSpecForOffset: IvParameterSpec?
                if (ivForOffsetByteArray.size < AES_BLOCK_SIZE) {
                    val resizedIvForOffsetByteArray = ByteArray(AES_BLOCK_SIZE)
                    System.arraycopy(ivForOffsetByteArray, 0, resizedIvForOffsetByteArray, AES_BLOCK_SIZE - ivForOffsetByteArray.size, ivForOffsetByteArray.size)
                    computedIvParameterSpecForOffset = IvParameterSpec(resizedIvForOffsetByteArray)
                } else {
                    computedIvParameterSpecForOffset = IvParameterSpec(ivForOffsetByteArray, ivForOffsetByteArray.size - AES_BLOCK_SIZE, AES_BLOCK_SIZE)
                }
                mCipher?.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, computedIvParameterSpecForOffset)
                val skipBuffer = ByteArray(skip)
                // i get that we need to update, but i don't get how we're able to take the shortcut from here to the previous comment
                mCipher?.update(skipBuffer, 0, skip, skipBuffer)
                Arrays.fill(skipBuffer, 0.toByte())
            } catch (e: Exception) {
                return 0
            }
            return skipped
        }

        // We need to return the available bytes from the upstream.
        // In this implementation we're front loading it, but it's possible the value might change during the lifetime
        // of this instance, and reference to the stream should be retained and queried for available bytes instead
        @Throws(IOException::class)
        override fun available(): Int {
            return mUpstream!!.available()
        }

        companion object {
            private const val AES_BLOCK_SIZE = 16
        }

        init {
            mUpstream = inputStream
            mCipher = cipher
            mSecretKeySpec = secretKeySpec
            mIvParameterSpec = ivParameterSpec
        }
    }

    companion object {
        private val TAG = EncryptedFileDataSource::class.java.simpleName
    }

    init {
        mCipher = cipher
        mSecretKeySpec = secretKeySpec
        mIvParameterSpec = ivParameterSpec
        mTransferListener = listener
    }
}