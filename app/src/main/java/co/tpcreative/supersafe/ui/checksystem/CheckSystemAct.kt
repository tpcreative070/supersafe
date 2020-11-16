package co.tpcreative.supersafe.ui.checksystem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.util.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.viewmodel.CheckSystemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.ThreadMode

class CheckSystemAct : BaseGoogleApi(){
    var handler: Handler? = Handler(Looper.getMainLooper())
    lateinit var viewModel : CheckSystemViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_system)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
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
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ENABLE_CLOUD -> if (resultCode == Activity.RESULT_OK) {
                Utils.Log(TAG, "onBackPressed onActivity Result")
                onBackPressed()
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }

    override fun onDriveClientReady() {
        Utils.Log(TAG, "onDriveClient")
        CoroutineScope(Dispatchers.Main).launch {
            addUserCloud()
        }
    }

    override fun onDriveSuccessful() {}
    override fun onDriveError() {}
    override fun onDriveSignOut() {}
    override fun onDriveRevokeAccess() {}

    override fun startServiceNow() {}
    override fun isSignIn(): Boolean {
        return true
    }
    companion object {
        private val TAG = CheckSystemAct::class.java.simpleName
    }
}