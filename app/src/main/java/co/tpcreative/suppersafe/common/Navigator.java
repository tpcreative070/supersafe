package co.tpcreative.suppersafe.common;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.ui.askpermission.AskPermissionActivity;
import co.tpcreative.suppersafe.ui.dashboard.DashBoardActivity;
import co.tpcreative.suppersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.suppersafe.ui.signin.SignInActivity;
import co.tpcreative.suppersafe.ui.main_tab.MainTabActivity;
import co.tpcreative.suppersafe.ui.signup.SignUpActivity;
import co.tpcreative.suppersafe.ui.verify.VerifyActivity;

public class Navigator {

    public static void onMoveToMainTab(Context context){
        Intent intent = new Intent(context, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToLogin(Context context){
        Intent intent = new Intent(context, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToDashBoard(Context context){
        Intent intent = new Intent(context, DashBoardActivity.class);
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

    public static void onMoveSetPin(Context context,boolean isSignUp){
        Intent intent = EnterPinActivity.getIntent(context,true,isSignUp);
        context.startActivity(intent);
    }


    public static void onMoveToVerifyPin(Context context,boolean isSignUp){
        Intent intent = EnterPinActivity.getIntent(context,false,isSignUp);
        context.startActivity(intent);
    }

    public static void onMoveToSignUp(Context context){
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }


}
