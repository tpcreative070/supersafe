package co.tpcreative.supersafe.ui.lockscreen;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonMultipleListener;
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig;
import co.tpcreative.supersafe.common.hiddencamera.CameraError;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation;
import co.tpcreative.supersafe.common.preference.MyPreference;
import co.tpcreative.supersafe.common.preference.MySwitchPreference;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;

public class EnterPinActivity extends BaseVerifyPinActivity implements BaseView<EnumPinAction>, FingerPrintAuthCallback,SingletonMultipleListener.Listener {

    public static final String TAG = EnterPinActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    public static final String EXTRA_SET_PIN = "SET_PIN";
    public static final String EXTRA_ENUM_ACTION = "ENUM_ACTION";
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
    @BindView(R.id.rlButton)
    RelativeLayout rlButton;
    @BindView(R.id.rlDots)
    RelativeLayout rlDots;
    @BindView(R.id.includeLayout)
    RelativeLayout includeLayout;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.imgFingerprint)
    ImageView imgFingerprint;
    @BindView(R.id.imgSwitchTypeUnClock)
    ImageView imgSwitchTypeUnClock;
    private int count = 0;
    private boolean isFingerprint ;


    private static EnumPinAction mPinAction;
    private static EnumPinAction mPinActionNext;
    private String mFirstPin = "";
    private static LockScreenPresenter presenter;
    private CameraConfig mCameraConfig;
    private FingerPrintAuthHelper mFingerPrintAuthHelper;

    public static Intent getIntent(Context context, int action, int actionNext) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_SET_PIN, action);
        intent.putExtra(EXTRA_ENUM_ACTION, actionNext);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpin);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Theme theme  = Theme.getInstance().getThemeInfo();
        rlLockScreen.setBackgroundColor(getResources().getColor(theme.getPrimaryColor()));
        setStatusBarColored(this,theme.getPrimaryColor(),theme.getPrimaryDarkColor());


        presenter = new LockScreenPresenter();
        presenter.bindView(this);
        int result = getIntent().getIntExtra(EXTRA_SET_PIN, 0);
        mPinAction = EnumPinAction.values()[result];
        int resultNext = getIntent().getIntExtra(EXTRA_ENUM_ACTION, 0);
        mPinActionNext = EnumPinAction.values()[resultNext];


        switch (mPinAction) {
            case SET: {
                onDisplayView();
                onDisplayText();
                break;
            }
            case VERIFY: {
                String pin = getPinFromSharedPreferences();
                if (pin.equals("")) {
                    mPinAction = EnumPinAction.SET;
                    onDisplayView();
                    onDisplayText();
                } else {

                    if (Utils.isSensorAvailable()){
                        boolean isFingerPrintUnLock = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
                        if (isFingerPrintUnLock){
                            imgSwitchTypeUnClock.setVisibility(View.VISIBLE);
                            isFingerprint = isFingerPrintUnLock;
                            onSetVisitFingerprintView(isFingerprint);
                        }
                    }

                    final boolean value = PrefsController.getBoolean(getString(R.string.key_secret_door), false);
                    if (value) {
                        imgSwitchTypeUnClock.setVisibility(View.INVISIBLE);
                        changeLayoutSecretDoor(true);
                    } else {
                        onDisplayView();
                        onDisplayText();
                    }


                }
                break;
            }
            case INIT_PREFERENCE: {
                initActionBar(true);
                onDisplayText();
                onDisplayView();
                onLauncherPreferences();
                break;
            }
            case RESET: {
                onDisplayView();
                onDisplayText();
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                onDisplayText();
                onDisplayView();
                break;
            }
            default: {
                Utils.Log(TAG, "Noting to do");
                break;
            }
        }

        final PinLockListener pinLockListener = new PinLockListener() {
            String pinResult = getPinFromSharedPreferences();

            @Override
            public void onComplete(String pin) {
                switch (mPinAction) {
                    case SET: {
                        setPin(pin);
                        break;
                    }
                    case VERIFY: {
                        checkPin(pin,true);
                        break;
                    }
                    case VERIFY_TO_CHANGE: {
                        checkPin(pin,true);
                        break;
                    }
                    case VERIFY_TO_CHANGE_FAKE_PIN: {
                        checkPin(pin,true);
                        break;
                    }
                    case CHANGE: {
                        setPin(pin);
                        break;
                    }
                    case FAKE_PIN: {
                        setPin(pin);
                        break;
                    }
                    case RESET: {
                        setPin(pin);
                        break;
                    }
                    default: {
                        Utils.Log(TAG, "Nothing working");
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
                switch (mPinAction) {
                    case VERIFY: {
                        checkPin(intermediatePin,false);
                        break;
                    }
                    case VERIFY_TO_CHANGE: {
                        checkPin(intermediatePin,false);
                        break;
                    }
                    case VERIFY_TO_CHANGE_FAKE_PIN: {
                        checkPin(intermediatePin,false);
                    }
                    default: {
                        Utils.Log(TAG, "Nothing working!!!");
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
        onInitHiddenCamera();

        imgLauncher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                changeLayoutSecretDoor(false);
                return false;
            }
        });


        if (Utils.isSensorAvailable()){
            mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        }

    }

    @Override
    public void onNotifier(EnumStatus status) {
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().onStartService();
    }

    @OnClick(R.id.btnDone)
    public void onClickedDone() {
        onBackPressed();
    }



    public void onDelete(View view) {
        Log.d(TAG, "onDelete here");
        if (mPinLockView != null) {
            mPinLockView.onDeleteClicked();
        }
    }



    /*Forgot pin*/
    @OnClick(R.id.llForgotPin)
    public void onForgotPin(View view) {
        Navigator.onMoveToForgotPin(this, false);
    }

    public void onSetVisitableForgotPin(int value) {
        llForgotPin.setVisibility(value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPinLockView != null) {
            mPinLockView.resetPinLockView();
        }
        onSetVisitableForgotPin(View.GONE);
        mTextAttempts.setText("");

        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.startAuth();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.stopAuth();
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

    private void writeFakePinToSharedPreferences(String pin) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeFakeKey(pin);
    }

    private String getFakePinFromSharedPreferences() {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readFakeKey();
    }

    public boolean isExistingFakePin(String pin) {
        final String value = getFakePinFromSharedPreferences();
        if (pin.equals(value)) {
            return true;
        }
        return false;
    }


    private void setPin(String pin) {
        switch (mPinAction) {
            case SET: {
                if (mFirstPin.equals("")) {
                    mFirstPin = pin;
                    mTextTitle.setText(getString(R.string.pinlock_secondPin));
                    mPinLockView.resetPinLockView();
                } else {
                    if (pin.equals(mFirstPin)) {
                        writePinToSharedPreferences(pin);
                        switch (mPinActionNext) {
                            case SIGN_UP: {
                                Navigator.onMoveToSignUp(this);
                                break;
                            }
                            default: {
                                Navigator.onMoveToMainTab(this);
                                presenter.onChangeStatus(EnumStatus.SET, EnumPinAction.DONE);
                                break;
                            }
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain));
                    }
                }
                break;
            }
            case CHANGE: {
                if (mFirstPin.equals("")) {
                    mFirstPin = pin;
                    mTextTitle.setText(getString(R.string.pinlock_secondPin));
                    mPinLockView.resetPinLockView();
                } else {
                    if (pin.equals(mFirstPin)) {
                        if (isExistingFakePin(pin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace));
                        } else {
                            writePinToSharedPreferences(pin);
                            presenter.onChangeStatus(EnumStatus.CHANGE, EnumPinAction.DONE);
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain));
                    }
                }
                break;
            }
            case FAKE_PIN: {
                if (mFirstPin.equals("")) {
                    mFirstPin = pin;
                    mTextTitle.setText(getString(R.string.pinlock_secondPin));
                    mPinLockView.resetPinLockView();
                } else {
                    if (pin.equals(mFirstPin)) {
                        writeFakePinToSharedPreferences(pin);
                        presenter.onChangeStatus(EnumStatus.CREATE_FAKE_PIN, EnumPinAction.DONE);
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain));
                    }
                }
                break;
            }
            case RESET: {
                if (mFirstPin.equals("")) {
                    mFirstPin = pin;
                    mTextTitle.setText(getString(R.string.pinlock_secondPin));
                    mPinLockView.resetPinLockView();
                } else {
                    if (pin.equals(mFirstPin)) {
                        if (isExistingFakePin(pin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace));
                        } else {
                            switch (mPinActionNext) {
                                case RESTORE: {
                                    writePinToSharedPreferences(pin);
                                    onRestore();
                                    break;
                                }
                                default: {
                                    writePinToSharedPreferences(pin);
                                    Navigator.onMoveToMainTab(this);
                                    presenter.onChangeStatus(EnumStatus.RESET, EnumPinAction.DONE);
                                    break;
                                }
                            }
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain));
                    }
                }
                break;
            }
        }
    }

    public void onRestore() {
        Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeBackup(), SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), new ServiceManager.ServiceManagerSyncDataListener() {
            @Override
            public void onCompleted() {
                Utils.Log(TAG, "Exporting successful");
                final User mUser = SuperSafeApplication.getInstance().readUseSecret();
                if (mUser != null) {
                    PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                    Navigator.onMoveToMainTab(EnterPinActivity.this);
                    presenter.onChangeStatus(EnumStatus.RESTORE, EnumPinAction.DONE);
                }
            }
            @Override
            public void onError() {
                Utils.Log(TAG, "Exporting error");
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void checkPin(String pin,boolean isCompleted) {
        final boolean isFakePinEnabled = PrefsController.getBoolean(getString(R.string.key_fake_pin), false);
        switch (mPinAction) {
            case VERIFY: {
                if (pin.equals(getPinFromSharedPreferences())) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE);
                } else if (pin.equals(getFakePinFromSharedPreferences()) && isFakePinEnabled) {
                    presenter.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE);
                } else if (pin.length() > getPinFromSharedPreferences().length()) {
                    onTakePicture(pin);
                    onAlertWarning("");
                }
                else {
                    if (isCompleted){
                        onTakePicture(pin);
                        onAlertWarning("");
                    }
                }
                break;
            }
            case VERIFY_TO_CHANGE: {
                if (pin.equals(getPinFromSharedPreferences())) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.CHANGE);
                } else if (pin.length() > getPinFromSharedPreferences().length()) {
                    onTakePicture(pin);
                    onAlertWarning("");
                }
                else{
                    if (isCompleted){
                        onTakePicture(pin);
                        onAlertWarning("");
                    }
                }
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                if (pin.equals(getPinFromSharedPreferences())) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.FAKE_PIN);
                } else if (pin.length() > getPinFromSharedPreferences().length()) {
                    onTakePicture(pin);
                    onAlertWarning("");
                }
                else{
                    if (isCompleted){
                        onTakePicture(pin);
                        onAlertWarning("");
                    }
                }
                break;
            }
        }
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(mPinLockView, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
        switch (mPinAction) {
            case VERIFY: {
                count += 1;
                if (count >= 4) {
                    onSetVisitableForgotPin(View.VISIBLE);
                }
                break;
            }
            case VERIFY_TO_CHANGE: {
                count += 1;
                if (count >= 4) {
                    onSetVisitableForgotPin(View.VISIBLE);
                }
                break;
            }
        }
        Utils.Log(TAG,"Visit...."+ count);
    }

    private void onAlertWarning(String title) {
        switch (mPinAction) {
            case SET: {
                shake();
                mTextTitle.setText(title);
                mPinLockView.resetPinLockView();
                mFirstPin = "";
                break;
            }
            case CHANGE: {
                shake();
                mTextTitle.setText(title);
                mPinLockView.resetPinLockView();
                mFirstPin = "";
                break;
            }
            case FAKE_PIN: {
                shake();
                mTextTitle.setText(title);
                mPinLockView.resetPinLockView();
                mFirstPin = "";
                break;
            }
            case RESET: {
                shake();
                mTextTitle.setText(title);
                mPinLockView.resetPinLockView();
                mFirstPin = "";
                break;
            }
            case VERIFY:{
                shake();
                mTextTitle.setText(title);
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
                break;
            }
            case VERIFY_TO_CHANGE:{
                shake();
                mTextTitle.setText(title);
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN:{
                shake();
                mTextTitle.setText(title);
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
                break;
            }
        }
    }

    private void changeLayoutSecretDoor(boolean isVisit) {
        if (isVisit) {
            mTextTitle.setVisibility(View.INVISIBLE);
            rlButton.setVisibility(View.INVISIBLE);
            rlDots.setVisibility(View.INVISIBLE);
            mTextAttempts.setVisibility(View.INVISIBLE);
            imgLauncher.setVisibility(View.VISIBLE);
            includeLayout.setVisibility(View.VISIBLE);
        } else {
            mTextTitle.setVisibility(View.VISIBLE);
            rlButton.setVisibility(View.VISIBLE);
            rlDots.setVisibility(View.VISIBLE);
            mTextAttempts.setVisibility(View.INVISIBLE);
            imgLauncher.setVisibility(View.INVISIBLE);
            includeLayout.setVisibility(View.GONE);


            if (Utils.isSensorAvailable()){
                boolean isFingerPrintUnLock = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
                if (isFingerPrintUnLock){
                    imgSwitchTypeUnClock.setVisibility(View.VISIBLE);
                    isFingerprint = isFingerPrintUnLock;
                    onSetVisitFingerprintView(isFingerprint);
                }
            }


        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, EnumPinAction action) {
        Utils.Log(TAG, "EnumPinAction 1:...." + action.name());
        switch (status) {
            case VERIFY: {
                Utils.Log(TAG, "Result here");
                mPinAction = action;
                switch (action) {
                    case VERIFY_TO_CHANGE: {
                        initActionBar(false);
                        onDisplayText();
                        onDisplayView();
                        break;
                    }
                    case FAKE_PIN: {
                        mPinLockView.resetPinLockView();
                        onDisplayText();
                        onDisplayView();
                        break;
                    }
                    case CHANGE: {
                        mPinLockView.resetPinLockView();
                        onDisplayText();
                        onDisplayView();
                        break;
                    }
                    case DONE: {
                        Utils.Log(TAG, "Action ...................done");
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                    case VERIFY: {
                        finish();
                        break;
                    }
                }
                break;
            }
            case SET: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case CHANGE: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case RESET: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case RESTORE: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            case FAKE_PIN: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        Navigator.onMoveFakePinComponent(this);
                        finish();
                        break;
                    }
                }
                break;
            }
            case CREATE_FAKE_PIN: {
                mPinAction = action;
                switch (action) {
                    case DONE: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                        finish();
                        break;
                    }
                }
                break;
            }
            default: {
                Utils.Log(TAG, "Nothing to do");
                break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        Utils.Log(TAG, mPinAction.name());
        switch (mPinAction) {
            case VERIFY: {
                int value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action) {
                    case SCREEN_LOCK: {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.STILL_SCREEN_LOCK.ordinal());
                        SingletonMultipleListener.getInstance().notifyListeners(EnumStatus.FINISH);
                        Utils.Log(TAG, "onStillScreenLock");
                    }
                }
                super.onBackPressed();
                break;
            }
            case CHANGE: {
                super.onBackPressed();
                break;
            }
            case DONE: {
                super.onBackPressed();
                break;
            }
            case VERIFY_TO_CHANGE: {
                super.onBackPressed();
                break;
            }
            case RESET: {
                break;
            }
            case SET: {
                switch (mPinActionNext) {
                    case SIGN_UP: {
                        super.onBackPressed();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case FAKE_PIN: {
                finish();
                break;
            }
            default: {
                super.onBackPressed();
                break;
            }
        }
    }

    public void onDisplayView() {
        Utils.Log(TAG, "EnumPinAction 2:...." + mPinAction.name());
        switch (mPinAction) {
            case SET: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case INIT_PREFERENCE: {
                rlLockScreen.setVisibility(View.INVISIBLE);
                rlPreference.setVisibility(View.VISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
            case FAKE_PIN: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }


    public void onDisplayText() {
        Utils.Log(TAG, "EnumPinAction 3:...." + mPinAction.name());
        switch (mPinAction) {
            case VERIFY: {
                mTextTitle.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.VISIBLE);
                imgLauncher.setEnabled(false);
                break;
            }
            case VERIFY_TO_CHANGE: {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE: {
                mTextTitle.setText(getString(R.string.pinlock_confirm_create));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case INIT_PREFERENCE: {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case FAKE_PIN: {
                mTextTitle.setText(getString(R.string.pinlock_confirm_create));
                mTextTitle.setVisibility(View.VISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case SET: {
                mTextTitle.setText(getString(R.string.pinlock_settitle));
                mTextTitle.setVisibility(View.VISIBLE);
                mTextAttempts.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
            case RESET: {
                mTextTitle.setText(getString(R.string.pinlock_settitle));
                mTextTitle.setVisibility(View.VISIBLE);
                mTextAttempts.setVisibility(View.INVISIBLE);
                imgLauncher.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    /*Call back finger print*/

    @Override
    public void onNoFingerPrintHardwareFound() {

    }

    @Override
    public void onNoFingerPrintRegistered() {

    }

    @Override
    public void onBelowMarshmallow() {

    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        boolean isFingerPrintUnLock = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
        isFingerprint = isFingerPrintUnLock;
        if (!isFingerPrintUnLock) {

            return;
        }

        switch (mPinAction) {
            case VERIFY: {
                presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE);
                break;
            }
        }
    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {

    }

    /*Call back end at finger print*/

    public void initActionBar(boolean isInit) {
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


    @OnClick(R.id.imgSwitchTypeUnClock)
    public void onClickedSwitchTypeUnlock(View view){
        if (isFingerprint){
            isFingerprint = false;
        }
        else{
            isFingerprint = true;
        }
        onSetVisitFingerprintView(isFingerprint);
    }

    public void onSetVisitFingerprintView(boolean isFingerprint){
        if (isFingerprint){
            mPinLockView.setVisibility(View.INVISIBLE);
            imgFingerprint.setVisibility(View.VISIBLE);
            rlDots.setVisibility(View.INVISIBLE);
            mTextAttempts.setText(getString(R.string.use_your_fingerprint_to_unlock_supersafe));
            mTextAttempts.setVisibility(View.VISIBLE);
            mTextTitle.setText("");
        }
        else{
            mPinLockView.setVisibility(View.VISIBLE);
            imgFingerprint.setVisibility(View.INVISIBLE);
            rlDots.setVisibility(View.VISIBLE);
            mTextAttempts.setText("");
            mTextTitle.setText(getString(R.string.pinlock_title));
        }
    }


    /*Settings preference*/

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private MyPreference mChangePin;
        private MySwitchPreference mFaceDown;
        private MySwitchPreference mFingerPrint;


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
                    Utils.Log(TAG, "change " + newValue);
                    return true;
                }
            };
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof Preference) {
                        if (preference.getKey().equals(getString(R.string.key_change_pin))) {
                            Utils.Log(TAG, "Action here!!!");
                            presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.VERIFY_TO_CHANGE);
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
            mChangePin = (MyPreference) findPreference(getString(R.string.key_change_pin));
            mChangePin.setOnPreferenceChangeListener(createChangeListener());
            mChangePin.setOnPreferenceClickListener(createActionPreferenceClickListener());


            /*Face down*/

            mFaceDown = (MySwitchPreference) findPreference(getString(R.string.key_face_down_lock));
            boolean switchFaceDown = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false);
            mFaceDown.setOnPreferenceChangeListener(createChangeListener());
            mFaceDown.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mFaceDown.setDefaultValue(switchFaceDown);
            Utils.Log(TAG, "default " + switchFaceDown);

            /*FingerPrint*/
            mFingerPrint = (MySwitchPreference) findPreference(getString(R.string.key_fingerprint_unlock));
            boolean switchFingerPrint = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
            mFingerPrint.setOnPreferenceChangeListener(createChangeListener());
            mFingerPrint.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mFingerPrint.setDefaultValue(switchFingerPrint);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_lock_screen);
        }
    }

    public void onLauncherPreferences() {
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
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                showMessage(getString(R.string.error_not_having_camera));
                break;
        }
    }

    public void onInitHiddenCamera() {
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false);
        if (!value) {
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

    public void onTakePicture(String pin) {
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false);
        if (!value) {
            return;
        }
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance())
                .setPin(pin);
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance()).
                setImageFile(SuperSafeApplication.getInstance().getDefaultStorageFile(CameraImageFormat.FORMAT_JPEG));
        takePicture();
    }

}
