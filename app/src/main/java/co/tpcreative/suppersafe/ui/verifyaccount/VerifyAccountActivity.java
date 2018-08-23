package co.tpcreative.suppersafe.ui.verifyaccount;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ftinc.kit.util.SizeUtils;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jaychang.sa.AuthCallback;
import com.jaychang.sa.AuthData;
import com.jaychang.sa.AuthDataHolder;
import com.jaychang.sa.SocialUser;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.demo.oauthor.GoogleAuthActivity;

public class VerifyAccountActivity extends BaseActivity {

    private static final String TAG = VerifyAccountActivity.class.getSimpleName();

    private SlidrConfig mConfig;
    @BindView(R.id.imgEdit)
    ImageView imgEdit;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.llGoogle)
    RelativeLayout llGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);

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
        imgEdit.setColorFilter(getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);

       // tvTitle.setText(getString(R.string.verify_title,"tpcreative.co@gmail.com"));

        String sourceString = getString(R.string.verify_title, "<font color='#000000'>" + "tpcreative.co@gmail.com" +"</font>");
        tvTitle.setText(Html.fromHtml(sourceString));

    }

    @OnClick(R.id.llGoogle)
    public void onClickedGoogle(View view){
        Log.d(TAG,"google");
        onShowDialog();
    }


    public void onShowDialog(){
        new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.signin_with_google)
                .setDescription(R.string.choose_google_account)
                .setHeaderDrawable(R.drawable.ic_google_transparent_margin_60)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(R.color.colorPrimary)
                .setCancelable(true)
                .setPositiveText(R.string.ok)
                .setNegativeText(R.string.cancel)
                .setCheckBox(true,R.string.enable_cloud, new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Log.d(TAG,"checked :" + b);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"positive");
                        //Navigator.onChooseActivity(VerifyAccountActivity.this);
                        Navigator.onHomeActivity(VerifyAccountActivity.this);
                    }
                })
                .show();
    }

}
