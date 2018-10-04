package co.tpcreative.supersafe.ui.lockscreen;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftinc.kit.util.SizeUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.preference.MyPreference;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;
import de.mrapp.android.preference.ListPreference;


public class EnterPinActivity extends BaseActivity implements LockScreenView {

    public static final String TAG = EnterPinActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    public static final String EXTRA_SET_PIN = "set_pin";
    public static final String EXTRA_SIGN_UP = "sign_up";
    public static final String EXTRA_FONT_TEXT = "textFont";
    public static final String EXTRA_FONT_NUM = "numFont";
    private static final int PIN_LENGTH = 20;

    @BindView(R.id.pinlockView)
    PinLockView mPinLockView;
    @BindView(R.id.indicator_dots)
    IndicatorDots mIndicatorDots;
    @BindView(R.id.title)
    TextView mTextTitle;
    @BindView(R.id.attempts)
    TextView mTextAttempts;
    @BindView(R.id.imgLauncher)
    ImageView imgLauncher;
    @BindView(R.id.rlLockScreen)
    RelativeLayout rlLockScreen;
    @BindView(R.id.rlPreference)
    RelativeLayout rlPreference;

    @BindView(R.id.llForgotPin)
    LinearLayout llForgotPin;

    private static EnumPinAction mPinAction;
    private boolean mSignUp = false;
    private String mFirstPin = "";
    private static LockScreenPresenter presenter;


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
        presenter = new LockScreenPresenter();
        presenter.bindView(this);

        int result = getIntent().getIntExtra(EXTRA_SET_PIN, 0);
        mPinAction = EnumPinAction.values()[result];
        mSignUp = getIntent().getBooleanExtra(EXTRA_SIGN_UP,false);

        switch (mPinAction){
            case SET:{
                onDisplayView();
                changeLayoutForSetPin();
                break;
            }
            case VERIFY:{
                onDisplayView();
                String pin = getPinFromSharedPreferences();
                if (pin.equals("")) {
                    changeLayoutForSetPin();
                    mPinAction = EnumPinAction.SET;
                }
                else{
                    onDisplayText();
                }
                break;
            }
            case SCREEN_OFF:{
                onDisplayView();
                String pin = getPinFromSharedPreferences();
                if (pin.equals("")) {
                    changeLayoutForSetPin();
                    mPinAction = EnumPinAction.SET;
                }
                else{
                    onDisplayText();
                }
                break;
            }
            case CHANGE:{
                initActionBar(true);
                onDisplayText();
                onDisplayView();
                onLauncherPreferences();
                break;
            }
            case RESET:{
                onDisplayView();
                changeLayoutForSetPin();
                break;
            }
            default:{
                Utils.Log(TAG,"Noting to do");
                break;
            }
        }

        final PinLockListener pinLockListener = new PinLockListener() {
            String pinResult = getPinFromSharedPreferences();
            @Override
            public void onComplete(String pin) {
                switch (mPinAction){
                    case SET:{
                        setPin(pin);
                        break;
                    }
                    case VERIFY:{
                        checkPin(pin);
                        break;
                    }
                    case VERIFY_TO_CHANGE:{
                        checkChangePin(pin);
                        break;
                    }
                    case SCREEN_OFF:{
                        checkScreenOff(pin);
                        break;
                    }

                    case CREATE:{
                        setCreatePin(pin);
                        break;
                    }
                    case RESET:{
                        resetPin(pin);
                    }
                    default:{
                        Utils.Log(TAG,"Nothing working");
                        break;
                    }
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                switch (mPinAction){
                    case VERIFY:{
                        if (pinResult.equals(intermediatePin)){
                            mPinLockView.setStop(true);
                            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                            Navigator.onMoveToMainTab(EnterPinActivity.this);
                        }
                        if (pinLength>pinResult.length()){
                            shake();
                            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                            mPinLockView.resetPinLockView();
                        }
                        break;
                    }
                    case SCREEN_OFF:{
                        if (pinResult.equals(intermediatePin)){
                            presenter.onChangeStatus(EnumPinAction.SCREEN_OFF);
                        }
                        if (pinLength>pinResult.length()){
                            shake();
                            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                            mPinLockView.resetPinLockView();
                        }
                        break;
                    }
                    case VERIFY_TO_CHANGE:{
                        if (pinResult.equals(intermediatePin)){
                            presenter.onChangeStatus(EnumPinAction.CREATE);
                        }
                        if (pinLength>pinResult.length()){
                            shake();
                            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                            mPinLockView.resetPinLockView();
                        }
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing working!!!");
                        break;
                    }
                }
                Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }

        };

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

    /*Forgot pin*/
    @OnClick(R.id.llForgotPin)
    public void onForgotPin(View view){
        Navigator.onMoveToForgotPin(this);
    }

    public void onSetVisitableForgotPin(int value){
        llForgotPin.setVisibility(value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPinLockView!=null){
            mPinLockView.resetPinLockView();
        }
        onSetVisitableForgotPin(View.GONE);
        mTextAttempts.setText("");
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

    private void resetPin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            mTextTitle.setText(getString(R.string.pinlock_secondPin));
            mPinLockView.resetPinLockView();
        } else {
            if (pin.equals(mFirstPin)) {
                writePinToSharedPreferences(pin);
                Navigator.onMoveToMainTab(this);
            } else {
                shake();
                mTextTitle.setText(getString(R.string.pinlock_tryagain));
                mPinLockView.resetPinLockView();
                mFirstPin = "";
            }
        }
    }

