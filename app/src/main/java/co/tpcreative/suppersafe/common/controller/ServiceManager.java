package co.tpcreative.suppersafe.common.controller;
import android.accounts.Account;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.services.SupperSafeService;
import co.tpcreative.suppersafe.ui.askpermission.AskPermissionActivity;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;

public class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private static ServiceManager instance;
    private SupperSafeService myService;
    private Context mContext;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public void setGoogleSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    private GoogleSignInClient mGoogleSignInClient;

    public GoogleSignInAccount getGoogleSignInAccount() {
        return mGoogleSignInAccount;
    }

    public void setGoogleSignInAccount(final GoogleSignInAccount mGoogleSignInAccount) {
        this.mGoogleSignInAccount = mGoogleSignInAccount;
    }

    public DriveClient getDriveClient() {
        return mDriveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    public void setDriveClient(final DriveClient mDriveClient) {
        this.mDriveClient = mDriveClient;
    }


    public void setDriveResourceClient(final DriveResourceClient mDriveResourceClient) {
        this.mDriveResourceClient = mDriveResourceClient;
    }


    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "connected");
            myService = ((SupperSafeService.LocalBinder) binder).getService();
        }

        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

    private void doBindService() {
        Intent intent = null;
        intent = new Intent(mContext, SupperSafeService.class);
        intent.putExtra(TAG, "Message");
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onStartService");
    }

    public void onStartService() {
        if (myService == null) {
            doBindService();
        }
    }

    public void onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection);
            myService = null;
        }
    }

    public SupperSafeService getMyService() {
        return myService;
    }


    public void onGetLastSignIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .requestIdToken(SupperSafeApplication.getInstance().getString(R.string.server_client_id))
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(SupperSafeApplication.getInstance(), signInOptions);
        final GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(SupperSafeApplication.getInstance());
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        }
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        final DriveClient driveClient = Drive.getDriveClient(SupperSafeApplication.getInstance(), signInAccount);;
        final DriveResourceClient driveResourceClient =  Drive.getDriveResourceClient(SupperSafeApplication.getInstance(), signInAccount);;
        final GoogleSignInAccount googleSignInAccount = signInAccount;
        mDriveClient = driveClient;
        mDriveResourceClient = driveResourceClient;
        mGoogleSignInAccount = googleSignInAccount;
        handleSignInResult(signInAccount);
    }


    private void handleSignInResult(GoogleSignInAccount signInAccount) {
        try {
            if (signInAccount != null) {
                Log.d(TAG, "name :" + signInAccount.getDisplayName());
            }
            // TODO(developer): send ID Token to server and validate
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "handleSignInResult:error", e);
        }
    }

    public void onSignOut(final ServiceManagerListener listener) {
        if (mGoogleSignInClient==null){
            listener.onError();
            return;
        }

        if (mDriveResourceClient==null){
            listener.onError();
            return;
        }
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               Log.d(TAG,"Sign out completed");
               mDriveResourceClient = null;
               mDriveClient = null;
               listener.onCompletedSignOut();
            }
        });

    }

    public void onDisconnectGoogleApi(final ServiceManagerListener ls) {
        if (mGoogleSignInClient==null){
            ls.onError();
            return;
        }
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mDriveResourceClient = null;
                        mDriveClient = null;
                        mGoogleSignInClient = null;
                        mGoogleSignInAccount = null;
                        ls.onCompletedDisconnect();
                    }
                });
    }

    public void onPickUpNewEmailNoTitle(Activity context,String account){
        try {
            Account account1 = new Account(account,GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
            intent.putExtra("overrideTheme", 1);
          //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpExistingEmail(Activity context,String account){
        try {
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_account),account);
            Account account1 = new Account(account,GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context){
        try {
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context,String account){
        try {
            Account mAccount = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE );
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(mAccount, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onAddPermission(final Activity activity, ServiceManagerAskPermissionListener ls, Collection<String> listPermission) {
        Dexter.withActivity(activity)
                .withPermissions(listPermission)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            ls.onGrantedPermission();
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                            ls.onError();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                            ls.onError();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }


    public interface ServiceManagerListener {
        void onCompletedDisconnect();
        void onCompletedSignOut();
        void onError();
    }

    public interface ServiceManagerAskPermissionListener{
        void onGrantedPermission();
        void onError();
    }


}
