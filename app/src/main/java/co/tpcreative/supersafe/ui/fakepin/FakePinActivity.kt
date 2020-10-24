package co.tpcreative.supersafe.ui.fakepin
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FakePinActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.btnSwitch)
    var btnSwitch: SwitchCompat? = null

    @BindView(R.id.tvCreatePin)
    var tvCreatePin: AppCompatTextView? = null

    @BindView(R.id.llView)
    var llView: LinearLayout? = null

    @BindView(R.id.imgView)
    var imgView: AppCompatImageView? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null

    @BindView(R.id.tvStatus)
    var tvStatus: AppCompatTextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_pin)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        btnSwitch?.setOnCheckedChangeListener(this)
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_fake_pin), false)
        btnSwitch?.setChecked(value)
        tvCreatePin?.setEnabled(value)
        val fakePin: String? = SuperSafeApplication.Companion.getInstance().readFakeKey()
        if (fakePin == "") {
            tvCreatePin?.setText(getText(R.string.create_fake_pin))
        } else {
            tvCreatePin?.setText(getText(R.string.change_fake_pin))
        }
        tvPremiumDescription?.setText(getString(R.string.fake_pin))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(BaseActivity.TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
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
            //imgView.setColorFilter(getResources().getColor(mThemeApp.getPrimaryColor()), PorterDuff.Mode.SRC_ATOP);
        } else {
            tvCreatePin?.setTextColor(ContextCompat.getColor(this,R.color.material_gray_500))
            tvCreatePin?.setEnabled(b)
            //imgView.setColorFilter(getResources().getColor(R.color.material_gray_500), PorterDuff.Mode.SRC_ATOP);
        }
        tvStatus?.setText(if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }

    @OnClick(R.id.tvCreatePin)
    fun onCreatePin(view: View?) {
        Navigator.onMoveToFakePin(this, EnumPinAction.NONE)
    }

    @OnClick(R.id.imgView)
    fun onViewComponent(view: View?) {
        Navigator.onMoveFakePinComponentInside(this)
    }

    @OnClick(R.id.rlSwitch)
    fun onActionSwitch(view: View?) {
        btnSwitch?.setChecked(!btnSwitch?.isChecked()!!)
    }
}