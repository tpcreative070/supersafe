package co.tpcreative.supersafe.common.activity
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.Unbinder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.HomeWatcher
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import spencerstudios.com.bungeelib.Bungee
import java.io.IOException

abstract class BaseGoogleApi : AppCompatActivity(), SensorFaceUpDownChangeNotifier.Listener {
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    var unbinder: Unbinder? = null
    protected var actionBar: ActionBar? = null
    private var mHomeWatcher: HomeWatcher? = null
    private var onStartCount = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = getSupportActionBar()
        onStartCount = 1
        if (savedInstanceState == null) {
            if (SingletonManager.Companion.getInstance().isReloadMainTab()) {
                Bungee.fade(this)
            } else {
                this.overridePendingTransition(R.animator.anim_slide_in_left,
                        R.animator.anim_slide_out_left)
            }
        } else {
            onStartCount = 2
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(null)!!)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    protected fun onStartOverridePendingTransition() {
        this.overridePendingTransition(R.animator.anim_slide_in_left,
                R.animator.anim_slide_out_left)
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true)
        }
        return theme
    }

    fun onCallLockScreen() {
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        val action = EnumPinAction.values()[value]
        when (action) {
            EnumPinAction.SPLASH_SCREEN -> {
                PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.SCREEN_LOCK.ordinal)
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Navigator.onMoveToVerifyPin(this, EnumPinAction.NONE)
                Utils.Log(TAG, "Lock screen")
            }
            else -> {
                EventBus.getDefault().post(EnumStatus.REGISTER_OR_LOGIN)
                Utils.Log(TAG, "Nothing to do " + action.name)
            }
        }
    }

    protected fun onFaceDown(isFaceDown: Boolean) {
        if (isFaceDown) {
            val result: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            if (result) {
                Navigator.onMoveToFaceDown(SuperSafeApplication.Companion.getInstance())
            }
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        try {
            super.setContentView(layoutResID)
            Utils.Log(TAG, "action here")
            unbinder = ButterKnife.bind(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.Companion.getInstance()?.remove(this)
        Utils.Log(TAG, "onPause")
        if (mHomeWatcher != null) {
            Utils.Log(TAG, "Stop home watcher....")
            mHomeWatcher?.stopWatch()
        }
    }

    protected override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    protected override fun onDestroy() {
        super.onDestroy()
        if (unbinder != null) {
            unbinder?.unbind()
        }
    }

    protected override fun onResume() {
        SensorFaceUpDownChangeNotifier.getInstance()?.addListener(this)
        super.onResume()
    }

    fun onRegisterHomeWatcher() {
        Utils.Log(TAG, "Register")
        /*Home action*/if (mHomeWatcher != null) {
            if (mHomeWatcher?.isRegistered!!) {
                return
            }
        }
        mHomeWatcher = HomeWatcher(this)
        mHomeWatcher?.setOnHomePressedListener(object : HomeWatcher.OnHomePressedListener {
            override fun onHomePressed() {
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                val action = EnumPinAction.values()[value]
                when (action) {
                    EnumPinAction.NONE -> {
                        Utils.onHomePressed()
                        onStopListenerAWhile()
                    }
                    else -> {
                        Utils.Log(TAG, "Nothing to do on home " + action.name)
                    }
                }
                mHomeWatcher?.stopWatch()
            }

            override fun onHomeLongPressed() {}
        })
        mHomeWatcher?.startWatch()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onStart() {
        super.onStart()
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        val action = EnumPinAction.values()[value]
        when (action) {
            EnumPinAction.SCREEN_LOCK -> {
                if (!SingletonManager.Companion.getInstance().isVisitLockScreen()) {
                    SuperSafeApplication.getInstance().getActivity()?.let { Navigator.onMoveToVerifyPin(it, EnumPinAction.NONE) }
                    Utils.Log(TAG, "Pressed home button")
                    SingletonManager.Companion.getInstance().setVisitLockScreen(true)
                    Utils.Log(TAG, "Verify pin")
                } else {
                    Utils.Log(TAG, "Verify pin already")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do on start " + action.name)
            }
        }
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.getAccount())
            initializeDriveClient(account)
            mSignInAccount = account
            onDriveSuccessful()
        } else {
            val mUser: User? = Utils.getUserInfo()
            if (mUser != null) {
                mUser.driveConnected = false
                Utils.setUserPreShare(mUser)
                onDriveError()
                Utils.onWriteLog("Sign-in failed on Google drive..", EnumStatus.SIGN_IN)
            }
        }
        Utils.Log(TAG, "onStart..........")
        if (SingletonManager.Companion.getInstance().isAnimation()) {
            if (onStartCount > 1) {
                this.overridePendingTransition(R.animator.anim_slide_in_right,
                        R.animator.anim_slide_out_right)
            } else if (onStartCount == 1) {
                onStartCount++
            }
        } else {
            Bungee.zoom(this)
            SingletonManager.Companion.getInstance().setAnimation(true)
        }
    }

    protected fun signIn(email: String?) {
        Utils.Log(TAG, "Sign in")
        val account = Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        startActivityForResult(mGoogleSignInClient?.getSignInIntent(), REQUEST_CODE_SIGN_IN)
    }

    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        return mGoogleSignInClient
    }

    /**
     * Handles resolution callbacks.
     */
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (resultCode != Activity.RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Utils.Log(TAG, "Sign-in failed.")
                    Utils.onWriteLog("Sign-in failed on Google drive ?..", EnumStatus.SIGN_IN)
                    onDriveError()
                    return
                }
                val getAccountTask: Task<GoogleSignInAccount?> = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (getAccountTask.isSuccessful()) {
                    Utils.Log(TAG, "sign in successful")
                    initializeDriveClient(getAccountTask.getResult())
                    onDriveSuccessful()
                } else {
                    onDriveError()
                    Utils.Log(TAG, "Sign-in failed..")
                    Utils.onWriteLog("Sign-in failed on Google drive..", EnumStatus.SIGN_IN)
                }
            }
        }
    }

    protected fun getAccessToken() {
        if (mSignInAccount != null) {
            Utils.Log(TAG, "Request token")
            GetAccessToken().execute(mSignInAccount!!.getAccount())
        } else {
            Utils.Log(TAG, "mSignInAccount is null")
        }
    }

    private inner class GetAccessToken : AsyncTask<Account?, Void?, String?>() {
        protected override fun doInBackground(vararg accounts: Account?): String? {
            try {
                if (accounts == null) {
                    return null
                }
                if (accounts[0] == null) {
                    return null
                }
                val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
                        SuperSafeApplication.Companion.getInstance(), SuperSafeApplication.Companion.getInstance().getRequiredScopesString())
                Utils.Log(TAG, "Account :" + Gson().toJson(accounts))
                credential.setSelectedAccount(accounts[0])
                try {
                    val value: String = credential.getToken()
                    if (value != null) {
                        Utils.Log(TAG, "access token  start $value")
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            mUser.access_token = kotlin.String.format(getString(R.string.access_token), value)
                            Utils.setUserPreShare(mUser)
                        }
                    }
                    return value
                } catch (e: GoogleAuthException) {
                    Utils.Log(TAG, "Error occurred on GoogleAuthException")
                }
            } catch (recoverableException: UserRecoverableAuthIOException) {
                Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
            } catch (e: IOException) {
                Utils.Log(TAG, "Error occurred on IOException")
            }
            return null
        }

        protected override fun onPostExecute(accessToken: String?) {
            super.onPostExecute(accessToken)
            try {
                if (accessToken != null) {
                    val mUser: User? = Utils.getUserInfo()
                    if (mUser != null) {
                        //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
                        if (ServiceManager.getInstance()?.getMyService() == null) {
                            Utils.Log(TAG, "SuperSafeService is null")
                            startServiceNow()
                            return
                        }
                        ServiceManager.getInstance()?.getMyService()?.getDriveAbout(object : BaseView<Any?> {
                            override fun onError(message: String?, status: EnumStatus?) {
                                Utils.Log(TAG, "onError " + message + " - " + status?.name)
                                when (status) {
                                    EnumStatus.REQUEST_ACCESS_TOKEN -> {
                                        revokeAccess()
                                    }
                                }
                                if (isSignIn()) {
                                    Utils.Log(TAG, "Call onDriveClientReady")
                                    onDriveClientReady()
                                }
                            }

                            override fun onSuccessful(message: String?) {
                                Utils.Log(TAG, "token request $message")
                            }

                            override fun onStartLoading(status: EnumStatus) {}
                            override fun onStopLoading(status: EnumStatus) {}
                            override fun onError(message: String?) {}
                            override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
                            override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Any?>?) {
                            }
                            override fun getContext(): Context? {
                                return getContext()
                            }

                            override fun getActivity(): Activity? {
                                return this@BaseGoogleApi
                            }

                            override fun onSuccessful(message: String?, status: EnumStatus?) {
                                Utils.Log(TAG, "onSuccessful " + message + " - " + status?.name)
                                val mUser: User? = Utils.getUserInfo()
                                //ServiceManager.getInstance().onGetListCategoriesSync();
                                if (mUser != null) {
                                    if (mUser.driveAbout == null) {
                                        ServiceManager.Companion.getInstance()?.onGetDriveAbout()
                                    }
                                }
                                if (isSignIn()) {
                                    Utils.Log(TAG, "Call onDriveClientReady")
                                    onDriveClientReady()
                                }
                            }
                        })
                    }
                }
                //Log.d(TAG, "response token : " + String.format(SuperSafeApplication.getInstance().getString(R.string.access_token), accessToken));
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.Log(TAG, "Call onDriveClientReady")
                onDriveClientReady()
            }
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Google client ready")
        Utils.Log(TAG, "Account :" + mSignInAccount?.getAccount())
        GetAccessToken().execute(mSignInAccount?.getAccount())
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected abstract fun onDriveClientReady()
    protected abstract fun onDriveSuccessful()
    protected abstract fun onDriveError()
    protected abstract fun onDriveSignOut()
    protected abstract fun onDriveRevokeAccess()
    protected abstract fun isSignIn(): Boolean
    protected abstract fun startServiceNow()
    protected abstract fun onStopListenerAWhile()
    protected fun signOut() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this, object : OnCompleteListener<Void?> {
            override fun onComplete(task: Task<Void?>) {
                val mUser: User? = Utils.getUserInfo()
                if (mUser != null) {
                    mUser.driveConnected = false
                    Utils.setUserPreShare(mUser)
                }
                onDriveSignOut()
            }
        })
    }

    protected fun signOut(ls: ServiceManager.ServiceManagerSyncDataListener) {
        Utils.Log(TAG, "Call signOut")
        if (mGoogleSignInClient == null) {
            return
        }
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this, object : OnCompleteListener<Void?> {
            override fun onComplete(task: Task<Void?>) {
                onDriveSignOut()
                ls.onCompleted()
            }
        })?.addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                ls.onError()
            }
        })
    }

    protected fun revokeAccess() {
        if (mGoogleSignInClient == null) {
            return
        }
        Utils.Log(TAG, "onRevokeAccess")
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this,
                object : OnCompleteListener<Void?> {
                    override fun onComplete(task: Task<Void?>) {
                        onDriveRevokeAccess()
                        PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive), false)
                    }
                })
    }

    protected fun onCheckRequestSignOut() {
        val isRequest: Boolean = PrefsController.getBoolean(getString(R.string.key_request_sign_out_google_drive), false)
        if (isRequest) {
            revokeAccess()
        }
    }

    companion object {
        private val TAG = BaseGoogleApi::class.java.simpleName
        protected const val REQUEST_CODE_SIGN_IN = 0
    }
}