package co.tpcreative.supersafe.ui.enterpin
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
import co.tpcreative.supersafe.common.extension.instantiate
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
import com.multidots.fingerprintauth.FingerPrintAuthHelper
import kotlinx.android.synthetic.main.activity_enterpin.*
import kotlinx.android.synthetic.main.include_calculator.*

class EnterPinAct : BaseVerifyPinActivity(), BaseView<EnumPinAction>, Calculator, FingerPrintAuthCallback, SingletonMultipleListener.Listener, SingletonScreenLock.SingletonScreenLockListener {
    var count = 0
    var countAttempt = 0
    var isFingerprint = false
    var mFirstPin: String? = ""
    var mCameraConfig: CameraConfig? = null
    var mFingerPrintAuthHelper: FingerPrintAuthHelper? = null
    var mRealPin: String? = Utils.getPinFromSharedPreferences()
    var mFakePin: String? = Utils.getFakePinFromSharedPreferences()
    var isFakePinEnabled: Boolean = Utils.isEnabledFakePin()
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enterpin)
        initUI()
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
                val result = remain!!.toInt()
                Utils.Log(TAG, "Result $result")
                crc_standard.value = result
                crc_standard.text = seconds
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
    override fun onDestroy() {
        super.onDestroy()
        SingletonManager.getInstance().setVisitLockScreen(false)
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        when (EnumPinAction.values()[value]) {
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

    fun onDelete(view: View?) {
        Utils.Log(TAG, "onDelete here")
        if (pinlockView != null) {
            pinlockView.onDeleteClicked()
        }
    }

    fun onSetVisitableForgotPin(value: Int) {
        llForgotPin?.visibility = value
    }

    override fun onResume() {
        super.onResume()
        SingletonManager.getInstance().setVisitLockScreen(true)
        Utils.Log(TAG, "onResume")
        if (pinlockView != null) {
            pinlockView.resetPinLockView()
        }
        onSetVisitableForgotPin(View.GONE)
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper?.startAuth()
        }
        mRealPin = Utils.getPinFromSharedPreferences()
        mFakePin = Utils.getFakePinFromSharedPreferences()
        isFakePinEnabled = Utils.isEnabledFakePin()
    }

    override fun onPause() {
        super.onPause()
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper?.stopAuth()
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, action: EnumPinAction?) {
        Utils.Log(TAG, "EnumPinAction 1:...." + action?.name)
        when (status) {
            EnumStatus.VERIFY -> {
                tvTopAttempts?.setText("")
                Utils.Log(TAG, "Result here")
                mPinAction = action
                when (action) {
                    EnumPinAction.VERIFY_TO_CHANGE -> {
                        initActionBar(false)
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.FAKE_PIN -> {
                        pinlockView.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.CHANGE -> {
                        pinlockView.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.DONE -> {
                        /*Unlock for real pin*/SingletonManager.getInstance().setAnimation(false)
                        Utils.onPushEventBus(EnumStatus.UNLOCK)
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
                                    Navigator.onMoveFakePinComponent(this@EnterPinAct)
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
                when (EnumPinAction.values()[value]) {
                    EnumPinAction.SCREEN_LOCK -> {
                        Utils.onPushEventBus(EnumStatus.FINISH)
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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(isInit)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return this
    }

    fun onSetVisitFingerprintView(isFingerprint: Boolean) {
        if (isFingerprint) {
            pinlockView.visibility = View.INVISIBLE
            imgFingerprint?.visibility = View.VISIBLE
            rlDots?.visibility = View.INVISIBLE
            tvTopAttempts?.text = getString(R.string.use_your_fingerprint_to_unlock_supersafe)
            tvTopAttempts?.visibility = View.VISIBLE
            tvTitle.text = ""
        } else {
            pinlockView.visibility = View.VISIBLE
            imgFingerprint?.visibility = View.INVISIBLE
            rlDots?.visibility = View.VISIBLE
            tvTopAttempts?.text = ""
            tvTitle.text = getString(R.string.pinlock_title)
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
            mChangePin?.onPreferenceChangeListener = createChangeListener()
            mChangePin?.onPreferenceClickListener = createActionPreferenceClickListener()
            /*Face down*/mFaceDown = findPreference(getString(R.string.key_face_down_lock)) as MySwitchPreference?
            val switchFaceDown: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            mFaceDown?.onPreferenceChangeListener = createChangeListener()
            mFaceDown?.onPreferenceClickListener = createActionPreferenceClickListener()
            mFaceDown?.setDefaultValue(switchFaceDown)
            Utils.Log(TAG, "default $switchFaceDown")
            /*FingerPrint*/mFingerPrint = findPreference(getString(R.string.key_fingerprint_unlock)) as MySwitchPreference?
            val switchFingerPrint: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
            mFingerPrint?.onPreferenceChangeListener = createChangeListener()
            mFingerPrint?.onPreferenceClickListener = createActionPreferenceClickListener()
            mFingerPrint?.setDefaultValue(switchFingerPrint)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_lock_screen)
        }
    }

    fun onLauncherPreferences() {
        val fragment: Fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
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
            CameraError.ERROR_CAMERA_OPEN_FAILED -> {
            }
            CameraError.ERROR_IMAGE_WRITE_FAILED -> {
            }
            CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE -> {
            }
            CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> showMessage(getString(R.string.error_not_having_camera))
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
        mCameraConfig?.getBuilder(SuperSafeApplication.getInstance())?.setImageFile(SuperSafeApplication.getInstance().getDefaultStorageFile(CameraImageFormat.FORMAT_JPEG))
        takePicture()
    }

    /*Calculator action*/
    override fun setValue(value: String?) {
        tvResult.text = value
    }

    // used only by Robolectric
    override fun setValueDouble(d: Double) {
        mCalc?.setValue(Formatter.doubleToString(d))
        mCalc?.setLastKey(Constants.DIGIT)
    }

    override fun setFormula(value: String?) {
        tvFormula?.text = value
    }

    fun numpadClicked(id: Int) {
        mCalc?.numpadClicked(id)
    }

    companion object {
        val TAG = EnterPinAct::class.java.simpleName
        val EXTRA_SET_PIN: String? = "SET_PIN"
        val EXTRA_ENUM_ACTION: String? = "ENUM_ACTION"
        const val PIN_LENGTH = 100
        var mCalc: CalculatorImpl? = null
        var mPinAction: EnumPinAction? = null
        var enumPinPreviousAction: EnumPinAction? = null
        var mPinActionNext: EnumPinAction? = null
        var presenter: LockScreenPresenter? = null
        fun getIntent(context: Context?, action: Int, actionNext: Int): Intent? {
            val intent = Intent(context, EnterPinAct::class.java)
            intent.putExtra(EXTRA_SET_PIN, action)
            intent.putExtra(EXTRA_ENUM_ACTION, actionNext)
            return intent
        }
    }
}