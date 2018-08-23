package co.tpcreative.suppersafe.ui.dashboard;
import android.os.Bundle;
import android.view.View;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;

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


}
