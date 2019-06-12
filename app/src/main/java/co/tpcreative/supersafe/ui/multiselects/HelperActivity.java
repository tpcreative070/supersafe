package co.tpcreative.supersafe.ui.multiselects;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;

public class HelperActivity extends BaseActivity {
    protected View view;
    private final int maxLines = 4;
    private final String[] permissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE };

    protected void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted();

        } else {
            ActivityCompat.requestPermissions(this, permissions, Navigator.PERMISSION_REQUEST_CODE);
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showRequestPermissionRationale();

        } else {
            showAppPermissionSettings();
        }
    }

    private void showRequestPermissionRationale() {
        Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.permission_info),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(
                                HelperActivity.this,
                                permissions,
                                Navigator.PERMISSION_REQUEST_CODE);
                    }
                });
        snackbar.show();
    }

    private void showAppPermissionSettings() {
        Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.permission_force),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.fromParts(
                                getString(R.string.permission_package),
                                HelperActivity.this.getPackageName(),
                                null);

                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setData(uri);
                        startActivityForResult(intent, Navigator.PERMISSION_REQUEST_CODE);
                    }
                });

        /*((TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(maxLines);*/
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != Navigator.PERMISSION_REQUEST_CODE
                || grantResults.length == 0
                || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            permissionDenied();

        } else {
            permissionGranted();
        }
    }

    protected void permissionGranted() {}

    private void permissionDenied() {
        hideViews();
        requestPermission();
    }

    protected void hideViews() {}

    protected void setView(View view) {
        this.view = view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }
}
