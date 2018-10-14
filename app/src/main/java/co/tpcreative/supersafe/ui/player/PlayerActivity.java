package co.tpcreative.supersafe.ui.player;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.snatik.storage.Storage;
import com.snatik.storage.security.SecurityUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactory;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import dyanamitechetan.vusikview.VusikView;

public class PlayerActivity extends BaseActivity implements BaseView {

    private static final String TAG = PlayerActivity.class.getSimpleName();
    @BindView(R.id.simpleexoplayerview)
    PlayerView playerView;
    @BindView(R.id.animationPlayer)
    VusikView animationPlayer;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rlTop)
    RelativeLayout rlTop;
    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;
    private File mEncryptedFile;
    private Storage storage;
    private PlayerPresenter presenter;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        Drawable note1 = getResources().getDrawable( R.drawable.music_1 );
        Drawable note2 = getResources().getDrawable( R.drawable.music_2 );
        Drawable note3 = getResources().getDrawable( R.drawable.music_3 );
        Drawable note4 = getResources().getDrawable( R.drawable.music_4 );
        Drawable[]  myImageList = new Drawable[]{note1,note2,note3,note4};
        animationPlayer.setImages(myImageList).start();


        playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                tvTitle.setVisibility(visibility);
                rlTop.setVisibility(visibility);
            }
        });

    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
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
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
            playerView.getPlayer().setRepeatMode(Player.REPEAT_MODE_OFF);

            videoSource.addEventListener(null, new MediaSourceEventListener() {
                @Override
                public void onMediaPeriodCreated(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {

                }

                @Override
                public void onMediaPeriodReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {

                }

                @Override
                public void onLoadStarted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {

                }

                @Override
                public void onLoadCompleted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
                    player.stop();
                }

                @Override
                public void onLoadCanceled(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {

                }

                @Override
                public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {

                }

                @Override
                public void onReadingStarted(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {

                }

                @Override
                public void onUpstreamDiscarded(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {

                }

                @Override
                public void onDownstreamFormatChanged(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            if (animationPlayer!=null){
                animationPlayer.stopNotesFall();
            }
            player.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
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

    @OnClick(R.id.rlTop)
    public void onClickedBack(View view){
        finish();
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case PLAY:{
                mEncryptedFile = new File(presenter.mItems.originalPath);
                Utils.Log(TAG,mEncryptedFile.getAbsolutePath());
                if (mCipher==null){
                    Utils.Log(TAG," mcipher is null");
                    return;
                }

                EnumFormatType formatType = EnumFormatType.values()[presenter.mItems.formatType];
                switch (formatType){
                    case AUDIO:{
                        animationPlayer.startNotesFall();
                        animationPlayer.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                tvTitle.setText(presenter.mItems.title);
                playVideo();
                break;
            }
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
