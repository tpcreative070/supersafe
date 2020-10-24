package co.tpcreative.supersafe.ui.lockscreen
import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.User
import com.multidots.fingerprintauth.FingerPrintAuthCallback
import org.greenrobot.eventbus.EventBus
import java.io.File
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.*
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig
import co.tpcreative.supersafe.common.hiddencamera.CameraError
import co.tpcreative.supersafe.common.hiddencamera.config.*
import co.tpcreative.supersafe.common.preference.MyPreference
import co.tpcreative.supersafe.common.preference.MySwitchPreference
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.*
import co.tpcreative.supersafe.model.BreakInAlertsModel
import co.tpcreative.supersafe.model.EnumStatus
import com.github.kratorius.circleprogress.CircleProgressView
import com.multidots.fingerprintauth.FingerPrintAuthHelper
import me.grantland.widget.AutofitHelper

class EnterPinActivity : BaseVerifyPinActivity(), BaseView<EnumPinAction>, Calculator, FingerPrintAuthCallback, SingletonMultipleListener.Listener, SingletonScreenLock.SingletonScreenLockListener {
    @BindView(R.id.pinlockView)
    var mPinLockView: PinLockView? = null

    @BindView(R.id.indicator_dots)
    var mIndicatorDots: IndicatorDots? = null

    @BindView(R.id.title)
    var mTextTitle: AppCompatTextView? = null

    @BindView(R.id.attempts)
    var mTextAttempts: AppCompatTextView? = null

    @BindView(R.id.imgLauncher)
    var imgLauncher: AppCompatImageView? = null

    @BindView(R.id.ic_SuperSafe)
    var ic_SuperSafe: AppCompatImageView? = null

    @BindView(R.id.rlLockScreen)
    var rlLockScreen: RelativeLayout? = null

    @BindView(R.id.rlPreference)
    var rlPreference: RelativeLayout? = null

    @BindView(R.id.llForgotPin)
    var llForgotPin: LinearLayout? = null

    @BindView(R.id.rlButton)
    var rlButton: RelativeLayout? = null

    @BindView(R.id.rlDots)
    var rlDots: RelativeLayout? = null

    @BindView(R.id.rlSecretDoor)
    var rlSecretDoor: RelativeLayout? = null

    @BindView(R.id.calculator_holder)
    var calculator_holder: LinearLayout? = null

    @BindView(R.id.btnDone)
    var btnDone: AppCompatButton? = null

    @BindView(R.id.imgFingerprint)
    var imgFingerprint: AppCompatImageView? = null

    @BindView(R.id.imgSwitchTypeUnClock)
    var imgSwitchTypeUnClock: AppCompatImageView? = null

