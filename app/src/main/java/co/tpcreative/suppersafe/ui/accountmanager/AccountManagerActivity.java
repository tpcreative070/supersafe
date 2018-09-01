package co.tpcreative.suppersafe.ui.accountmanager;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ftinc.kit.util.SizeUtils;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.activity.BaseGoogleApi;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.User;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class AccountManagerActivity extends BaseGoogleApi {

    private static final String TAG = AccountManagerActivity.class.getSimpleName();
    private SlidrConfig mConfig;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvLicenseStatus)
    TextView tvLicenseStatus;
    @BindView(R.id.progressbar_circular)
    CircularProgressBar mCircularProgressBar;
    @BindView(R.id.btnSignOut)
    Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);
        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int primary = getResources().getColor(R.color.colorPrimary);
        int secondary = getResources().getColor(R.color.colorPrimaryDark);

        mConfig = new SlidrConfig.Builder()
                .primaryColor(primary)
                .secondaryColor(secondary)
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(this, mConfig);

        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.verified){
                tvStatus.setText("Verified");
            }
            tvEmail.setText(mUser.email);
        }
        setProgressValue();
    }

    public void setProgressValue(){
        CircularProgressDrawable circularProgressDrawable;
        CircularProgressDrawable.Builder b = new CircularProgressDrawable.Builder(this)
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(2)
                .rotationSpeed(2)
                .strokeWidth(Utils.dpToPx(3))
                .style(CircularProgressDrawable.STYLE_ROUNDED);
        mCircularProgressBar.setIndeterminateDrawable(circularProgressDrawable = b.build());
        // /!\ Terrible hack, do not do this at home!
        circularProgressDrawable.setBounds(0,
                0,
                mCircularProgressBar.getWidth(),
                mCircularProgressBar.getHeight());
        mCircularProgressBar.setVisibility(View.INVISIBLE);
        mCircularProgressBar.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btnSignOut)
    public void onSignOut(View view){
        Log.d(TAG,"sign out");
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            mUser.verified = false;
            mUser.driveConnected = false;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                @Override
                public void onCompleted() {
                    onBackPressed();
                }
                @Override
                public void onError() {
                    onBackPressed();
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @Override
    protected void onDriveClientReady() {

    }

    @Override
    protected void onDriveSuccessful() {
        Log.d(TAG,"onDriveSuccessful");
        btnSignOut.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDriveError() {
        Log.d(TAG,"onDriveError");
    }

    @Override
    protected void onDriveSignOut() {
        Log.d(TAG,"onDriveSignOut");
    }

    @Override
    protected void onDriveRevokeAccess() {
        Log.d(TAG,"onDriveRevokeAccess");
    }

}
