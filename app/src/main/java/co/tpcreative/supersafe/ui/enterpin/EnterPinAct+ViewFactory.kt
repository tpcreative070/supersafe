package co.tpcreative.supersafe.ui.enterpin
import android.animation.ObjectAnimator
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.infinum.goldfinger.Goldfinger
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.SingletonScreenLock
import co.tpcreative.supersafe.common.extension.getScreenStatus
import co.tpcreative.supersafe.common.extension.isSecretDoor
import co.tpcreative.supersafe.common.extension.isSecretDoorOfCalculator
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.CalculatorImpl
import co.tpcreative.supersafe.common.util.Constants
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.ui.enterpin.EnterPinAct.Companion.viewModel
import co.tpcreative.supersafe.viewmodel.LockScreenViewModel
import kotlinx.android.synthetic.main.activity_enterpin.*
import kotlinx.android.synthetic.main.footer_layout.*
import kotlinx.android.synthetic.main.include_calculator.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.grantland.widget.AutofitHelper

fun EnterPinAct.initUI() {
    TAG = this::class.java.simpleName
    CoroutineScope(Dispatchers.Main).launch {
        val mResult = async {
            setupViewModel()
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            val result: Int = intent.getIntExtra(EnterPinAct.EXTRA_SET_PIN, 0)
            EnterPinAct.mPinAction = EnumPinAction.values()[result]
            val resultNext: Int = intent.getIntExtra(EnterPinAct.EXTRA_ENUM_ACTION, 0)
            EnterPinAct.mPinActionNext = EnumPinAction.values()[resultNext]
            SingletonScreenLock.getInstance()?.setListener(this@initUI)
            EnterPinAct.enumPinPreviousAction = EnterPinAct.mPinAction
            when (EnterPinAct.mPinAction) {
                EnumPinAction.SET -> {
                    onDisplayView()
                    onDisplayText()
                }
                EnumPinAction.VERIFY -> {
                    if (mRealPin == "") {
                        EnterPinAct.mPinAction = EnumPinAction.SET
                        onDisplayView()
                        onDisplayText()
                    } else {
                        if (Utils.isSensorAvailable()) {
                            val isFingerPrintUnLock: Boolean = Utils.isAvailableBiometric()
                            if (isFingerPrintUnLock) {
                                imgSwitchTypeUnClock?.visibility = View.VISIBLE
                                isFingerprint = isFingerPrintUnLock
                                onSetVisitFingerprintView(isFingerprint)
                                Utils.Log(TAG, "Action find fingerPrint")
                            }
                        }
                        val value: Boolean = Utils.isSecretDoor()
                        if (value) {
                            imgSwitchTypeUnClock?.visibility = View.INVISIBLE
                            changeLayoutSecretDoor(true)
                        } else {
                            calculator_holder?.visibility = View.INVISIBLE
                            onDisplayView()
                            onDisplayText()
                        }
                    }
                }
                EnumPinAction.INIT_PREFERENCE -> {
                    initActionBar(true)
                    onDisplayText()
                    onDisplayView()
                    onLauncherPreferences()
                }
                EnumPinAction.RESET -> {
                    onDisplayView()
                    onDisplayText()
                }
                EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                    onDisplayText()
                    onDisplayView()
                }
                else -> {
                    Utils.Log(TAG, "Noting to do")
                }
            }
            imgLauncher?.setOnLongClickListener {
                changeLayoutSecretDoor(false)
                false
            }
            ic_SuperSafe?.setOnLongClickListener {
                changeLayoutSecretDoor(false)
                false
            }
            if (Utils.isSensorAvailable()) {
                if (Utils.isAvailableBiometric() && Utils.getScreenStatus()==EnumPinAction.SCREEN_LOCK.ordinal) {
                    initBiometric()
                }
            }
            onInitPin()
            /*Calculator init*/
            EnterPinAct.mCalc = CalculatorImpl(this@initUI)
            AutofitHelper.create(tvResult)
            AutofitHelper.create(tvFormula)
            Utils.Log(TAG, "onCreated->EnterPinActivity")
            llForgotPin.setOnClickListener {
                Navigator.onMoveToForgotPin(this@initUI, false)
            }

        }
        mResult.await()

        btnDone.setOnClickListener {
            Navigator.onMoveToFaceDown(this@initUI)
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
            isFingerprint = !isFingerprint
            onSetVisitFingerprintView(isFingerprint)
        }
        imgFingerprint.setOnClickListener {
            val isFingerPrintUnLock: Boolean = Utils.isAvailableBiometric()
            if (isFingerPrintUnLock) {
                startBiometricPrompt()
            }
        }
    }
}

