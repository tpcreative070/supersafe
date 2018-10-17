package co.tpcreative.supersafe.ui.premium;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.EnumStatus;

public class PremiumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
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
    }

}
