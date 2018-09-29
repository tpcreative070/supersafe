
package co.tpcreative.supersafe.common.activity;
import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeServiceView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;


public abstract class BaseGoogleApi extends AppCompatActivity{

    private static final String TAG = BaseGoogleApi.class.getSimpleName();

    /**
     * Request code for Google Sign-in
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;

    private GoogleSignInAccount mSignInAccount;

    /*
     * Handle GoogleSignInClient
     *
     * */

    private GoogleSignInClient mGoogleSignInClient;

    private User mUser;

    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.animator.anim_slide_in_left,
                    R.animator.anim_slide_out_left);
        } else // already created so reverse animation
        {
            onStartCount = 2;
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(null));
        mUser = User.getInstance().getUserInfo();
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    protected void setDisplayHomeAsUpEnabled(boolean check){
        actionBar.setDisplayHomeAsUpEnabled(check);
    }

    protected void setNoTitle(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void setAdjustScreen(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        /*android:windowSoftInputMode="adjustPan|adjustResize"*/
    }

    protected String getResourceString(int code) {
        return getResources().getString(code);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (onStartCount > 1) {
            this.overridePendingTransition(R.animator.anim_slide_in_right,
                    R.animator.anim_slide_out_right);
        } else if (onStartCount == 1) {
            onStartCount++;
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, Drive.SCOPE_FILE,Drive.SCOPE_APPFOLDER)) {
            getGoogleSignInClient(account.getAccount());
            initializeDriveClient(account);
            mSignInAccount = account;
            onDriveSuccessful();
        } else {
            final User mUser = User.getInstance().getUserInfo();
            mUser.driveConnected = false;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            onDriveError();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Handles resolution callbacks.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Log.e(TAG, "Sign-in failed.");
                    onDriveError();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    Log.d(TAG,"sign in successful");
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    onDriveError();
                    Log.e(TAG, "Sign-in failed..");
                }
                break;
        }
    }

    protected void signIn(final String email) {
        Log.d(TAG,"Sign in");
        Account account = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(account));
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient getGoogleSignInClient(Account account){
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(account));
        return mGoogleSignInClient;
    }

    protected void getAccessToken(){
        if (mSignInAccount!=null){
            Utils.Log(TAG,"Request token");
            new GetAccessToken().execute(mSignInAccount.getAccount());
        }
        else{
            Utils.Log(TAG,"mSignInAccount is null");
        }
    }

    private class GetAccessToken extends AsyncTask<Account, Void, String> {
        @Override
        protected String doInBackground(Account... accounts) {
            try {
                if (accounts==null){
                    return null;
                }
                if (accounts[0]==null){
                    return null;
                }
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        SuperSafeApplication.getInstance(), SuperSafeApplication.getInstance().getRequiredScopesString());
                Log.d(TAG,"Account :"+ new Gson().toJson(accounts));
                credential.setSelectedAccount(accounts[0]);
                try {
                    String value = credential.getToken();
                    return value;
                }
                catch (GoogleAuthException e){
                    Log.d(TAG,"Error occurred on GoogleAuthException");
                }
            } catch (UserRecoverableAuthIOException recoverableException) {
                Log.d(TAG,"Error occurred on UserRecoverableAuthIOException");
            } catch (IOException e) {
                Log.d(TAG,"Error occurred on IOException");
            }
            return null;
        }
        @Override
        protected void onPostExecute(String accessToken) {
            super.onPostExecute(accessToken);
            if (accessToken!=null){
                mUser = User.getInstance().getUserInfo();
                if (mUser!=null){
                    mUser.access_token =String.format(SuperSafeApplication.getInstance().getString(R.string.access_token),accessToken);
                    PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(mUser));
                    if (ServiceManager.getInstance().getMyService()==null){
                        return;
                    }
                    Log.d(TAG,"Call DriveAbout");
                    ServiceManager.getInstance().getMyService().getDriveAbout(new SuperSafeServiceView() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Log.d(TAG,"error :"+ message);
                            revokeAccess();
                        }

                        @Override
                        public void onSuccessful(String message) {
                            ServiceManager.getInstance().onSyncDataOwnServer("0");
                            Log.d(TAG,"successful :"+ message);
                        }
                        @Override
                        public void onStart() {

                        }
                        @Override
                        public void startLoading() {

                        }
                        @Override
                        public void stopLoading() {

                        }
                        @Override
                        public void onNetworkConnectionChanged(boolean isConnect) {

                        }

                        @Override
                        public void onSuccessful(List<DriveResponse> lists) {

                        }

                        @Override
                        public void onSuccessful(String message, EnumStatus status) {

                        }

                        @Override
                        public void onSuccessfulOnCheck(List<Items> lists) {

                        }
                    });
                }
            }
            Log.d(TAG,"response token : "+ String.format(SuperSafeApplication.getInstance().getString(R.string.access_token),accessToken));
        }
    }


    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */


    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mSignInAccount = signInAccount;
        onDriveClientReady();
        Log.d(TAG,"Google client ready");
        Log.d(TAG,"Account :"+ mSignInAccount.getAccount());
        new GetAccessToken().execute(mSignInAccount.getAccount());
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */

    protected abstract void onDriveClientReady();

    protected abstract void onDriveSuccessful();

    protected abstract void onDriveError();

    protected abstract void onDriveSignOut();

    protected abstract void onDriveRevokeAccess();


    protected void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final  User mUser = User.getInstance().getUserInfo();
                if (mUser!=null){
                    mUser.driveConnected = false;
                    PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                }
                onDriveSignOut();
            }
        });
    }

    protected void signOut(ServiceManager.ServiceManagerSyncDataListener ls) {
        if (mGoogleSignInClient==null){
            return;
        }
        mGoogleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onDriveSignOut();
                ls.onCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ls.onError();
            }
        });
    }

    protected void revokeAccess() {
        if (mGoogleSignInClient==null){
            return;
        }
        Log.d(TAG,"onRevokeAccess");
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                     onDriveRevokeAccess();
                    }
                });
    }

}
