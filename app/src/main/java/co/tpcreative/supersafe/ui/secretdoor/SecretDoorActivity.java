package co.tpcreative.supersafe.ui.secretdoor;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;

public class SecretDoorActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{

    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.rlScanner)
    RelativeLayout rlScanner;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_door);
        onDrawOverLay(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnSwitch.setOnCheckedChangeListener(this);
        final boolean value = PrefsController.getBoolean(getString(R.string.key_secret_door),false);
        btnSwitch.setChecked(value);

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSwitch.isChecked()){
                    Navigator.onMoveSecretDoorSetUp(SecretDoorActivity.this);
                }
                else{
                    rlScanner.setVisibility(View.INVISIBLE);
                }
            }
        });
        tvPremiumDescription.setText(getString(R.string.secret_door));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        final Theme mTheme = Theme.getInstance().getThemeInfo();
        PrefsController.putBoolean(getString(R.string.key_secret_door),b);
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
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(SecretDoorActivity.class.getSimpleName());
        final boolean value = PrefsController.getBoolean(getString(R.string.key_secret_door),false);
        if (value){
            rlScanner.setVisibility(View.VISIBLE);
        }
        else{
            rlScanner.setVisibility(View.INVISIBLE);
        }
        btnSwitch.setChecked(value);
    }

}
