package co.tpcreative.supersafe.ui.secretdoor
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import com.afollestad.materialdialogs.MaterialDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecretDoorActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.btnSwitch)
    var btnSwitch: SwitchCompat? = null

    @BindView(R.id.rlScanner)
    var rlScanner: RelativeLayout? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null

    @BindView(R.id.tvOptionItems)
    var tvOptionItems: AppCompatTextView? = null

    @BindView(R.id.imgIcons)
    var imgIcons: AppCompatImageView? = null

    @BindView(R.id.tvStatus)
    var tvStatus: AppCompatTextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_door)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        btnSwitch?.setOnCheckedChangeListener(this)
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
        val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
        btnSwitch?.setChecked(value)
        if (options) {
            tvOptionItems?.setText(getString(R.string.calculator))
            imgIcons?.setImageResource(R.drawable.ic_calculator)
        } else {
            tvOptionItems?.setText(getString(R.string.virus_scanner))
            imgIcons?.setImageResource(R.drawable.baseline_donut_large_white_48)
        }
        btnSwitch?.setOnClickListener(View.OnClickListener {
            if (btnSwitch?.isChecked()!!) {
                Navigator.onMoveSecretDoorSetUp(this@SecretDoorActivity)
            }
        })
        tvPremiumDescription?.setText(getString(R.string.secret_door))
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        if (!b) {
            PrefsController.putBoolean(getString(R.string.key_secret_door), b)
        }
        tvStatus?.setText(if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }

    @OnClick(R.id.rlSwitch)
    fun onActionSwitch(view: View?) {
        btnSwitch?.setChecked(!btnSwitch?.isChecked()!!)
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
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
        val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
        btnSwitch?.setChecked(value)
        if (options) {
            tvOptionItems?.setText(getString(R.string.calculator))
            imgIcons?.setImageResource(R.drawable.ic_calculator)
        } else {
            tvOptionItems?.setText(getString(R.string.virus_scanner))
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

    @OnClick(R.id.rlScanner)
    fun onClickedOption(view: View?) {
        onChooseOptionItems()
    }

    fun onChooseOptionItems() {
        val dialog: MaterialDialog = MaterialDialog.Builder(this)
                .items(R.array.select_option)
                .itemsCallback(object : MaterialDialog.ListCallback {
                    override fun onSelection(dialog: MaterialDialog?, itemView: View?, position: Int, text: CharSequence?) {
                        Utils.Log(TAG, "position $position")
                        when (position) {
                            0 -> {
                                PrefsController.putBoolean(getString(R.string.key_calculator), false)
                                tvOptionItems?.setText(getString(R.string.virus_scanner))
                                imgIcons?.setImageResource(R.drawable.baseline_donut_large_white_48)
                                val isFirstScanVirus: Boolean = PrefsController.getBoolean(getString(R.string.is_first_scan_virus), false)
                                if (!isFirstScanVirus) {
                                    Navigator.onMoveSecretDoorSetUp(this@SecretDoorActivity)
                                    PrefsController.putBoolean(getString(R.string.is_first_scan_virus), true)
                                }
                            }
                            else -> {
                                PrefsController.putBoolean(getString(R.string.key_calculator), true)
                                tvOptionItems?.setText(getString(R.string.calculator))
                                imgIcons?.setImageResource(R.drawable.ic_calculator)
                                val isFirstCalculator: Boolean = PrefsController.getBoolean(getString(R.string.is_first_calculator), false)
                                if (!isFirstCalculator) {
                                    Navigator.onMoveSecretDoorSetUp(this@SecretDoorActivity)
                                    PrefsController.putBoolean(getString(R.string.is_first_calculator), true)
                                }
                            }
                        }
                    }
                })
                .build()
        dialog.show()
    }

    companion object {
        private val TAG = SecretDoorActivity::class.java.simpleName
    }
}