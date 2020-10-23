package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.Unbinder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.HomeWatcher
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage
import spencerstudios.com.bungeelib.Bungee

abstract class BaseActivity : AppCompatActivity(), SensorFaceUpDownChangeNotifier.Listener {
    var unbinder: Unbinder? = null
    protected var actionBar: ActionBar? = null
    var onStartCount = 0
    private var mHomeWatcher: HomeWatcher? = null
    protected var storage: Storage? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = getSupportActionBar()
        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.animator.anim_slide_in_left,
                    R.animator.anim_slide_out_left)
        } else {
            onStartCount = 2
        }
        storage = Storage(this)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    protected open fun setStatusBarColored(context: AppCompatActivity, colorPrimary: Int, colorPrimaryDark: Int) {
        context.getSupportActionBar()?.setBackgroundDrawable(ColorDrawable(ContextCompat
                .getColor(context,colorPrimary)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = context.getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, colorPrimaryDark)
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
                Navigator.onMoveToFaceDown(SuperSafeApplication.Companion.getInstance())
            }
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        Log.d(TAG, "action here")
        unbinder = ButterKnife.bind(this)
    }

    protected override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.getInstance()?.remove(this)
        Utils.Log(TAG, "onPause")
        if (mHomeWatcher != null) {
            Utils.Log(TAG, "Stop home watcher....")
            mHomeWatcher?.stopWatch()
        }
    }

    protected override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop....")
    }

    protected override fun onDestroy() {
        Utils.Log(TAG, "onDestroy....")
        unbinder?.unbind()
        super.onDestroy()
    }

    protected override fun onResume() {
        Utils.Log(TAG, "onResume....")
        SensorFaceUpDownChangeNotifier.getInstance()?.addListener(this)
        super.onResume()
    }

    protected fun onRegisterHomeWatcher() {
        /*Home action*/
        if (mHomeWatcher != null) {
            if (mHomeWatcher?.isRegistered!!) {
                return
            }
        }
        mHomeWatcher = HomeWatcher(this)
        mHomeWatcher?.setOnHomePressedListener(object : HomeWatcher.OnHomePressedListener {
            override fun onHomePressed() {
                Utils.Log(TAG, "HomePressed")
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                val action = EnumPinAction.values()[value]
                when (action) {
                    EnumPinAction.NONE -> {
                        Utils.onHomePressed()
                        onStopListenerAWhile()
                    }
                    else -> {
                        Utils.Log(TAG, "Nothing to do on home " + action.name)
                    }
                }
                mHomeWatcher?.stopWatch()
            }

            override fun onHomeLongPressed() {
                Utils.Log(TAG, "Pressed long home button")
            }
        })
        mHomeWatcher?.startWatch()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onStart() {
        super.onStart()
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        val action = EnumPinAction.values()[value]
        when (action) {
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
        if (SingletonManager.Companion.getInstance().isAnimation()) {
            if (onStartCount > 1) {
                this.overridePendingTransition(R.animator.anim_slide_in_right,
                        R.animator.anim_slide_out_right)
            } else if (onStartCount == 1) {
                onStartCount++
            }
        } else {
            Bungee.zoom(this)
            SingletonManager.Companion.getInstance().setAnimation(true)
        }
    }

    protected abstract fun onStopListenerAWhile()

    companion object {
        val TAG = BaseActivity::class.java.simpleName
    }
}