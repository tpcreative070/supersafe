package co.tpcreative.supersafe.common;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.GoogleOauth;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivity;
import co.tpcreative.supersafe.ui.askpermission.AskPermissionActivity;
import co.tpcreative.supersafe.ui.camera.CameraActivity;
import co.tpcreative.supersafe.ui.checksystem.CheckSystemActivity;
import co.tpcreative.supersafe.ui.dashboard.DashBoardActivity;
import co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivity;
import co.tpcreative.supersafe.ui.help.HelpActivity;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivity;
import co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivity;
import co.tpcreative.supersafe.ui.player.PlayerActivity;
import co.tpcreative.supersafe.ui.settings.AlbumSettingsActivity;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;
import co.tpcreative.supersafe.ui.signin.SignInActivity;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;
import co.tpcreative.supersafe.ui.signup.SignUpActivity;
import co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity;
import co.tpcreative.supersafe.ui.trash.TrashActivity;
import co.tpcreative.supersafe.ui.verify.VerifyActivity;
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivity;

public class Navigator {

    public static final int PHOTO_SLIDE_SHOW = 100;
    public static final int CAMERA_ACTION = 1001;
    public static final int ALBUM_DETAIL = 1002;


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
        Intent intent = EnterPinActivity.getIntent(context, EnumPinAction.SET.ordinal(),isSignUp);
        context.startActivity(intent);
    }

    public static void onMoveToVerifyPin(Context context,boolean isSignUp){
        Intent intent = EnterPinActivity.getIntent(context,EnumPinAction.VERIFY.ordinal(),isSignUp);
        context.startActivity(intent);
    }

    public static void onMoveToSignUp(Context context){
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveCamera(Activity activity,final MainCategories mainCategories){
        Intent intent = new Intent(activity, CameraActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(activity.getString(R.string.key_main_categories),mainCategories);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent,Navigator.CAMERA_ACTION);
    }

    public static void onMoveToAlbum(Activity activity){
        Intent intent = new Intent(activity, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    public static void onMoveAlbumDetail(Activity context, MainCategories mainCategories){
        Intent intent = new Intent(context,AlbumDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_main_categories),mainCategories);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,ALBUM_DETAIL);
    }

    public static void onPhotoSlider(Activity context, final Items items, final List<Items> mList){
        Intent intent = new Intent(context, PhotoSlideShowActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_items),items);
        bundle.putSerializable(context.getString(R.string.key_list_items),(ArrayList)mList);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,PHOTO_SLIDE_SHOW);
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

    public static void onPlayer(Context context,final Items items){
        Intent intent = new Intent(context, PlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_items),items);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void onMoveTrash(Context context){
        Intent intent = new Intent(context, TrashActivity.class);
        context.startActivity(intent);
    }

    public static void onAlbumSettings(Context context,MainCategories items){
        Intent intent = new Intent(context, AlbumSettingsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_main_categories),items);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}
