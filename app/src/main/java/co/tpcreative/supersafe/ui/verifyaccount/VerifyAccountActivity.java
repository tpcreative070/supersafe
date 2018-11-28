package co.tpcreative.supersafe.ui.verifyaccount;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GoogleDriveConnectionManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonEnterPinManager;
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.GoogleOauth;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;

public class VerifyAccountActivity extends BaseActivity implements TextView.OnEditorActionListener ,BaseView{
    private static final String TAG = VerifyAccountActivity.class.getSimpleName();
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

    @BindView(R.id.btnReSend)
    Button btnResend;
    @BindView(R.id.btnSendVerifyCode)
    Button btnSendVerifyCode;
    @BindView(R.id.progressBarCircularIndeterminateSignIn)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminateSignIn;
    @BindView(R.id.progressBarCircularIndeterminateReSend)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminateReSend;

    @BindView(R.id.progressBarCircularIndeterminateVerifyCode)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminateVerifyCode;
    @BindView(R.id.tvEmail)
    TextView tvEmail;

    private VerifyAccountPresenter presenter;


    private boolean isBack = true;
    private boolean isSync  = true;

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
        onDrawOverLay(this);
        imgEdit.setColorFilter(getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);

        edtCode.addTextChangedListener(mTextWatcher);
        edtEmail.addTextChangedListener(mTextWatcher);
        edtCode.setOnEditorActionListener(this);
        edtEmail.setOnEditorActionListener(this);

        presenter = new VerifyAccountPresenter();
        presenter.bindView(this);

        if (presenter.mUser!=null){
           if (presenter.mUser.email!=null){
               tvEmail.setText(presenter.mUser.email);
               String sourceString = getString(R.string.verify_title, "<font color='#000000'>" + presenter.mUser.email +"</font>");
               tvTitle.setText(Html.fromHtml(sourceString));
           }
        }

