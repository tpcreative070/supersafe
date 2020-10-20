package co.tpcreative.supersafe.ui.photosslideshowimport

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.User
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Priority
import com.snatik.storage.Storage
import io.reactivex.Observable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class PhotoSlideShowActivity : BaseGalleryActivity(), View.OnClickListener, BaseView<Any?> {
    private val options: RequestOptions? = RequestOptions()
            .centerInside()
            .placeholder(R.color.black38)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)

    @BindView(R.id.rlTop)
    var rlTop: RelativeLayout? = null

    @BindView(R.id.llBottom)
    var llBottom: LinearLayout? = null

    @BindView(R.id.imgArrowBack)
    var imgArrowBack: AppCompatImageView? = null

    @BindView(R.id.imgOverflow)
    var imgOverflow: AppCompatImageView? = null

    @BindView(R.id.imgShare)
    var imgShare: AppCompatImageView? = null

    @BindView(R.id.imgExport)
    var imgExport: AppCompatImageView? = null

    @BindView(R.id.imgMove)
    var imgMove: AppCompatImageView? = null

    @BindView(R.id.imgRotate)
    var imgRotate: AppCompatImageView? = null

    @BindView(R.id.imgDelete)
    var imgDelete: AppCompatImageView? = null
    private var isHide = false
    private var presenter: PhotoSlideShowPresenter? = null
    private var storage: Storage? = null
    private var viewPager: ViewPager? = null
    private var adapter: SamplePagerAdapter? = null
    private var isReload = false
    private var dialog: AlertDialog? = null
    private var subscriptions: Disposable? = null
    private var isProgressing = false
    private var position = 0
    private var photoView: PhotoView? = null
    var mDialogProgress: SweetAlertDialog? = null
    private var handler: Handler? = null
    private val delay = 2000 //milliseconds
    private var page = 0
    var runnable: Runnable? = object : Runnable {
        override fun run() {
            if (adapter.getCount() == page) {
                page = 0
            } else {
                page++
            }
            viewPager.setCurrentItem(page, true)
            handler.postDelayed(this, delay.toLong())
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_slideshow)
        storage = Storage(this)
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
        presenter = PhotoSlideShowPresenter()
        presenter.bindView(this)
        presenter.getIntent(this)
        viewPager = findViewById<ViewPager?>(R.id.view_pager)
        adapter = SamplePagerAdapter(this)
        viewPager.setAdapter(adapter)
        imgArrowBack.setOnClickListener(this)
        imgOverflow.setOnClickListener(this)
        imgDelete.setOnClickListener(this)
        imgExport.setOnClickListener(this)
        imgRotate.setOnClickListener(this)
        imgShare.setOnClickListener(this)
        imgMove.setOnClickListener(this)
        attachFragment(R.id.gallery_root)
        /*Auto slide*/handler = Handler()
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                page = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    fun onStartSlider() {
        try {
            handler.postDelayed(runnable, delay.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onStopSlider() {
        try {
            handler.removeCallbacks(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.START_PROGRESS -> {
                onStartProgressing()
            }
            EnumStatus.STOP_PROGRESS -> {
                try {
                    Utils.Companion.Log(TAG, "onStopProgress")
                    onStopProgressing()
                    when (presenter.status) {
                        EnumStatus.SHARE -> {
                            if (presenter.mListShare != null) {
                                if (presenter.mListShare.size > 0) {
                                    Utils.Companion.shareMultiple(presenter.mListShare, this)
                                }
                            }
                        }
                        EnumStatus.EXPORT -> {
                            runOnUiThread(Runnable { Toast.makeText(this@PhotoSlideShowActivity, "Exported at " + SuperSafeApplication.Companion.getInstance().getSupersafePicture(), Toast.LENGTH_LONG).show() })
                        }
                    }
                } catch (e: Exception) {
                    Utils.Companion.Log(TAG, e.message)
                }
            }
            EnumStatus.DOWNLOAD_COMPLETED -> {
                mDialogProgress.setTitleText("Success!")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                mDialogProgress.setConfirmClickListener(object : OnSweetClickListener {
                    override fun onClick(sweetAlertDialog: SweetAlertDialog?) {
                        sweetAlertDialog.dismiss()
                        onShowDialog(EnumStatus.EXPORT, position)
                    }
                })
                Utils.Companion.Log(TAG, " already sync")
            }
            EnumStatus.DOWNLOAD_FAILED -> {
                mDialogProgress.setTitleText("No connection, Try again")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter.unbindView()
        onStopSlider()
        Utils.Companion.Log(TAG, "Destroy")
        if (subscriptions != null) {
            subscriptions.dispose()
        }
        try {
            storage.deleteFile(Utils.Companion.getPackagePath(getApplicationContext()).getAbsolutePath())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    /*BaseGallery*/
    override fun getConfiguration(): Configuration? {
        //default configuration
        try {
            return Configuration.Builder()
                    .hasCamera(true)
                    .hasShade(true)
                    .hasPreview(true)
                    .setSpaceSize(4)
                    .setPhotoMaxWidth(120)
                    .setLocalCategoriesId(presenter.mainCategories.categories_local_id)
                    .setCheckBoxColor(-0xc0ae4b)
                    .setDialogHeight(Configuration.Companion.DIALOG_HALF)
                    .setDialogMode(Configuration.Companion.DIALOG_LIST)
                    .setMaximum(9)
                    .setTip(null)
                    .setAblumsTitle(null)
                    .build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun getListItems(): MutableList<ItemModel?>? {
        try {
            val list: MutableList<ItemModel?> = ArrayList<ItemModel?>()
            val item: ItemModel = presenter.mList.get(position)
            if (item != null) {
                item.isChecked = true
                list.add(item)
                return list
            }
            onBackPressed()
            return null
        } catch (e: Exception) {
            onBackPressed()
        }
        return null
    }

    internal inner class SamplePagerAdapter(private val context: Context?) : PagerAdapter() {
        override fun getCount(): Int {
            return presenter.mList.size
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): View? {
            //PhotoView photoView = new PhotoView(container.getContext());
            val inflater: LayoutInflater = getLayoutInflater()
            val myView: View = inflater.inflate(R.layout.content_view, null)
            photoView = myView.findViewById(R.id.imgPhoto)
            val imgPlayer = myView.findViewById<ImageView?>(R.id.imgPlayer)
            val mItems: ItemModel = presenter.mList.get(position)
            val enumTypeFile = EnumFormatType.values()[mItems.formatType]
            photoView.setOnPhotoTapListener(object : OnPhotoTapListener {
                override fun onPhotoTap(view: ImageView?, x: Float, y: Float) {
                    Utils.Companion.Log(TAG, "on Clicked")
                    onStopSlider()
                    isHide = !isHide
                    onHideView()
                }
            })
            imgPlayer.setOnClickListener(View.OnClickListener {
                val items: ItemModel = presenter.mList.get(viewPager.getCurrentItem())
                Navigator.onPlayer(this@PhotoSlideShowActivity, items, presenter.mainCategories)
            })
            try {
                val path: String = mItems.thumbnailPath
                val file = File("" + path)
                if (file.exists() || file.isFile) {
                    photoView.setRotation(mItems.degrees.toFloat())
                    if (mItems.mimeType == getString(R.string.key_gif)) {
                        val mOriginal: String = mItems.originalPath
                        val mFileOriginal = File("" + mOriginal)
                        if (mFileOriginal.exists() || mFileOriginal.isFile) {
                            Glide.with(context)
                                    .asGif()
                                    .load(storage.readFile(mOriginal))
                                    .apply(options)
                                    .into(photoView)
                        }
                    } else {
                        Glide.with(context)
                                .load(storage.readFile(path))
                                .apply(options)
                                .into(photoView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            when (enumTypeFile) {
                EnumFormatType.VIDEO -> {
                    imgPlayer.setVisibility(View.VISIBLE)
                }
                EnumFormatType.AUDIO -> {
                    imgPlayer.setVisibility(View.VISIBLE)
                }
                EnumFormatType.FILES -> {
                    imgPlayer.setVisibility(View.INVISIBLE)
                }
                else -> {
                    imgPlayer.setVisibility(View.INVISIBLE)
                }
            }
            container.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            photoView.setTag("myview$position")
            return myView
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container.removeView(`object` as View?)
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

    }

    fun onHideView() {
        if (isHide) {
            Utils.Companion.slideToTopHeader(rlTop)
            Utils.Companion.slideToBottomFooter(llBottom)
        } else {
            Utils.Companion.slideToBottomHeader(rlTop)
            Utils.Companion.slideToTopFooter(llBottom)
        }
    }

    override fun onClick(view: View?) {
        position = viewPager.getCurrentItem()
        when (view.getId()) {
            R.id.imgArrowBack -> {
                if (isHide) {
                    break
                }
                onBackPressed()
            }
            R.id.imgOverflow -> {
                openOptionMenu(view)
            }
            R.id.imgShare -> {
                if (isHide) {
                    break
                }
                try {
                    if (presenter.mList != null) {
                        if (presenter.mList.size > 0) {
                            storage.createDirectory(SuperSafeApplication.Companion.getInstance().getSupersafeShare())
                            presenter.status = EnumStatus.SHARE
                            onShowDialog(EnumStatus.SHARE, position)
                        } else {
                            onBackPressed()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.imgDelete -> {
                if (isHide) {
                    break
                }
                try {
                    if (presenter.mList != null) {
                        if (presenter.mList.size > 0) {
                            presenter.status = EnumStatus.DELETE
                            onShowDialog(EnumStatus.DELETE, position)
                        } else {
                            onBackPressed()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.imgExport -> {
                if (isHide) {
                    break
                }
                Utils.Companion.Log(TAG, "Action here")
                try {
                    if (presenter.mList != null) {
                        if (presenter.mList.size > 0) {
                            storage.createDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePicture())
                            presenter.status = EnumStatus.EXPORT
                            if (presenter.mList.get(position).isSaver) {
                                onEnableSyncData(position)
                            } else {
                                onShowDialog(EnumStatus.EXPORT, position)
                            }
                        } else {
                            onBackPressed()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.imgRotate -> {
                if (isHide) {
                    break
                }
                try {
                    if (isProgressing) {
                        return
                    }
                    val items: ItemModel = SQLHelper.getItemId(presenter.mList.get(viewPager.getCurrentItem()).items_id, presenter.mList.get(viewPager.getCurrentItem()).isFakePin)
                    val formatTypeFile = EnumFormatType.values()[items.formatType]
                    if (formatTypeFile != EnumFormatType.AUDIO && formatTypeFile != EnumFormatType.FILES) {
                        if (items != null) {
                            onRotateBitmap(items)
                            isReload = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.imgMove -> {
                presenter.status = EnumStatus.MOVE
                openAlbum()
            }
        }
    }

    /*Download file*/
    fun onEnableSyncData(position: Int) {
        val mUser: User = Utils.Companion.getUserInfo()
        if (mUser != null) {
            if (mUser.verified) {
                if (!mUser.driveConnected) {
                    Navigator.onCheckSystem(this, null)
                } else {
                    onDialogDownloadFile()
                    val list: MutableList<ItemModel?> = ArrayList<ItemModel?>()
                    val items: ItemModel = presenter.mList.get(position)
                    items.isChecked = true
                    list.add(items)
                    ServiceManager.Companion.getInstance().onPreparingEnableDownloadData(list)
                }
            } else {
                Navigator.onVerifyAccount(this)
            }
        }
    }

    fun onDialogDownloadFile() {
        mDialogProgress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading))
        mDialogProgress.show()
        mDialogProgress.setCancelable(false)
    }

    override fun onMoveAlbumSuccessful() {
        try {
            isReload = true
            presenter.mList.removeAt(position)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onShowDialog(status: EnumStatus?, position: Int) {
        var content: String? = ""
        when (status) {
            EnumStatus.EXPORT -> {
                content = kotlin.String.format(getString(R.string.export_items), "1")
            }
            EnumStatus.SHARE -> {
                content = kotlin.String.format(getString(R.string.share_items), "1")
            }
            EnumStatus.DELETE -> {
                content = kotlin.String.format(getString(R.string.move_items_to_trash), "1")
            }
            EnumStatus.MOVE -> {
            }
        }
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(content)
                .titleColor(getResources().getColor(R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onNegative(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val items: ItemModel = presenter.mList.get(position)
                        val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
                        val formatType = EnumFormatType.values()[items.formatType]
                        when (formatType) {
                            EnumFormatType.IMAGE -> {
                                items.isSaver = isSaver
                                SQLHelper.updatedItem(items)
                                if (isSaver) {
                                    storage.deleteFile(items.originalPath)
                                }
                            }
                        }
                    }
                })
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val mListExporting: MutableList<ExportFiles?> = ArrayList<ExportFiles?>()
                        when (status) {
                            EnumStatus.SHARE -> {
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter.mListShare.clear()
                                val index: ItemModel = presenter.mList.get(position)
                                if (index != null) {
                                    val formatType = EnumFormatType.values()[index.formatType]
                                    when (formatType) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.FILES -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.VIDEO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        else -> {
                                            var path = ""
                                            path = if (index.mimeType == getString(R.string.key_gif)) {
                                                index.originalPath
                                            } else {
                                                index.thumbnailPath
                                            }
                                            val input = File(path)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                    }
                                }
                                onStartProgressing()
                                ServiceManager.Companion.getInstance().setmListExport(mListExporting)
                                ServiceManager.Companion.getInstance().onExportingFiles()
                            }
                            EnumStatus.EXPORT -> {
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter.mListShare.clear()
                                val index: ItemModel = presenter.mList.get(position)
                                if (index != null) {
                                    val formatType = EnumFormatType.values()[index.formatType]
                                    when (formatType) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.FILES -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.VIDEO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        else -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage.isFileExist(output.getAbsolutePath())) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage.isFileExist(input.absolutePath)) {
                                                presenter.mListShare.add(output)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                    }
                                }
                                onStartProgressing()
                                ServiceManager.Companion.getInstance().setmListExport(mListExporting)
                                ServiceManager.Companion.getInstance().onExportingFiles()
                            }
                            EnumStatus.DELETE -> {
                                presenter.onDelete(position)
                                isReload = true
                            }
                        }
                    }
                })
        builder.show()
    }

    /*Gallery interface*/
    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    dialog = SpotsDialog.Builder()
                            .setContext(this@PhotoSlideShowActivity)
                            .setMessage(getString(R.string.exporting))
                            .setCancelable(true)
                            .build()
                }
                if (!dialog.isShowing()) {
                    dialog.show()
                    Utils.Companion.Log(TAG, "Showing dialog...")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onStopProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog != null) {
                    dialog.dismiss()
                    deselectAll()
                    Utils.Companion.Log(TAG, "Action 1")
                }
            })
        } catch (e: Exception) {
            Utils.Companion.Log(TAG, e.message)
        }
    }

    private fun deselectAll() {
        when (presenter.status) {
            EnumStatus.EXPORT -> {
                Utils.Companion.Log(TAG, "Action 2")
                presenter.mList.get(position).isExport = true
                presenter.mList.get(position).isDeleteLocal = true
                SQLHelper.updatedItem(presenter.mList.get(position))
                onCheckDelete()
            }
        }
    }

    fun onCheckDelete() {
        val mList: MutableList<ItemModel?> = presenter.mList
        Utils.Companion.Log(TAG, "Action 3")
        val formatTypeFile = EnumFormatType.values()[mList[position].formatType]
        if (formatTypeFile == EnumFormatType.AUDIO && mList[position].global_original_id == null) {
            SQLHelper.deleteItem(mList[position])
        } else if (formatTypeFile == EnumFormatType.FILES && mList[position].global_original_id == null) {
            SQLHelper.deleteItem(mList[position])
        } else if (mList[position].global_original_id == null and mList[position].global_thumbnail_id == null) {
            SQLHelper.deleteItem(mList[position])
        } else {
            mList[position].deleteAction = EnumDelete.DELETE_WAITING.ordinal
            SQLHelper.updatedItem(mList[position])
            Utils.Companion.Log(TAG, "ServiceManager waiting for delete")
        }
        storage.deleteDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePrivate() + mList[position].items_id)
        presenter.onDelete(position)
        isReload = true
        Utils.Companion.Log(TAG, "Action 4")
    }

    @SuppressLint("RestrictedApi")
    fun openOptionMenu(v: View?) {
        onStopSlider()
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.menu_slideshow, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_slideshow -> {
                    Utils.Companion.Log(TAG, "Slide show images")
                    onStartSlider()
                    isHide = true
                    onHideView()
                    return@OnMenuItemClickListener true
                }
            }
            true
        })
        popup.show()
    }

    /*ViewPresenter*/
    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun onBackPressed() {
        if (isReload) {
            SingletonPrivateFragment.Companion.getInstance().onUpdateView()
            SingletonFakePinComponent.Companion.getInstance().onUpdateView()
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    protected override fun onPause() {
        super.onPause()
        onStopSlider()
    }

    fun onRotateBitmap(items: ItemModel?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            isProgressing = true
            Utils.Companion.Log(TAG, "Start Progressing encrypt thumbnail data")
            val mItem: ItemModel? = items
            var mDegrees: Int = mItem.degrees
            mDegrees = if (mDegrees >= 360) {
                90
            } else {
                if (mDegrees > 90) {
                    mDegrees + 90
                } else {
                    180
                }
            }
            val valueDegrees = mDegrees
            mItem.degrees = valueDegrees
            presenter.mList.get(position).degrees = valueDegrees
            subscriber.onNext(mItem)
            subscriber.onComplete()
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mItem: ItemModel? = response as ItemModel?
                    if (mItem != null) {
                        SQLHelper.updatedItem(items)
                        runOnUiThread(Runnable {
                            isProgressing = false
                            viewPager.getAdapter().notifyDataSetChanged()
                        })
                        Utils.Companion.Log(TAG, "Thumbnail saved successful")
                    } else {
                        Utils.Companion.Log(TAG, "Thumbnail saved failed")
                    }
                }
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.DELETE -> {
                isReload = true
                adapter.notifyDataSetChanged()
                if (presenter.mList.size == 0) {
                    onBackPressed()
                }
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}

    companion object {
        private val TAG = PhotoSlideShowActivity::class.java.simpleName
    }
}