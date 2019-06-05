package co.tpcreative.supersafe.ui.signup;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.SignUpRequest;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class SignUpActivity extends BaseActivityNoneSlide implements TextView.OnEditorActionListener, BaseView<User>{

    private static final String TAG = SignUpActivity.class.getSimpleName();
    @BindView(R.id.edtName)
    MaterialEditText edtName;
    @BindView(R.id.edtEmail)
    MaterialEditText edtEmail;
    @BindView(R.id.btnFinish)
    Button btnFinish;
    @BindView(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    private boolean isEmail;
    private boolean isName;
    private SignUpPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edtName.addTextChangedListener(mTextWatcher);
        edtEmail.addTextChangedListener(mTextWatcher);
        edtEmail.setOnEditorActionListener(this);
        edtName.setOnEditorActionListener(this);
        presenter = new SignUpPresenter();
        presenter.bindView(this);
        Utils.Log(TAG,"onCreate");
        isName = true;
        edtName.setText(getString(R.string.free));
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
        ServiceManager.getInstance().onStartService();
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(SignUpActivity.this,getString(R.string.internet));
                return false;
            }
            if (isEmail && isName){
                Utils.Log(TAG,"Next");
                Utils.hideSoftKeyboard(this);
                onSignUp();
                return true;
            }
            return false;
        }
        else if (actionId == EditorInfo.IME_ACTION_DONE){
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(SignUpActivity.this,getString(R.string.internet));
                return false;
            }
            if (isEmail && isName){
                Utils.hideSoftKeyboard(this);
                onSignUp();
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
            if (getCurrentFocus()==edtName){
                if (Utils.isValid(s)){
                    isName = true;
                }
                else{
                    isName = false;
                }
            }

            if (getCurrentFocus()==edtEmail){
                if (Utils.isValidEmail(s)){
                    isEmail = true;
                }
                else{
                    isEmail = false;
                }
            }

            if (isEmail && isName){
                btnFinish.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnFinish.setTextColor(getResources().getColor(R.color.white));
                btnFinish.setEnabled(true);
            }
            else{
                btnFinish.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnFinish.setTextColor(getResources().getColor(R.color.colorDisableText));
                btnFinish.setEnabled(false);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @OnClick(R.id.btnFinish)
    public void onClickedFinish(View view){
        if (isName && isEmail){
           onSignUp();
        }
    }

    public void onSignUp(){
        String email = edtEmail.getText().toString().toLowerCase().trim();
        String name = edtName.getText().toString().trim();
        SignUpRequest request = new SignUpRequest();
        request.email = email;
        request.name = name;
        presenter.onSignUp(request);
        Utils.hideSoftKeyboard(this);
        Utils.Log(TAG,"onFished");
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
    public void onStartLoading(EnumStatus status) {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
        btnFinish.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String message, EnumStatus status) {
        switch (status){
            case SIGN_UP:{
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

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, User object) {
        switch (status){
            case SIGN_UP:{
                Navigator.onMoveToMainTab(this);
            }
            break;
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List<User> list) {

    }

}
