package co.tpcreative.suppersafe.ui.lockscreen;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class EnterPinActivity extends BaseActivity {

    public static final String TAG = EnterPinActivity.class.getSimpleName();

    public static final int RESULT_BACK_PRESSED = RESULT_FIRST_USER;
    //    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
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

    private boolean mSetPin = false;
    private boolean mSignUp = false;
    private String mFirstPin = "";
    //    private int mTryCount = 0;

    public static Intent getIntent(Context context, boolean setPin,boolean isSignUp) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_SET_PIN, setPin);
        intent.putExtra(EXTRA_SIGN_UP,isSignUp);
        return intent;
    }

    public static Intent getIntent(Context context, String fontText, String fontNum) {
        Intent intent = new Intent(context, EnterPinActivity.class);

        intent.putExtra(EXTRA_FONT_TEXT, fontText);
        intent.putExtra(EXTRA_FONT_NUM, fontNum);

        return intent;
    }

    public static Intent getIntent(Context context, boolean setPin, String fontText, String fontNum) {
        Intent intent = getIntent(context, fontText, fontNum);
        intent.putExtra(EXTRA_SET_PIN, setPin);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpin);
        mTextAttempts = (TextView) findViewById(R.id.attempts);
        mTextTitle = (TextView) findViewById(R.id.title);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        mSetPin = getIntent().getBooleanExtra(EXTRA_SET_PIN, false);
        mSignUp = getIntent().getBooleanExtra(EXTRA_SIGN_UP,false);

        if (mSetPin) {
            changeLayoutForSetPin();
        } else {
            String pin = getPinFromSharedPreferences();
            if (pin.equals("")) {
                changeLayoutForSetPin();
                mSetPin = true;
            }
        }

        final PinLockListener pinLockListener = new PinLockListener() {

            @Override
            public void onComplete(String pin) {
                if (mSetPin) {
                    setPin(pin);
                } else {
                    checkPin(pin);
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
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
        SupperSafeApplication.getInstance().writeKey(pin);
    }

    private String getPinFromSharedPreferences() {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SupperSafeApplication.getInstance().readKey();
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
        setResult(RESULT_BACK_PRESSED);
        super.onBackPressed();
    }

}
