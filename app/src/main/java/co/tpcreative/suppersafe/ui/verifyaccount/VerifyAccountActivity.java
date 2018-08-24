package co.tpcreative.suppersafe.ui.verifyaccount;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.request.VerifyCodeRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeReceiver;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.demo.oauthor.GoogleAuthActivity;
import co.tpcreative.suppersafe.ui.verify.VerifyActivity;

public class VerifyAccountActivity extends BaseActivity implements TextView.OnEditorActionListener{

    private static final String TAG = VerifyAccountActivity.class.getSimpleName();

    private SlidrConfig mConfig;
    @BindView(R.id.imgEdit)
    ImageView imgEdit;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.llGoogle)
    RelativeLayout llGoogle;
    @BindView(R.id.llAction)
    LinearLayout llAction;
    @BindView(R.id.llChangeEmail)
    LinearLayout llChangeEmail;
    @BindView(R.id.llVerifyCode)
    LinearLayout llVerifyCode;
    @BindView(R.id.edtEmail)
    MaterialEditText edtEmail;
    @BindView(R.id.edtCode)
    MaterialEditText edtCode;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    private boolean isNext ;


    private boolean isBack = true;

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

        edtCode.addTextChangedListener(mTextWatcher);
        edtEmail.addTextChangedListener(mTextWatcher);

    }



    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SupperSafeReceiver.isConnected()){
                Utils.showDialog(this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                Log.d(TAG,"Next");
                VerifyCodeRequest request = new VerifyCodeRequest();
                request.code = edtCode.getText().toString().trim();
                Utils.hideSoftKeyboard(this);

                /*Do Something here*/

                return true;
            }
            return false;
        }
        return false;
    }


    /*Detecting textWatch*/

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (getCurrentFocus() == edtEmail){
                if (Utils.isValidEmail(value)){
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                    btnSave.setTextColor(getResources().getColor(R.color.white));
                    isNext = true;
                }
                else{
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                    btnSave.setTextColor(getResources().getColor(R.color.colorDisableText));
                    isNext = false;
                }
            }
            else if (getCurrentFocus() == edtCode){
                if (Utils.isValid(value)){
                    btnSignIn.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                    btnSignIn.setTextColor(getResources().getColor(R.color.white));
                    isNext = true;
                }
                else{
                    btnSignIn.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                    btnSignIn.setTextColor(getResources().getColor(R.color.colorDisableText));
                    isNext = false;
                }
            }

        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @OnClick(R.id.llGoogle)
    public void onClickedGoogle(View view){
        Log.d(TAG,"google");
        onShowDialog();
    }

    @OnClick(R.id.imgEdit)
    public void onClickedEdit(View view){
        Log.d(TAG,"edit");
        onShowView(view);
    }

    @OnClick(R.id.btnSendVerifyCode)
    public void onClickedSendVerifyCode(View view){
        Log.d(TAG,"Verify code");
        onShowView(view);
    }

    @OnClick(R.id.btnCancel)
    public void onClickedCancel(View view){
        Log.d(TAG,"onCancel");
        Utils.hideSoftKeyboard(this);
        onShowView(llAction);
    }

    @OnClick(R.id.btnSave)
    public void onClickedSave(){
        Log.d(TAG,"onSave");
        if (isNext){
            /*Do something here*/
        }
    }

    @OnClick(R.id.btnReSend)
    public void onClickedResend(){
        Utils.hideSoftKeyboard(this);
        Log.d(TAG,"onResend");
    }

    @OnClick(R.id.btnSignIn)
    public void onClickedSignIn(View view){
        Log.d(TAG,"onSignIn");
        if (isNext){
            /*Do something here*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                Log.d(TAG,"home");
                if (isBack){
                    finish();
                    return true;
                }
                Utils.hideSoftKeyboard(this);
                isBack = true;
                onShowView(llAction);
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"home");
        if (isBack){
            super.onBackPressed();
        }
        Utils.hideSoftKeyboard(this);
        isBack = true;
        onShowView(llAction);
    }

    public void onShowView(View view){
        switch (view.getId()){
            case R.id.imgEdit :
                llAction.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.GONE);
                llChangeEmail.setVisibility(View.VISIBLE);
                edtCode.setText("");
                isBack = false;
                isNext = false;
                break;
            case R.id.btnSendVerifyCode :
                llAction.setVisibility(View.GONE);
                llChangeEmail.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.VISIBLE);
                edtEmail.setText("");
                edtCode.setText("");
                isBack = false;
                isNext = false;
                break;
            case R.id.llAction :{
                llAction.setVisibility(View.VISIBLE);
                llChangeEmail.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.GONE);
                edtEmail.setText("");
                edtCode.setText("");
                isBack = true;
                isNext = false;
            }
        }
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