    @BindView(R.id.root)
    var root: CoordinatorLayout? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.llLockScreen_1)
    var llLockScreen_1: LinearLayout? = null

    @BindView(R.id.rlAttempt)
    var rlAttempt: RelativeLayout? = null

    @BindView(R.id.crc_standard)
    var circleProgressView: CircleProgressView? = null

    @BindView(R.id.tvAttempt)
    var tvAttempt: AppCompatTextView? = null

    @BindView(R.id.result)
    var mResult: AppCompatTextView? = null

    @BindView(R.id.formula)
    var mFormula: AppCompatTextView? = null
    private var count = 0
    private var countAttempt = 0
    private var isFingerprint = false
    private var mFirstPin: String? = ""
    private var mCameraConfig: CameraConfig? = null
    private var mFingerPrintAuthHelper: FingerPrintAuthHelper? = null
    private var mRealPin: String? = Utils.getPinFromSharedPreferences()
    private var mFakePin: String? = Utils.getFakePinFromSharedPreferences()
    private var isFakePinEnabled: Boolean = Utils.isEnabledFakePin()
    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enterpin)
        toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(false)
        presenter = LockScreenPresenter()
        presenter?.bindView(this)
        val result: Int = getIntent().getIntExtra(EXTRA_SET_PIN, 0)
        mPinAction = EnumPinAction.values()[result]
        val resultNext: Int = getIntent().getIntExtra(EXTRA_ENUM_ACTION, 0)
        mPinActionNext = EnumPinAction.values()[resultNext]
        SingletonScreenLock.Companion.getInstance()?.setListener(this)
        enumPinPreviousAction = mPinAction
        when (mPinAction) {
            EnumPinAction.SET -> {
                onDisplayView()
                onDisplayText()
            }
            EnumPinAction.VERIFY -> {
                if (mRealPin == "") {
                    mPinAction = EnumPinAction.SET
                    onDisplayView()
                    onDisplayText()
                } else {
                    if (Utils.isSensorAvailable()) {
                        val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
                        if (isFingerPrintUnLock) {
                            imgSwitchTypeUnClock?.setVisibility(View.VISIBLE)
                            isFingerprint = isFingerPrintUnLock
                            onSetVisitFingerprintView(isFingerprint)
                            Utils.Log(TAG, "Action find fingerPrint")
                        }
                    }
                    val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
                    if (value) {
                        imgSwitchTypeUnClock?.setVisibility(View.INVISIBLE)
                        changeLayoutSecretDoor(true)
                    } else {
                        calculator_holder?.setVisibility(View.INVISIBLE)
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
        imgLauncher?.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                changeLayoutSecretDoor(false)
                return false
            }
        })
        ic_SuperSafe?.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                changeLayoutSecretDoor(false)
                return false
            }
        })
        if (Utils.isSensorAvailable()) {
            mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this)
        }
        onInitPin()
        /*Calculator init*/mCalc = CalculatorImpl(this)
        AutofitHelper.create(mResult)
        AutofitHelper.create(mFormula)
        Utils.Log(TAG, "onCreated->EnterPinActivity")
    }

    val pinLockListener: PinLockListener? = object : PinLockListener {
        override fun onComplete(pin: String?) {
            Utils.Log(TAG, "Complete button " + mPinAction?.name)
            when (mPinAction) {
                EnumPinAction.SET -> {
                    setPin(pin)
                }
                EnumPinAction.VERIFY -> {
                    checkPin(pin, true)
                }
                EnumPinAction.VERIFY_TO_CHANGE -> {
                    checkPin(pin, true)
                }
                EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                    checkPin(pin, true)
                }
                EnumPinAction.CHANGE -> {
                    setPin(pin)
                }
                EnumPinAction.FAKE_PIN -> {
                    setPin(pin)
                }
                EnumPinAction.RESET -> {
                    setPin(pin)
                }
                else -> {
                    Utils.Log(TAG, "Nothing working")
                }
            }
        }

        override fun onEmpty() {
            Utils.Log(TAG, "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String?) {
            when (mPinAction) {
                EnumPinAction.VERIFY -> {
                    checkPin(intermediatePin, false)
                }
                EnumPinAction.VERIFY_TO_CHANGE -> {
                    checkPin(intermediatePin, false)
                }
                EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                    run { checkPin(intermediatePin, false) }
                    run { Utils.Log(TAG, "Nothing working!!!") }
                }
                else -> {
                    Utils.Log(TAG, "Nothing working!!!")
                }
            }
            Utils.Log(TAG, "Pin changed, new length $pinLength with intermediate pin $intermediatePin")
        }
    }

    override fun onAttemptTimer(seconds: String?) {
        runOnUiThread(Runnable {
            try {
                val response = seconds?.toDouble()
                Utils.Log(TAG, "Timer  Attempt $countAttempt Count $count")
                val remain = (response?.div(countAttempt))?.times(100)
                val result = remain as Int
                Utils.Log(TAG, "Result $result")
                circleProgressView?.setValue(result)
                circleProgressView?.setText(seconds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    override fun onAttemptTimerFinish() {
        runOnUiThread(Runnable {
            mPinAction = enumPinPreviousAction
            onDisplayView()
            Utils.Log(TAG, "onAttemptTimerFinish")
        })
    }

    override fun onNotifier(status: EnumStatus?) {
        when (status) {
            EnumStatus.FINISH -> {
                finish()
            }
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    protected override fun onDestroy() {
        super.onDestroy()
        SingletonManager.Companion.getInstance().setVisitLockScreen(false)
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        val action = EnumPinAction.values()[value]
        when (action) {
            EnumPinAction.NONE -> {
                val mUser: User? = Utils.getUserInfo()
                if (mUser != null) {
                    ServiceManager.getInstance()?.onStartService()
                    SingletonResetPin.getInstance()?.onStop()
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
        Utils.Log(TAG, "onDestroy")
    }

    @OnClick(R.id.btnDone)
    fun onClickedDone() {
        finish()
    }

    fun onDelete(view: View?) {
        Utils.Log(TAG, "onDelete here")
        if (mPinLockView != null) {
            mPinLockView?.onDeleteClicked()
        }
    }

    /*Forgot pin*/
    @OnClick(R.id.llForgotPin)
    fun onForgotPin(view: View?) {
        Navigator.onMoveToForgotPin(this, false)
    }

    fun onSetVisitableForgotPin(value: Int) {
        llForgotPin?.setVisibility(value)
    }

    protected override fun onResume() {
        super.onResume()
        SingletonManager.Companion.getInstance().setVisitLockScreen(true)
        Utils.Log(TAG, "onResume")
        if (mPinLockView != null) {
            mPinLockView?.resetPinLockView()
        }
        onSetVisitableForgotPin(View.GONE)
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper?.startAuth()
        }
        mRealPin = Utils.getPinFromSharedPreferences()
        mFakePin = Utils.getFakePinFromSharedPreferences()
        isFakePinEnabled = Utils.isEnabledFakePin()
    }

    fun onInitPin() {
        mIndicatorDots?.setActivity(this)
        mPinLockView?.attachIndicatorDots(mIndicatorDots)
        mPinLockView?.setPinLockListener(pinLockListener)
        mPinLockView?.setPinLength(PIN_LENGTH)
        mIndicatorDots?.setIndicatorType(IndicatorDots.IndicatorType.Companion.FILL_WITH_ANIMATION)
        onInitHiddenCamera()
    }

    protected override fun onPause() {
        super.onPause()
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper?.stopAuth()
        }
    }

    private fun setPin(pin: String?) {
        when (mPinAction) {
            EnumPinAction.SET -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle?.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView?.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        Utils.writePinToSharedPreferences(pin)
                        when (mPinActionNext) {
                            EnumPinAction.SIGN_UP -> {
                                Navigator.onMoveToSignUp(this)
                            }
                            else -> {
                                Navigator.onMoveToMainTab(this)
                                presenter?.onChangeStatus(EnumStatus.SET, EnumPinAction.DONE)
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
                    mTextTitle?.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView?.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.isExistingFakePin(pin, mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            Utils.writePinToSharedPreferences(pin)
                            presenter?.onChangeStatus(EnumStatus.CHANGE, EnumPinAction.DONE)
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
            EnumPinAction.FAKE_PIN -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle?.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView?.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.isExistingRealPin(pin, mRealPin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            Utils.writeFakePinToSharedPreferences(pin)
                            presenter?.onChangeStatus(EnumStatus.CREATE_FAKE_PIN, EnumPinAction.DONE)
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
            EnumPinAction.RESET -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle?.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView?.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.isExistingFakePin(pin, mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            when (mPinActionNext) {
                                EnumPinAction.RESTORE -> {
                                    Utils.writePinToSharedPreferences(pin)
                                    onRestore()
                                }
                                else -> {
                                    Utils.writePinToSharedPreferences(pin)
                                    Navigator.onMoveToMainTab(this)
                                    presenter?.onChangeStatus(EnumStatus.RESET, EnumPinAction.DONE)
                                }
                            }
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
        }
    }

    fun onRestore() {
        Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeBackup(), SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), object : ServiceManager.ServiceManagerSyncDataListener {
            override fun onCompleted() {
                Utils.Log(TAG, "Exporting successful")
                val mUser: User? = SuperSafeApplication.getInstance().readUseSecret()
                if (mUser != null) {
                    Utils.setUserPreShare(mUser)
                    Navigator.onMoveToMainTab(this@EnterPinActivity)
                    presenter?.onChangeStatus(EnumStatus.RESTORE, EnumPinAction.DONE)
                }
            }

            override fun onError() {
                Utils.Log(TAG, "Exporting error")
            }

            override fun onCancel() {}
        })
    }

    private fun checkPin(pin: String?, isCompleted: Boolean) {
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                if (SingletonManager.Companion.getInstance().isVisitFakePin()) {
                    if (pin == mFakePin && isFakePinEnabled) {
                        presenter?.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE)
                    } else {
                        if (isCompleted) {
                            onTakePicture(pin)
                            onAlertWarning("")
                        }
                    }
                } else {
                    if (pin == mRealPin) {
                        presenter?.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE)
                    } else if (pin == mFakePin && isFakePinEnabled) {
                        presenter?.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE)
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
                    presenter?.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.CHANGE)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                if (pin == mRealPin) {
                    presenter?.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.FAKE_PIN)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            }
        }
    }

    private fun shake() {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(mPinLockView, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
        objectAnimator.start()
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                count += 1
                onSetVisitableForgotPin(View.VISIBLE)
                if (count >= 3) {
                    countAttempt = count * 10
                    val attemptWaiting = count * 10000.toLong()
                    mPinAction = EnumPinAction.ATTEMPT
                    onDisplayView()
                    SingletonScreenLock.Companion.getInstance()?.onStartTimer(attemptWaiting)
                }
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                count += 1
                onSetVisitableForgotPin(View.VISIBLE)
                if (count >= 3) {
                    countAttempt = count * 10
                    val attemptWaiting = count * 10000.toLong()
                    mPinAction = EnumPinAction.ATTEMPT
                    onDisplayView()
                    SingletonScreenLock.Companion.getInstance()?.onStartTimer(attemptWaiting)
                }
            }
        }
        Utils.Log(TAG, "Visit....$count")
    }

    private fun onAlertWarning(title: String?) {
        when (mPinAction) {
            EnumPinAction.SET -> {
                shake()
                mTextTitle?.setText(title)
                mPinLockView?.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.CHANGE -> {
                shake()
                mTextTitle?.setText(title)
                mPinLockView?.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.FAKE_PIN -> {
                shake()
                mTextTitle?.setText(title)
                mPinLockView?.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.RESET -> {
                shake()
                mTextTitle?.setText(title)
                mPinLockView?.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.VERIFY -> {
                shake()
                mTextTitle?.setText(title)
                mTextAttempts?.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView?.resetPinLockView()
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                shake()
                mTextTitle?.setText(title)
                mTextAttempts?.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView?.resetPinLockView()
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                shake()
                mTextTitle?.setText(title)
                mTextAttempts?.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView?.resetPinLockView()
            }
        }
    }

    private fun changeLayoutSecretDoor(isVisit: Boolean) {
        if (isVisit) {
            mTextTitle?.setVisibility(View.INVISIBLE)
            rlButton?.setVisibility(View.INVISIBLE)
            rlDots?.setVisibility(View.INVISIBLE)
            mTextAttempts?.setVisibility(View.INVISIBLE)
            val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
            if (options) {
                imgLauncher?.setVisibility(View.INVISIBLE)
                rlSecretDoor?.setVisibility(View.INVISIBLE)
                calculator_holder?.setVisibility(View.VISIBLE)
            } else {
                imgLauncher?.setVisibility(View.VISIBLE)
                rlSecretDoor?.setVisibility(View.VISIBLE)
                calculator_holder?.setVisibility(View.INVISIBLE)
            }
        } else {
            mTextTitle?.setVisibility(View.VISIBLE)
            rlButton?.setVisibility(View.VISIBLE)
            rlDots?.setVisibility(View.VISIBLE)
            mTextAttempts?.setVisibility(View.VISIBLE)
            mTextAttempts?.setText("")
            imgLauncher?.setVisibility(View.INVISIBLE)
            rlSecretDoor?.setVisibility(View.INVISIBLE)
            calculator_holder?.setVisibility(View.INVISIBLE)
            if (Utils.isSensorAvailable()) {
                val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
                if (isFingerPrintUnLock) {
                    imgSwitchTypeUnClock?.setVisibility(View.VISIBLE)
                    isFingerprint = isFingerPrintUnLock
                    onSetVisitFingerprintView(isFingerprint)
                } else {
                    imgSwitchTypeUnClock?.setVisibility(View.GONE)
                }
            } else {
                imgSwitchTypeUnClock?.setVisibility(View.GONE)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, action: EnumPinAction?) {
        Utils.Log(TAG, "EnumPinAction 1:...." + action?.name)
        when (status) {
            EnumStatus.VERIFY -> {
                mTextAttempts?.setText("")
                Utils.Log(TAG, "Result here")
                mPinAction = action
                when (action) {
                    EnumPinAction.VERIFY_TO_CHANGE -> {
                        initActionBar(false)
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.FAKE_PIN -> {
                        mPinLockView?.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.CHANGE -> {
                        mPinLockView?.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.DONE -> {
                        /*Unlock for real pin*/SingletonManager.Companion.getInstance().setAnimation(false)
                        EventBus.getDefault().post(EnumStatus.UNLOCK)
                        Utils.onObserveData(100, object : Listener{
                            override fun onStart() {
                               finish()
                            }
                        })
                        Utils.Log(TAG, "Action ...................done")
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                    }
                    EnumPinAction.VERIFY -> {
                        finish()
                    }
                }
            }
            EnumStatus.SET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.CHANGE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.RESET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.RESTORE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.FAKE_PIN -> {
                /*UnLock for fake pin*/mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        SingletonManager.Companion.getInstance().setAnimation(false)
                        Utils.onObserveData(100, object :Listener {
                            override fun onStart() {
                                PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                                if (SingletonManager.Companion.getInstance().isVisitFakePin()) {
                                    finish()
                                } else {
                                    Navigator.onMoveFakePinComponent(this@EnterPinActivity)
                                }
                            }
                        })
                    }
                }
            }
            EnumStatus.CREATE_FAKE_PIN -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
    }

    override fun onBackPressed() {
        mPinAction?.name?.let { Utils.Log(TAG, it) }
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                val action = EnumPinAction.values()[value]
                when (action) {
                    EnumPinAction.SCREEN_LOCK -> {
                        EventBus.getDefault().post(EnumStatus.FINISH)
                        Navigator.onMoveToFaceDown(this)
                        Utils.Log(TAG, "onStillScreenLock ???")
                    }
                }
                super.onBackPressed()
            }
            EnumPinAction.SET -> {
                super.onBackPressed()
            }
            EnumPinAction.CHANGE -> {
                super.onBackPressed()
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                super.onBackPressed()
            }
        }
    }

    fun onDisplayView() {
        Utils.Log(TAG, "EnumPinAction 2:...." + mPinAction?.name)
        when (mPinAction) {
            EnumPinAction.SET -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.CHANGE -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.INIT_PREFERENCE -> {
                rlLockScreen?.setVisibility(View.INVISIBLE)
                rlPreference?.setVisibility(View.VISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.FAKE_PIN -> {
                rlLockScreen?.setVisibility(View.VISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.ATTEMPT -> {
                rlLockScreen?.setVisibility(View.INVISIBLE)
                rlPreference?.setVisibility(View.INVISIBLE)
                rlAttempt?.setVisibility(View.VISIBLE)
                val result: String = kotlin.String.format(getString(R.string.in_correct_pin), count.toString() + "", countAttempt.toString() + "")
                tvAttempt?.setText(result)
                Utils.Log(TAG, mPinAction!!.name)
            }
        }
    }

    fun onDisplayText() {
        Utils.Log(TAG, "EnumPinAction 3:...." + mPinAction?.name)
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                mTextTitle?.setVisibility(View.INVISIBLE)
                imgLauncher?.setVisibility(View.VISIBLE)
                imgLauncher?.setEnabled(false)
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                mTextTitle?.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle?.setVisibility(View.VISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.CHANGE -> {
                mTextTitle?.setText(getString(R.string.pinlock_confirm_create))
                mTextTitle?.setVisibility(View.VISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.INIT_PREFERENCE -> {
                mTextTitle?.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle?.setVisibility(View.VISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                mTextTitle?.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle?.setVisibility(View.VISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.FAKE_PIN -> {
                mTextTitle?.setText(getString(R.string.pinlock_confirm_create))
                mTextTitle?.setVisibility(View.VISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.SET -> {
                mTextTitle?.setText(getString(R.string.pinlock_settitle))
                mTextTitle?.setVisibility(View.VISIBLE)
                mTextAttempts?.setVisibility(View.INVISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.RESET -> {
                mTextTitle?.setText(getString(R.string.pinlock_settitle))
                mTextTitle?.setVisibility(View.VISIBLE)
                mTextAttempts?.setVisibility(View.INVISIBLE)
                imgLauncher?.setVisibility(View.INVISIBLE)
            }
        }
    }

    /*Call back finger print*/
    override fun onNoFingerPrintHardwareFound() {}
    override fun onNoFingerPrintRegistered() {}
    override fun onBelowMarshmallow() {}
    override fun onAuthSuccess(cryptoObject: FingerprintManager.CryptoObject?) {
        val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
        isFingerprint = isFingerPrintUnLock
        if (!isFingerPrintUnLock) {
            return
        }
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                presenter?.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE)
            }
        }
    }

    override fun onAuthFailed(errorCode: Int, errorMessage: String?) {}

    /*Call back end at finger print*/
    fun initActionBar(isInit: Boolean) {
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(isInit)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    @OnClick(R.id.imgSwitchTypeUnClock)
    fun onClickedSwitchTypeUnlock(view: View?) {
        isFingerprint = if (isFingerprint) {
            false
        } else {
            true
        }
        onSetVisitFingerprintView(isFingerprint)
    }

    fun onSetVisitFingerprintView(isFingerprint: Boolean) {
        if (isFingerprint) {
            mPinLockView?.setVisibility(View.INVISIBLE)
            imgFingerprint?.setVisibility(View.VISIBLE)
            rlDots?.setVisibility(View.INVISIBLE)
            mTextAttempts?.setText(getString(R.string.use_your_fingerprint_to_unlock_supersafe))
            mTextAttempts?.setVisibility(View.VISIBLE)
            mTextTitle?.setText("")
        } else {
            mPinLockView?.setVisibility(View.VISIBLE)
            imgFingerprint?.setVisibility(View.INVISIBLE)
            rlDots?.setVisibility(View.VISIBLE)
            mTextAttempts?.setText("")
            mTextTitle?.setText(getString(R.string.pinlock_title))
        }
    }

    /*Settings preference*/
    class SettingsFragment : PreferenceFragmentCompat() {
        private var mChangePin: MyPreference? = null
        private var mFaceDown: MySwitchPreference? = null
        private var mFingerPrint: MySwitchPreference? = null
        private fun createChangeListener(): Preference.OnPreferenceChangeListener? {
            return Preference.OnPreferenceChangeListener { preference, newValue ->
                Utils.Log(TAG, "change $newValue")
                true
            }
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener? {
            return Preference.OnPreferenceClickListener { preference ->
                if (preference is Preference) {
                    if (preference.key == getString(R.string.key_change_pin)) {
                        Utils.Log(TAG, "Action here!!!")
                        enumPinPreviousAction = EnumPinAction.VERIFY_TO_CHANGE
                        presenter?.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.VERIFY_TO_CHANGE)
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Changing pin*/mChangePin = findPreference(getString(R.string.key_change_pin)) as MyPreference?
            mChangePin?.setOnPreferenceChangeListener(createChangeListener())
            mChangePin?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            /*Face down*/mFaceDown = findPreference(getString(R.string.key_face_down_lock)) as MySwitchPreference?
            val switchFaceDown: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            mFaceDown?.setOnPreferenceChangeListener(createChangeListener())
            mFaceDown?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mFaceDown?.setDefaultValue(switchFaceDown)
            Utils.Log(TAG, "default $switchFaceDown")
            /*FingerPrint*/mFingerPrint = findPreference(getString(R.string.key_fingerprint_unlock)) as MySwitchPreference?
            val switchFingerPrint: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
            mFingerPrint?.setOnPreferenceChangeListener(createChangeListener())
            mFingerPrint?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mFingerPrint?.setDefaultValue(switchFingerPrint)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_lock_screen)
        }
    }

    fun onLauncherPreferences() {
        var fragment: Fragment? = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG)
        if (fragment == null) {
            fragment = Fragment.instantiate(this, co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity.SettingsFragment::class.java.name)
        }
        val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EnumPinAction>?) {}
    override fun onImageCapture(imageFile: File, pin: String) {
        super.onImageCapture(imageFile, pin)
        val inAlerts = BreakInAlertsModel()
        inAlerts.fileName = imageFile.absolutePath
        inAlerts.pin = pin
        inAlerts.time = System.currentTimeMillis()
        SQLHelper.onInsert(inAlerts)
    }

    override fun onCameraError(errorCode: Int) {
        super.onCameraError(errorCode)
        when (errorCode) {
            CameraError.Companion.ERROR_CAMERA_OPEN_FAILED -> {
            }
            CameraError.Companion.ERROR_IMAGE_WRITE_FAILED -> {
            }
            CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE -> {
            }
            CameraError.Companion.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> showMessage(getString(R.string.error_not_having_camera))
        }
    }

    fun onInitHiddenCamera() {
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
        if (!value) {
            return
        }
        mCameraConfig = CameraConfig()
                .getBuilder(this)
                ?.setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                ?.setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                ?.setImageFormat(CameraImageFormat.FORMAT_JPEG)
                ?.setImageRotation(CameraRotation.ROTATION_270)
                ?.setCameraFocus(CameraFocus.AUTO)
                ?.build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig)
        }
    }

    fun onTakePicture(pin: String?) {
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
        if (!value) {
            return
        }
        mCameraConfig?.getBuilder(SuperSafeApplication.getInstance())
                ?.setPin(pin)
        mCameraConfig?.getBuilder(SuperSafeApplication.getInstance())?.setImageFile(SuperSafeApplication.getInstance().getDefaultStorageFile(CameraImageFormat.Companion.FORMAT_JPEG))
        takePicture()
    }

    /*Calculator action*/
    override fun setValue(value: String?) {
        mResult?.setText(value)
    }

    // used only by Robolectric
    override fun setValueDouble(d: Double) {
        mCalc?.setValue(Formatter.doubleToString(d))
        mCalc?.setLastKey(Constants.DIGIT)
    }

    override fun setFormula(value: String?) {
        mFormula?.setText(value)
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

    companion object {
        val TAG = EnterPinActivity::class.java.simpleName
        private val FRAGMENT_TAG: String? = co.tpcreative.supersafe.ui.settings.SettingsActivity::class.java.getSimpleName() + "::fragmentTag"
        val EXTRA_SET_PIN: String? = "SET_PIN"
        val EXTRA_ENUM_ACTION: String? = "ENUM_ACTION"
        private const val PIN_LENGTH = 100
        private var mCalc: CalculatorImpl? = null
        private var mPinAction: EnumPinAction? = null
        private var enumPinPreviousAction: EnumPinAction? = null
        private var mPinActionNext: EnumPinAction? = null
        private var presenter: LockScreenPresenter? = null
        fun getIntent(context: Context?, action: Int, actionNext: Int): Intent? {
            val intent = Intent(context, EnterPinActivity::class.java)
            intent.putExtra(EXTRA_SET_PIN, action)
            intent.putExtra(EXTRA_ENUM_ACTION, actionNext)
            return intent
        }
    }
}