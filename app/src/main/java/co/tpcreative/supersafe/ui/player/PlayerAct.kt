package co.tpcreative.supersafe.ui.player
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.gson.Gson
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_player.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PlayerAct : BasePlayerActivity(), BaseView<EmptyModel>, PlayerAdapter.ItemSelectedListener {
    var mCipher: Cipher? = null
    var mSecretKeySpec: SecretKeySpec? = null
    var mIvParameterSpec: IvParameterSpec? = null
    var mEncryptedFile: File? = null
    var presenter: PlayerPresenter? = null
    var player: SimpleExoPlayer? = null
    var adapter: PlayerAdapter? = null
    var lastWindowIndex = 0
    var isPortrait = false
    var seekTo: Long = 0
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        initUI()
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
            val themeApp = ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton, "#0091EA")
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_theme_object), Gson().toJson(themeApp))
        }
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

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
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

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }


    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return applicationContext
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}

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
                        animationPlayer?.visibility = View.VISIBLE
                        playerView?.setBackgroundResource(R.color.yellow_700)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    EnumFormatType.VIDEO -> {
                        playerView?.setBackgroundColor(ContextCompat.getColor(this,R.color.black))
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
}