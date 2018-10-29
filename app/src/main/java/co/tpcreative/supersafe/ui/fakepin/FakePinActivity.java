package co.tpcreative.supersafe.ui.fakepin;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.ui.resetpin.ResetPinActivity;

public class FakePinActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{

    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.tvCreatePin)
    TextView tvCreatePin;
    @BindView(R.id.llView)
    LinearLayout llView;
    @BindView(R.id.imgView)
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_pin);
        onDrawOverLay(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnSwitch.setOnCheckedChangeListener(this);
        final boolean value = PrefsController.getBoolean(getString(R.string.key_fake_pin),false);
        btnSwitch.setChecked(value);
        tvCreatePin.setEnabled(value);
        final String fakePin = SuperSafeApplication.getInstance().readFakeKey();
        if (fakePin.equals("")){
            tvCreatePin.setText(getText(R.string.create_fake_pin));
        }
        else{
            tvCreatePin.setText(getText(R.string.change_fake_pin));
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
        onFaceDown(isFaceDown);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        final Theme mTheme = Theme.getInstance().getThemeInfo();
        PrefsController.putBoolean(getString(R.string.key_fake_pin),b);
        if (b){
            tvCreatePin.setTextColor(getResources().getColor(mTheme.getPrimaryColor()));
            tvCreatePin.setEnabled(b);
            //imgView.setColorFilter(getResources().getColor(mTheme.getPrimaryColor()), PorterDuff.Mode.SRC_ATOP);
        }
        else{
            tvCreatePin.setTextColor(getResources().getColor(R.color.material_gray_500));
            tvCreatePin.setEnabled(b);
            //imgView.setColorFilter(getResources().getColor(R.color.material_gray_500), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @OnClick(R.id.tvCreatePin)
    public void onCreatePin(View view){
        Navigator.onMoveToFakePin(this, EnumPinAction.NONE);
    }

    @OnClick(R.id.imgView)
    public void onViewComponent(View view){
        Navigator.onMoveFakePinComponent(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(FakePinActivity.class.getSimpleName());
    }

}
