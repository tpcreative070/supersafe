package co.tpcreative.supersafe.common.activity
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
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
import co.tpcreative.supersafe.model.*
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.Callable

abstract class BaseGoogleApi : AppCompatActivity(), SensorFaceUpDownChangeNotifier.Listener {
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mHomeWatcher: HomeWatcher? = null
    var TAG : String = this::class.java.simpleName
    private var compositeDisposable: CompositeDisposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(null)!!)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true)
        }
        return theme
    }

    fun onCallLockScreen() {
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        when (val action = EnumPinAction.values()[value]) {
            EnumPinAction.SPLASH_SCREEN -> {
                PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.SCREEN_LOCK.ordinal)
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Navigator.onMoveToVerifyPin(this, EnumPinAction.NONE)
                Utils.Log(TAG, "Lock screen")
            }
            else -> {
                Utils.onPushEventBus(EnumStatus.REGISTER_OR_LOGIN)
                Utils.Log(TAG, "Nothing to do " + action.name)
            }
        }
    }

    protected fun onFaceDown(isFaceDown: Boolean) {
        if (isFaceDown) {
            val result: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            if (result) {
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance())
            }
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        try {
            super.setContentView(layoutResID)
            Utils.Log(TAG, "action here")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.getInstance()?.remove(this)
        Utils.Log(TAG, "onPause")
        if (mHomeWatcher != null) {
            Utils.Log(TAG, "Stop home watcher....")
            mHomeWatcher?.stopWatch()
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onResume() {
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
                when (val action = EnumPinAction.values()[value]) {
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

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        when (val action = EnumPinAction.values()[value]) {
            EnumPinAction.SCREEN_LOCK -> {
                if (!SingletonManager.getInstance().isVisitLockScreen()) {
                    SuperSafeApplication.getInstance().getActivity()?.let { Navigator.onMoveToVerifyPin(it, EnumPinAction.NONE) }
                    Utils.Log(TAG, "Pressed home button")
                    SingletonManager.getInstance().setVisitLockScreen(true)
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
            getGoogleSignInClient(account.account)
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
    }

    protected fun signIn(email: String?) {
        Utils.Log(TAG, "Sign in")
        val account = Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        startActivityForResult(mGoogleSignInClient?.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        return mGoogleSignInClient
    }

    /**
     * Handles resolution callbacks.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                if (getAccountTask.isSuccessful) {
                    Utils.Log(TAG, "sign in successful")
                    initializeDriveClient(getAccountTask.result)
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
            onRefreshAccessToken(mSignInAccount!!.getAccount())
        } else {
            Utils.Log(TAG, "mSignInAccount is null")
        }
    }

    private fun onRefreshAccessToken(accounts: Account?) {
        Utils.Log(TAG,"onRefreshAccessToken")
        compositeDisposable = CompositeDisposable()
        compositeDisposable!!.add(Observable.fromCallable(Callable {
            try {
                if (accounts == null) {
                    return@Callable null
                }
                val credential = GoogleAccountCredential.usingOAuth2(
                        SuperSafeApplication.getInstance(), SuperSafeApplication.getInstance().getRequiredScopesString())
                credential.selectedAccount = accounts
                try {
                    val value = credential.token
                    if (value != null) {
                        val mAuthor: User? = Utils.getUserInfo()
                        if (mAuthor != null) {
                            mAuthor.driveConnected = true
                            mAuthor.access_token = String.format(SuperSafeApplication.getInstance().getString(R.string.access_token), value)
                            Utils.Log(TAG, "Refresh access token value=> " + mAuthor.access_token)
                            mAuthor.email = credential.selectedAccount.name
                            Utils.setUserPreShare(mAuthor)
                            ServiceManager.getInstance()?.onPreparingSyncData()
                        }
                    }
                    return@Callable value
                } catch (e: GoogleAuthException) {
                    Utils.Log(TAG, "Error occurred on GoogleAuthException")
                }
            } catch (recoverableException: UserRecoverableAuthIOException) {
                Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
            } catch (e: IOException) {
                Utils.Log(TAG, "Error occurred on IOException")
            }
            ""
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { response: String? ->
                    Utils.Log(TAG,"response token==> $response")
                    try {
                        if (response != null) {
                            if (ServiceManager.getInstance()?.getMyService() == null) {
                                Utils.Log(TAG, "SuperSafeService is null")
                                startServiceNow()
                                compositeDisposable?.dispose()
                                return@subscribe
                            }
                            ServiceManager.getInstance()?.getMyService()?.getDriveAbout(object : BaseView<EmptyModel?> {
                                override fun onError(message: String?, status: EnumStatus?) {
                                    Utils.Log(TAG, "onError " + message + " - " + status?.name)
                                    when (status) {
                                        EnumStatus.REQUEST_ACCESS_TOKEN -> {
                                            revokeAccess()
                                        }
                                        else -> Utils.Log(TAG,"Nothing")
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
                                override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
                                override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel?>?) {
                                }
                                override fun getContext(): Context? {
                                    return getContext()
                                }
                                override fun getActivity(): Activity? {
                                    return this@BaseGoogleApi
                                }
                                override fun onSuccessful(message: String?, status: EnumStatus?) {
                                    Utils.Log(TAG, "onSuccessful " + message + " - " + status?.name)
                                    ServiceManager.getInstance()?.onGetDriveAbout()
                                    if (isSignIn()) {
                                        Utils.Log(TAG, "Call onDriveClientReady")
                                        onDriveClientReady()
                                    }
                                }
                            })
                        }
                        compositeDisposable?.dispose()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Utils.Log(TAG, "Call onDriveClientReady")
                        compositeDisposable?.dispose()
                        onDriveClientReady()
                    }
                    try {
                        compositeDisposable?.dispose()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Utils.Log(TAG, "Call onDriveClientReady")
                        compositeDisposable!!.dispose()
                    }
                })
    }
    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Google client ready")
        Utils.Log(TAG, "Account :" + mSignInAccount?.getAccount())
        onRefreshAccessToken(mSignInAccount?.account)
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
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) {
            val mUser: User? = Utils.getUserInfo()
            if (mUser != null) {
                mUser.driveConnected = false
                Utils.setUserPreShare(mUser)
            }
            onDriveSignOut()
        }
    }

    fun signOut(ls: ServiceManager.ServiceManagerSyncDataListener) {
        Utils.Log(TAG, "Call signOut")
        if (mGoogleSignInClient == null) {
            return
        }
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) {
            onDriveSignOut()
            ls.onCompleted()
        }?.addOnFailureListener { ls.onError() }
    }

    protected fun revokeAccess() {
        if (mGoogleSignInClient == null) {
            return
        }
        Utils.Log(TAG, "onRevokeAccess")
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this) {
            onDriveRevokeAccess()
            PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive), false)
        }
    }

    protected fun onCheckRequestSignOut() {
        val isRequest: Boolean = PrefsController.getBoolean(getString(R.string.key_request_sign_out_google_drive), false)
        if (isRequest) {
            revokeAccess()
        }
    }

    companion object {
        protected const val REQUEST_CODE_SIGN_IN = 0
    }
}