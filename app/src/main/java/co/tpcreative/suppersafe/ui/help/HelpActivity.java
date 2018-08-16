package co.tpcreative.suppersafe.ui.help;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
