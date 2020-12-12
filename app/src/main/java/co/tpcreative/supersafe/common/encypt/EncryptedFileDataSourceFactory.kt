package co.tpcreative.supersafe.common.encypt
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.TransferListener
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
/**
 * Created by michaeldunn on 3/13/17.
 */
class EncryptedFileDataSourceFactory(private val mCipher: Cipher?, private val mSecretKeySpec: SecretKeySpec?, private val mIvParameterSpec: IvParameterSpec?, listener: TransferListener<in DataSource?>?) : DataSource.Factory {
    private val mTransferListener: TransferListener<in DataSource?>? = listener
    override fun createDataSource() : EncryptedFileDataSource? {
        return EncryptedFileDataSource(mCipher, mSecretKeySpec, mIvParameterSpec, mTransferListener)
    }
    companion object {
        private val TAG = EncryptedFileDataSourceFactory::class.java.simpleName
    }
}