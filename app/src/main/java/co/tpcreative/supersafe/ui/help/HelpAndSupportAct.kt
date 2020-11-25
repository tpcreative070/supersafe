package co.tpcreative.supersafe.ui.help
import android.app.Activity
import android.content.Context
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.HelpAndSupportModel
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.viewmodel.HelpAndSupportViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class HelpAndSupportAct : BaseActivity(), HelpAndSupportCell.ItemSelectedListener {
    lateinit var viewModel : HelpAndSupportViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)
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

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "position :$position")
        Navigator.onMoveHelpAndSupportContent(this, dataSource[position])
    }

    val dataSource : MutableList<HelpAndSupportModel> = mutableListOf()
    companion object {
        private val TAG = HelpAndSupportAct::class.java.simpleName
    }
}