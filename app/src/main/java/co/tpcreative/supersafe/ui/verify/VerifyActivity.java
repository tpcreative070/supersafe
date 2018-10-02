package co.tpcreative.supersafe.ui.verify;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.rengwuxian.materialedittext.MaterialEditText;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;

public class VerifyActivity extends BaseActivity implements VerifyView, TextView.OnEditorActionListener{

    private static final String TAG = VerifyActivity.class.getSimpleName();
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnReSend)
    Button btnResend;
    @BindView(R.id.edtCode)
    MaterialEditText edtCode;
    @BindView(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    @BindView(R.id.progressBarCircularIndeterminateReSend)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminateReSend;

    private boolean isNext ;
    private VerifyPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        presenter = new VerifyPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        tvTitle.setText(getString(R.string.verify_title,presenter.user.email));
        edtCode.setOnEditorActionListener(this);
        edtCode.addTextChangedListener(mTextWatcher);
    }

    @OnClick(R.id.imgBack)
    public void onClickedBack(){
        onBackPressed();
    }

    @OnClick(R.id.btnLogin)
    public void onLogin(View view){
        if (isNext){
           onVerifyCode();
        }
    }

    @OnClick(R.id.btnReSend)
    public void onResend(View view){
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.email = presenter.user.email;
        presenter.onResendCode(request);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(VerifyActivity.this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                Log.d(TAG,"Next");
                onVerifyCode();
                return true;
            }
            return false;
        }
        return false;
    }

    public void onVerifyCode(){
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.code = edtCode.getText().toString().trim();
        request.email = presenter.user.email;
        presenter.onVerifyCode(request);
        Utils.hideSoftKeyboard(this);
    }

    /*Detecting textWatch*/

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (Utils.isValid(value)){
                btnLogin.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnLogin.setTextColor(getResources().getColor(R.color.white));
                isNext = true;
            }
            else{
                btnLogin.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnLogin.setTextColor(getResources().getColor(R.color.colorDisableText));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void startLoading() {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
    }

    @Override
    public void stopLoading() {
        progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoading() {
        progressBarCircularIndeterminateReSend.setVisibility(View.VISIBLE);
        btnResend.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFinishing() {
        progressBarCircularIndeterminateReSend.setVisibility(View.INVISIBLE);
        btnResend.setVisibility(View.VISIBLE);
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
    public void showError(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessful(String message) {
        Navigator.onMoveSetPin(this,false);
    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }
}
