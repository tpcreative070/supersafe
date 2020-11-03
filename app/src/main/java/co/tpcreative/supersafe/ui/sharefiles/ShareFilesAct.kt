package co.tpcreative.supersafe.ui.sharefiles
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNone
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.snatik.storage.Storage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ShareFilesAct : BaseActivityNone() {
    val mListFile: MutableList<Int> = ArrayList()
    var dialog: AlertDialog? = null
    val mListImport: MutableList<ImportFilesModel> = ArrayList<ImportFilesModel>()
    var count = 0
    var mStore : Storage? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_files)
        mStore = Storage(this)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        ServiceManager.getInstance()?.setRequestShareIntent(true)
        Utils.onScanFile(this, "scan.log")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Utils.onDeleteTemporaryFile()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.IMPORTED_COMPLETELY -> {
                try {
                    onStopProgressing()
                    onShowUI(View.VISIBLE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }
}