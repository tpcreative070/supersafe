package co.tpcreative.supersafe.ui.enterpin
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.infinum.goldfinger.Goldfinger
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity
import co.tpcreative.supersafe.common.controller.*
import co.tpcreative.supersafe.common.extension.*
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.preference.MyPreference
import co.tpcreative.supersafe.common.preference.MySwitchPreference
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.*
import co.tpcreative.supersafe.model.BreakInAlertsModel
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.ui.main_tab.MainTabAct
import co.tpcreative.supersafe.viewmodel.LockScreenViewModel
import com.cottacush.android.hiddencam.CameraType
import com.cottacush.android.hiddencam.HiddenCam
import com.cottacush.android.hiddencam.OnImageCapturedListener
import kotlinx.android.synthetic.main.activity_enterpin.*
import kotlinx.android.synthetic.main.include_calculator.*
import java.io.File
import kotlin.properties.Delegates

class EnterPinAct : BaseVerifyPinActivity(),  Calculator, SingletonMultipleListener.Listener, SingletonScreenLock.SingletonScreenLockListener, OnImageCapturedListener {
    var count = 0
    var countAttempt = 0
    var isFingerprint = false
    var mFirstPin: String? = ""
    var mRealPin: String? = null
    var mFakePin: String? = null
    var isFakePinEnabled by Delegates.notNull<Boolean>()
    var hiddenCam: HiddenCam? = null
    var mPinAlert = ""
    var goldfinger: Goldfinger? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enterpin)
        enterPinAct = this
        initUI()
    }

    val pinLockListener: PinLockListener = object : PinLockListener {
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
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onDestroy() {
        super.onDestroy()
        SingletonManager.getInstance().setVisitLockScreen(false)
        when (EnumPinAction.values()[Utils.getScreenStatus()]) {
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
        hiddenCam?.destroy()
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
        mRealPin = Utils.getPinFromSharedPreferences()
        mFakePin = Utils.getFakePinFromSharedPreferences()
        isFakePinEnabled = Utils.isFacePin()
    }

    override fun onPause() {
        super.onPause()
    }

    fun onSuccessful(status: EnumStatus?, action: EnumPinAction?) {
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
                        /*Unlock for real pin*/
                        SingletonManager.getInstance().setAnimation(false)
                        Utils.onPushEventBus(EnumStatus.UNLOCK)
                        finish()
                        Utils.Log(TAG, "Action ...................done")
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                    }
                    EnumPinAction.VERIFY -> {
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            EnumStatus.SET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            EnumStatus.CHANGE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            EnumStatus.RESET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            EnumStatus.RESTORE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            EnumStatus.FAKE_PIN -> {
                /*UnLock for fake pin*/mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        SingletonManager.getInstance().setAnimation(false)
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        if (SingletonManager.getInstance().isVisitFakePin()) {
                            finish()
                        } else {
                            Navigator.onMoveFakePinComponent(this@EnterPinAct)
                        }
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
            EnumStatus.CREATE_FAKE_PIN -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
                        finish()
                    }
                    else -> Utils.Log(TAG, "Nothing")
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
                when (EnumPinAction.values()[Utils.getScreenStatus()]) {
                    EnumPinAction.SCREEN_LOCK -> {
                        Utils.onPushEventBus(EnumStatus.FINISH)
                        Navigator.onMoveToFaceDown(this)
                        Utils.Log(TAG, "onStillScreenLock ???")
                    }
                    else -> Utils.Log(TAG, "Nothing")
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
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                super.onBackPressed()
            }
            EnumPinAction.FAKE_PIN ->{
                super.onBackPressed()
            }
            EnumPinAction.INIT_PREFERENCE ->{
                super.onBackPressed()
            }
            else -> {
                Utils.Log(TAG,"Waiting...")
                Utils.Log(TAG, "Nothing ${mPinAction?.name}")
            }
        }
    }

    /*Call back finger print*/
    override fun onBiometricSuccessful() {
        val isFingerPrintUnLock: Boolean = Utils.isAvailableBiometric()
        isFingerprint = isFingerPrintUnLock
        if (!isFingerPrintUnLock) {
            return
        }
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                changeStatus(EnumStatus.VERIFY, EnumPinAction.DONE,null,true,false)
            }
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    /*Call back end at finger print*/
    fun initActionBar(isInit: Boolean) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(isInit)
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
        private var mTwoFactoryAuthentication : MyPreference? = null
        private fun createChangeListener(): Preference.OnPreferenceChangeListener {
            return Preference.OnPreferenceChangeListener { preference, newValue ->
                Utils.Log(TAG, "change $newValue")
                true
            }
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener {
            return Preference.OnPreferenceClickListener { preference ->
                if (preference is Preference) {
                    if (preference.key == getString(R.string.key_change_pin)) {
                        Utils.Log(TAG, "Action here!!!")
                        enumPinPreviousAction = EnumPinAction.VERIFY_TO_CHANGE
                        enterPinAct.changeStatus(EnumStatus.VERIFY, EnumPinAction.VERIFY_TO_CHANGE,null,true,false)
                    }
                    if (preference.key == getString(R.string.key_enable_two_factor_authentication)){
                        Navigator.onMoveTwoFactoryAuthentication(activity!!)
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
            val switchFaceDown: Boolean = Utils.isFaceDown()
            mFaceDown?.onPreferenceChangeListener = createChangeListener()
            mFaceDown?.onPreferenceClickListener = createActionPreferenceClickListener()
            mFaceDown?.setDefaultValue(switchFaceDown)
            Utils.Log(TAG, "default $switchFaceDown")
            /*FingerPrint*/mFingerPrint = findPreference(getString(R.string.key_fingerprint_unlock)) as MySwitchPreference?
            val switchFingerPrint: Boolean = Utils.isAvailableBiometric()
            mFingerPrint?.onPreferenceChangeListener = createChangeListener()
            mFingerPrint?.onPreferenceClickListener = createActionPreferenceClickListener()
            mFingerPrint?.setDefaultValue(switchFingerPrint)
            mFingerPrint?.isVisible = Utils.isSensorAvailable()

            mTwoFactoryAuthentication = findPreference(getString(R.string.key_enable_two_factor_authentication)) as MyPreference?
            mTwoFactoryAuthentication?.onPreferenceChangeListener = createChangeListener()
            mTwoFactoryAuthentication?.onPreferenceClickListener = createActionPreferenceClickListener()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (SuperSafeApplication.getInstance().isLiveMigration()){
                preferenceManager.preferenceDataStore = EncryptedPreferenceDataStore.getInstance(requireContext())
            }
            addPreferencesFromResource(R.xml.pref_general_lock_screen)
        }
    }

    fun onLauncherPreferences() {
        val fragment: Fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    fun onInitHiddenCamera() {
        if (!Utils.isBreakAlert()) {
            return
        }
        hiddenCam = HiddenCam(
                imageCapturedListener = this,
                baseFileDirectory = File(SuperSafeApplication.getInstance().getSuperSafeBreakInAlerts()),
                context = this,
                cameraType = CameraType.FRONT_CAMERA)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            hiddenCam?.start()
        }
        Utils.Log(TAG,"Init camera....")
    }

    fun onTakePicture(pin: String?) {
        if (!Utils.isBreakAlert()) {
            return
        }
        pin?.let {
            this.mPinAlert = it
        }
        hiddenCam?.captureImage()
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

    override fun onImageCaptured(image: File) {
        val inAlerts = BreakInAlertsModel()
        inAlerts.fileName = image.absolutePath
        inAlerts.pin = mPinAlert
        inAlerts.time = System.currentTimeMillis()
        SQLHelper.onInsert(inAlerts)
    }

    override fun onImageCaptureError(e: Throwable?) {
        TODO("Not yet implemented")
    }

    companion object {
        val TAG = EnterPinAct::class.java.simpleName
        val EXTRA_SET_PIN: String = "SET_PIN"
        val EXTRA_ENUM_ACTION: String = "ENUM_ACTION"
        const val PIN_LENGTH = 100
        var mCalc: CalculatorImpl? = null
        var mPinAction: EnumPinAction? = null
        var enumPinPreviousAction: EnumPinAction? = null
        var mPinActionNext: EnumPinAction? = null
        lateinit var viewModel: LockScreenViewModel
        lateinit var enterPinAct : EnterPinAct
        fun getIntent(context: Context?, action: Int, actionNext: Int): Intent {
            Utils.Log(TAG,"start intent EnterPinAct")
            val intent = Intent(context, EnterPinAct::class.java)
            intent.putExtra(EXTRA_SET_PIN, action)
            intent.putExtra(EXTRA_ENUM_ACTION, actionNext)
            return intent
        }
    }
}