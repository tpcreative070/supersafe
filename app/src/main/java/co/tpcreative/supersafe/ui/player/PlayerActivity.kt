package co.tpcreative.supersafe.ui.player
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BasePlayerActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactory
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.gson.Gson
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import dyanamitechetan.vusikview.VusikView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PlayerActivity : BasePlayerActivity(), BaseView<EmptyModel>, PlayerAdapter.ItemSelectedListener {
    @BindView(R.id.simpleexoplayerview)
    var playerView: PlayerView? = null
    @BindView(R.id.animationPlayer)
    var animationPlayer: VusikView? = null
    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null
    @BindView(R.id.rlTop)
    var rlTop: RelativeLayout? = null
    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null
    private var mCipher: Cipher? = null
    private var mSecretKeySpec: SecretKeySpec? = null
    private var mIvParameterSpec: IvParameterSpec? = null
    private var mEncryptedFile: File? = null
    private var presenter: PlayerPresenter? = null
    private var player: SimpleExoPlayer? = null
    private var adapter: PlayerAdapter? = null
    var lastWindowIndex = 0
    private var isPortrait = false
    private var seekTo: Long = 0
    private val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        presenter = PlayerPresenter()
        presenter?.bindView(this)
        try {
            storage = Storage(this)
            storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
            mCipher = storage?.getCipher(Cipher.DECRYPT_MODE)
            mSecretKeySpec = SecretKeySpec(storage?.getmConfiguration()?.secretKey, SecurityUtil.AES_ALGORITHM)
            mIvParameterSpec = IvParameterSpec(storage?.getmConfiguration()?.ivParameter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        seekTo = PrefsController.getLong(getString(R.string.key_seek_to), 0)
        lastWindowIndex = PrefsController.getInt(getString(R.string.key_lastWindowIndex), 0)
        if (mCipher != null) {
            initRecycleView(getLayoutInflater())
            presenter?.onGetIntent(this)
        }
        val note1: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_1)
        val note2: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_music_5)
        val note3: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_3)
        val note4: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_4)
        val myImageList = arrayOf<Drawable?>(note1, note2, note3, note4)
        animationPlayer?.setImages(myImageList)?.start()
        playerView?.setControllerVisibilityListener(object : PlayerControlView.VisibilityListener {
            override fun onVisibilityChange(visibility: Int) {
                if (tvTitle != null) {
                    tvTitle?.setVisibility(visibility)
                    rlTop?.setVisibility(visibility)
                    recyclerView?.setVisibility(visibility)
                }
            }
        })
        isPortrait = true
        try {
            if (themeApp != null) {
                tvTitle?.setTextColor(getResources().getColor(themeApp.getAccentColor()))
            }
        } catch (e: Exception) {
            val themeApp = ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton, "#0091EA")
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_theme_object), Gson().toJson(themeApp))
        }
    }

    fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = PlayerAdapter(layoutInflater, this, this)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView?.setItemAnimator(DefaultItemAnimator())
        recyclerView?.setAdapter(adapter)
    }

    override fun onClickGalleryItem(position: Int) {
        Utils.Log(TAG, "Position :$position")
        onUpdatedUI(position)
        player?.seekToDefaultPosition(position)
    }

    fun onUpdatedUI(position: Int) {
        for (i in presenter?.mList!!.indices) {
            if (i == position) {
                presenter?.mList?.get(i)?.isChecked = true
            } else {
                presenter?.mList?.get(i)?.isChecked = false
            }
        }
        adapter?.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
        if (player != null) {
            if (animationPlayer != null) {
                animationPlayer?.stopNotesFall()
            }
            player?.stop()
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    fun playVideo() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        playerView?.setPlayer(player)
        playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        val dataSourceFactory: DataSource.Factory = EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter)
        val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
        try {
            presenter?.mListSource?.clear()
            for (index in presenter?.mList!!) {
                mEncryptedFile = File(index.originalPath)
                val uri = Uri.fromFile(mEncryptedFile)
                presenter?.mListSource?.add(ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(uri))
            }
            val mResource = presenter?.mListSource?.toTypedArray()
            val concatenatedSource = ConcatenatingMediaSource(*mResource!!)
            val haveStartPosition = lastWindowIndex != C.INDEX_UNSET
            if (haveStartPosition) {
                player?.seekTo(lastWindowIndex, seekTo)
                Utils.Log(TAG, "Return value $lastWindowIndex - $seekTo")
            } else {
                Utils.Log(TAG, "No value $lastWindowIndex - $seekTo")
            }
            player?.prepare(concatenatedSource, !haveStartPosition, false)
            player?.setPlayWhenReady(true)
            player?.setRepeatMode(Player.REPEAT_MODE_ALL)
            player?.addListener(object : Player.EventListener {
                override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                    Utils.Log(TAG, "1")
                }

                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    Utils.Log(TAG, "2")
                }

                override fun onLoadingChanged(isLoading: Boolean) {
                    Utils.Log(TAG, "3")
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    Utils.Log(TAG, "4 $playbackState")
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    Utils.Log(TAG, "5")
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    Utils.Log(TAG, "6")
                }

                override fun onPlayerError(error: ExoPlaybackException?) {
                    Utils.Log(TAG, "7")
                }

                override fun onPositionDiscontinuity(reason: Int) {
                    val latestWindowIndex: Int? = player?.getCurrentWindowIndex()
                    if (latestWindowIndex != lastWindowIndex) {
                        if (latestWindowIndex != null) {
                            lastWindowIndex = latestWindowIndex
                        }
                    }
                    tvTitle?.setText(presenter?.mList?.get(lastWindowIndex)?.title)
                    onUpdatedUI(lastWindowIndex)
                    Utils.Log(TAG, "position ???????$lastWindowIndex")
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                    Utils.Log(TAG, "9")
                }

                override fun onSeekProcessed() {
                    Utils.Log(TAG, "10")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}

    @OnClick(R.id.imgArrowBack)
    fun onClickedBack(view: View?) {
        val isLandscape: Boolean = Utils.isLandscape(this)
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            isPortrait = true
            Utils.Log(TAG, "Request SCREEN_ORIENTATION_PORTRAIT")
        } else {
            PrefsController.putLong(getString(R.string.key_seek_to), 0)
            PrefsController.putInt(getString(R.string.key_lastWindowIndex), 0)
            finish()
        }
    }

    @OnClick(R.id.imgRotate)
    fun onClickedRotate(view: View?) {
        isPortrait = if (isPortrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            false
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            true
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.PLAY -> {
                mEncryptedFile = File(presenter?.mItems?.originalPath)
                Utils.Log(TAG, mEncryptedFile!!.getAbsolutePath())
                if (mCipher == null) {
                    Utils.Log(TAG, " mcipher is null")
                    return
                }
                val formatType = EnumFormatType.values()[presenter?.mItems?.formatType!!]
                when (formatType) {
                    EnumFormatType.AUDIO -> {
                        animationPlayer?.startNotesFall()
                        animationPlayer?.setVisibility(View.VISIBLE)
                        playerView?.setBackgroundResource(R.color.yellow_700)
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    EnumFormatType.VIDEO -> {
                        playerView?.setBackgroundColor(getResources().getColor(R.color.black))
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                tvTitle?.setText(presenter?.mItems?.title)
                adapter?.setDataSource(presenter?.mList)
                playVideo()
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }


    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val seekPosition: Long? = player?.getCurrentPosition()
        outState.putBoolean(getString(R.string.key_rotate), isPortrait)
        PrefsController.putLong(getString(R.string.key_seek_to), seekPosition!!.toLong())
        PrefsController.putInt(getString(R.string.key_lastWindowIndex), player!!.getCurrentWindowIndex())
        Utils.Log(TAG, "Saved------------------------ " + seekPosition + " - " + player?.getCurrentWindowIndex())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPortrait = savedInstanceState.getBoolean(getString(R.string.key_rotate), false)
        Utils.Log(TAG, "Restore $seekTo")
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}