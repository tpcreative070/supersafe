package co.tpcreative.supersafe.ui.facedown;
import android.os.Bundle;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivityNone;
import co.tpcreative.supersafe.common.util.Utils;

public class FaceDownActivity extends BaseActivityNone {
    private static String TAG = FaceDownActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_down);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
        Utils.Log(TAG,"Finish");
    }
}
