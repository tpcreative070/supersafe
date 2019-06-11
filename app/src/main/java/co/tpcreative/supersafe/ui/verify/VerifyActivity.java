package co.tpcreative.supersafe.ui.verify;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;

public class VerifyActivity extends BaseActivityNoneSlide implements BaseView, TextView.OnEditorActionListener{

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
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new VerifyPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        String result = Utils.getFontString(R.string.verify_title,presenter.user.email);
        tvTitle.setText(Html.fromHtml(result));
        edtCode.setOnEditorActionListener(this);
        edtCode.addTextChangedListener(mTextWatcher);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
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
        request._id = presenter.user._id;
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
    public void onStartLoading(EnumStatus status) {
        switch (status){
            case RESEND_CODE:{
                progressBarCircularIndeterminateReSend.setVisibility(View.VISIBLE);
                btnResend.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_CODE:{
                progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        switch (status){
            case RESEND_CODE:{
                progressBarCircularIndeterminateReSend.setVisibility(View.INVISIBLE);
                btnResend.setVisibility(View.VISIBLE);
                break;
            }
            case VERIFY_CODE:{
                progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                break;
            }
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
        switch (status){
            case VERIFY_CODE:{
                edtCode.setError(message);
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
            case VERIFY_CODE:{
                Navigator.onMoveSetPin(this, EnumPinAction.NONE);
                finish();
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
