package co.tpcreative.supersafe.common.controller
import android.accounts.Account
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.User
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class RefreshTokenSingleton private constructor() {
    var TAG = this::class.java.simpleName
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient?
    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(SuperSafeApplication.getInstance(), it) }
        return mGoogleSignInClient
    }

    fun <T> onStart(tClass: Class<T>?) {
        if (tClass != null) {
            TAG = tClass.simpleName
        }
        val account = GoogleSignIn.getLastSignedInAccount(SuperSafeApplication.getInstance())
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.account)
            initializeDriveClient(account)
            mSignInAccount = account
        } else {
            val mAuthor: User? = Utils.getUserInfo()
            if (mAuthor != null) {
                mAuthor.driveConnected = false
                Utils.setUserPreShare(mAuthor)
            }
            Utils.Log(TAG,"account is null")
        }
    }

    private fun onRefreshAccessToken(accounts: Account?) = CoroutineScope(Dispatchers.IO).launch{
        Utils.Log(TAG,"onRefreshAccessToken")
        try {
            if (accounts == null) {
                return@launch
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
                        mAuthor.access_token = kotlin.String.format(SuperSafeApplication.getInstance().getString(R.string.access_token), value)
                        Utils.Log(TAG, "Refresh access token value: " + mAuthor.access_token)
                        mAuthor.email = credential.selectedAccount.name
                        Utils.setUserPreShare(mAuthor)
                        ServiceManager.getInstance()?.onPreparingSyncData()
                    }
                }
            } catch (e: GoogleAuthException) {
                Utils.Log(TAG, "Error occurred on GoogleAuthException")
                e.printStackTrace()
            }
        } catch (recoverableException: UserRecoverableAuthIOException) {
            Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
            recoverableException.printStackTrace()
        } catch (e: IOException) {
            Utils.Log(TAG, "Error occurred on IOException")
            e.printStackTrace()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Request refresh access token")
        onRefreshAccessToken(mSignInAccount!!.account)
        //new RefreshTokenSingleton.GetAccessToken().execute(mSignInAccount.getAccount());
    }

    companion object {
        private var instance: RefreshTokenSingleton? = null
        fun getInstance(): RefreshTokenSingleton {
            if (instance == null) {
                instance = RefreshTokenSingleton()
            }
            return instance!!
        }
    }

    init {
        mGoogleSignInClient = SuperSafeApplication.getInstance().getGoogleSignInOptions(null)?.let { GoogleSignIn.getClient(SuperSafeApplication.getInstance(), it) }
    }
}