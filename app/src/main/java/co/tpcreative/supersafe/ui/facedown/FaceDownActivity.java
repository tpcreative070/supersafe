package co.tpcreative.supersafe.ui.facedown;
import android.os.Bundle;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.EnumStatus;

public class FaceDownActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_down);
        onDrawOverLay(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onNotifier(EnumStatus status) {

    }
}
