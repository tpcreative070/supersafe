package co.tpcreative.supersafe.ui.breakinalerts;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import java.io.File;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumStatus;

public class BreakInAlertsDetailActivity extends BaseActivity{

    @BindView(R.id.imgPicture)
    ImageView imageView;
    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400,600)
            .priority(Priority.HIGH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break_in_alerts_detail);
        onDrawOverLay(this);
        Bundle bundle = getIntent().getExtras();
        final BreakInAlerts inAlerts = (BreakInAlerts) bundle.get(getString(R.string.key_break_in_alert));
        if (inAlerts!=null){
            Glide.with(this)
                    .load(new File(inAlerts.fileName))
                    .apply(options).into(imageView);
        }
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
}
