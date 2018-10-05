package co.tpcreative.supersafe.ui.dashboard;
import android.os.Bundle;
import android.view.View;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.model.EnumStatus;

public class DashBoardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
    }

    @OnClick(R.id.btnLogin)
    public void onClickedLogin(View view){
        Navigator.onMoveToLogin(this);
    }

    @OnClick(R.id.btnSignUp)
    public void onClickedSignUp(View view){
        Navigator.onMoveSetPin(this,true);
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
