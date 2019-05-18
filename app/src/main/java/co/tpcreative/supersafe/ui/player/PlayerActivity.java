package co.tpcreative.supersafe.ui.player;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BasePlayerActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactory;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.ThemeApp;
import dyanamitechetan.vusikview.VusikView;

public class PlayerActivity extends BasePlayerActivity implements BaseView, PlayerAdapter.ItemSelectedListener {

    private static final String TAG = PlayerActivity.class.getSimpleName();
    @BindView(R.id.simpleexoplayerview)
    PlayerView playerView;
    @BindView(R.id.animationPlayer)
    VusikView animationPlayer;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rlTop)
    RelativeLayout rlTop;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;
    private File mEncryptedFile;
    private Storage storage;
    private PlayerPresenter presenter;
    private SimpleExoPlayer player;
    private PlayerAdapter adapter;
    int lastWindowIndex = 0;
    private boolean isPortrait ;
    private long seekTo = 0;
    private ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();

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
        } catch (Exception e) {
            e.printStackTrace();
        }


        seekTo = PrefsController.getLong(getString(R.string.key_seek_to),0);
        lastWindowIndex = PrefsController.getInt(getString(R.string.key_lastWindowIndex),0);
        if (mCipher != null) {
            initRecycleView(getLayoutInflater());
            presenter.onGetIntent(this);
        }

        Utils.Log(TAG,"Create PlayerActivity");

        Drawable note1 = getResources().getDrawable(R.drawable.music_1);
        Drawable note2 = getResources().getDrawable(R.drawable.music_2);
        Drawable note3 = getResources().getDrawable(R.drawable.music_3);
        Drawable note4 = getResources().getDrawable(R.drawable.music_4);
        Drawable[] myImageList = new Drawable[]{note1, note2, note3, note4};
        animationPlayer.setImages(myImageList).start();

        playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (tvTitle!=null){
                    tvTitle.setVisibility(visibility);
                    rlTop.setVisibility(visibility);
                    recyclerView.setVisibility(visibility);
                }
            }
        });
        isPortrait = true;
        tvTitle.setTextColor(getResources().getColor(themeApp.getAccentColor()));
    }

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new PlayerAdapter(layoutInflater, this, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickGalleryItem(int position) {
        Utils.Log(TAG, "Position :" + position);
        onUpdatedUI(position);
        player.seekToDefaultPosition(position);
    }

    public void onUpdatedUI(int position) {
        for (int i = 0; i < presenter.mList.size(); i++) {
            if (i == position) {
                presenter.mList.get(i).isChecked = true;
            } else {
                presenter.mList.get(i).isChecked = false;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
        //SuperSafeApplication.getInstance().writeKeyHomePressed(PlayerActivity.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
        if (player != null) {
            if (animationPlayer != null) {
                animationPlayer.stopNotesFall();
            }
            player.stop();
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
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

        //Uri uri = Uri.fromFile(mEncryptedFile);
        try {
            //MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(uri);
            presenter.mListSource.clear();
            for (Items index : presenter.mList) {
                mEncryptedFile = new File(index.originalPath);
                Uri uri = Uri.fromFile(mEncryptedFile);
                presenter.mListSource.add(new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(uri));
            }
            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(
                    presenter.mListSource.toArray(new MediaSource[presenter.mListSource.size()]));
            boolean haveStartPosition = lastWindowIndex != C.INDEX_UNSET;
            if (haveStartPosition) {
                player.seekTo(lastWindowIndex, seekTo);
                Utils.Log(TAG,"Return value "+ lastWindowIndex + " - "+ seekTo);
            }
            else{
                Utils.Log(TAG,"No value "+ lastWindowIndex + " - "+ seekTo);
            }
            player.prepare(concatenatedSource,!haveStartPosition,false);
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);

            player.addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                    Utils.Log(TAG, "1");
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    Utils.Log(TAG, "2");
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                    Utils.Log(TAG, "3");
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Utils.Log(TAG, "4 " +playbackState);
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    Utils.Log(TAG, "5");
                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                    Utils.Log(TAG, "6");
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Utils.Log(TAG, "7");
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    int latestWindowIndex = player.getCurrentWindowIndex();
                    if (latestWindowIndex != lastWindowIndex) {
                        lastWindowIndex = latestWindowIndex;
                    }
                    tvTitle.setText(presenter.mList.get(lastWindowIndex).title);
                    onUpdatedUI(lastWindowIndex);
                    Utils.Log(TAG, "position ???????" + lastWindowIndex);
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                    Utils.Log(TAG, "9");
                }

                @Override
                public void onSeekProcessed() {
                    Utils.Log(TAG, "10");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @OnClick(R.id.imgArrowBack)
    public void onClickedBack(View view) {
        boolean isLandscape = Utils.isLandscape(this);
        if (isLandscape){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isPortrait = true;
            Utils.Log(TAG,"Request SCREEN_ORIENTATION_PORTRAIT");
        }
        else{
            PrefsController.putLong(getString(R.string.key_seek_to),0);
            PrefsController.putInt(getString(R.string.key_lastWindowIndex),0);
            finish();
        }
    }

    @OnClick(R.id.imgRotate)
    public void onClickedRotate(View view){
        if (isPortrait){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isPortrait = false;
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isPortrait = true;
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case PLAY: {
                mEncryptedFile = new File(presenter.mItems.originalPath);
                Utils.Log(TAG, mEncryptedFile.getAbsolutePath());
                if (mCipher == null) {
                    Utils.Log(TAG, " mcipher is null");
                    return;
                }
                EnumFormatType formatType = EnumFormatType.values()[presenter.mItems.formatType];
                switch (formatType) {
                    case AUDIO: {
                        animationPlayer.startNotesFall();
                        animationPlayer.setVisibility(View.VISIBLE);
                        playerView.setBackgroundResource(R.color.yellow_700);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    }
                    case VIDEO: {
                        playerView.setBackgroundColor(getResources().getColor(R.color.black));
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    }
                }
                tvTitle.setText(presenter.mItems.title);
                adapter.setDataSource(presenter.mList);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long seekPosition = player.getCurrentPosition();
        outState.putBoolean(getString(R.string.key_rotate),isPortrait);
        PrefsController.putLong(getString(R.string.key_seek_to),seekPosition);
        PrefsController.putInt(getString(R.string.key_lastWindowIndex),player.getCurrentWindowIndex());
        Utils.Log(TAG, "Saved------------------------ "+seekPosition +" - "+player.getCurrentWindowIndex());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isPortrait = savedInstanceState.getBoolean(getString(R.string.key_rotate),false);
        Utils.Log(TAG, "Restore "+seekTo);
    }


}
