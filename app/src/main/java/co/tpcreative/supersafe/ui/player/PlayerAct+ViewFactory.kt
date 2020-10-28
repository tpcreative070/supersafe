package co.tpcreative.supersafe.ui.player
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.gson.Gson
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_player.*
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun PlayerAct.initUI(){
    TAG = this::class.java.simpleName
    presenter = PlayerPresenter()
    presenter?.bindView(this)
    try {
        val storage = Storage(this)
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
        initRecycleView(layoutInflater)
        presenter?.onGetIntent(this)
    }
    val note1: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_1)
    val note2: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_music_5)
    val note3: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_3)
    val note4: Drawable? = ContextCompat.getDrawable(this, R.drawable.music_4)
    val myImageList = arrayOf<Drawable?>(note1, note2, note3, note4)
    animationPlayer?.setImages(myImageList)?.start()
    playerView?.setControllerVisibilityListener { visibility ->
        if (tvTitle != null) {
            tvTitle?.visibility = visibility
            rlTop?.visibility = visibility
            recyclerView?.visibility = visibility
        }
    }
    isPortrait = true
    try {
        if (themeApp != null) {
            tvTitle?.setTextColor(ContextCompat.getColor(this,themeApp.getAccentColor()))
        }
    } catch (e: Exception) {
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_theme_object), 0)
    }
    imgArrowBack.setOnClickListener {
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

    imgRotate.setOnClickListener {
        isPortrait = if (isPortrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            false
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            true
        }
    }
}

fun PlayerAct.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = PlayerAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

fun PlayerAct.playVideo() {
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    playerView?.player = player
    playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
    val dataSourceFactory: DataSource.Factory = EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter)
    val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
    try {
        presenter?.mListSource?.clear()
        for (index in presenter?.mList!!) {
            mEncryptedFile = File(index.getOriginal())
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

