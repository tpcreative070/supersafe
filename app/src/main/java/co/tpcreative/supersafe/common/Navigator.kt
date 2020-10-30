package co.tpcreative.supersafe.common
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import co.tpcreative.supersafe.R

import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivity
import co.tpcreative.supersafe.ui.accountmanager.AccountManagerAct
import co.tpcreative.supersafe.ui.albumcover.AlbumCoverAct
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAct
import co.tpcreative.supersafe.ui.askpermission.AskPermissionAct
import co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAct
import co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivity
import co.tpcreative.supersafe.ui.camera.CameraAct
import co.tpcreative.supersafe.ui.checksystem.CheckSystemAct
import co.tpcreative.supersafe.ui.cloudmanager.CloudManagerAct
import co.tpcreative.supersafe.ui.dashboard.DashBoardAct
import co.tpcreative.supersafe.ui.enablecloud.EnableCloudAct
import co.tpcreative.supersafe.ui.facedown.FaceDownActivity
import co.tpcreative.supersafe.ui.fakepin.FakePinAct
import co.tpcreative.supersafe.ui.fakepin.FakePinComponentAct
import co.tpcreative.supersafe.ui.help.HelpAndSupportAct
import co.tpcreative.supersafe.ui.help.HelpAndSupportContentAct
import co.tpcreative.supersafe.ui.enterpin.EnterPinAct
import co.tpcreative.supersafe.ui.main_tab.MainTabAct
import co.tpcreative.supersafe.ui.multiselects.AlbumSelectActivity
import co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowAct
import co.tpcreative.supersafe.ui.player.PlayerAct
import co.tpcreative.supersafe.ui.premium.PremiumAct
import co.tpcreative.supersafe.ui.resetpin.ResetPinAct
import co.tpcreative.supersafe.ui.restore.RestoreAct
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorAct
import co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpAct
import co.tpcreative.supersafe.ui.settings.SettingsAct
import co.tpcreative.supersafe.ui.settings.AlbumSettingsAct
import co.tpcreative.supersafe.ui.signin.SignInAct
import co.tpcreative.supersafe.ui.signup.SignUpAct
import co.tpcreative.supersafe.ui.theme.ThemeSettingsAct
import co.tpcreative.supersafe.ui.trash.TrashAct
import co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumAct
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountAct
import co.tpcreative.supersafe.ui.verify.VerifyAct
import java.util.*

object Navigator {
    const val LIMIT_UPLOAD = 50
    const val PHOTO_SLIDE_SHOW = 100
    const val CAMERA_ACTION = 1001
    const val ALBUM_DETAIL = 1002
    const val THEME_SETTINGS = 1003
    const val VERIFY_PIN = 1004
    const val SECRET_DOOR_SUET_UP = 1005
    const val ENABLE_CLOUD = 1006
    const val REQUEST_CODE_EMAIL = 1007
    const val REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT = 1008
    const val SHARE = 1009
    const val ALBUM_COVER = 1010

