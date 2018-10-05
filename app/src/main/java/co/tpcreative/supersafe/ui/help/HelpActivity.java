package co.tpcreative.supersafe.ui.help;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.EnumStatus;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

}
