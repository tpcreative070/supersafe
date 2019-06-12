package co.tpcreative.supersafe.ui.secretdoor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Calculator;
import co.tpcreative.supersafe.common.util.CalculatorImpl;
import co.tpcreative.supersafe.common.util.Constants;
import co.tpcreative.supersafe.common.util.Formatter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import me.grantland.widget.AutofitHelper;


public class SecretDoorSetUpActivity extends BaseActivity implements Calculator {

    private static final String TAG = SecretDoorSetUpActivity.class.getSimpleName();
    @BindView(R.id.imgLauncher)
    ImageView imgLauncher;
    @BindView(R.id.ic_SuperSafe)
    ImageView ic_SuperSafe;
    @BindView(R.id.rlSecretDoor)
    RelativeLayout relativeLayout;
    @BindView(R.id.calculator_holder)
    LinearLayout calculator_holder;
    @BindView(R.id.result)
    TextView mResult;
    @BindView(R.id.formula) TextView mFormula;
    private static CalculatorImpl mCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_door_set_up);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        mCalc = new CalculatorImpl(this);
        AutofitHelper.create(mResult);
        AutofitHelper.create(mFormula);

        final boolean options = PrefsController.getBoolean(getString(R.string.key_calculator),false);
        if (options){
            calculator_holder.setVisibility(View.VISIBLE);
            imgLauncher.setVisibility(View.INVISIBLE);
            relativeLayout.setVisibility(View.INVISIBLE);
            final SpannableString spannedDesc = new SpannableString(getString(R.string.long_press_the_log));
            TapTargetView.showFor(this, TapTarget.forView(ic_SuperSafe, getString(R.string.try_it_now), spannedDesc)
                    .cancelable(false)
                    .titleTextDimen(R.dimen.text_size_title)
                    .titleTypeface(Typeface.DEFAULT_BOLD)
                    .tintTarget(true), new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    // .. which evidently starts the sequence we defined earlier
                }

                @Override
                public void onTargetLongClick(TapTargetView view) {
                    super.onTargetLongClick(view);
                    onShowDialog();
                }

                @Override
                public void onOuterCircleClick(TapTargetView view) {
                    super.onOuterCircleClick(view);
                }
                @Override
                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                    Utils.Log("TapTargetViewSample", "You dismissed me :(");
                }
            });
        }
        else {
            calculator_holder.setVisibility(View.INVISIBLE);
            imgLauncher.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
            final SpannableString spannedDesc = new SpannableString(getString(R.string.long_press_the_log));
            TapTargetView.showFor(this, TapTarget.forView(imgLauncher, getString(R.string.try_it_now), spannedDesc)
                    .cancelable(false)
                    .titleTextDimen(R.dimen.text_size_title)
                    .titleTypeface(Typeface.DEFAULT_BOLD)
                    .tintTarget(true), new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    // .. which evidently starts the sequence we defined earlier
                }

                @Override
                public void onTargetLongClick(TapTargetView view) {
                    super.onTargetLongClick(view);
                    onShowDialog();
                }

                @Override
                public void onOuterCircleClick(TapTargetView view) {
                    super.onOuterCircleClick(view);
                }
                @Override
                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                    Utils.Log("TapTargetViewSample", "You dismissed me :(");
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                finish();
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    public void onShowDialog(){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.enable_secret_door))
                .content(getString(R.string.enable_secret_door_detail))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door),true);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door),false);
                        onBackPressed();
                    }
                });
        builder.show();
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

    @Override
    public void setValue(String value) {
        mResult.setText(value);
    }

    @Override
    public void setValueDouble(double d) {
        mCalc.setValue(Formatter.doubleToString(d));
        mCalc.setLastKey(Constants.DIGIT);
    }

    public void setFormula(String value) {
        mFormula.setText(value);
    }

}
