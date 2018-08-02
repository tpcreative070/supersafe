package co.tpcreative.suppersafe.ui.askpermission;
import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.util.List;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.services.KeepSafetyApplication;

public class AskPermissionActivity extends BaseActivity {

    private static final String TAG = AskPermissionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_permission);
    }

    public void onAddPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Navigator.onMoveToLogin(AskPermissionActivity.this);
                            PrefsController.putBoolean(getString(R.string.key_grant_access),true);
                            KeepSafetyApplication.getInstance().initFolder();
                            finish();
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
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

    @OnClick(R.id.btnGrantAccess)
    public void onGrantAccess(){
        onAddPermission();
    }

    @OnClick(R.id.btnExit)
    public void onExit(){
        finish();
    }

}
