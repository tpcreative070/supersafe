package co.tpcreative.supersafe.ui.fakepin
import android.os.Bundle
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.activity_fake_pin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FakePinAct : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_pin)
        initUI()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(BaseActivity.TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        PrefsController.putBoolean(getString(R.string.key_fake_pin), b)
        if (b) {
            tvCreatePin?.setTextColor(ContextCompat.getColor(this,mThemeApp?.getPrimaryColor()!!))
            tvCreatePin?.setEnabled(b)
        } else {
            tvCreatePin?.setTextColor(ContextCompat.getColor(this,R.color.material_gray_500))
            tvCreatePin?.setEnabled(b)
        }
        tvStatus?.text = (if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }
}