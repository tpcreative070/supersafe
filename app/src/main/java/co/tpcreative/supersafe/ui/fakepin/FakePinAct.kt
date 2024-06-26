package co.tpcreative.supersafe.ui.fakepin
import android.os.Bundle
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.extension.putFacePin
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
        Utils.Log(BaseActivity.TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        Utils.putFacePin(b)
        if (b) {
            tvCreatePin?.setTextColor(ContextCompat.getColor(this,mThemeApp?.getAccentColor()!!))
            tvCreatePin?.isEnabled = b
        } else {
            tvCreatePin?.setTextColor(ContextCompat.getColor(this,R.color.material_gray_500))
            tvCreatePin?.isEnabled = b
        }
        tvStatus?.text = (if (b) getString(R.string.enabled) else getString(R.string.disable))
    }
}