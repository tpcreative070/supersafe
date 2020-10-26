package co.tpcreative.supersafe.ui.secretdoor
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.*
import co.tpcreative.supersafe.model.EnumStatus
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import me.grantland.widget.AutofitHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecretDoorSetUpActivity : BaseActivity(), Calculator {
    @BindView(R.id.imgLauncher)
    var imgLauncher: AppCompatImageView? = null

    @BindView(R.id.ic_SuperSafe)
    var ic_SuperSafe: ImageView? = null

    @BindView(R.id.rlSecretDoor)
    var relativeLayout: RelativeLayout? = null

    @BindView(R.id.calculator_holder)
    var calculator_holder: LinearLayout? = null

    @BindView(R.id.tvResult)
    var mResult: AppCompatTextView? = null

    @BindView(R.id.tvFormula)
    var mFormula: AppCompatTextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_door_set_up)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.hide()
        mCalc = CalculatorImpl(this)
        AutofitHelper.create(mResult)
        AutofitHelper.create(mFormula)
        val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
        if (options) {
            calculator_holder?.setVisibility(View.VISIBLE)
            imgLauncher?.setVisibility(View.INVISIBLE)
            relativeLayout?.setVisibility(View.INVISIBLE)
            val spannedDesc = SpannableString(getString(R.string.long_press_the_log))
            TapTargetView.showFor(this, TapTarget.forView(ic_SuperSafe, getString(R.string.try_it_now), spannedDesc)
                    .cancelable(false)
                    .titleTextDimen(R.dimen.text_size_title)
                    .titleTypeface(Typeface.DEFAULT_BOLD)
                    .tintTarget(true), object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    // .. which evidently starts the sequence we defined earlier
                }

                override fun onTargetLongClick(view: TapTargetView?) {
                    super.onTargetLongClick(view)
                    onShowDialog()
                }

                override fun onOuterCircleClick(view: TapTargetView?) {
                    super.onOuterCircleClick(view)
                }

                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    Utils.Log("TapTargetViewSample", "You dismissed me :(")
                }
            })
        } else {
            calculator_holder?.setVisibility(View.INVISIBLE)
            imgLauncher?.setVisibility(View.VISIBLE)
            relativeLayout?.setVisibility(View.VISIBLE)
            val spannedDesc = SpannableString(getString(R.string.long_press_the_log))
            TapTargetView.showFor(this, TapTarget.forView(imgLauncher, getString(R.string.try_it_now), spannedDesc)
                    .cancelable(false)
                    .titleTextDimen(R.dimen.text_size_title)
                    .titleTypeface(Typeface.DEFAULT_BOLD)
                    .tintTarget(true), object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    // .. which evidently starts the sequence we defined earlier
                }

                override fun onTargetLongClick(view: TapTargetView?) {
                    super.onTargetLongClick(view)
                    onShowDialog()
                }

                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    Utils.Log("TapTargetViewSample", "You dismissed me :(")
                }
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                finish()
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
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    fun onShowDialog() {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.enable_secret_door))
                .content(getString(R.string.enable_secret_door_detail))
                .theme(Theme.LIGHT)
                .titleColor(ContextCompat.getColor(this,R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door), true)
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door), false)
                        onBackPressed()
                    }
                })
        builder.show()
    }

    @OnClick(R.id.btn_plus)
    fun plusClicked() {
        mCalc?.handleOperation(Constants.PLUS)
    }

    @OnClick(R.id.btn_minus)
    fun minusClicked() {
        mCalc?.handleOperation(Constants.MINUS)
    }

    @OnClick(R.id.btn_multiply)
    fun multiplyClicked() {
        mCalc?.handleOperation(Constants.MULTIPLY)
    }

    @OnClick(R.id.btn_divide)
    fun divideClicked() {
        mCalc?.handleOperation(Constants.DIVIDE)
    }

    @OnClick(R.id.btn_modulo)
    fun moduloClicked() {
        mCalc?.handleOperation(Constants.MODULO)
    }

    @OnClick(R.id.btn_power)
    fun powerClicked() {
        mCalc?.handleOperation(Constants.POWER)
    }

    @OnClick(R.id.btn_root)
    fun rootClicked() {
        mCalc?.handleOperation(Constants.ROOT)
    }

    @OnClick(R.id.btn_clear)
    fun clearClicked() {
        mCalc?.handleClear()
    }

    @OnLongClick(R.id.btn_clear)
    fun clearLongClicked(): Boolean {
        mCalc?.handleReset()
        return true
    }

    @OnClick(R.id.btn_equals)
    fun equalsClicked() {
        mCalc?.handleEquals()
    }

    @OnClick(R.id.btn_decimal, R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9)
    fun numpadClick(view: View?) {
        view?.getId()?.let { numpadClicked(it) }
    }

    fun numpadClicked(id: Int) {
        mCalc?.numpadClicked(id)
    }

    override fun setValue(value: String?) {
        mResult?.setText(value)
    }

    override fun setValueDouble(d: Double) {
        mCalc?.setValue(Formatter.doubleToString(d))
        mCalc?.setLastKey(Constants.DIGIT)
    }

    override fun setFormula(value: String?) {
        mFormula?.setText(value)
    }

    companion object {
        private val TAG = SecretDoorSetUpActivity::class.java.simpleName
        private var mCalc: CalculatorImpl? = null
    }
}