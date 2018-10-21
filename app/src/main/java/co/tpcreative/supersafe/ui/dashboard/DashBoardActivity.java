package co.tpcreative.supersafe.ui.dashboard;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class DashBoardActivity extends BaseActivityNoneSlide {

    private static String TAG = DashBoardActivity.class.getSimpleName();
    private boolean isCancel = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Log.d(TAG,"PIN " +SuperSafeApplication.getInstance().readKey());
    }

    @OnClick(R.id.btnLogin)
    public void onClickedLogin(View view){
        Navigator.onMoveToLogin(this);
    }

    @OnClick(R.id.btnSignUp)
    public void onClickedSignUp(View view){
        Navigator.onMoveSetPin(this, EnumPinAction.SIGN_UP);
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

    @Override
    protected void onResume() {
        super.onResume();
        final User mUser = SuperSafeApplication.getInstance().readUseSecret();
        Log.d(TAG,new Gson().toJson(mUser));
        if (mUser!=null){
            onShowDialog();
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onShowDialog(){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.key_restore))
                .theme(Theme.LIGHT)
                .content(getString(R.string.restore_detail))
                .titleColor(getResources().getColor(R.color.black))
                .negativeText(getString(R.string.key_delete))
                .positiveText(getString(R.string.key_restore))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Navigator.onMoveRestore(DashBoardActivity.this);
                        isCancel = false;
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        SuperSafeApplication.getInstance().deleteFolder();
                        SuperSafeApplication.getInstance().initFolder();
                        isCancel = false;
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Utils.Log(TAG,"Dismiss");
                        if (isCancel){
                            finish();
                        }
                    }
                });

        builder.show();
    }



}
