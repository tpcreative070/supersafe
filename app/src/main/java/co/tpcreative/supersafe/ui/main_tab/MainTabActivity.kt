package co.tpcreative.supersafe.ui.main_tabimport

import android.Manifest
import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.Categories
import co.tpcreative.supersafe.model.Image
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.User
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.karumi.dexter.listener.PermissionRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class MainTabActivity : BaseGoogleApi(), BaseView<Any?> {
    @BindView(R.id.speedDial)
    var mSpeedDialView: SpeedDialView? = null

    @BindView(R.id.viewpager)
    var viewPager: ViewPager? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.tabs)
    var tabLayout: TabLayout? = null

    @BindView(R.id.rlOverLay)
    var rlOverLay: RelativeLayout? = null

    @BindView(R.id.viewFloatingButton)
    var viewFloatingButton: View? = null
    private var adapter: MainViewPagerAdapter? = null
    private var presenter: MainTabPresenter? = null
    var animation: FramesSequenceAnimation? = null
    private var menuItem: MenuItem? = null
    private var previousStatus: EnumStatus? = null
    private val mInterstitialAd: InterstitialAd? = null
    private var mCountToRate = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        initSpeedDial(true)
        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.main_tab)
        val ab: ActionBar = getSupportActionBar()
        ab.setHomeAsUpIndicator(R.drawable.baseline_account_circle_white_24)
        ab.setDisplayHomeAsUpEnabled(true)
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
        PrefsController.putBoolean(getString(R.string.key_running), true)
        presenter = MainTabPresenter()
        presenter.bindView(this)
        presenter.onGetUserInfo()
        onShowSuggestion()
        if (Utils.Companion.isCheckSyncSuggestion()) {
            onSuggestionSyncData()
        }
        if (presenter.mUser != null) {
            if (presenter.mUser.driveConnected) {
                if (NetworkUtil.pingIpAddress(this)) {
                    return
                }
                Utils.Companion.onObserveData(2000, Listener { onAnimationIcon(EnumStatus.DONE) })
            }
        }
        Utils.Companion.Log(TAG, "system access token : " + Utils.Companion.getAccessToken())
    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show()
        }
    }

    fun onShowSuggestion() {
        val isFirstFile: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_files), false)
        if (!isFirstFile) {
            val mList: MutableList<ItemModel?> = SQLHelper.getListAllItems(false)
            if (mList != null && mList.size > 0) {
                PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                return
            }
            viewFloatingButton.setVisibility(View.VISIBLE)
            onSuggestionAddFiles()
        } else {
            val isFirstEnableSyncData: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_enable_sync_data), false)
            if (!isFirstEnableSyncData) {
                if (presenter.mUser.driveConnected) {
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                }
                onSuggestionSyncData()
            }
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        Utils.Companion.Log(TAG, "onOrientationChange")
        onFaceDown(isFaceDown)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REGISTER_OR_LOGIN -> {
                rlOverLay.setVisibility(View.INVISIBLE)
            }
            EnumStatus.UNLOCK -> {
                rlOverLay.setVisibility(View.INVISIBLE)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.PRIVATE_DONE -> {
                if (mSpeedDialView != null) {
                    mSpeedDialView.show()
                }
            }
            EnumStatus.DOWNLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.UPLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.DONE -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.SYNC_ERROR -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.REQUEST_ACCESS_TOKEN -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "Request token")
                    getAccessToken()
                })
            }
            EnumStatus.SHOW_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { mSpeedDialView.show() })
            }
            EnumStatus.HIDE_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { mSpeedDialView.hide() })
            }
            EnumStatus.CONNECTED -> {
                getAccessToken()
            }
            EnumStatus.NO_SPACE_LEFT -> {
                onAlert(getString(R.string.key_no_space_left_space))
            }
            EnumStatus.NO_SPACE_LEFT_CLOUD -> {
                onAlert(getString(R.string.key_no_space_left_space_cloud))
            }
        }
    }

    fun onAlert(content: String?) {
        MaterialDialog.Builder(this).title("Alert")
                .content(content)
                .positiveText("Ok")
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {}
                })
                .show()
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onCallLockScreen()
        onRegisterHomeWatcher()
        presenter.onGetUserInfo()
        ServiceManager.Companion.getInstance().setRequestShareIntent(false)
        Utils.Companion.Log(TAG, "onResume")
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "OnDestroy")
        Utils.Companion.onUpdatedCountRate()
        EventBus.getDefault().unregister(this)
        PrefsController.putBoolean(getString(R.string.second_loads), true)
        if (SingletonManager.Companion.getInstance().isReloadMainTab()) {
            SingletonManager.Companion.getInstance().setReloadMainTab(false)
        } else {
            ServiceManager.Companion.getInstance().onDismissServices()
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                Utils.Companion.Log(TAG, "Home action")
                if (presenter != null) {
                    if (presenter.mUser.verified) {
                        Navigator.onManagerAccount(getActivity())
                    } else {
                        Navigator.onVerifyAccount(getActivity())
                    }
                }
                return true
            }
            R.id.action_sync -> {
                onEnableSyncData()
                return true
            }
            R.id.settings -> {
                Navigator.onSettings(this)
                return true
            }
            R.id.help -> {
                Navigator.onMoveHelpSupport(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        viewPager.setOffscreenPageLimit(3)
        adapter = MainViewPagerAdapter(getSupportFragmentManager())
        viewPager.setAdapter(adapter)
    }

    private fun initSpeedDial(addActionItems: Boolean) {
        Utils.Companion.Log(TAG, "Init floating button")
        val mThemeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
        if (addActionItems) {
            var drawable: Drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.camera))
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.album))
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            mSpeedDialView.show()
        }

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                return false // True to keep the Speed Dial open
            }

            override fun onToggleChanged(isOpen: Boolean) {
                //mSpeedDialView.setMainFabOpenedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                //mSpeedDialView.setMainFabClosedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                Utils.Companion.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
            }
        })

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(object : OnActionSelectedListener {
            override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
                when (actionItem.getId()) {
                    R.id.fab_album -> {
                        onShowDialog()
                        return false // false will close it without animation
                    }
                    R.id.fab_photo -> {
                        Navigator.onMoveToAlbum(this@MainTabActivity)
                        return false // closes without animation (same as mSpeedDialView.close(false); return false;)
                    }
                    R.id.fab_camera -> {
                        onAddPermissionCamera()
                        return false
                    }
                }
                return true // To keep the Speed Dial open
            }
        })
    }

    fun onShowDialog() {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(ContextCompat.getColor(this, R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, object : InputCallback {
                    override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                        Utils.Companion.Log(TAG, "Value")
                        val value = input.toString()
                        val base64Code: String = Utils.Companion.getHexCode(value)
                        val item: MainCategoryModel = SQLHelper.getTrashItem()
                        val result: String = item.categories_hex_name
                        if (base64Code == result) {
                            Toast.makeText(this@MainTabActivity, "This name already existing", Toast.LENGTH_SHORT).show()
                        } else {
                            val response: Boolean = SQLHelper.onAddCategories(base64Code, value, false)
                            if (response) {
                                Toast.makeText(this@MainTabActivity, "Created album successful", Toast.LENGTH_SHORT).show()
                                ServiceManager.Companion.getInstance().onPreparingSyncCategoryData()
                            } else {
                                Toast.makeText(this@MainTabActivity, "Album name already existing", Toast.LENGTH_SHORT).show()
                            }
                            SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                        }
                    }
                })
        builder.show()
    }

    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            val list: MutableList<MainCategoryModel?> = SQLHelper.getList()
                            if (list != null) {
                                Navigator.onMoveCamera(this@MainTabActivity, list[0])
                            }
                        } else {
                            Utils.Companion.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Companion.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Companion.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Companion.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.COMPLETED_RECREATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    SingletonManager.Companion.getInstance().setReloadMainTab(true)
                    Navigator.onMoveToMainTab(this)
                    Utils.Companion.Log(TAG, "New Activity")
                } else {
                    Utils.Companion.Log(TAG, "Nothing Updated theme")
                }
            }
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val images: ArrayList<Image?> = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    val mListImportFiles: MutableList<ImportFilesModel?> = ArrayList<ImportFilesModel?>()
                    var i = 0
                    val l = images.size
                    while (i < l) {
                        val path = images[i].path
                        val name = images[i].name
                        val id = "" + images[i].id
                        val mimeType: String = Utils.Companion.getMimeType(path)
                        Utils.Companion.Log(TAG, "mimeType $mimeType")
                        Utils.Companion.Log(TAG, "name $name")
                        Utils.Companion.Log(TAG, "path $path")
                        val fileExtension: String = Utils.Companion.getFileExtension(path)
                        Utils.Companion.Log(TAG, "file extension " + Utils.Companion.getFileExtension(path))
                        try {
                            val mimeTypeFile: MimeTypeFile = Utils.Companion.mediaTypeSupport().get(fileExtension)
                                    ?: return
                            mimeTypeFile.name = name
                            val list: MutableList<MainCategoryModel?> = SQLHelper.getList()
                            if (list == null) {
                                Utils.Companion.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val mCategory: MainCategoryModel? = list[0]
                            Utils.Companion.Log(TAG, "Show category " + Gson().toJson(mCategory))
                            val importFiles = ImportFilesModel(list[0], mimeTypeFile, path, i, false)
                            mListImportFiles.add(importFiles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }
                    ServiceManager.Companion.getInstance().setListImport(mListImportFiles)
                    ServiceManager.Companion.getInstance().onPreparingImportData()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Gallery")
                }
            }
            else -> {
                Utils.Companion.Log(TAG, "Nothing to do")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (toolbar == null) {
            return false
        }
        toolbar.inflateMenu(R.menu.main_tab)
        menuItem = toolbar.getMenu().getItem(0)
        return true
    }

    fun getMenuItem(): MenuItem? {
        return menuItem
    }

    protected override fun onPause() {
        super.onPause()
        val user: User = Utils.Companion.getUserInfo()
        if (user != null) {
            if (!user.driveConnected) {
                onAnimationIcon(EnumStatus.SYNC_ERROR)
            }
        }
    }

    override fun onBackPressed() {
        if (mSpeedDialView.isOpen()) {
            mSpeedDialView.close()
        } else {
            PremiumManager.Companion.getInstance().onStop()
            Utils.Companion.onDeleteTemporaryFile()
            Utils.Companion.onExportAndImportFile(SuperSafeApplication.Companion.getInstance().getSupersafeDataBaseFolder(), SuperSafeApplication.Companion.getInstance().getSupersafeBackup(), object : ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    Utils.Companion.Log(TAG, "Exporting successful")
                }

                override fun onError() {
                    Utils.Companion.Log(TAG, "Exporting error")
                }

                override fun onCancel() {}
            })
            SuperSafeApplication.Companion.getInstance().writeUserSecret(presenter.mUser)
            val isPressed: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team), false)
            if (isPressed) {
                super.onBackPressed()
            } else {
                val isSecondLoad: Boolean = PrefsController.getBoolean(getString(R.string.second_loads), false)
                if (isSecondLoad) {
                    val isPositive: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive), false)
                    mCountToRate = PrefsController.getInt(getString(R.string.key_count_to_rate), 0)
                    if (!isPositive && mCountToRate > Utils.Companion.COUNT_RATE) {
                        onAskingRateApp()
                    } else {
                        super.onBackPressed()
                    }
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    fun onAnimationIcon(status: EnumStatus?) {
        Utils.Companion.Log(TAG, "value : " + status.name)
        if (getMenuItem() == null) {
            Utils.Companion.Log(TAG, "Menu is nulll")
            return
        }
        if (previousStatus == status && status == EnumStatus.DOWNLOAD) {
            Utils.Companion.Log(TAG, "Action here 1")
            return
        }
        if (previousStatus == status && status == EnumStatus.UPLOAD) {
            Utils.Companion.Log(TAG, "Action here 2")
            return
        }
        val item = getMenuItem()
        if (animation != null) {
            animation.stop()
        }
        Utils.Companion.Log(TAG, "Calling AnimationsContainer........................")
        Utils.Companion.onWriteLog("Calling AnimationsContainer", EnumStatus.CREATE)
        previousStatus = status
        animation = AnimationsContainer.Companion.getInstance().createSplashAnim(item, status)
        animation.start()
    }

    /*MainTab View*/
    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    protected override fun onDriveClientReady() {}
    protected override fun isSignIn(): Boolean {
        return false
    }

    protected override fun onDriveSuccessful() {
        onCheckRequestSignOut()
    }

    protected override fun onDriveError() {}
    protected override fun onDriveSignOut() {}
    protected override fun onDriveRevokeAccess() {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    protected override fun startServiceNow() {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}
    fun onSuggestionAddFiles() {
        TapTargetView.showFor(this,  // `this` is an Activity
                TapTarget.forView(viewFloatingButton, getString(R.string.tap_here_to_add_items), getString(R.string.tap_here_to_add_items_description))
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextColor(R.color.md_light_blue_200)
                        .descriptionTextSize(17)
                        .outerCircleColor(R.color.colorPrimary)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .cancelable(true)
                        .transparentTarget(true)
                        .dimColor(R.color.transparent),
                object : TapTargetView.Listener() {
                    // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view) // This call is optional
                        mSpeedDialView.open()
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onTargetCancel")
                    }
                })
    }

    fun onSuggestionSyncData() {
        TapTargetView.showFor(this,  // `this` is an Activity
                TapTarget.forToolbarMenuItem(toolbar, R.id.action_sync, getString(R.string.tap_here_to_enable_sync_data), getString(R.string.tap_here_to_enable_sync_data_description))
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextColor(R.color.colorPrimary)
                        .descriptionTextSize(17)
                        .outerCircleColor(R.color.colorButton)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .cancelable(true)
                        .dimColor(R.color.white),
                object : TapTargetView.Listener() {
                    // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view) // This call is optional
                        onEnableSyncData()
                        view.dismiss(true)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        Utils.Companion.Log(TAG, "onTargetClick")
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onTargetCancel")
                    }
                })
    }

    fun onEnableSyncData() {
        val mUser: User = Utils.Companion.getUserInfo()
        if (mUser != null) {
            if (mUser.verified) {
                if (!mUser.driveConnected) {
                    Navigator.onCheckSystem(this, null)
                } else {
                    Navigator.onManagerCloud(this)
                }
            } else {
                Navigator.onVerifyAccount(this)
            }
        }
    }

    fun onAskingRateApp() {
        val inflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.custom_view_rate_app_dialog, null)
        val happy: TextView? = view.findViewById<TextView?>(R.id.tvHappy)
        val unhappy: TextView? = view.findViewById<TextView?>(R.id.tvUnhappy)
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.how_are_we_doing))
                .customView(view, true)
                .theme(Theme.LIGHT)
                .cancelable(true)
                .titleColor(getResources().getColor(R.color.black))
                .positiveText(getString(R.string.i_love_it))
                .negativeText(getString(R.string.report_problem))
                .neutralText(getString(R.string.no_thanks))
                .onNeutral(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                        finish()
                    }
                })
                .onNegative(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val categories = Categories(1, getString(R.string.contact_support))
                        val support = HelpAndSupport(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null)
                        Navigator.onMoveReportProblem(getContext(), support)
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                    }
                })
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Companion.Log(TAG, "Positive")
                        onRateApp()
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                        PrefsController.putBoolean(getString(R.string.we_are_a_team_positive), true)
                    }
                })
        builder.build().show()
    }

    fun onRateApp() {
        val uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live))
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))))
        }
    }

    companion object {
        private val TAG = MainTabActivity::class.java.simpleName
    }
}