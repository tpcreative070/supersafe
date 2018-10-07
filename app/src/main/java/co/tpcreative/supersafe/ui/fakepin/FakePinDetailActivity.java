package co.tpcreative.supersafe.ui.fakepin;
import android.os.Bundle;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;

public class FakePinDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_pin_detail);
        onDrawOverLay(this);
    }

}
