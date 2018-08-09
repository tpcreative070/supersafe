package co.tpcreative.suppersafe.ui.signup;
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
import co.tpcreative.suppersafe.common.services.SupperSafeReceiver;
import co.tpcreative.suppersafe.common.util.Utils;

public class SignUpActivity extends BaseActivity implements TextView.OnEditorActionListener{

    private static final String TAG = SignUpActivity.class.getSimpleName();
    @BindView(R.id.edtName)
    MaterialEditText edtName;
    @BindView(R.id.edtEmail)
    MaterialEditText edtEmail;
    @BindView(R.id.btnFinish)
    Button btnFinish;
    private boolean isEmail;
    private boolean isName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        edtName.addTextChangedListener(mTextWatcher);
        edtEmail.addTextChangedListener(mTextWatcher);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SupperSafeReceiver.isConnected()){
                Utils.showDialog(SignUpActivity.this,getString(R.string.internet));
                return false;
            }
            if (isEmail && isName){
                Log.d(TAG,"Next");
                Utils.hideSoftKeyboard(this);
                return true;
            }
            return false;
        }
        else if (actionId == EditorInfo.IME_ACTION_DONE){
            if (!SupperSafeReceiver.isConnected()){
                Utils.showDialog(SignUpActivity.this,getString(R.string.internet));
                return false;
            }
            if (isEmail && isName){
                Log.d(TAG,"Next");
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
            Navigator.onMoveToMainTab(this);
            Log.d(TAG,"onFished");
        }
    }

}
