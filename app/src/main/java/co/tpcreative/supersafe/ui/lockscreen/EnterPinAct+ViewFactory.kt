package co.tpcreative.supersafe.ui.lockscreen
import android.view.View
import butterknife.OnClick
import butterknife.OnLongClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.util.Constants
import kotlinx.android.synthetic.main.activity_enterpin.*
import kotlinx.android.synthetic.main.footer_layout.*
import kotlinx.android.synthetic.main.include_calculator.*

fun EnterPinAct.initUI(){

    llForgotPin.setOnClickListener {
        Navigator.onMoveToForgotPin(this, false)
    }

    btnDone.setOnClickListener {
        finish()
    }

    btn_decimal.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_0.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_1.setOnClickListener {
        numpadClicked(it.id)
    }

    btn_2.setOnClickListener {
        numpadClicked(it.id)
    }

    btn_3.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_4.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_5.setOnClickListener {
        numpadClicked(it.id)
    }

    btn_6.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_7.setOnClickListener {
        numpadClicked(it.id)
    }
    btn_8.setOnClickListener {
        numpadClicked(it.id)
    }

    btn_9.setOnClickListener {
        numpadClicked(it.id)
    }

    btn_plus.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.PLUS)
    }

    btn_minus.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.MINUS)
    }

    btn_multiply.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.MULTIPLY)
    }

    btn_divide.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.DIVIDE)
    }

    btn_modulo.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.MODULO)
    }

    btn_power.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.POWER)
    }

    btn_root.setOnClickListener {
        EnterPinAct.mCalc?.handleOperation(Constants.ROOT)
    }

    btn_clear.setOnClickListener {
        EnterPinAct.mCalc?.handleClear()
    }

    btn_clear.setOnLongClickListener {
        EnterPinAct.mCalc?.handleReset()
        true
    }

    btn_equals.setOnClickListener {
        EnterPinAct.mCalc?.handleEquals()
    }

    imgSwitchTypeUnClock.setOnClickListener {
        isFingerprint = if (isFingerprint) {
            false
        } else {
            true
        }
        onSetVisitFingerprintView(isFingerprint)
    }
}