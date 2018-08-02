package co.tpcreative.suppersafe.ui.verify;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.services.KeepSafetyReceiver;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.suppersafe.ui.login.LoginActivity;

public class VerifyActivity extends BaseActivity implements VerifyView, TextView.OnEditorActionListener{


    private static final String TAG = VerifyActivity.class.getSimpleName();
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.edtCode)
    MaterialEditText edtCode;

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
            Navigator.onMoveSetPin(this);
            Utils.hideSoftKeyboard(this);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!KeepSafetyReceiver.isConnected()){
                Utils.showDialog(VerifyActivity.this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                Log.d(TAG,"Next");
                Navigator.onMoveSetPin(this);
                Utils.hideSoftKeyboard(this);
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

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
