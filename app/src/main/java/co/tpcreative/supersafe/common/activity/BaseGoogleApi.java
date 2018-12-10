
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
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.ftinc.kit.util.SizeUtils;
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
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
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
import co.tpcreative.supersafe.common.controller.SingletonMultipleListener;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;


public abstract class BaseGoogleApi extends AppCompatActivity implements SensorFaceUpDownChangeNotifier.Listener{

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
    Unbinder unbinder;
    protected ActionBar actionBar ;
    private HomeWatcher mHomeWatcher;
    private int onStartCount = 0;
    private SlidrConfig mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState != null) {
            onStartCount = 2;
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, SuperSafeApplication.getInstance().getGoogleSignInOptions(null));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
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
        final Theme result = Theme.getInstance().getThemeInfo();
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
                Utils.Log(TAG,"Nothing to do " +action.name());
            }
        }
    }

    protected void setStatusBarColored(Activity context, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context,color));
        }
    }

    protected void onDrawOverLay(Activity activity){
        final Theme theme = Theme.getInstance().getThemeInfo();
        mConfig = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(theme.getPrimaryColor()))
                .secondaryColor(getResources().getColor(theme.getPrimaryDarkColor()))
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(activity, mConfig);
    }

    protected void onFaceDown(final boolean isFaceDown){
        if (isFaceDown){
            final boolean result = PrefsController.getBoolean(getString(R.string.key_face_down_lock),false);
            if (result){
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance());
            }
        }
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        try {
            super.setContentView(layoutResID);
            Log.d(TAG,"action here");
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
    }

    @Override
    protected void onDestroy() {
        if (mHomeWatcher!=null){
            Utils.Log(TAG,"Stop home watcher....");
            mHomeWatcher.stopWatch();
        }
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
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
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
                        Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);                        Utils.Log(TAG,"Pressed home button");
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing to do on home" +action.name());
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
            Utils.Log(TAG,"2");
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
            if (mUser!=null){
                mUser.driveConnected = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                onDriveError();
                Utils.onWriteLog("Sign-in failed on Google drive..",EnumStatus.SIGN_IN);
            }
        }
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
                    Log.e(TAG, "Sign-in failed.");
                    Utils.onWriteLog("Sign-in failed on Google drive ?..",EnumStatus.SIGN_IN);
                    onDriveError();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    Log.d(TAG, "sign in successful");
                    initializeDriveClient(getAccountTask.getResult());
                    onDriveSuccessful();
                } else {
                    onDriveError();
                    Log.e(TAG, "Sign-in failed..");
                    Utils.onWriteLog("Sign-in failed on Google drive..",EnumStatus.SIGN_IN);
                }
                break;
            }
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
            try {
                if (accessToken != null) {
                    final User mUser = User.getInstance().getUserInfo();
                    if (mUser != null) {
                        //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
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

    protected abstract boolean isSignIn();

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
