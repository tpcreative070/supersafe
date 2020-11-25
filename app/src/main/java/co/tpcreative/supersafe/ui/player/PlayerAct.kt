package co.tpcreative.supersafe.ui.player
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BasePlayerActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import kotlinx.android.synthetic.main.activity_player.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PlayerAct : BasePlayerActivity(), PlayerAdapter.ItemSelectedListener {
    var mCipher: Cipher? = null
    var mSecretKeySpec: SecretKeySpec? = null
    var mIvParameterSpec: IvParameterSpec? = null
    var mEncryptedFile: File? = null
    var player: SimpleExoPlayer? = null
    var adapter: PlayerAdapter? = null
    var lastWindowIndex = 0
    var isPortrait = false
    var seekTo: Long = 0
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    lateinit var viewModel: PlayerViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        initUI()
    }

    override fun onClickGalleryItem(position: Int) {
        Utils.Log(TAG, "Position :$position")
        onUpdatedUI(position)
        player?.seekToDefaultPosition(position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG, "Nothing")
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val seekPosition: Long? = player?.currentPosition
        outState.putBoolean(getString(R.string.key_rotate), isPortrait)
        PrefsController.putLong(getString(R.string.key_seek_to), seekPosition!!.toLong())
        PrefsController.putInt(getString(R.string.key_lastWindowIndex), player!!.currentWindowIndex)
        Utils.Log(TAG, "Saved------------------------ " + seekPosition + " - " + player?.currentWindowIndex)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPortrait = savedInstanceState.getBoolean(getString(R.string.key_rotate), false)
        Utils.Log(TAG, "Restore $seekTo")
    }

    val dataSource: MutableList<ItemModel>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    val mediaDataSource : MutableList<MediaSource> = mutableListOf()
}

