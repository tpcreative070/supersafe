package co.tpcreative.supersafe.ui.secretdoor
import android.os.Bundle
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.activity_secret_door.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecretDoorAct : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_door)
        initUI()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        if (!b) {
            PrefsController.putBoolean(getString(R.string.key_secret_door), b)
        }
        tvStatus?.text = if (b) getString(R.string.enabled) else getString(R.string.disabled)
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
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
        val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
        btnSwitch?.isChecked = value
        if (options) {
            tvOptionItems?.text = getString(R.string.calculator)
            imgIcons?.setImageResource(R.drawable.ic_calculator)
        } else {
            tvOptionItems?.text = getString(R.string.virus_scanner)
            imgIcons?.setImageResource(R.drawable.baseline_donut_large_white_48)
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

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }
}