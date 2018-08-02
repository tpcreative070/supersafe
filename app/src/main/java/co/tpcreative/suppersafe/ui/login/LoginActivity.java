package co.tpcreative.suppersafe.ui.login;
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
import co.tpcreative.suppersafe.model.User;

public class LoginActivity extends BaseActivity implements TextView.OnEditorActionListener{


    private static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.edtEmail)
    MaterialEditText edtEmail;
    @BindView(R.id.btnNext)
    Button btnNext;
    private boolean isNext ;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtEmail.setOnEditorActionListener(this);
        edtEmail.addTextChangedListener(mTextWatcher);
        user = new User();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!KeepSafetyReceiver.isConnected()){
                Utils.showDialog(LoginActivity.this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                Log.d(TAG,"Next");
                user.email = edtEmail.getText().toString().toLowerCase();
                Navigator.onMoveToVerify(this,user);
                Utils.hideSoftKeyboard(this);
                return true;
            }
            return false;
        }
        return false;
    }

    @OnClick(R.id.btnNext)
    public void onNext(View view){
        if (!KeepSafetyReceiver.isConnected()){
            Utils.showDialog(LoginActivity.this,getString(R.string.internet));
            return ;
        }
        if (isNext){
            user.email = edtEmail.getText().toString().toLowerCase();
            Navigator.onMoveToVerify(this,user);
            Utils.hideSoftKeyboard(this);
        }
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


}