        Theme theme = Theme.getInstance().getThemeInfo();
        progressBarCircularIndeterminateSignIn.setBackgroundColor(getResources().getColor(theme.getAccentColor()));
        progressBarCircularIndeterminateReSend.setBackgroundColor(getResources().getColor(theme.getAccentColor()));
        progressBarCircularIndeterminateVerifyCode.setBackgroundColor(getResources().getColor(theme.getAccentColor()));

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
        isSync = true;
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(VerifyAccountActivity.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }


    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                Log.d(TAG,"Next");
                if (getCurrentFocus() == edtCode){
                    onVerifyCode();
                }
                if (getCurrentFocus() == edtEmail){
                    Utils.hideSoftKeyboard(this);
                    onChangedEmail();

                }
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
                Log.d(TAG,"code");
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
       onShowDialog();
    }

    @OnClick(R.id.imgEdit)
    public void onClickedEdit(View view){
        Log.d(TAG,"edit");
        onShowView(view);
        edtEmail.requestFocus();
        edtEmail.setText(presenter.mUser.email);
        edtEmail.setSelection(edtEmail.length());
    }

    @OnClick(R.id.btnSendVerifyCode)
    public void onClickedSendVerifyCode(View view){
        Log.d(TAG,"Verify code");
        SingletonManagerProcessing.getInstance().onStartProgressing(this,R.string.progressing);
        presenter.onCheckUser(presenter.mUser.email,presenter.mUser.other_email);
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
           onChangedEmail();
            /*Do something here*/
        }
    }

    public void onChangedEmail(){
        final String email = edtEmail.getText().toString().trim();
        tvEmail.setText(email);
        String sourceString = getString(R.string.verify_title, "<font color='#000000'>" + email +"</font>");
        tvTitle.setText(Html.fromHtml(sourceString));

        if (email.equals(presenter.mUser.email)){
            return;
        }
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.email = email;
        request.other_email = email;
        request.user_id = presenter.mUser.email;
        request._id = presenter.mUser._id;


        presenter.onChangeEmail(request);
        Utils.hideSoftKeyboard(this);
        Utils.hideKeyboard(edtEmail);

    }

    @OnClick(R.id.btnReSend)
    public void onClickedResend(){
        try{
            SingletonManagerProcessing.getInstance().onStartProgressing(this,R.string.progressing);
            Utils.hideSoftKeyboard(this);
            VerifyCodeRequest request = new VerifyCodeRequest();
            request.email = presenter.mUser.email;
            presenter.onResendCode(request);
            Log.d(TAG,"onResend");
        }catch (Exception e){

        }
    }

    @OnClick(R.id.btnSignIn)
    public void onClickedSignIn(View view){
        Log.d(TAG,"onSignIn");
        if (isNext){
            /*Do something here*/
            onVerifyCode();
        }
    }

    public void onVerifyCode(){
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.code = edtCode.getText().toString().trim();
        request.email = presenter.mUser.email;
        request._id = presenter.mUser._id;
        presenter.onVerifyCode(request);
        Utils.hideSoftKeyboard(this);
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
        Theme theme = Theme.getInstance().getThemeInfo();
        new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.signin_with_google)
                .setDescription(R.string.choose_google_account)
                .setHeaderDrawable(R.drawable.ic_google_transparent_margin_60)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(theme.getPrimaryColor())
                .setCancelable(true)
                .setPositiveText(R.string.ok)
                .setNegativeText(R.string.cancel)
                .setCheckBox(true,R.string.enable_cloud, new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Log.d(TAG,"checked :" + b);
                       isSync = b;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"positive");
                        ServiceManager.getInstance().onPickUpNewEmail(VerifyAccountActivity.this);
                    }
                })
                .show();
    }

    public void onShowDialogEnableSync(){
        Theme theme = Theme.getInstance().getThemeInfo();
        new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.enable_cloud_sync)
                .setDescription(R.string.message_prompt)
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(theme.getPrimaryColor())
                .setCancelable(true)
                .setPositiveText(R.string.enable_now)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false,R.string.enable_cloud)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"positive");
                        Navigator.onCheckSystem(VerifyAccountActivity.this,null);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Navigator.ENABLE_CLOUD :
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                }
                break;
            case Navigator.REQUEST_CODE_EMAIL :
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG,"accountName : " + accountName);
                    GoogleOauth googleOauth = new GoogleOauth();
                    googleOauth.email = accountName;
                    googleOauth.isEnableSync = isSync;
                    Navigator.onCheckSystem(VerifyAccountActivity.this,googleOauth);
                }
                break;
             default:
                 Log.d(TAG,"Nothing action");
                 break;
        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {
        switch (status){
            case RESEND_CODE:{
                break;
            }
            case VERIFY_CODE:{
                progressBarCircularIndeterminateVerifyCode.setVisibility(View.VISIBLE);
                btnSendVerifyCode.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnSendVerifyCode.setText("");
                break;
            }
        }
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        switch (status){
            case RESEND_CODE:{
                break;
            }
            case VERIFY_CODE:{
                progressBarCircularIndeterminateVerifyCode.setVisibility(View.INVISIBLE);
                btnSendVerifyCode.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnSendVerifyCode.setText(getString(R.string.send_verification_code));
                break;
            }
        }
    }

    @Override
    public void onError(String message, EnumStatus status) {
        switch (status){
            case CHANGE_EMAIL:{
                edtEmail.setError(message);
                break;
            }
        }
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case CHANGE_EMAIL:{
                onShowView(llAction);
                break;
            }
            case SEND_EMAIL:{
                onShowView(btnSendVerifyCode);
                SingletonManagerProcessing.getInstance().onStopProgressing(VerifyAccountActivity.this);
                Utils.showGotItSnackbar(tvEmail,R.string.we_sent_access_code_to_your_email);
                break;
            }
            case VERIFY_CODE:{
                edtCode.setText("");
                onShowDialogEnableSync();
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

}
