package co.tpcreative.supersafe.ui.splashscreen
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.deleteDirectory
import co.tpcreative.supersafe.common.extension.isRunning
import co.tpcreative.supersafe.common.extension.putScreenStatus
import co.tpcreative.supersafe.common.extension.setRequestSyncData
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SplashScreenAct : BaseActivityNoneSlide() {
    private var grantAccess = false
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Utils.setRequestSyncData(true)
        /*Black list*/
        if (SuperSafeApplication.getInstance().getDeviceId() == "66801ac00252fe84") {
            finish()
        }
        try {
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            if (themeApp != null) {
                rlScreen?.setBackgroundColor(ContextCompat.getColor(this,themeApp.getPrimaryColor()))
            }
        } catch (e: Exception) {
            Utils.putThemeColor(0)
        }
        grantAccess = SuperSafeApplication.getInstance().isGrantAccess()
        SuperSafeApplication.getInstance().initFolder()
        var mCount = 0
        SQLHelper.getList()
        SuperSafeApplication.getInstance().responseMigration = {
            it?.let {
                runOnUiThread {
                    mCount +=1
                    tvTotal.text  = "$it/$mCount"
                }
            }
        }
        if(SuperSafeApplication.getInstance().isRequestMigration() && SuperSafeApplication.getInstance().isLiveMigration()){
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this@SplashScreenAct,R.string.improving_storage_fies)
            CoroutineScope(Dispatchers.Main).launch {
                val mPreparing = async {
                    SuperSafeApplication.getInstance().onPreparingMigration()
                }
                mPreparing.await()
                SuperSafeApplication.getInstance().getSuperSafeOldPath().deleteDirectory()
                onMessageEvent(EnumStatus.MIGRATION_DONE)
            }
        }else {
            if (grantAccess) {
                val mCurrentPin = SuperSafeApplication.getInstance().readKey()
                if (Utils.isRunning()) {
                    if (mCurrentPin?.isNotEmpty() == true) {
                        EncryptDecryptFilesHelper.getInstance()
                        EncryptDecryptPinHelper.getInstance()
                        Utils.putScreenStatus(EnumPinAction.SPLASH_SCREEN.ordinal)
                        Navigator.onMoveToMainTab(this@SplashScreenAct, false)
                    } else {
                        Utils.clearAppDataAndReCreateData()
                        Navigator.onMoveToDashBoard(this@SplashScreenAct)
                    }
                } else {
                    Navigator.onMoveToDashBoard(this@SplashScreenAct)
                }
            } else {
                Navigator.onMoveGrantAccess(this@SplashScreenAct)
            }
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.MIGRATION_DONE -> {
                EncryptDecryptFilesHelper.getInstance()
                EncryptDecryptPinHelper.getInstance()
                SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
                Utils.putScreenStatus(EnumPinAction.SPLASH_SCREEN.ordinal)
                Navigator.onMoveToMainTab(this,false)
                SuperSafeApplication.getInstance().getSuperSafeOldPath().deleteDirectory()
                finish()
            }
            else -> Utils.Log(TAG,"Nothing")
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
        menuInflater.inflate(R.menu.main_tab, menu)
        return true
    }
    override fun onOrientationChange(isFaceDown: Boolean) {}
}