fun EnterPinAct.checkPin(pin: String?, isCompleted: Boolean)  = CoroutineScope(Dispatchers.Main).launch{
    when (EnterPinAct.mPinAction) {
        EnumPinAction.VERIFY -> {
            /*Existing fake instance*/
            if (SingletonManager.getInstance().isVisitFakePin()) {
                if (pin == mFakePin && isFakePinEnabled) {
                    changeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE,pin,false,false)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            } else {
                if (pin == mRealPin) {
                    /*Noted code here*/
                    changeStatus(EnumStatus.VERIFY, EnumPinAction.DONE,pin,true,false)
                } else if (pin == mFakePin && isFakePinEnabled) {
                    /*New instance app*/
                    changeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE,pin,false,false)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            }
        }
        EnumPinAction.VERIFY_TO_CHANGE -> {
            if (pin == mRealPin) {
                changeStatus(EnumStatus.VERIFY, EnumPinAction.CHANGE,pin,true,false)
            } else {
                if (isCompleted) {
                    onTakePicture(pin)
                    onAlertWarning("")
                }
            }
        }
        EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
            /*This is special for this case*/
            if (pin == mRealPin) {
                changeStatus(EnumStatus.VERIFY, EnumPinAction.FAKE_PIN,pin,true,false)
            } else {
                if (isCompleted) {
                    onTakePicture(pin)
                    onAlertWarning("")
                }
            }
        }
        else -> Utils.Log(TAG,"Nothing")
    }
}

fun EnterPinAct.shake() {
    val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(pinlockView, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
    objectAnimator.start()
    when (EnterPinAct.mPinAction) {
        EnumPinAction.VERIFY -> {
            count += 1
            onSetVisitableForgotPin(View.VISIBLE)
            if (count >= 3) {
                countAttempt = count * 10
                val attemptWaiting = count * 10000.toLong()
                EnterPinAct.mPinAction = EnumPinAction.ATTEMPT
                onDisplayView()
                SingletonScreenLock.getInstance()?.onStartTimer(attemptWaiting)
            }
        }
        EnumPinAction.VERIFY_TO_CHANGE -> {
            count += 1
            onSetVisitableForgotPin(View.VISIBLE)
            if (count >= 3) {
                countAttempt = count * 10
                val attemptWaiting = count * 10000.toLong()
                EnterPinAct.mPinAction = EnumPinAction.ATTEMPT
                onDisplayView()
                SingletonScreenLock.getInstance()?.onStartTimer(attemptWaiting)
            }
        }
        else -> Utils.Log(TAG,"Nothing")
    }
    Utils.Log(TAG, "Visit....$count")
}

fun EnterPinAct.onAlertWarning(title: String?) {
    when (EnterPinAct.mPinAction) {
        EnumPinAction.SET -> {
            shake()
            tvTitle.text = title
            pinlockView.resetPinLockView()
            mFirstPin = ""
        }
        EnumPinAction.CHANGE -> {
            shake()
            tvTitle.text = title
            pinlockView.resetPinLockView()
            mFirstPin = ""
        }
        EnumPinAction.FAKE_PIN -> {
            shake()
            tvTitle.text = title
            pinlockView.resetPinLockView()
            mFirstPin = ""
        }
        EnumPinAction.RESET -> {
            shake()
            tvTitle.text = title
            pinlockView.resetPinLockView()
            mFirstPin = ""
        }
        EnumPinAction.VERIFY -> {
            shake()
            tvTitle.text = title
            tvTopAttempts?.text = getString(R.string.pinlock_wrongpin)
            pinlockView.resetPinLockView()
        }
        EnumPinAction.VERIFY_TO_CHANGE -> {
            shake()
            tvTitle.text = title
            tvTopAttempts?.text = getString(R.string.pinlock_wrongpin)
            pinlockView.resetPinLockView()
        }
        EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
            shake()
            tvTitle.text = title
            tvTopAttempts?.text = getString(R.string.pinlock_wrongpin)
            pinlockView.resetPinLockView()
        }
        else -> Utils.Log(TAG,"Nothing")
    }
}

