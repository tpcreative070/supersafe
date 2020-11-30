package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.HomeWatcher
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.ui.move_album.MoveAlbumFragment
import co.tpcreative.supersafe.ui.move_album.openAlbum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel


abstract class BaseGalleryActivity : AppCompatActivity(), MoveAlbumFragment.OnGalleryAttachedListener, SensorFaceUpDownChangeNotifier.Listener {
    private var mHomeWatcher: HomeWatcher? = null
    private var fragment: MoveAlbumFragment? = null
    var TAG : String = this::class.java.simpleName
    val mainScope = CoroutineScope(Dispatchers.Main)
    fun attachFragment(layoutId: Int) {
        fragment = MoveAlbumFragment.newInstance() as MoveAlbumFragment
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(layoutId, fragment!!).commit()
    }

    fun openAlbum() {
        fragment?.openAlbum()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true)
        }
        return theme
    }

    protected fun onFaceDown(isFaceDown: Boolean) {
        if (isFaceDown) {
            val result: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            if (result) {
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop....")
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.getInstance()?.remove(this)
        Utils.Log(TAG, "onPause")
        if (mHomeWatcher != null) {
            Utils.Log(TAG, "Stop home watcher....")
            mHomeWatcher!!.stopWatch()
        }
    }

    override fun onResume() {
        Utils.Log(TAG, "Action here........onResume")
        SensorFaceUpDownChangeNotifier.getInstance()?.addListener(this)
        super.onResume()
    }

    protected fun onRegisterHomeWatcher() {
        /*Home action*/
//        if (mHomeWatcher != null) {
//            if (mHomeWatcher!!.isRegistered) {
//                return
//            }
//        }
//        mHomeWatcher = HomeWatcher(this)
//        mHomeWatcher?.setOnHomePressedListener(object : HomeWatcher.OnHomePressedListener {
//            override fun onHomePressed() {
//                when (val action = EnumPinAction.values()[Utils.getScreenStatus()]) {
//                    EnumPinAction.NONE -> {
//                        Utils.onHomePressed()
//                        onStopListenerAWhile()
//                    }
//                    else -> {
//                        Utils.Log(TAG, "Nothing to do on home " + action.name)
//                    }
//                }
//                mHomeWatcher?.stopWatch()
//            }
//
//            override fun onHomeLongPressed() {
//                Utils.Log(TAG, "Pressed long home button")
//            }
//        })
//        mHomeWatcher?.startWatch()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Utils.onScanFile(this,"scan.log")
        when (val action = EnumPinAction.values()[Utils.getScreenStatus()]) {
            EnumPinAction.SCREEN_LOCK -> {
                if (!SingletonManager.Companion.getInstance().isVisitLockScreen()) {
                    SuperSafeApplication.getInstance().getActivity()?.let { Navigator.onMoveToVerifyPin(it, EnumPinAction.NONE) }
                    Utils.Log(TAG, "Pressed home button")
                    SingletonManager.Companion.getInstance().setVisitLockScreen(true)
                    Utils.Log(TAG, "Verify pin")
                } else {
                    Utils.Log(TAG, "Verify pin already")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do on start " + action.name)
            }
        }
    }

    protected abstract fun onStopListenerAWhile()

    companion object {
        val TAG = BaseGalleryActivity::class.java.simpleName
    }
}