package co.tpcreative.supersafe.ui.secretdoor;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;

public class SecretDoorActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{

    private static final String TAG = SecretDoorActivity.class.getSimpleName();
    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.rlScanner)
    RelativeLayout rlScanner;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;
    @BindView(R.id.tvOptionItems)
    TextView tvOptionItems;
    @BindView(R.id.imgIcons)
    ImageView imgIcons;

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
        final boolean options = PrefsController.getBoolean(getString(R.string.key_calculator),false);
        btnSwitch.setChecked(value);
        if (options){
            tvOptionItems.setText(getString(R.string.calculator));
            imgIcons.setImageResource(R.drawable.ic_calculator);
        }
        else {
            tvOptionItems.setText(getString(R.string.virus_scanner));
            imgIcons.setImageResource(R.drawable.baseline_donut_large_white_48);
        }

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSwitch.isChecked()){
                    Navigator.onMoveSecretDoorSetUp(SecretDoorActivity.this);
                }
            }
        });
        tvPremiumDescription.setText(getString(R.string.secret_door));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b){
            PrefsController.putBoolean(getString(R.string.key_secret_door),b);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
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
        final boolean value = PrefsController.getBoolean(getString(R.string.key_secret_door),false);
        final boolean options = PrefsController.getBoolean(getString(R.string.key_calculator),false);

        btnSwitch.setChecked(value);

        if (options){
            tvOptionItems.setText(getString(R.string.calculator));
            imgIcons.setImageResource(R.drawable.ic_calculator);
        }
        else {
            tvOptionItems.setText(getString(R.string.virus_scanner));
            imgIcons.setImageResource(R.drawable.baseline_donut_large_white_48);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @OnClick(R.id.rlScanner)
    public void onClickedOption(View view){
        onChooseOptionItems();
    }

    public void onChooseOptionItems(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .items(R.array.select_option)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        Utils.Log(TAG,"position "+position);
                        switch (position){
                            case 0 :{
                                PrefsController.putBoolean(getString(R.string.key_calculator),false);
                                tvOptionItems.setText(getString(R.string.virus_scanner));
                                imgIcons.setImageResource(R.drawable.baseline_donut_large_white_48);
                                break;
                            }
                            default:{
                                PrefsController.putBoolean(getString(R.string.key_calculator),true);
                                tvOptionItems.setText(getString(R.string.calculator));
                                imgIcons.setImageResource(R.drawable.ic_calculator);
                                break;
                            }
                        }
                    }
                })
                .build();
        dialog.show();
    }

}