fun EnterPinAct.changeLayoutSecretDoor(isVisit: Boolean) {
    if (isVisit) {
        tvTitle.visibility = View.INVISIBLE
        rlButton?.visibility = View.INVISIBLE
        rlDots?.visibility = View.INVISIBLE
        tvTopAttempts?.visibility = View.INVISIBLE
        val options: Boolean = Utils.isSecretDoorOfCalculator()
        if (options) {
            imgLauncher?.visibility = View.INVISIBLE
            rlSecretDoor?.visibility = View.INVISIBLE
            calculator_holder?.visibility = View.VISIBLE
        } else {
            imgLauncher?.visibility = View.VISIBLE
            rlSecretDoor?.visibility = View.VISIBLE
            calculator_holder?.visibility = View.INVISIBLE
        }
    } else {
        tvTitle.visibility = View.VISIBLE
        rlButton?.visibility = View.VISIBLE
        rlDots?.visibility = View.VISIBLE
        tvTopAttempts.visibility = View.VISIBLE
        tvTopAttempts.text = ""
        imgLauncher?.visibility = View.INVISIBLE
        rlSecretDoor?.visibility = View.INVISIBLE
        calculator_holder?.visibility = View.INVISIBLE
        if (Utils.isSensorAvailable()) {
            val isFingerPrintUnLock: Boolean = Utils.isAvailableBiometric()
            if (isFingerPrintUnLock) {
                imgSwitchTypeUnClock?.visibility = View.VISIBLE
                isFingerprint = isFingerPrintUnLock
                onSetVisitFingerprintView(isFingerprint)
            } else {
                imgSwitchTypeUnClock?.visibility = View.GONE
            }
        } else {
            imgSwitchTypeUnClock?.visibility = View.GONE
        }
    }
}


