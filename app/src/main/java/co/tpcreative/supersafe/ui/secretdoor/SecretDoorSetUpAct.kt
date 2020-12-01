package co.tpcreative.supersafe.ui.secretdoor
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.util.*
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.include_calculator.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecretDoorSetUpAct : BaseActivity(), Calculator {
    var mCalc: CalculatorImpl? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_door_set_up)
        intUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                finish()
            }
            else -> Utils.Log(TAG, "Nothing")
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

    override fun onOrientationChange(isFaceDown: Boolean) {}

    override fun setValue(value: String?) {
        tvResult?.text = value
    }

    override fun setValueDouble(d: Double) {
        mCalc?.setValue(Formatter.doubleToString(d))
        mCalc?.setLastKey(Constants.DIGIT)
    }

    override fun setFormula(value: String?) {
        tvFormula?.text = value
    }
}