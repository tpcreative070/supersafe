package co.tpcreative.supersafe.common.activity;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.HomeWatcher;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import spencerstudios.com.bungeelib.Bungee;

public abstract class BaseGoogleApi extends AppCompatActivity implements SensorFaceUpDownChangeNotifier.Listener{
    private static final String TAG = BaseGoogleApi.class.getSimpleName();
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    private GoogleSignInAccount mSignInAccount;
    private GoogleSignInClient mGoogleSignInClient;
    Unbinder unbinder;
    protected ActionBar actionBar ;
    private HomeWatcher mHomeWatcher;
    private int onStartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState == null) {
            if (SingletonManager.getInstance().isReloadMainTab()){
                Bungee.fade(this);
            }else{
                this.overridePendingTransition(R.animator.anim_slide_in_left,
                        R.animator.anim_slide_out_left);
            }
        } else {
            onStartCount = 2;
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(null));
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected void onStartOverridePendingTransition(){
        this.overridePendingTransition(R.animator.anim_slide_in_left,
                R.animator.anim_slide_out_left);
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        final ThemeApp result = ThemeApp.getInstance().getThemeInfo();
        if (result!=null){
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true);
        }
        return theme;
    }

    public void onCallLockScreen(){
        int  value = PrefsController.getInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
        EnumPinAction action = EnumPinAction.values()[value];
        switch (action){
            case SPLASH_SCREEN:{
                PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Navigator.onMoveToVerifyPin(this,EnumPinAction.NONE);
                Utils.Log(TAG,"Lock screen");
                break;
            }
            default:{
                EventBus.getDefault().post(EnumStatus.REGISTER_OR_LOGIN);
                Utils.Log(TAG,"Nothing to do " +action.name());
            }
        }
    }

    protected void onFaceDown(final boolean isFaceDown){
        if (isFaceDown){
            final boolean result = PrefsController.getBoolean(getString(R.string.key_face_down_lock),false);
            if (result){
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance());
            }
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID){
        try {
            super.setContentView(layoutResID);
            Utils.Log(TAG,"action here");
            unbinder = ButterKnife.bind(this);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorFaceUpDownChangeNotifier.getInstance().remove(this);
        Utils.Log(TAG,"onPause");
        if (mHomeWatcher!=null){
            Utils.Log(TAG,"Stop home watcher....");
            mHomeWatcher.stopWatch();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @Override
    protected void onResume() {
        SensorFaceUpDownChangeNotifier.getInstance().addListener(this);
        super.onResume();
    }

    public void onRegisterHomeWatcher(){
        Utils.Log(TAG,"Register");
        /*Home action*/
        if (mHomeWatcher!=null){
            if (mHomeWatcher.isRegistered){
                return;
            }
        }
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action){
                    case NONE:{
                        Utils.onHomePressed();
                        onStopListenerAWhile();
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing to do on home " +action.name());
                        break;
                    }
                }
                mHomeWatcher.stopWatch();
            }
            @Override
            public void onHomeLongPressed() {
            }
        });
        mHomeWatcher.startWatch();
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
        int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
        EnumPinAction action = EnumPinAction.values()[value];
        switch (action){
            case SCREEN_LOCK:{
                if (!SingletonManager.getInstance().isVisitLockScreen()){
                    Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);                        Utils.Log(TAG,"Pressed home button");
                    SingletonManager.getInstance().setVisitLockScreen(true);
                    Utils.Log(TAG,"Verify pin");
                }else{
                    Utils.Log(TAG,"Verify pin already");
                }
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do on start " +action.name());
                break;
            }
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account,new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.getAccount());
            initializeDriveClient(account);
            mSignInAccount = account;
            onDriveSuccessful();
        } else {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                mUser.driveConnected = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                onDriveError();
                Utils.onWriteLog("Sign-in failed on Google drive..",EnumStatus.SIGN_IN);
            }
        }
        Utils.Log(TAG,"onStart..........");
        if (SingletonManager.getInstance().isAnimation()){
            if (onStartCount > 1) {
                this.overridePendingTransition(R.animator.anim_slide_in_right,
                            R.animator.anim_slide_out_right);
            } else if (onStartCount == 1) {
                    onStartCount++;
            }
        }else{
            Bungee.zoom(this);
            SingletonManager.getInstance().setAnimation(true);
        }
    }

    protected void signIn(final String email) {
        Utils.Log(TAG,"Sign in");
        Account account = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(account));
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient getGoogleSignInClient(Account account){
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(account));
        return mGoogleSignInClient;
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN: {
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Utils.Log(TAG, "Sign-in failed.");
                    Utils.onWriteLog("Sign-in failed on Google drive ?..",EnumStatus.SIGN_IN);
                    onDriveError();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    Utils.Log(TAG, "sign in successful");
                    initializeDriveClient(getAccountTask.getResult());
                    onDriveSuccessful();
                } else {
                    onDriveError();
                    Utils.Log(TAG, "Sign-in failed..");
                    Utils.onWriteLog("Sign-in failed on Google drive..",EnumStatus.SIGN_IN);
                }
                break;
            }
        }
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
                Utils.Log(TAG,"Account :"+ new Gson().toJson(accounts));
                credential.setSelectedAccount(accounts[0]);
                try {
                    String value = credential.getToken();
                    if (value!=null){
                        Utils.Log(TAG,"access token  start "+ value);
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.access_token = String.format(getString(R.string.access_token),value);
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        }
                    }
                    return value;
                }
                catch (GoogleAuthException e){
                    Utils.Log(TAG,"Error occurred on GoogleAuthException");
                }
            } catch (UserRecoverableAuthIOException recoverableException) {
                Utils.Log(TAG,"Error occurred on UserRecoverableAuthIOException");
            } catch (IOException e) {
                Utils.Log(TAG,"Error occurred on IOException");
            }
            return null;
        }
        @Override
        protected void onPostExecute(String accessToken) {
            super.onPostExecute(accessToken);
            try {
                if (accessToken != null) {
                    final User mUser = User.getInstance().getUserInfo();
                    if (mUser != null) {
                        //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
                        if (ServiceManager.getInstance().getMyService()==null){
                            Utils.Log(TAG,"SuperSafeService is null");
                            startServiceNow();
                            return;
                        }
                        ServiceManager.getInstance().getMyService().getDriveAbout(new BaseView() {
                            @Override
                            public void onError(String message, EnumStatus status) {
                                Utils.Log(TAG,"onError " +message + " - " +status.name());
                                switch (status){
                                    case REQUEST_ACCESS_TOKEN:{
                                        revokeAccess();
                                        break;
                                    }
                                }
                                if (isSignIn()) {
                                    Utils.Log(TAG,"Call onDriveClientReady");
                                    onDriveClientReady();
                                }
                            }
                            @Override
                            public void onSuccessful(String message) {
                                Utils.Log(TAG,"token request "+ message);
                            }
                            @Override
                            public void onStartLoading(EnumStatus status) {

                            }
                            @Override
                            public void onStopLoading(EnumStatus status) {

                            }
                            @Override
                            public void onError(String message) {

                            }
                            @Override
                            public void onSuccessful(String message, EnumStatus status, Object object) {
                            }
                            @Override
                            public void onSuccessful(String message, EnumStatus status, List list) {

                            }
                            @Override
                            public Context getContext() {
                                return getContext();
                            }

                            @Override
                            public Activity getActivity() {
                                return BaseGoogleApi.this;
                            }

                            @Override
                            public void onSuccessful(String message, EnumStatus status) {
                                Utils.Log(TAG,"onSuccessful " +message + " - " +status.name());
                                final User mUser = User.getInstance().getUserInfo();
                                //ServiceManager.getInstance().onGetListCategoriesSync();
                                if (mUser != null) {
                                    if (mUser.driveAbout == null) {
                                        ServiceManager.getInstance().onGetDriveAbout();
                                    }
                                }
                                if (isSignIn()) {
                                    Utils.Log(TAG,"Call onDriveClientReady");
                                    onDriveClientReady();
                                }
                            }
                        });
                    }
                }
                //Log.d(TAG, "response token : " + String.format(SuperSafeApplication.getInstance().getString(R.string.access_token), accessToken));
            }
            catch (Exception e){
                e.printStackTrace();
                Utils.Log(TAG,"Call onDriveClientReady");
                onDriveClientReady();
            }
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mSignInAccount = signInAccount;
        Utils.Log(TAG,"Google client ready");
        Utils.Log(TAG,"Account :"+ mSignInAccount.getAccount());
        new GetAccessToken().execute(mSignInAccount.getAccount());
    }
    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */

    protected abstract void onDriveClientReady();

    protected abstract void onDriveSuccessful();

    protected abstract void onDriveError();

    protected abstract void onDriveSignOut();

    protected abstract void onDriveRevokeAccess();

    protected abstract boolean isSignIn();

    protected abstract void startServiceNow();

    protected abstract void onStopListenerAWhile();

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
        Utils.Log(TAG,"Call signOut");
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
        Utils.Log(TAG,"onRevokeAccess");
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                     onDriveRevokeAccess();
                     PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive),false);
                    }
                });
    }

    protected void onCheckRequestSignOut(){
        boolean isRequest = PrefsController.getBoolean(getString(R.string.key_request_sign_out_google_drive),false);
        if (isRequest){
            revokeAccess();
        }
    }
}