    /*Multiple selects*/
    const val PERMISSION_REQUEST_CODE = 1000
    const val PERMISSION_GRANTED = 1001
    const val PERMISSION_DENIED = 1002
    const val REQUEST_CODE = 2000
    const val FETCH_STARTED = 2001
    const val FETCH_COMPLETED = 2002
    const val ERROR = 2005
    const val COMPLETED_RECREATE = 2006
    const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23
    val INTENT_EXTRA_ALBUM: String? = "album"
    val INTENT_EXTRA_IMAGES: String? = "images"
    val INTENT_EXTRA_LIMIT: String? = "limit"
    const val DEFAULT_LIMIT = 20
    var limit = 0
    fun onMoveToMainTab(context: Context?) {
        val intent = Intent(context, MainTabAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun onMoveToFaceDown(context: Context?) {
        val intent = Intent(context, FaceDownActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun onMoveToLogin(context: Context?) {
        val intent = Intent(context, SignInAct::class.java)
        context?.startActivity(intent)
    }

    fun onMoveToDashBoard(context: Context?) {
        val intent = Intent(context, DashBoardAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun onMoveGrantAccess(context: Context?) {
        val intent = Intent(context, AskPermissionAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun onMoveToVerify(context: Context?, user: User?) {
        val intent = Intent(context, VerifyAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context?.getString(R.string.key_data), user)
        intent.putExtras(bundle)
        context?.startActivity(intent)
    }

    fun onMoveSetPin(context: Context, action: EnumPinAction) {
        val intent: Intent? = EnterPinAct.getIntent(context, EnumPinAction.SET.ordinal, action.ordinal)
        context.startActivity(intent)
    }

    fun onMoveToVerifyPin(activity: Activity, action: EnumPinAction) {
        val intent: Intent? = EnterPinAct.getIntent(activity, EnumPinAction.VERIFY.ordinal, action.ordinal)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivityForResult(intent, Navigator.VERIFY_PIN)
    }

    fun onMoveToChangePin(context: Context, action: EnumPinAction) {
        val intent: Intent? = EnterPinAct.getIntent(context, EnumPinAction.INIT_PREFERENCE.ordinal, action.ordinal)
        context.startActivity(intent)
    }

    fun onMoveToFakePin(context: Context, action: EnumPinAction) {
        val intent: Intent? = EnterPinAct.getIntent(context, EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN.ordinal, action.ordinal)
        context.startActivity(intent)
    }

    fun onMoveToResetPin(context: Context, action: EnumPinAction) {
        val intent: Intent? = EnterPinAct.getIntent(context, EnumPinAction.RESET.ordinal, action.ordinal)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun onMoveToSignUp(context: Context) {
        val intent = Intent(context, SignUpAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveCamera(activity: Activity, mainCategories: MainCategoryModel) {
        val intent = Intent(activity, CameraAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(activity.getString(R.string.key_main_categories), mainCategories)
        intent.putExtras(bundle)
        activity.startActivityForResult(intent,CAMERA_ACTION)
    }

    fun onMoveToAlbum(activity: Activity) {
        val intent = Intent(activity, AlbumSelectActivity::class.java)
        intent.putExtra(INTENT_EXTRA_LIMIT, 20)
        activity.startActivityForResult(intent, Navigator.REQUEST_CODE)
    }

    fun onMoveAlbumDetail(context: Activity, mainCategories: MainCategoryModel) {
        val intent = Intent(context, AlbumDetailAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_main_categories), mainCategories)
        intent.putExtras(bundle)
        context.startActivityForResult(intent,ALBUM_DETAIL)
    }

    fun onPhotoSlider(context: Activity, items: ItemModel, mList: MutableList<ItemModel>, mainCategories: MainCategoryModel) {
        val intent = Intent(context, PhotoSlideShowAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_items), items)
        bundle.putSerializable(context.getString(R.string.key_list_items), mList as ArrayList<*>?)
        bundle.putSerializable(context.getString(R.string.key_main_categories), mainCategories)
        intent.putExtras(bundle)
        context.startActivityForResult(intent, PHOTO_SLIDE_SHOW)
    }

    fun onSettings(context: Activity) {
        val intent = Intent(context, SettingsAct::class.java)
        context.startActivityForResult(intent,COMPLETED_RECREATE)
    }

    fun onVerifyAccount(context: Context) {
        val intent = Intent(context, VerifyAccountAct::class.java)
        context.startActivity(intent)
    }

    fun onManagerAccount(context: Context) {
        val intent = Intent(context, AccountManagerAct::class.java)
        context.startActivity(intent)
    }

    fun onEnableCloud(context: Activity) {
        val intent = Intent(context, EnableCloudAct::class.java)
        context.startActivityForResult(intent,ENABLE_CLOUD)
    }

    fun onManagerCloud(context: Context) {
        val intent = Intent(context, CloudManagerAct::class.java)
        context.startActivity(intent)
    }

    fun onCheckSystem(context: Activity, googleOauth: GoogleOauth?) {
        val intent = Intent(context, CheckSystemAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_google_oauth), googleOauth)
        intent.putExtras(bundle)
        context.startActivityForResult(intent, Navigator.ENABLE_CLOUD)
    }

    fun onPlayer(context: Context, items: ItemModel, main: MainCategoryModel) {
        val intent = Intent(context, PlayerAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_items), items)
        bundle.putSerializable(context.getString(R.string.key_main_categories), main)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveTrash(context: Context) {
        val intent = Intent(context, TrashAct::class.java)
        context.startActivity(intent)
    }

    fun onAlbumSettings(context: Context, items: MainCategoryModel) {
        val intent = Intent(context, AlbumSettingsAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_main_categories), items)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveToForgotPin(context: Context, isRestoreFile: Boolean) {
        val intent = Intent(context, ResetPinAct::class.java)
        val bundle = Bundle()
        bundle.putBoolean(ResetPinAct::class.java.getSimpleName(), isRestoreFile)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveThemeSettings(context: Activity) {
        val intent = Intent(context, ThemeSettingsAct::class.java)
        context.startActivityForResult(intent, Navigator.THEME_SETTINGS)
    }

    fun onMoveBreakInAlerts(context: Context) {
        val intent = Intent(context, BreakInAlertsAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveBreakInAlertsDetail(context: Context, inAlerts: BreakInAlertsModel) {
        val intent = Intent(context,BreakInAlertsDetailActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(context.getString(R.string.key_break_in_alert), inAlerts)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveFakePin(context: Context) {
        val intent = Intent(context, FakePinAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveFakePinComponent(context: Context) {
        val intent = Intent(context, FakePinComponentAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun onMoveFakePinComponentInside(context: Context) {
        val intent = Intent(context, FakePinComponentAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveSecretDoor(context: Context) {
        val intent = Intent(context, SecretDoorAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveSecretDoorSetUp(context: Activity) {
        val intent = Intent(context, SecretDoorSetUpAct::class.java)
        context.startActivityForResult(intent, SECRET_DOOR_SUET_UP)
    }

    fun onMoveHelpSupport(context: Context) {
        val intent = Intent(context, HelpAndSupportAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveAboutSuperSafe(context: Context) {
        val intent = Intent(context, AboutSuperSafeActivity::class.java)
        context.startActivity(intent)
    }

    fun onMoveUnlockAllAlbums(context: Context) {
        val intent = Intent(context, UnlockAllAlbumAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveHelpAndSupportContent(context: Context, helpAndSupport: HelpAndSupport) {
        val intent = Intent(context, HelpAndSupportContentAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(HelpAndSupport::class.java.getSimpleName(), helpAndSupport)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveReportProblem(context: Context, helpAndSupport: HelpAndSupport) {
        val intent = Intent(context, HelpAndSupportContentAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val bundle = Bundle()
        bundle.putSerializable(HelpAndSupport::class.java.getSimpleName(), helpAndSupport)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun onMoveToPremium(context: Context) {
        val intent = Intent(context, PremiumAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun onMoveRestore(context: Context) {
        val intent = Intent(context, RestoreAct::class.java)
        context.startActivity(intent)
    }

    fun onMoveAlbumCover(context: Activity, categories: MainCategoryModel) {
        val intent = Intent(context, AlbumCoverAct::class.java)
        val bundle = Bundle()
        bundle.putSerializable(MainCategoryModel::class.java.getSimpleName(), categories)
        intent.putExtras(bundle)
        context.startActivityForResult(intent, ALBUM_COVER)
    }
}