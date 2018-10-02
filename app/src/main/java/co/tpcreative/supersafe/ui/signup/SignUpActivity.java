package co.tpcreative.supersafe.ui.signup;
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
import co.tpcreative.supersafe.common.request.SignUpRequest;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class SignUpActivity extends BaseActivity implements TextView.OnEditorActionListener, SignUpView{

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        edtName.addTextChangedListener(mTextWatcher);
        edtEmail.addTextChangedListener(mTextWatcher);
        edtEmail.setOnEditorActionListener(this);
        edtName.setOnEditorActionListener(this);
        presenter = new SignUpPresenter();
        presenter.bindView(this);
        Log.d(TAG,"onCreate");
        isName = true;
        edtName.setText(getString(R.string.free));
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessful(String message, User user) {
        Navigator.onMoveToMainTab(this);
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(SignUpActivity.this,getString(R.string.internet));
                return false;
            }
            if (isEmail && isName){
                Log.d(TAG,"Next");
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
        String email = edtEmail.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        SignUpRequest request = new SignUpRequest();
        request.email = email;
        request.name = name;
        presenter.onSignUp(request);
        Utils.hideSoftKeyboard(this);
        Log.d(TAG,"onFished");
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
    public void startLoading() {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.INVISIBLE);
    }

    @Override
    public void stopLoading() {
        progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
        btnFinish.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unbindView();
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
