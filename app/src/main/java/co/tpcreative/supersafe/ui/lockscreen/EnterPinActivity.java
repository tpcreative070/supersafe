package co.tpcreative.supersafe.ui.lockscreen;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;


public class EnterPinActivity extends BaseActivity {

    public static final String TAG = EnterPinActivity.class.getSimpleName();
    public static final String EXTRA_SET_PIN = "set_pin";
    public static final String EXTRA_SIGN_UP = "sign_up";
    public static final String EXTRA_FONT_TEXT = "textFont";
    public static final String EXTRA_FONT_NUM = "numFont";

    private static final int PIN_LENGTH = 20;

    private static final String PREFERENCES = "com.amirarcane.lockscreen";


    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView mTextTitle;
    private TextView mTextAttempts;

    private EnumPinAction mPinAction;
    private boolean mSignUp = false;
    private String mFirstPin = "";

    @BindView(R.id.imgLauncher)
    ImageView imgLauncher;


    public static Intent getIntent(Context context, int action,boolean isSignUp) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_SET_PIN, action);
        intent.putExtra(EXTRA_SIGN_UP,isSignUp);
        return intent;
    }

    public static Intent getIntent(Context context, String fontText, String fontNum) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_FONT_TEXT, fontText);
        intent.putExtra(EXTRA_FONT_NUM, fontNum);

        return intent;
    }

    public static Intent getIntent(Context context, int action, String fontText, String fontNum) {
        Intent intent = getIntent(context, fontText, fontNum);
        intent.putExtra(EXTRA_SET_PIN, action);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpin);
        mTextAttempts = (TextView) findViewById(R.id.attempts);
        mTextTitle = (TextView) findViewById(R.id.title);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        int result = getIntent().getIntExtra(EXTRA_SET_PIN, 0);
        mPinAction = EnumPinAction.values()[result];
        mSignUp = getIntent().getBooleanExtra(EXTRA_SIGN_UP,false);

        if (mPinAction ==EnumPinAction.SET) {
            changeLayoutForSetPin();
        } else if (mPinAction == EnumPinAction.VERIFY){
            String pin = getPinFromSharedPreferences();
            if (pin.equals("")) {
                changeLayoutForSetPin();
                mPinAction = EnumPinAction.SET;
            }
            else{
                imgLauncher.setVisibility(View.VISIBLE);
                mTextTitle.setVisibility(View.INVISIBLE);
            }
        }
        else if (mPinAction == EnumPinAction.RESET){

        }
        else{
            Utils.Log(TAG,"Nothing action");
        }

        final PinLockListener pinLockListener = new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (mPinAction == EnumPinAction.SET) {
                    setPin(pin);
                }
                else if (mPinAction ==EnumPinAction.VERIFY){
                    checkPin(pin);
                }
                else if (mPinAction ==EnumPinAction.RESET){
                    Utils.Log(TAG,"Reset");
                }
                else{
                    Utils.Log(TAG,"Nothing action");
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                String pinResult = getPinFromSharedPreferences();
                if (mPinAction == EnumPinAction.VERIFY){
                    if (pinResult.equals(intermediatePin)){
                        setResult(RESULT_OK);
                        Navigator.onMoveToMainTab(EnterPinActivity.this);
                    }
                    if (pinLength>pinResult.length()){
                        shake();
                        mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                        mPinLockView.resetPinLockView();
                    }
                }
                Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }

        };

        mPinLockView = (PinLockView) findViewById(R.id.pinlockView);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(pinLockListener);

        mPinLockView.setPinLength(PIN_LENGTH);

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        checkForFont();

    }

    private void checkForFont() {
        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_FONT_TEXT)) {

            String font = intent.getStringExtra(EXTRA_FONT_TEXT);
            setTextFont(font);
        }
        if (intent.hasExtra(EXTRA_FONT_NUM)) {
            String font = intent.getStringExtra(EXTRA_FONT_NUM);
            setNumFont(font);
        }
    }

    public void onDelete(View view){
        Log.d(TAG,"onDelete here");
        if (mPinLockView!=null){
            mPinLockView.onDeleteClicked();
        }
    }

    private void setTextFont(String font) {
        try {
            Typeface typeface = Typeface.createFromAsset(getAssets(), font);
            mTextTitle.setTypeface(typeface);
            mTextAttempts.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNumFont(String font) {
        try {
            Typeface typeface = Typeface.createFromAsset(getAssets(), font);

            mPinLockView.setTypeFace(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writePinToSharedPreferences(String pin) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeKey(pin);
    }

    private String getPinFromSharedPreferences() {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readKey();
    }

    private void setPin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            mTextTitle.setText(getString(R.string.pinlock_secondPin));
            mPinLockView.resetPinLockView();
        } else {
            if (pin.equals(mFirstPin)) {
                writePinToSharedPreferences(pin);
                setResult(RESULT_OK);
                if (mSignUp){
                    Navigator.onMoveToSignUp(this);
                }
                else{
                    Navigator.onMoveToMainTab(this);
                }
            } else {
                shake();
                mTextTitle.setText(getString(R.string.pinlock_tryagain));
                mPinLockView.resetPinLockView();
                mFirstPin = "";
            }
        }
    }

    private void checkPin(String pin) {
        if (pin.equals(getPinFromSharedPreferences())) {
            setResult(RESULT_OK);
            Navigator.onMoveToMainTab(this);
        } else {
            shake();
            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();
        }
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(mPinLockView, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
    }

    private void changeLayoutForSetPin() {
        mTextAttempts.setVisibility(View.GONE);
        mTextTitle.setText(getString(R.string.pinlock_settitle));
    }

    @Override
    public void onBackPressed() {
        if (mPinAction==EnumPinAction.VERIFY){
            finish();
        }
    }

}