    private void setCreatePin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            mTextTitle.setText(getString(R.string.pinlock_secondPin));
            mPinLockView.resetPinLockView();
        } else {
            if (pin.equals(mFirstPin)) {
                writePinToSharedPreferences(pin);
                presenter.onChangeStatus(EnumPinAction.CREATE_DONE);
            } else {
                shake();
                mTextTitle.setText(getString(R.string.pinlock_tryagain));
                mPinLockView.resetPinLockView();
                mFirstPin = "";
            }
        }
    }

    private void checkChangePin(String pin) {
        if (pin.equals(getPinFromSharedPreferences())) {
            presenter.onChangeStatus(EnumPinAction.CREATE);
        } else {
            shake();
            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();
        }
    }

    private void checkPin(String pin) {
        if (pin.equals(getPinFromSharedPreferences())) {
            setResult(RESULT_OK);
            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
            Navigator.onMoveToMainTab(this);
        } else {
            shake();
            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();
        }
    }

    private void checkScreenOff(String pin) {
        if (pin.equals(getPinFromSharedPreferences())) {
            presenter.onChangeStatus(EnumPinAction.SCREEN_OFF);
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
        switch (mPinAction){
            case VERIFY:{
                onSetVisitableForgotPin(View.VISIBLE);
                break;
            }
        }
    }

    private void changeLayoutForSetPin() {
        mTextAttempts.setVisibility(View.GONE);
        mTextTitle.setText(getString(R.string.pinlock_settitle));
    }

    @Override
    public void onBackPressed() {
        Utils.Log(TAG,mPinAction.name());
        switch (mPinAction){
            case VERIFY:{
                super.onBackPressed();
                break;
            }
            case CHANGE:{
                super.onBackPressed();
                break;
            }
            case CREATE_DONE:{
                super.onBackPressed();
                break;
            }
            case VERIFY_TO_CHANGE:{
                super.onBackPressed();
                break;
            }
            case CREATE:{
                super.onBackPressed();
                break;
            }
            case SCREEN_OFF:{
                break;
            }
            case RESET:{
                break;
            }
        }
    }

    public void onDisplayView(){
        switch (mPinAction){
            case SET:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE:{
                rlLockScreen.setVisibility(View.INVISIBLE);
                rlPreference.setVisibility(View.VISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case CREATE:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case SCREEN_OFF:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }


    public void onDisplayText(){
        switch (mPinAction){
            case VERIFY:{
                mTextTitle.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.VISIBLE);
                break;
            }
            case CHANGE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case CREATE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_create));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case SCREEN_OFF:{
                mTextTitle.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onChangeStatus(EnumPinAction action) {
        Utils.Log(TAG,"Result here");
        mPinAction = action;
        switch (action){
            case VERIFY_TO_CHANGE:{
                initActionBar(false);
                onDisplayText();
                onDisplayView();
                break;
            }
            case CREATE:{
                mPinLockView.resetPinLockView();
                onDisplayText();
                onDisplayView();
                break;
            }
            case CREATE_DONE:{
                onBackPressed();
                break;
            }
            case SCREEN_OFF:{
                PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                finish();
                break;
            }
        }
    }

    public void initActionBar(boolean isInit){
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(isInit);
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    /*Settings preference*/

    public static class SettingsFragment extends PreferenceFragmentCompat {

        /**
         * The {@link ListPreference}.
         */

        private MyPreference mChangePin;

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type {@link
         * Preference.OnPreferenceChangeListener}
         */

        private Preference.OnPreferenceChangeListener createChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    return true;
                }
            };
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof MyPreference){
                        if (preference.getKey().equals(getString(R.string.key_change_pin))){
                            Utils.Log(TAG,"Action here!!!");
                            presenter.onChangeStatus(EnumPinAction.VERIFY_TO_CHANGE);
                        }
                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Changing pin*/
            mChangePin = (MyPreference)findPreference(getString(R.string.key_change_pin));
            mChangePin.setOnPreferenceChangeListener(createChangeListener());
            mChangePin.setOnPreferenceClickListener(createActionPreferenceClickListener());
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_lock_screen);
        }
    }

    public void onLauncherPreferences(){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = Fragment.instantiate(this, EnterPinActivity.SettingsFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
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

    @Override
    public Activity getActivity() {
        return this;
    }


    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
