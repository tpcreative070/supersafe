package co.tpcreative.supersafe.common;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.GoogleOauth;
import co.tpcreative.supersafe.model.HelpAndSupport;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivity;
import co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivity;
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivity;
import co.tpcreative.supersafe.ui.askpermission.AskPermissionActivity;
import co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivity;
import co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivity;
import co.tpcreative.supersafe.ui.camera.CameraActivity;
import co.tpcreative.supersafe.ui.checksystem.CheckSystemActivity;
import co.tpcreative.supersafe.ui.dashboard.DashBoardActivity;
import co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivity;
import co.tpcreative.supersafe.ui.facedown.FaceDownActivity;
import co.tpcreative.supersafe.ui.fakepin.FakePinActivity;
import co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivity;
import co.tpcreative.supersafe.ui.help.HelpAndSupportActivity;
import co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivity;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivity;
import co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivity;
import co.tpcreative.supersafe.ui.multiselects.AlbumSelectActivity;
import co.tpcreative.supersafe.ui.player.PlayerActivity;
import co.tpcreative.supersafe.ui.premium.PremiumActivity;
import co.tpcreative.supersafe.ui.resetpin.ResetPinActivity;
import co.tpcreative.supersafe.ui.restore.RestoreActivity;
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivity;
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivity;
import co.tpcreative.supersafe.ui.settings.AlbumSettingsActivity;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;
import co.tpcreative.supersafe.ui.signin.SignInActivity;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;
import co.tpcreative.supersafe.ui.signup.SignUpActivity;
import co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity;
import co.tpcreative.supersafe.ui.theme.ThemeSettingsActivity;
import co.tpcreative.supersafe.ui.trash.TrashActivity;
import co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivity;
import co.tpcreative.supersafe.ui.verify.VerifyActivity;
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivity;

public class Navigator {

    public static final int LIMIT_UPLOAD = 100;

    public static final int PHOTO_SLIDE_SHOW = 100;
    public static final int CAMERA_ACTION = 1001;
    public static final int ALBUM_DETAIL = 1002;
    public static final int THEME_SETTINGS = 1003;
    public static final int VERIFY_PIN = 1004;
    public static final int SECRET_DOOR_SUET_UP = 1005;
    public static final int ENABLE_CLOUD = 1006;
    public static final int REQUEST_CODE_EMAIL = 1007;
    public static final int REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT = 1008;
    public static final int SHARE = 1009;
    public static final int ALBUM_COVER = 1010;

    /*Multiple selects*/

    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int PERMISSION_GRANTED = 1001;
    public static final int PERMISSION_DENIED = 1002;
    public static final int REQUEST_CODE = 2000;
    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int ERROR = 2005;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23;
    public static final String INTENT_EXTRA_ALBUM = "album";
    public static final String INTENT_EXTRA_IMAGES = "images";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final int DEFAULT_LIMIT = 10;
    public static int limit;


    public static void onMoveToMainTab(Context context){
        Intent intent = new Intent(context, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveToFaceDown(Context context){
        Intent intent = new Intent(context, FaceDownActivity.class);
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

    public static void onMoveSetPin(Context context,EnumPinAction action){
        Intent intent = EnterPinActivity.getIntent(context, EnumPinAction.SET.ordinal(),action.ordinal());
        context.startActivity(intent);
    }

    public static void onMoveToVerifyPin(Activity activity,EnumPinAction action){
        Intent intent = EnterPinActivity.getIntent(activity,EnumPinAction.VERIFY.ordinal(),action.ordinal());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivityForResult(intent,VERIFY_PIN);
    }

    public static void onMoveToChangePin(Context context,EnumPinAction action){
        Intent intent = EnterPinActivity.getIntent(context,EnumPinAction.INIT_PREFERENCE.ordinal(),action.ordinal());
        context.startActivity(intent);
    }

    public static void onMoveToFakePin(Context context,EnumPinAction action){
        Intent intent = EnterPinActivity.getIntent(context,EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN.ordinal(),action.ordinal());
        context.startActivity(intent);
    }

    public static void onMoveToResetPin(Context context,EnumPinAction action){
        Intent intent = EnterPinActivity.getIntent(context,EnumPinAction.RESET.ordinal(),action.ordinal());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        intent.putExtra(Navigator.INTENT_EXTRA_LIMIT, 10);
        activity.startActivityForResult(intent, Navigator.REQUEST_CODE);
    }

    public static void onMoveAlbumDetail(Activity context, MainCategories mainCategories){
        Intent intent = new Intent(context,AlbumDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_main_categories),mainCategories);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,ALBUM_DETAIL);
    }

    public static void onPhotoSlider(Activity context, final Items items, final List<Items> mList, MainCategories mainCategories){
        Intent intent = new Intent(context, PhotoSlideShowActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_items),items);
        bundle.putSerializable(context.getString(R.string.key_list_items),(ArrayList)mList);
        bundle.putSerializable(context.getString(R.string.key_main_categories),mainCategories);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,PHOTO_SLIDE_SHOW);
    }

    public static void onSettings(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
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
        context.startActivityForResult(intent,ENABLE_CLOUD);
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
        context.startActivityForResult(intent,ENABLE_CLOUD);
    }

    public static void onPlayer(Context context,final Items items,final MainCategories main){
        Intent intent = new Intent(context, PlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_items),items);
        bundle.putSerializable(context.getString(R.string.key_main_categories),main);
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

    public static void onMoveToForgotPin(Context context,boolean isRestoreFile){
        Intent intent = new Intent(context, ResetPinActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ResetPinActivity.class.getSimpleName(),isRestoreFile);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void onMoveThemeSettings(Activity context){
        Intent intent = new Intent(context, ThemeSettingsActivity.class);
        context.startActivityForResult(intent,THEME_SETTINGS);
    }

    public static void onMoveBreakInAlerts(Context context){
        Intent intent = new Intent(context, BreakInAlertsActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveBreakInAlertsDetail(Context context, BreakInAlerts inAlerts){
        Intent intent = new Intent(context, BreakInAlertsDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_break_in_alert),inAlerts);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void onMoveFakePin(Context context){
        Intent intent = new Intent(context, FakePinActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveFakePinComponent(Context context){
        Intent intent = new Intent(context, FakePinComponentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void onMoveSecretDoor(Context context){
        Intent intent = new Intent(context, SecretDoorActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveSecretDoorSetUp(Activity context){
        Intent intent = new Intent(context, SecretDoorSetUpActivity.class);
        context.startActivityForResult(intent,Navigator.SECRET_DOOR_SUET_UP);
    }

    public static void onMoveHelpSupport(Context context){
        Intent intent = new Intent(context, HelpAndSupportActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveAboutSuperSafe(Context context){
        Intent intent = new Intent(context, AboutSuperSafeActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveUnlockAllAlbums(Context context){
        Intent intent = new Intent(context, UnlockAllAlbumActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveHelpAndSupportContent(Context context, HelpAndSupport helpAndSupport){
        Intent intent = new Intent(context, HelpAndSupportContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(HelpAndSupport.class.getSimpleName(),helpAndSupport);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void onMoveToPremium(Context context){
        Intent intent = new Intent(context, PremiumActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveRestore(Context context){
        Intent intent = new Intent(context, RestoreActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveAlbumCover(Activity context,MainCategories categories){
        Intent intent = new Intent(context, AlbumCoverActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainCategories.class.getSimpleName(),categories);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,ALBUM_COVER);
    }

}
