
package co.tpcreative.suppersafe.common.activity;
import android.accounts.Account;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.ServiceManager;

/**
 * An abstract activity that handles authorization and connection to the Drive services.
 */

public abstract class BaseGoogleApi extends BaseActivity{

    private static final String TAG = BaseGoogleApi.class.getSimpleName();

    /**
     * Request code for Google Sign-in
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;


    /*
     * Request OAuthor
     *
     *
     * */

    protected static final int REQUEST_CODE_OAU_THOR = 10;


    /**
     * Request code for the Drive picker
     */
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;

    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    /*
     * Handle User manager
     *
     * */

    private GoogleSignInAccount mSignInAccount;

    /*
     * Handle GoogleSignInClient
     *
     * */

    GoogleSignInClient mGoogleSignInClient;

    /**
     * Tracks completion of the drive picker
     */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    private static final int RC_GET_TOKEN = 9002;

    @Override
    protected void onStart() {
        super.onStart();
        //signIn();
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
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    Log.d(TAG,"sign in successful");
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */

    protected void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =

                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.server_client_id))
                            .requestEmail()
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            //mGoogleSignInClient.getSignInIntent().putExtra("email","butlerichotel@gmail.com");
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    protected void signIn(final String email) {
        Log.d(TAG,"Sign in");
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            Account account = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.server_client_id))
                            .requestProfile()
                            .setAccount(account)
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        mSignInAccount = signInAccount;
        onDriveClientReady();
        handleSignInResult(mSignInAccount);
        createFile();
        Log.d(TAG,"Google client ready");
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

    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    protected GoogleSignInAccount getSignInAccount(){
        return mSignInAccount;
    }


    private void handleSignInResult(GoogleSignInAccount signInAccount) {
        try {
            if (signInAccount!=null){
             Log.d(TAG,"name :"+signInAccount.getDisplayName());
            }
            // TODO(developer): send ID Token to server and validate
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "handleSignInResult:error", e);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    private void createFile() {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = rootFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write("Hello World!");
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("HelloWorld123.jpg")
                            .setMimeType("image/jpeg")
                            .build();
                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                });
        // [END create_file]
    }

}