fun EnterPinAct.onDisplayView() {
    Utils.Log(TAG, "EnumPinAction 2:...." + EnterPinAct.mPinAction?.name)
    when (EnterPinAct.mPinAction) {
        EnumPinAction.SET -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.VERIFY -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.VERIFY_TO_CHANGE -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.CHANGE -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.INIT_PREFERENCE -> {
            rlLockScreen?.visibility = View.INVISIBLE
            rlPreference?.visibility = View.VISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.FAKE_PIN -> {
            rlLockScreen?.visibility = View.VISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.INVISIBLE
        }
        EnumPinAction.ATTEMPT -> {
            rlLockScreen?.visibility = View.INVISIBLE
            rlPreference?.visibility = View.INVISIBLE
            rlAttempt?.visibility = View.VISIBLE
            val result: String = kotlin.String.format(getString(R.string.in_correct_pin), count.toString() + "", countAttempt.toString() + "")
            tvAttempt?.text = result
            Utils.Log(TAG, EnterPinAct.mPinAction!!.name)
        }
        else -> Utils.Log(TAG,"Nothing")
    }
}

fun EnterPinAct.onDisplayText() {
    Utils.Log(TAG, "EnumPinAction 3:...." + EnterPinAct.mPinAction?.name)
    when (EnterPinAct.mPinAction) {
        EnumPinAction.VERIFY -> {
            tvTitle.visibility = View.INVISIBLE
            imgLauncher?.visibility = View.VISIBLE
            imgLauncher?.isEnabled = false
        }
        EnumPinAction.VERIFY_TO_CHANGE -> {
            tvTitle.text = getString(R.string.pinlock_confirm_your_pin)
            tvTitle.visibility = View.VISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.CHANGE -> {
            tvTitle.text = getString(R.string.pinlock_confirm_create)
            tvTitle.visibility = View.VISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.INIT_PREFERENCE -> {
            tvTitle.text = getString(R.string.pinlock_confirm_your_pin)
            tvTitle.visibility = View.VISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
            tvTitle.text = getString(R.string.pinlock_confirm_your_pin)
            tvTitle.visibility = View.VISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.FAKE_PIN -> {
            tvTitle.text = getString(R.string.pinlock_confirm_create)
            tvTitle.visibility = View.VISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.SET -> {
            tvTitle.text = getString(R.string.pinlock_settitle)
            tvTitle.visibility = View.VISIBLE
            tvTopAttempts?.visibility = View.INVISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        EnumPinAction.RESET -> {
            tvTitle.text = getString(R.string.pinlock_settitle)
            tvTitle.visibility = View.VISIBLE
            tvTopAttempts?.visibility = View.INVISIBLE
            imgLauncher?.visibility = View.INVISIBLE
        }
        else -> Utils.Log(TAG,"Nothing")
    }
}

fun EnterPinAct.setPin(pin: String?) {
    when (EnterPinAct.mPinAction) {
        EnumPinAction.SET -> {
            if (mFirstPin == "") {
                mFirstPin = pin
                tvTitle.text = getString(R.string.pinlock_secondPin)
                pinlockView.resetPinLockView()
            } else {
                if (pin == mFirstPin) {
                    /*Close for old version*/
                    //Utils.writePinToSharedPreferences(pin)
                    when (EnterPinAct.mPinActionNext) {
                        EnumPinAction.SIGN_UP -> {
                            pin?.let {
                                Navigator.onMoveToSignUp(this,it)
                            }
                        }
                        else -> {
                            Navigator.onMoveToMainTab(this,true)
                            changeStatus(EnumStatus.SET, EnumPinAction.DONE,pin,true,true)
                        }
                    }
                } else {
                    onAlertWarning(getString(R.string.pinlock_tryagain))
                }
            }
        }
        EnumPinAction.CHANGE -> {
            if (mFirstPin == "") {
                mFirstPin = pin
                tvTitle.text = getString(R.string.pinlock_secondPin)
                pinlockView.resetPinLockView()
            } else {
                if (pin == mFirstPin) {
                    if (Utils.isExistingFakePin(pin, mFakePin)) {
                        onAlertWarning(getString(R.string.pin_lock_replace))
                    } else {
                        changeStatus(EnumStatus.CHANGE, EnumPinAction.DONE,pin,true,true)
                    }
                } else {
                    onAlertWarning(getString(R.string.pinlock_tryagain))
                }
            }
        }
        EnumPinAction.FAKE_PIN -> {
            if (mFirstPin == "") {
                mFirstPin = pin
                tvTitle.text = getString(R.string.pinlock_secondPin)
                pinlockView.resetPinLockView()
            } else {
                if (pin == mFirstPin) {
                    if (Utils.isExistingRealPin(pin, mRealPin)) {
                        onAlertWarning(getString(R.string.pin_lock_replace))
                    } else {
                        changeStatus(EnumStatus.CREATE_FAKE_PIN, EnumPinAction.DONE,pin,false,true)
                    }
                } else {
                    onAlertWarning(getString(R.string.pinlock_tryagain))
                }
            }
        }
        EnumPinAction.RESET -> {
            if (mFirstPin == "") {
                mFirstPin = pin
                tvTitle.text = getString(R.string.pinlock_secondPin)
                pinlockView.resetPinLockView()
            } else {
                if (pin == mFirstPin) {
                    if (Utils.isExistingFakePin(pin, mFakePin)) {
                        onAlertWarning(getString(R.string.pin_lock_replace))
                    } else {
                        Navigator.onMoveToMainTab(this,true)
                        changeStatus(EnumStatus.RESET, EnumPinAction.DONE,pin,true,true)
                    }
                } else {
                    onAlertWarning(getString(R.string.pinlock_tryagain))
                }
            }
        }
        else -> Utils.Log(TAG,"Nothing")
    }
}

fun EnterPinAct.initBiometric(){
    goldfinger = Goldfinger.Builder(this)
            .logEnabled(true)
            .build()
    startBiometricPrompt()
}

fun EnterPinAct.startBiometricPrompt(){
    if (goldfinger!!.canAuthenticate()){
        goldfinger!!.authenticate(buildPromptParams()!!, object : Goldfinger.Callback {
            override fun onError(e: Exception) {}
            override fun onResult(result: Goldfinger.Result) {
                handleGoldfingerResult(result)
            }
        })
    }
}

fun EnterPinAct.onInitPin() {
    indicator_dots?.setActivity(this)
    pinlockView.attachIndicatorDots(indicator_dots)
    pinlockView.setPinLockListener(pinLockListener)
    pinlockView.setPinLength(EnterPinAct.PIN_LENGTH)
    indicator_dots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION)
    onInitHiddenCamera()
}

private fun EnterPinAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(LockScreenViewModel::class.java)
}

fun EnterPinAct.changeStatus(status: EnumStatus?, action: EnumPinAction?, value : String?,isRealPin: Boolean,isWrite : Boolean){
    value?.let {
        SuperSafeApplication.getInstance().checkingMigrationAfterVerifiedPin(value,isRealPin,isWrite)
    }
    viewModel.changeStatus(status,action).observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            onSuccessful(it.status,it.action)
        }
    })
}


