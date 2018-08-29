package co.tpcreative.suppersafe.ui.signin;
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
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.request.SignInRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeReceiver;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.User;

public class SignInActivity extends BaseActivity implements TextView.OnEditorActionListener, SignInView{

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

    @Override
    public void showError(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessful(String message,User user) {
        Log.d(TAG,"user : " + new Gson().toJson(user));
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        Navigator.onMoveToVerify(this,user);
        presenter.onSendGmail(user.email,user.code);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SupperSafeReceiver.isConnected()){
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
        if (!SupperSafeReceiver.isConnected()){
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
    public void startLoading() {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
    }

    @Override
    public void stopLoading() {
        progressBarCircularIndeterminate.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unbindView();
    }

}
