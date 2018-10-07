package co.tpcreative.supersafe.ui.lockscreen;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonBaseActivity;
import co.tpcreative.supersafe.common.controller.SingletonBaseApiActivity;
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig;
import co.tpcreative.supersafe.common.hiddencamera.CameraError;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;


public class EnterPinActivity extends BaseVerifyPinActivity implements BaseView<EnumPinAction> {

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
    private CameraConfig mCameraConfig;


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
            case INIT_PREFERENCE:{
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
                    case CHANGE:{
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
                            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                            presenter.onChangeStatus(EnumStatus.VERIFY,EnumPinAction.DONE);
                        }
                        if (pinLength>pinResult.length()){
                            onTakePicture(intermediatePin);
                            shake();
                            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                            mPinLockView.resetPinLockView();
                        }
                        break;
                    }
                    case VERIFY_TO_CHANGE:{
                        if (pinResult.equals(intermediatePin)){
                            presenter.onChangeStatus(EnumStatus.VERIFY,EnumPinAction.CHANGE);
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

        onInitHiddenCamera();

    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
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
                if (mSignUp){
                    Navigator.onMoveToSignUp(this);
                }
                else{
                    Navigator.onMoveToMainTab(this);
                    presenter.onChangeStatus(EnumStatus.SET,EnumPinAction.DONE);
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
                presenter.onChangeStatus(EnumStatus.RESET,EnumPinAction.DONE);
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
                presenter.onChangeStatus(EnumStatus.CHANGE,EnumPinAction.DONE);
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
            presenter.onChangeStatus(EnumStatus.VERIFY,EnumPinAction.CHANGE);
        } else {
            shake();
            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();
        }
    }

    private void checkPin(String pin) {
        if (pin.equals(getPinFromSharedPreferences())) {
            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
            presenter.onChangeStatus(EnumStatus.VERIFY,EnumPinAction.DONE);
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
    public void onSuccessful(String message, EnumStatus status, EnumPinAction action) {
        Utils.Log(TAG,"EnumPinAction 1:...."+action.name());
        switch (status){
            case VERIFY:{
                Utils.Log(TAG,"Result here");
                mPinAction = action;
                switch (action){
                    case VERIFY_TO_CHANGE:{
                        initActionBar(false);
                        onDisplayText();
                        onDisplayView();
                        break;
                    }
                    case CHANGE:{
                        mPinLockView.resetPinLockView();
                        onDisplayText();
                        onDisplayView();
                        break;
                    }
                    case DONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                    case VERIFY:{
                        finish();
                        break;
                    }
                }
                break;
            }
            case SET:{
                mPinAction = action;
                switch (action){
                    case DONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case CHANGE:{
                mPinAction = action;
                switch (action){
                    case DONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case RESET:{
                mPinAction = action;
                switch (action){
                    case DONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        Utils.Log(TAG,mPinAction.name());
        switch (mPinAction){
            case VERIFY:{
                int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action){
                    case SCREEN_LOCK:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.STILL_SCREEN_LOCK.ordinal());
                        SingletonBaseActivity.getInstance().onStillScreenLock(EnumStatus.FINISH);
                        SingletonBaseApiActivity.getInstance().onStillScreenLock(EnumStatus.FINISH);
                        Utils.Log(TAG,"onStillScreenLock");
                    }
                }
                super.onBackPressed();
                break;
            }
            case CHANGE:{
                super.onBackPressed();
                break;
            }
            case DONE:{
                super.onBackPressed();
                break;
            }
            case VERIFY_TO_CHANGE:{
                super.onBackPressed();
                break;
            }
            case RESET:{
                break;
            }
            case SET:{
                break;
            }
        }
    }

    public void onDisplayView(){
        Utils.Log(TAG,"EnumPinAction 2:...."+mPinAction.name());
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
            case VERIFY_TO_CHANGE:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE:{
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case INIT_PREFERENCE:{
                rlLockScreen.setVisibility(View.INVISIBLE);
                rlPreference.setVisibility(View.VISIBLE);
                break;
            }
        }
    }


    public void onDisplayText(){
        Utils.Log(TAG,"EnumPinAction 3:...."+mPinAction.name());
        switch (mPinAction){
            case VERIFY:{
                mTextTitle.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.VISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_create));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case INIT_PREFERENCE:{
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
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
        private Preference mChangePin;

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
                    if (preference instanceof Preference){
                        if (preference.getKey().equals(getString(R.string.key_change_pin))){
                            Utils.Log(TAG,"Action here!!!");
                            presenter.onChangeStatus(EnumStatus.VERIFY,EnumPinAction.VERIFY_TO_CHANGE);
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
            mChangePin = findPreference(getString(R.string.key_change_pin));
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
    public void onSuccessful(String message, EnumStatus status, List<EnumPinAction> list) {
    }

    @Override
    public void onImageCapture(@NonNull File imageFile, @NonNull String pin) {
        super.onImageCapture(imageFile, pin);
        showMessage(imageFile.getAbsolutePath());
        BreakInAlerts inAlerts = new BreakInAlerts();
        inAlerts.fileName = imageFile.getAbsolutePath();
        inAlerts.pin = pin;
        inAlerts.time = System.currentTimeMillis();
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(inAlerts);
    }

    @Override
    public void onCameraError(int errorCode) {
        super.onCameraError(errorCode);
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                showMessage(getString(R.string.error_cannot_open));
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                showMessage(getString(R.string.error_cannot_write));
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                showMessage(getString(R.string.error_cannot_get_permission));
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                showMessage(getString(R.string.error_not_having_camera));
                break;
        }
    }

    public void onInitHiddenCamera(){
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert),false);
        if (!value){
            showMessage("Permission denied");
            return;
        }
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270)
                .setCameraFocus(CameraFocus.AUTO)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig);
        }
    }

    public void onTakePicture(String pin){
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert),false);
        if (!value){
            showMessage("Permission denied");
            return;
        }
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance())
                .setPin(pin);
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance()).
                setImageFile(SuperSafeApplication.getInstance().getDefaultStorageFile(CameraImageFormat.FORMAT_JPEG));
        takePicture();
    }

}
