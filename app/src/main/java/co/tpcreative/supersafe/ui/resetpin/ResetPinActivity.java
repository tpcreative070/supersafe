package co.tpcreative.supersafe.ui.resetpin;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.User;

public class ResetPinActivity extends BaseActivity {
    @BindView(R.id.tvStep1)
    TextView tvStep1;
    @BindView(R.id.edtAccessCode)
    EditText edtAccessCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pin);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            String result = String.format(getString(R.string.request_an_access_code),mUser.email);
            tvStep1.setText(result);
        }
    }

}
