package co.tpcreative.suppersafe.common.encypt;

import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by michaeldunn on 3/13/17.
 */

public class EncryptedFileDataSourceFactory implements DataSource.Factory {

  private Cipher mCipher;
  private SecretKeySpec mSecretKeySpec;
  private IvParameterSpec mIvParameterSpec;
  private TransferListener<? super DataSource> mTransferListener;
  private static final String TAG = EncryptedFileDataSourceFactory.class.getSimpleName();

  public EncryptedFileDataSourceFactory(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec, TransferListener<? super DataSource> listener) {
    mCipher = cipher;
    mSecretKeySpec = secretKeySpec;
    mIvParameterSpec = ivParameterSpec;
    mTransferListener = listener;
  }

  @Override
  public EncryptedFileDataSource createDataSource() {
    Log.d(TAG,"createDataSource");
    return new EncryptedFileDataSource(mCipher, mSecretKeySpec, mIvParameterSpec, mTransferListener);
  }

}