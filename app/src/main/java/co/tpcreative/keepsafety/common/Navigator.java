package co.tpcreative.keepsafety.common;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import co.tpcreative.keepsafety.R;
import co.tpcreative.keepsafety.model.User;
import co.tpcreative.keepsafety.ui.askpermission.AskPermissionActivity;
import co.tpcreative.keepsafety.ui.lockscreen.EnterPinActivity;
import co.tpcreative.keepsafety.ui.login.LoginActivity;
import co.tpcreative.keepsafety.ui.main_tab.MainTabActivity;
import co.tpcreative.keepsafety.ui.verify.VerifyActivity;

public class Navigator {

    public static void onMoveToMainTab(Context context){
        Intent intent = new Intent(context, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToLogin(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveGrantAccess(Context context){
        Intent intent = new Intent(context, AskPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToVerify(Context context, User user){
        Intent intent = new Intent(context, VerifyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_data),user);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void onMoveSetPin(Context context){
        Intent intent = EnterPinActivity.getIntent(context,true);
        context.startActivity(intent);
    }

    public static void onMoveToVerifyPin(Context context){
        Intent intent = EnterPinActivity.getIntent(context,false);
        context.startActivity(intent);
    }


}
