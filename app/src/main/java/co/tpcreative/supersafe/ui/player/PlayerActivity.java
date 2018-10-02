package co.tpcreative.supersafe.ui.player;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.snatik.storage.Storage;
import com.snatik.storage.security.SecurityUtil;
import java.io.File;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactory;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;

public class PlayerActivity extends BaseActivity implements PlayerViews{

    private static final String TAG = PlayerActivity.class.getSimpleName();
    @BindView(R.id.simpleexoplayerview)
    PlayerView playerView;
    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;
    private File mEncryptedFile;
    private Storage storage;
    private PlayerPresenter presenter;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        presenter = new PlayerPresenter();
        presenter.bindView(this);

        try {
            storage = new Storage(this);
            storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            mCipher = storage.getCipher(Cipher.DECRYPT_MODE);
            mSecretKeySpec = new SecretKeySpec(storage.getmConfiguration().getSecretKey(), SecurityUtil.AES_ALGORITHM);
            mIvParameterSpec = new IvParameterSpec(storage.getmConfiguration().getIvParameter());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (mCipher!=null){
            presenter.onGetIntent(this);
        }

    }


    public void playVideo() {
        final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        Uri uri = Uri.fromFile(mEncryptedFile);
        try {
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(uri);
            player.prepare(videoSource);
            player.setPlayWhenReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            player.stop();
        }

    }

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onPlay() {
       mEncryptedFile = new File(presenter.mItems.originalPath);
       Utils.Log(TAG,mEncryptedFile.getAbsolutePath());
       if (mCipher==null){
           Utils.Log(TAG," mcipher is null");
           return;
       }
       playVideo();
    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }
}
