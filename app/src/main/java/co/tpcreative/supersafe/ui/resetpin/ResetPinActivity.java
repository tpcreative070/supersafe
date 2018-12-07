package co.tpcreative.supersafe.ui.resetpin;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivity;
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivity;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class ResetPinActivity extends BaseActivity implements BaseView, TextView.OnEditorActionListener{
    @BindView(R.id.tvStep1)
    TextView tvStep1;
    @BindView(R.id.edtCode)
    EditText edtCode;
    @BindView(R.id.btnSendRequest)
    Button btnSendRequest;
    @BindView(R.id.btnReset)
    Button btnReset;
    @BindView(R.id.progressbar_circular)
    CircularProgressBar mCircularProgressBar;
    private ResetPinPresenter presenter;
    private boolean isNext;
    private Boolean isRestoreFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pin);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new ResetPinPresenter();
        presenter.bindView(this);

        if (presenter.mUser!=null){
            String email = presenter.mUser.email;
            if (email!=null){
                String result = Utils.getFontString(R.string.request_an_access_code,email);
                tvStep1.setText(Html.fromHtml(result));
                tvStep1.setText(Html.fromHtml(result));
            }
        }
        edtCode.addTextChangedListener(mTextWatcher);
        edtCode.setOnEditorActionListener(this);

        try {
            Bundle bundle = getIntent().getExtras();
            isRestoreFiles = (boolean)bundle.get(ResetPinActivity.class.getSimpleName());
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
        //SuperSafeApplication.getInstance().writeKeyHomePressed(ResetPinActivity.class.getSimpleName());
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


    public void setProgressValue(){
        Theme theme = Theme.getInstance().getThemeInfo();
        CircularProgressDrawable circularProgressDrawable;
        CircularProgressDrawable.Builder b = new CircularProgressDrawable.Builder(this)
                .color(getResources().getColor(theme.getAccentColor()))
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

//        mCircularProgressBar.getIndeterminateDrawable().setColorFilter(.getResources().getColor(theme.getAccentColor()),
//                PorterDuff.Mode.SRC_IN);
        mCircularProgressBar.setVisibility(View.VISIBLE);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (Utils.isValid(value)){
                btnReset.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnReset.setTextColor(getResources().getColor(R.color.white));
                btnReset.setEnabled(true);
                isNext = true;
            }
            else{
                btnReset.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnReset.setTextColor(getResources().getColor(R.color.colorDisableText));
                btnReset.setEnabled(false);
                isNext = false;
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void onVerifyCode(){
        if (isNext){
            String code = edtCode.getText().toString().trim();
            final VerifyCodeRequest request = new VerifyCodeRequest();
            request.code = code;
            request.email = presenter.mUser.email;
            request._id = presenter.mUser._id;
            presenter.onVerifyCode(request);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                onVerifyCode();
                return true;
            }
            return false;
        }
        return false;
    }

    @OnClick(R.id.btnReset)
    public void onSendReset(View view){
       onVerifyCode();
    }

    @OnClick(R.id.btnSendRequest)
    public void onSentRequest(){
        btnSendRequest.setEnabled(false);
        btnSendRequest.setText("");
        onStartLoading(EnumStatus.OTHER);
        if (presenter.mUser!=null){
            if (presenter.mUser.email!=null){
                final VerifyCodeRequest request = new VerifyCodeRequest();
                request.email = presenter.mUser.email;
                presenter.onRequestCode(request);
            }
        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {
        setProgressValue();
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        if (mCircularProgressBar!=null){
            mCircularProgressBar.progressiveStop();
        }
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
    public void onError(String message, EnumStatus status) {
        onStopLoading(EnumStatus.OTHER);
        switch (status){
            case REQUEST_CODE_ERROR:{
                btnSendRequest.setText(getString(R.string.send_verification_code));
                btnSendRequest.setEnabled(true);
                Utils.showGotItSnackbar(tvStep1,R.string.request_code_occurred_error);
                break;
            }
            case SEND_EMAIL_ERROR:{
                Utils.showGotItSnackbar(tvStep1,R.string.sent_email_occurred_error);
                break;
            }
            case VERIFIED_ERROR:{
                Utils.showGotItSnackbar(tvStep1,R.string.verify_occurred_error);
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case REQUEST_CODE_SUCCESSFUL:{
                btnSendRequest.setEnabled(false);
                break;
            }
            case SEND_EMAIL_SUCCESSFUL:{
                onStopLoading(EnumStatus.OTHER);
                btnSendRequest.setText(getString(R.string.send_verification_code));
                Utils.showGotItSnackbar(tvStep1,R.string.we_sent_access_code_to_your_email);
                break;
            }
            case VERIFIED_SUCCESSFUL:{
                if (isRestoreFiles){
                    Navigator.onMoveToResetPin(this, EnumPinAction.RESTORE);
                }
                else {
                    Navigator.onMoveToResetPin(this,EnumPinAction.NONE);
                }
                break;
            }
        }
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
}
