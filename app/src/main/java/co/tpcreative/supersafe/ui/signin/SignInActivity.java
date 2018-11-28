package co.tpcreative.supersafe.ui.signin;
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
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;
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
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class SignInActivity extends BaseActivityNoneSlide implements TextView.OnEditorActionListener, BaseView<User>{

    private static final String TAG = SignInActivity.class.getSimpleName();
    @BindView(R.id.edtEmail)
    MaterialEditText edtEmail;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    private boolean isNext ;
    private SignInPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        edtEmail.setOnEditorActionListener(this);
        edtEmail.addTextChangedListener(mTextWatcher);
        presenter = new SignInPresenter();
        presenter.bindView(this);
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

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(SignInActivity.this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                  onSignIn();
                return true;
            }
            return false;
        }
        return false;
    }

    @OnClick(R.id.btnNext)
    public void onNext(View view){
        if (!SuperSafeReceiver.isConnected()){
            Utils.showDialog(SignInActivity.this,getString(R.string.internet));
            return ;
        }
        if (isNext){
          onSignIn();
        }
    }

    public void onSignIn(){
        String email = edtEmail.getText().toString().toLowerCase();
        SignInRequest request = new SignInRequest();
        request.email = email;
        presenter.onSignIn(request);
        Utils.hideSoftKeyboard(this);
    }

    /*Detecting textWatch*/

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           if (Utils.isValidEmail(s)){
               btnNext.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
               btnNext.setTextColor(getResources().getColor(R.color.white));
               isNext = true;
           }
           else{
               btnNext.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
               btnNext.setTextColor(getResources().getColor(R.color.colorDisableText));
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
        btnNext.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }


    @Override
    public void onError(String message, EnumStatus status) {
        switch (status){
            case SIGN_IN:{
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
            case SIGN_IN:{
                Log.d(TAG,"user : " + new Gson().toJson(object));
                Navigator.onMoveToVerify(this,object);
                presenter.onSendGmail(object.email,object.code);
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List<User> list) {

    }
}
