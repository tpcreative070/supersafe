package co.tpcreative.supersafe.ui.dashboard;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;

public class DashBoardActivity extends BaseActivityNoneSlide {

    private static String TAG = DashBoardActivity.class.getSimpleName();
    private boolean isCancel = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Utils.Log(TAG,"PIN " +SuperSafeApplication.getInstance().readKey());
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
    protected void onResume() {
        super.onResume();
        final User mUser = SuperSafeApplication.getInstance().readUseSecret();
        Utils.Log(TAG,new Gson().toJson(mUser));
        if (mUser!=null){
            onShowRestore();
        }
    }

    @Override
    protected void onStopListenerAWhile() {
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void onShowRestore(){
        try {
            de.mrapp.android.dialog.MaterialDialog.Builder builder = new de.mrapp.android.dialog.MaterialDialog.Builder(this);
            ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
            builder.setHeaderBackground(themeApp.getAccentColor());
            builder.setTitle(getString(R.string.key_restore));
            builder.setMessage(getString(R.string.restore_detail));
            builder.setCustomHeader(R.layout.custom_header_restore);
            builder.setPadding(40,40,40,0);
            builder.setMargin(60,0,60,0);
            builder.showHeader(true);
            builder.setPositiveButton(getString(R.string.key_restore), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Navigator.onMoveRestore(DashBoardActivity.this);
                    isCancel = false;
                }
            });
            builder.setNegativeButton(getText(R.string.key_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SuperSafeApplication.getInstance().deleteFolder();
                    SuperSafeApplication.getInstance().initFolder();
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();
                    isCancel = false;
                }
            })
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Utils.Log(TAG,"Dismiss");
                    if (isCancel){
                        finish();
                    }
                }
            });
            de.mrapp.android.dialog.MaterialDialog dialog = builder.show();
            builder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positive = dialog.findViewById(android.R.id.button1);
                    Button negative = dialog.findViewById(android.R.id.button2);
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    if (positive!=null && negative!=null && textView!=null){
                        positive.setTextColor(getResources().getColor(themeApp.getAccentColor()));
                        negative.setTextColor(getResources().getColor(themeApp.getAccentColor()));
                        textView.setTextSize(16);
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
