package co.tpcreative.suppersafe;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.jaychang.sa.DialogFactory;
import com.jaychang.sa.SocialUser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import co.tpcreative.suppersafe.common.controller.PrefsController;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile, which also adds a request dialog to access the user's Google Drive.
 */
public class SignInActivityWithDrive extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivityWithDrive";
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SING_OAUTHOR = 9002;

    private GoogleSignInClient mGoogleSignInClient;

    private TextView mStatusTextView;

    private boolean isProgessing ;


    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mStatusTextView = findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .requestIdToken(getString(R.string.server_client_id))
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        // [END build_client]
        // [END customize_button]
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, Drive.SCOPE_FILE,Drive.SCOPE_APPFOLDER)) {
            initializeDriveClient(account);
            updateUI(account);
            //getAccessToken(account);

        } else {
            updateUI(null);
        }

        // Check if the user is already signed in and all required scopes are granted
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()){
               handleSignInResult(task);
            }
        }
        else if (requestCode == RC_SING_OAUTHOR){
            Log.d(TAG,"OAUTHROR");
            isProgessing = false;
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            // Signed in successfully, show authenticated U
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            initializeDriveClient(account);
            updateUI(account);
            //getAccessToken(account);
        } catch (ApiException e) {
            // Signed out, show unauthenticated UI.
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            //Log.d(TAG,String.format(getString(R.string.access_token),account.getIdToken()));

            SocialUser user = new SocialUser();
            user.fullName = account.getDisplayName();
            user.userId = account.getId();
            user.email = account.getEmail();
            user.username = account.getGivenName();

            Log.d(TAG,"response user :"+new Gson().toJson(user));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

    private String getAccessTokenScope() {
        List<String> requiredScopes = new ArrayList<>();
        requiredScopes.add(DriveScopes.DRIVE);
        String scopes = "oauth2:id profile email";
        if (requiredScopes.size() > 0) {
            scopes = "oauth2:" + TextUtils.join(" ", requiredScopes);
        }
        Log.d(TAG,"getAccessTokenScope :"+scopes);
        return scopes;
    }

    private void getAccessToken(final GoogleSignInAccount account) {
        final ProgressDialog loadingDialog = DialogFactory.createLoadingDialog(this);
        loadingDialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (account.getAccount() == null) {
                        loadingDialog.dismiss();
                       Log.d(TAG,"Account is null");
                    } else {
                        loadingDialog.dismiss();
                        String token = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(),getAccessTokenScope());
                        Log.d(TAG,"token : " + token);
                    }
                }
                catch (IOException e){
                    Log.e(TAG, "IO Exception: " + e.getMessage());
                    e.printStackTrace();
                    loadingDialog.dismiss();
                }
                catch (UserRecoverableAuthException e){
                    e.printStackTrace();
                    loadingDialog.dismiss();
                    Log.d(TAG,"UserRecoverableAuthException");
                    if (!isProgessing){
                        startActivityForResult(e.getIntent(), RC_SING_OAUTHOR);
                        isProgessing = true;
                    }
                }
                catch (GoogleAuthException e) {
                    Log.e(TAG, "GoogleAuthException: " + e.getMessage());
                    e.printStackTrace();
                    loadingDialog.dismiss();
                }

            }
        });
    }


    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
    }



}
