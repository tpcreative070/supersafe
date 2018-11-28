package co.tpcreative.supersafe.ui.facedown;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import co.tpcreative.supersafe.R;
import spencerstudios.com.bungeelib.Bungee;

public class FaceDownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_down);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
        Bungee.fade(this);
    }
}
