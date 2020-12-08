package co.tpcreative.supersafe.ui.secretdoor
import android.graphics.Typeface
import android.text.InputType
import android.text.SpannableString
import android.view.View
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.CalculatorImpl
import co.tpcreative.supersafe.common.util.Constants
import co.tpcreative.supersafe.common.util.Utils
import com.afollestad.materialdialogs.MaterialDialog
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import kotlinx.android.synthetic.main.activity_secret_door_set_up.*
import kotlinx.android.synthetic.main.footer_layout.*
import kotlinx.android.synthetic.main.include_calculator.*
import me.grantland.widget.AutofitHelper

fun SecretDoorSetUpAct.intUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.hide()
    mCalc = CalculatorImpl(this)
    AutofitHelper.create(tvResult)
    AutofitHelper.create(tvFormula)
    val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
    if (options) {
        calculator_holder?.visibility = View.VISIBLE
        imgLauncher?.visibility = View.INVISIBLE
        rlSecretDoor?.visibility = View.INVISIBLE
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

            override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                Utils.Log("TapTargetViewSample", "You dismissed me :(")
            }
        })
    } else {
        calculator_holder?.visibility = View.INVISIBLE
        imgLauncher?.visibility = View.VISIBLE
        rlSecretDoor?.visibility = View.VISIBLE
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

    btn_plus.setOnClickListener {
        mCalc?.handleOperation(Constants.PLUS)
    }

    btn_minus.setOnClickListener {
        mCalc?.handleOperation(Constants.MINUS)
    }

    btn_multiply.setOnClickListener {
        mCalc?.handleOperation(Constants.MULTIPLY)
    }

    btn_divide.setOnClickListener {
        mCalc?.handleOperation(Constants.DIVIDE)
    }

    btn_modulo.setOnClickListener {
        mCalc?.handleOperation(Constants.MODULO)
    }

    btn_power.setOnClickListener {
        mCalc?.handleOperation(Constants.POWER)
    }

    btn_root.setOnClickListener {
        mCalc?.handleOperation(Constants.ROOT)
    }

    btn_clear.setOnClickListener {
        mCalc?.handleClear()
    }

    btn_clear.setOnLongClickListener {
        mCalc?.handleReset()
        true
    }

    btn_equals.setOnClickListener {
        mCalc?.handleEquals()
    }

    btn_decimal.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_0.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_1.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_2.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_3.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_4.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_5.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_6.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_7.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_8.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
    btn_9.setOnClickListener {
        it?.id?.let { numpadClicked(it) }
    }
}

fun SecretDoorSetUpAct.numpadClicked(id: Int) {
    mCalc?.numpadClicked(id)
}

fun SecretDoorSetUpAct.onShowDialog() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.enable_secret_door))
            .message(text = getString(R.string.enable_secret_door_detail))
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .positiveButton {  PrefsController.putBoolean(getString(R.string.key_secret_door), true) }
            .negativeButton {
                PrefsController.putBoolean(getString(R.string.key_secret_door), false)
                onBackPressed()
            }
    builder.show()
}
