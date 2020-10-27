package co.tpcreative.supersafe.ui.splashscreen
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SplashScreenActivity : BaseActivityNoneSlide() {
    private var value: String? = ""
    private var grant_access = false
    private var isRunning = false

    @BindView(R.id.rlScreen)
    var rlScreen: RelativeLayout? = null
    private val DELAY = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        /*Black list*/if (SuperSafeApplication.getInstance().getDeviceId() == "66801ac00252fe84") {
            finish()
        }
        try {
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            if (themeApp != null) {
                rlScreen?.setBackgroundColor(ContextCompat.getColor(this,themeApp.getPrimaryColor()))
            }
        } catch (e: Exception) {
            val themeApp = ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton, "#0091EA")
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_theme_object), Gson().toJson(themeApp))
        }
        value = SuperSafeApplication.getInstance().readKey()
        grant_access = PrefsController.getBoolean(getString(R.string.key_grant_access), false)
        isRunning = PrefsController.getBoolean(getString(R.string.key_running), false)
        grant_access = if (SuperSafeApplication.getInstance().isGrantAccess()) {
            true
        } else {
            false
        }
        SuperSafeApplication.getInstance().initFolder()
        Utils.Log(TAG, "Key $value")
        val manufacturer: String = Build.MANUFACTURER
        val model: String = Build.MODEL
        val version: Int = Build.VERSION.SDK_INT
        val versionRelease: String = Build.VERSION.RELEASE
        Utils.Log(TAG, """manufacturer $manufacturer 
 model $model 
 version $version 
 versionRelease $versionRelease"""
        )
        SQLHelper.getList()
        Utils.onWriteLog(Utils.DeviceInfo(), EnumStatus.DEVICE_ABOUT)
        Utils.onObserveData(DELAY.toLong(), object :Listener {
            override fun onStart() {
                if (grant_access) {
                    if (isRunning) {
                        if ("" != value) {
                            PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.SPLASH_SCREEN.ordinal)
                            Navigator.onMoveToMainTab(this@SplashScreenActivity)
                        } else {
                            SuperSafeApplication.getInstance().deleteFolder()
                            SuperSafeApplication.getInstance().initFolder()
                            SQLHelper.onCleanDatabase()
                            Utils.setUserPreShare(User())
                            SQLHelper.getList()
                            PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive), true)
                            Navigator.onMoveToDashBoard(this@SplashScreenActivity)
                        }
                    } else {
                        Navigator.onMoveToDashBoard(this@SplashScreenActivity)
                    }
                } else {
                    Navigator.onMoveGrantAccess(this@SplashScreenActivity)
                }
                finish()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_tab, menu)
        return true
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onPause() {
        super.onPause()
    }

    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }
}