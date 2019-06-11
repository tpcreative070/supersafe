package co.tpcreative.supersafe.ui.lockscreen;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.kratorius.circleprogress.CircleProgressView;
import com.google.gson.Gson;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonMultipleListener;
import co.tpcreative.supersafe.common.controller.SingletonResetPin;
import co.tpcreative.supersafe.common.controller.SingletonScreenLock;
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig;
import co.tpcreative.supersafe.common.hiddencamera.CameraError;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation;
import co.tpcreative.supersafe.common.listener.Listener;
import co.tpcreative.supersafe.common.preference.MyPreference;
import co.tpcreative.supersafe.common.preference.MySwitchPreference;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Calculator;
import co.tpcreative.supersafe.common.util.CalculatorImpl;
import co.tpcreative.supersafe.common.util.Constants;
import co.tpcreative.supersafe.common.util.Formatter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivity;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;
import me.grantland.widget.AutofitHelper;
import spencerstudios.com.bungeelib.Bungee;

public class EnterPinActivity extends BaseVerifyPinActivity implements BaseView<EnumPinAction>,Calculator, FingerPrintAuthCallback, SingletonMultipleListener.Listener,SingletonScreenLock.SingletonScreenLockListener {
    public static final String TAG = EnterPinActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    public static final String EXTRA_SET_PIN = "SET_PIN";
    public static final String EXTRA_ENUM_ACTION = "ENUM_ACTION";
    private static final int PIN_LENGTH = 100;
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
    @BindView(R.id.ic_SuperSafe)
    ImageView ic_SuperSafe;
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
    @BindView(R.id.rlSecretDoor)
    RelativeLayout rlSecretDoor;
    @BindView(R.id.calculator_holder)
    LinearLayout calculator_holder;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.imgFingerprint)
    ImageView imgFingerprint;
    @BindView(R.id.imgSwitchTypeUnClock)
    ImageView imgSwitchTypeUnClock;
    @BindView(R.id.root)
    CoordinatorLayout root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.llLockScreen_1)
    LinearLayout llLockScreen_1;
    @BindView(R.id.rlAttempt)
    RelativeLayout rlAttempt;
    @BindView(R.id.crc_standard)
    CircleProgressView circleProgressView;
    @BindView(R.id.tvAttempt)
    TextView tvAttempt;
    @BindView(R.id.result) TextView mResult;
    @BindView(R.id.formula) TextView mFormula;
    private static CalculatorImpl mCalc;
    private int count = 0;
    private int countAttempt=0;
    private boolean isFingerprint;
    private static EnumPinAction mPinAction;
    private static EnumPinAction enumPinPreviousAction;
    private static EnumPinAction mPinActionNext;
    private String mFirstPin = "";
    private static LockScreenPresenter presenter;
    private CameraConfig mCameraConfig;
    private FingerPrintAuthHelper mFingerPrintAuthHelper;
    public static boolean isVisible ;
    private String mRealPin = Utils.getPinFromSharedPreferences();
    private String mFakePin = Utils.getFakePinFromSharedPreferences();
    private boolean isFakePinEnabled = Utils.isEnabledFakePin();

    public static Intent getIntent(Context context, int action, int actionNext) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_SET_PIN, action);
        intent.putExtra(EXTRA_ENUM_ACTION, actionNext);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpin);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        presenter = new LockScreenPresenter();
        presenter.bindView(this);
        int result = getIntent().getIntExtra(EXTRA_SET_PIN, 0);
        mPinAction = EnumPinAction.values()[result];
        int resultNext = getIntent().getIntExtra(EXTRA_ENUM_ACTION, 0);
        mPinActionNext = EnumPinAction.values()[resultNext];
        SingletonScreenLock.getInstance().setListener(this);
        enumPinPreviousAction = mPinAction;
        switch (mPinAction) {
            case SET: {
                onDisplayView();
                onDisplayText();
                break;
            }
            case VERIFY: {
                if (mRealPin.equals("")) {
                    mPinAction = EnumPinAction.SET;
                    onDisplayView();
                    onDisplayText();
                } else {
                    if (Utils.isSensorAvailable()) {
                        boolean isFingerPrintUnLock = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
                        if (isFingerPrintUnLock) {
                            imgSwitchTypeUnClock.setVisibility(View.VISIBLE);
                            isFingerprint = isFingerPrintUnLock;
                            onSetVisitFingerprintView(isFingerprint);
                            Utils.Log(TAG,"Action find fingerPrint");
                        }
                    }
                    final boolean value = PrefsController.getBoolean(getString(R.string.key_secret_door), false);
                    if (value) {
                        imgSwitchTypeUnClock.setVisibility(View.INVISIBLE);
                        changeLayoutSecretDoor(true);
                    } else {
                        calculator_holder.setVisibility(View.INVISIBLE);
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
        imgLauncher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                changeLayoutSecretDoor(false);
                return false;
            }
        });
        ic_SuperSafe.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                changeLayoutSecretDoor(false);
                return false;
            }
        });

        if (Utils.isSensorAvailable()) {
            mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        }
        onInitPin();
        /*Calculator init*/
        mCalc = new CalculatorImpl(this);
        AutofitHelper.create(mResult);
        AutofitHelper.create(mFormula);
    }

    final PinLockListener pinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            Utils.Log(TAG,"Complete button " +mPinAction.name());
            switch (mPinAction) {
                case SET: {
                    setPin(pin);
                    break;
                }
                case VERIFY: {
                    checkPin(pin, true);
                    break;
                }
                case VERIFY_TO_CHANGE: {
                    checkPin(pin, true);
                    break;
                }
                case VERIFY_TO_CHANGE_FAKE_PIN: {
                    checkPin(pin, true);
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
            Utils.Log(TAG, "Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            switch (mPinAction) {
                case VERIFY: {
                    checkPin(intermediatePin, false);
                    break;
                }
                case VERIFY_TO_CHANGE: {
                    checkPin(intermediatePin, false);
                    break;
                }
                case VERIFY_TO_CHANGE_FAKE_PIN: {
                    checkPin(intermediatePin, false);
                }
                default: {
                    Utils.Log(TAG, "Nothing working!!!");
                    break;
                }
            }
            Utils.Log(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
        }
    };

    @Override
    public void onAttemptTimer(String seconds) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    double response = Double.parseDouble(seconds);
                    Utils.Log(TAG,"Timer  " +"Attempt "+ countAttempt + " Count "+count);
                    double remain = (response/countAttempt) * 100;
                    int result = (int)remain;
                    Utils.Log(TAG,"Result "+result);
                    circleProgressView.setValue(result);
                    circleProgressView.setText(seconds);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onAttemptTimerFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPinAction = enumPinPreviousAction;
                onDisplayView();
                Utils.Log(TAG,"onAttemptTimerFinish");
            }
        });
    }

    @Override
    public void onNotifier(EnumStatus status) {
        switch (status) {
            case FINISH: {
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
        isVisible = false;
        int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
        EnumPinAction action = EnumPinAction.values()[value];
        switch (action){
            case NONE:{
                final User mUser = User.getInstance().getUserInfo();
                if (mUser!=null){
                    ServiceManager.getInstance().onStartService();
                    SingletonResetPin.getInstance().onStop();
                }
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do");
            }
        }
        Utils.Log(TAG,"onDestroy");
    }

    @OnClick(R.id.btnDone)
    public void onClickedDone() {
        onBackPressed();
    }

    public void onDelete(View view) {
        Utils.Log(TAG, "onDelete here");
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
        isVisible = true;
        Utils.Log(TAG,"onResume");
        if (mPinLockView != null) {
            mPinLockView.resetPinLockView();
        }
        onSetVisitableForgotPin(View.GONE);
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.startAuth();
        }
        mRealPin = Utils.getPinFromSharedPreferences();
        mFakePin = Utils.getFakePinFromSharedPreferences();
        isFakePinEnabled = Utils.isEnabledFakePin();
    }

    public void onInitPin(){
        mIndicatorDots.setActivity(this);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(pinLockListener);
        mPinLockView.setPinLength(PIN_LENGTH);
        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        onInitHiddenCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.stopAuth();
        }
    }

    private void setPin(String pin) {
        switch (mPinAction) {
            case SET: {
                if (mFirstPin.equals("")) {
                    mFirstPin = pin;
                    mTextTitle.setText(getString(R.string.pinlock_secondPin));
                    mPinLockView.resetPinLockView();
                }
                else {
                    if (pin.equals(mFirstPin)) {
                        Utils.writePinToSharedPreferences(pin);
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
                        if (Utils.isExistingFakePin(pin,mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace));
                        } else {
                            Utils.writePinToSharedPreferences(pin);
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
                        if (Utils.isExistingRealPin(pin,mRealPin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace));
                        } else {
                            Utils.writeFakePinToSharedPreferences(pin);
                            presenter.onChangeStatus(EnumStatus.CREATE_FAKE_PIN, EnumPinAction.DONE);
                        }
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
                        if (Utils.isExistingFakePin(pin,mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace));
                        } else {
                            switch (mPinActionNext) {
                                case RESTORE: {
                                    Utils.writePinToSharedPreferences(pin);
                                    onRestore();
                                    break;
                                }
                                default: {
                                    Utils.writePinToSharedPreferences(pin);
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

    private void checkPin(String pin, boolean isCompleted) {
        switch (mPinAction) {
            case VERIFY: {
                if (pin.equals(mRealPin)) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE);
                } else if (pin.equals(mFakePin) && isFakePinEnabled) {
                    presenter.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE);
                }else {
                    if (isCompleted) {
                        onTakePicture(pin);
                        onAlertWarning("");
                    }
                }
                break;
            }
            case VERIFY_TO_CHANGE: {
                if (pin.equals(mRealPin)) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.CHANGE);
                } else {
                    if (isCompleted) {
                        onTakePicture(pin);
                        onAlertWarning("");
                    }
                }
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                if (pin.equals(mRealPin)) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.FAKE_PIN);
                }else {
                    if (isCompleted) {
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
                onSetVisitableForgotPin(View.VISIBLE);
                if (count >= 3) {
                    countAttempt = count*10;
                    final long attemptWaiting = count * 10000;
                    mPinAction = EnumPinAction.ATTEMPT;
                    onDisplayView();
                    SingletonScreenLock.getInstance().onStartTimer(attemptWaiting);
                }
                break;
            }
            case VERIFY_TO_CHANGE: {
                count += 1;
                onSetVisitableForgotPin(View.VISIBLE);
                if (count >= 3) {
                    countAttempt = count*10;
                    final long attemptWaiting = count * 10000;
                    mPinAction = EnumPinAction.ATTEMPT;
                    onDisplayView();
                    SingletonScreenLock.getInstance().onStartTimer(attemptWaiting);
                }
                break;
            }
        }
        Utils.Log(TAG, "Visit...." + count);
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
            case VERIFY: {
                shake();
                mTextTitle.setText(title);
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
                break;
            }
            case VERIFY_TO_CHANGE: {
                shake();
                mTextTitle.setText(title);
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
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

            final boolean options = PrefsController.getBoolean(getString(R.string.key_calculator),false);
            if (options){
                imgLauncher.setVisibility(View.INVISIBLE);
                rlSecretDoor.setVisibility(View.INVISIBLE);
                calculator_holder.setVisibility(View.VISIBLE);
            }
            else {
                imgLauncher.setVisibility(View.VISIBLE);
                rlSecretDoor.setVisibility(View.VISIBLE);
                calculator_holder.setVisibility(View.INVISIBLE);
            }
        } else {
            mTextTitle.setVisibility(View.VISIBLE);
            rlButton.setVisibility(View.VISIBLE);
            rlDots.setVisibility(View.VISIBLE);
            mTextAttempts.setVisibility(View.VISIBLE);
            mTextAttempts.setText("");
            imgLauncher.setVisibility(View.INVISIBLE);
            rlSecretDoor.setVisibility(View.INVISIBLE);
            calculator_holder.setVisibility(View.INVISIBLE);
            if (Utils.isSensorAvailable()) {
                boolean isFingerPrintUnLock = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false);
                if (isFingerPrintUnLock) {
                    imgSwitchTypeUnClock.setVisibility(View.VISIBLE);
                    isFingerprint = isFingerPrintUnLock;
                    onSetVisitFingerprintView(isFingerprint);
                }
                else{
                    imgSwitchTypeUnClock.setVisibility(View.GONE);
                }
            }
            else{
                imgSwitchTypeUnClock.setVisibility(View.GONE);
            }
        }
    }

    public void onHideUI(){
        rlLockScreen.setVisibility(View.GONE);
        llLockScreen_1.setVisibility(View.GONE);
        rlSecretDoor.setVisibility(View.GONE);
        calculator_holder.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        rlPreference.setVisibility(View.GONE);
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, EnumPinAction action) {
        Utils.Log(TAG, "EnumPinAction 1:...." + action.name());
        switch (status) {
            case VERIFY: {
                mTextAttempts.setText("");
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
                        EventBus.getDefault().post(EnumStatus.UNLOCK);
                        Utils.onObserveData(100, new Listener() {
                            @Override
                            public void onStart() {
                                onBackPressed();
                            }
                        });
                        Utils.Log(TAG, "Action ...................done");
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
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
                        Utils.onObserveData(100, new Listener() {
                            @Override
                            public void onStart() {
                                PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                                if (FakePinComponentActivity.isVisit){
                                    finish();
                                }
                                else{
                                    Navigator.onMoveFakePinComponent(EnterPinActivity.this);
                                }
                            }
                        });
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
                        EventBus.getDefault().post(EnumStatus.FINISH);
                        Utils.Log(TAG, "onStillScreenLock ???");
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
                Bungee.fade(this);
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
            case ATTEMPT:{
                break;
            }
            default: {
                super.onBackPressed();
                Utils.Log(TAG,"onBackPressed");
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
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case CHANGE: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case INIT_PREFERENCE: {
                rlLockScreen.setVisibility(View.INVISIBLE);
                rlPreference.setVisibility(View.VISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case VERIFY_TO_CHANGE_FAKE_PIN: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case FAKE_PIN: {
                rlLockScreen.setVisibility(View.VISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.INVISIBLE);
                break;
            }
            case ATTEMPT:{
                rlLockScreen.setVisibility(View.INVISIBLE);
                rlPreference.setVisibility(View.INVISIBLE);
                rlAttempt.setVisibility(View.VISIBLE);
                String result = String.format(getString(R.string.in_correct_pin),count+"",countAttempt+"");
                tvAttempt.setText(result);
                Utils.Log(TAG,mPinAction.name());
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
    public void onClickedSwitchTypeUnlock(View view) {
        if (isFingerprint) {
            isFingerprint = false;
        } else {
            isFingerprint = true;
        }
        onSetVisitFingerprintView(isFingerprint);
    }
    public void onSetVisitFingerprintView(boolean isFingerprint) {
        if (isFingerprint) {
            mPinLockView.setVisibility(View.INVISIBLE);
            imgFingerprint.setVisibility(View.VISIBLE);
            rlDots.setVisibility(View.INVISIBLE);
            mTextAttempts.setText(getString(R.string.use_your_fingerprint_to_unlock_supersafe));
            mTextAttempts.setVisibility(View.VISIBLE);
            mTextTitle.setText("");
        } else {
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
                            enumPinPreviousAction = EnumPinAction.VERIFY_TO_CHANGE;
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

    /*Calculator action*/
    @Override
    public void setValue(String value) {
        mResult.setText(value);
    }
    // used only by Robolectric
    @Override
    public void setValueDouble(double d) {
        mCalc.setValue(Formatter.doubleToString(d));
        mCalc.setLastKey(Constants.DIGIT);
    }
    public void setFormula(String value) {
        mFormula.setText(value);
    }
    @OnClick(R.id.btn_plus)
    public void plusClicked() {
        mCalc.handleOperation(Constants.PLUS);
    }
    @OnClick(R.id.btn_minus)
    public void minusClicked() {
        mCalc.handleOperation(Constants.MINUS);
    }
    @OnClick(R.id.btn_multiply)
    public void multiplyClicked() {
        mCalc.handleOperation(Constants.MULTIPLY);
    }
    @OnClick(R.id.btn_divide)
    public void divideClicked() {
        mCalc.handleOperation(Constants.DIVIDE);
    }
    @OnClick(R.id.btn_modulo)
    public void moduloClicked() {
        mCalc.handleOperation(Constants.MODULO);
    }
    @OnClick(R.id.btn_power)
    public void powerClicked() {
        mCalc.handleOperation(Constants.POWER);
    }
    @OnClick(R.id.btn_root)
    public void rootClicked() {
        mCalc.handleOperation(Constants.ROOT);
    }
    @OnClick(R.id.btn_clear)
    public void clearClicked() {
        mCalc.handleClear();
    }
    @OnLongClick(R.id.btn_clear)
    public boolean clearLongClicked() {
        mCalc.handleReset();
        return true;
    }
    @OnClick(R.id.btn_equals)
    public void equalsClicked() {
        mCalc.handleEquals();
    }
    @OnClick({R.id.btn_decimal, R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8,
            R.id.btn_9})
    public void numpadClick(View view) {
        numpadClicked(view.getId());
    }
    public void numpadClicked(int id) {
        mCalc.numpadClicked(id);
    }
}
