package co.tpcreative.suppersafe.common;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;

import co.tpcreative.suppersafe.ChooserActivity;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.demo.HomeActivity;
import co.tpcreative.suppersafe.model.GoogleOauth;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.ui.albumdetail.AlbumDetailActivity;
import co.tpcreative.suppersafe.ui.askpermission.AskPermissionActivity;
import co.tpcreative.suppersafe.ui.camera.CameraActivity;
import co.tpcreative.suppersafe.ui.checksystem.CheckSystemActivity;
import co.tpcreative.suppersafe.ui.dashboard.DashBoardActivity;
import co.tpcreative.suppersafe.ui.enablecloud.EnableCloudActivity;
import co.tpcreative.suppersafe.ui.help.HelpActivity;
import co.tpcreative.suppersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.suppersafe.ui.accountmanager.AccountManagerActivity;
import co.tpcreative.suppersafe.ui.cloudmanager.CloudManagerActivity;
import co.tpcreative.suppersafe.ui.settings.SettingsActivity;
import co.tpcreative.suppersafe.ui.signin.SignInActivity;
import co.tpcreative.suppersafe.ui.main_tab.MainTabActivity;
import co.tpcreative.suppersafe.ui.signup.SignUpActivity;
import co.tpcreative.suppersafe.ui.slidepictures.SlidePicturesActivity;
import co.tpcreative.suppersafe.ui.verify.VerifyActivity;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;

public class Navigator {

    public static void onMoveToMainTab(Context context){
        Intent intent = new Intent(context, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToLogin(Context context){
        Intent intent = new Intent(context, SignInActivity.class);
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

    public static void onMoveCamera(Context context){
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToAlbum(Activity activity){
        Intent intent = new Intent(activity, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    public static void onMoveAlbumDetail(Context context){
        Intent intent = new Intent(context, AlbumDetailActivity.class);
        intent.putExtra(AlbumDetailActivity.EXTRA_NAME, "Hello World");
        context.startActivity(intent);
    }

    public static void onSlidePictures(Context context){
        Intent intent = new Intent(context, SlidePicturesActivity.class);
        context.startActivity(intent);
    }

    public static void onSettings(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void onHelp(Context context){
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
    }

    public static void onVerifyAccount(Context context){
        Intent intent = new Intent(context,VerifyAccountActivity.class);
        context.startActivity(intent);
    }

    public static void onHomeActivity(Context context){
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    public static void onChooseActivity(Context context){
        Intent intent = new Intent(context, ChooserActivity.class);
        context.startActivity(intent);
    }

    public static void onManagerAccount(Context context){
        Intent intent = new Intent(context, AccountManagerActivity.class);
        context.startActivity(intent);
    }

    public static void onEnableCloud(Activity context){
        Intent intent = new Intent(context, EnableCloudActivity.class);
        context.startActivityForResult(intent,EnableCloudActivity.ENABLE_CLOUD);
    }

    public static void onManagerCloud(Context context){
        Intent intent = new Intent(context, CloudManagerActivity.class);
        context.startActivity(intent);
    }

    public static void onCheckSystem(Activity context, final GoogleOauth googleOauth){
        Intent intent = new Intent(context, CheckSystemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_google_oauth),googleOauth);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,EnableCloudActivity.ENABLE_CLOUD);
    }

}
