package co.tpcreative.supersafe.ui.lockscreenimport

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonMultipleListener
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.util.Constants
import co.tpcreative.supersafe.common.util.Formatter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.User
import org.greenrobot.eventbus.EventBus
import java.io.File

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class EnterPinActivity : BaseVerifyPinActivity(), BaseView<EnumPinAction?>, Calculator, FingerPrintAuthCallback, SingletonMultipleListener.Listener, SingletonScreenLockListener {
    @BindView(R.id.pinlockView)
    var mPinLockView: PinLockView? = null

    @BindView(R.id.indicator_dots)
    var mIndicatorDots: IndicatorDots? = null

    @BindView(R.id.title)
    var mTextTitle: AppCompatTextView? = null

    @BindView(R.id.attempts)
    var mTextAttempts: AppCompatTextView? = null

    @BindView(R.id.imgLauncher)
    var imgLauncher: AppCompatImageView? = null

    @BindView(R.id.ic_SuperSafe)
    var ic_SuperSafe: AppCompatImageView? = null

    @BindView(R.id.rlLockScreen)
    var rlLockScreen: RelativeLayout? = null

    @BindView(R.id.rlPreference)
    var rlPreference: RelativeLayout? = null

    @BindView(R.id.llForgotPin)
    var llForgotPin: LinearLayout? = null

    @BindView(R.id.rlButton)
    var rlButton: RelativeLayout? = null

    @BindView(R.id.rlDots)
    var rlDots: RelativeLayout? = null

    @BindView(R.id.rlSecretDoor)
    var rlSecretDoor: RelativeLayout? = null

    @BindView(R.id.calculator_holder)
    var calculator_holder: LinearLayout? = null

    @BindView(R.id.btnDone)
    var btnDone: AppCompatButton? = null

    @BindView(R.id.imgFingerprint)
    var imgFingerprint: AppCompatImageView? = null

    @BindView(R.id.imgSwitchTypeUnClock)
    var imgSwitchTypeUnClock: AppCompatImageView? = null

    @BindView(R.id.root)
    var root: CoordinatorLayout? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.llLockScreen_1)
    var llLockScreen_1: LinearLayout? = null

    @BindView(R.id.rlAttempt)
    var rlAttempt: RelativeLayout? = null

    @BindView(R.id.crc_standard)
    var circleProgressView: CircleProgressView? = null

    @BindView(R.id.tvAttempt)
    var tvAttempt: AppCompatTextView? = null

    @BindView(R.id.result)
    var mResult: AppCompatTextView? = null

    @BindView(R.id.formula)
    var mFormula: AppCompatTextView? = null
    private var count = 0
    private var countAttempt = 0
    private var isFingerprint = false
    private var mFirstPin: String? = ""
    private var mCameraConfig: CameraConfig? = null
    private var mFingerPrintAuthHelper: FingerPrintAuthHelper? = null
    private var mRealPin: String? = Utils.Companion.getPinFromSharedPreferences()
    private var mFakePin: String? = Utils.Companion.getFakePinFromSharedPreferences()
    private var isFakePinEnabled: Boolean = Utils.Companion.isEnabledFakePin()
    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enterpin)
        toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(false)
        presenter = LockScreenPresenter()
        presenter.bindView(this)
        val result: Int = getIntent().getIntExtra(EXTRA_SET_PIN, 0)
        mPinAction = EnumPinAction.values()[result]
        val resultNext: Int = getIntent().getIntExtra(EXTRA_ENUM_ACTION, 0)
        mPinActionNext = EnumPinAction.values()[resultNext]
        SingletonScreenLock.Companion.getInstance().setListener(this)
        enumPinPreviousAction = mPinAction
        when (mPinAction) {
            EnumPinAction.SET -> {
                onDisplayView()
                onDisplayText()
            }
            EnumPinAction.VERIFY -> {
                if (mRealPin == "") {
                    mPinAction = EnumPinAction.SET
                    onDisplayView()
                    onDisplayText()
                } else {
                    if (Utils.Companion.isSensorAvailable()) {
                        val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
                        if (isFingerPrintUnLock) {
                            imgSwitchTypeUnClock.setVisibility(View.VISIBLE)
                            isFingerprint = isFingerPrintUnLock
                            onSetVisitFingerprintView(isFingerprint)
                            Utils.Companion.Log(TAG, "Action find fingerPrint")
                        }
                    }
                    val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
                    if (value) {
                        imgSwitchTypeUnClock.setVisibility(View.INVISIBLE)
                        changeLayoutSecretDoor(true)
                    } else {
                        calculator_holder.setVisibility(View.INVISIBLE)
                        onDisplayView()
                        onDisplayText()
                    }
                }
            }
            EnumPinAction.INIT_PREFERENCE -> {
                initActionBar(true)
                onDisplayText()
                onDisplayView()
                onLauncherPreferences()
            }
            EnumPinAction.RESET -> {
                onDisplayView()
                onDisplayText()
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                onDisplayText()
                onDisplayView()
            }
            else -> {
                Utils.Companion.Log(TAG, "Noting to do")
            }
        }
        imgLauncher.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                changeLayoutSecretDoor(false)
                return false
            }
        })
        ic_SuperSafe.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                changeLayoutSecretDoor(false)
                return false
            }
        })
        if (Utils.Companion.isSensorAvailable()) {
            mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this)
        }
        onInitPin()
        /*Calculator init*/mCalc = CalculatorImpl(this)
        AutofitHelper.create(mResult)
        AutofitHelper.create(mFormula)
        Utils.Companion.Log(TAG, "onCreated->EnterPinActivity")
    }

    val pinLockListener: PinLockListener? = object : PinLockListener {
        override fun onComplete(pin: String?) {
            Utils.Companion.Log(TAG, "Complete button " + mPinAction.name)
            when (mPinAction) {
                EnumPinAction.SET -> {
                    setPin(pin)
                }
                EnumPinAction.VERIFY -> {
                    checkPin(pin, true)
                }
                EnumPinAction.VERIFY_TO_CHANGE -> {
                    checkPin(pin, true)
                }
                EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                    checkPin(pin, true)
                }
                EnumPinAction.CHANGE -> {
                    setPin(pin)
                }
                EnumPinAction.FAKE_PIN -> {
                    setPin(pin)
                }
                EnumPinAction.RESET -> {
                    setPin(pin)
                }
                else -> {
                    Utils.Companion.Log(TAG, "Nothing working")
                }
            }
        }

        override fun onEmpty() {
            Utils.Companion.Log(TAG, "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String?) {
            when (mPinAction) {
                EnumPinAction.VERIFY -> {
                    checkPin(intermediatePin, false)
                }
                EnumPinAction.VERIFY_TO_CHANGE -> {
                    checkPin(intermediatePin, false)
                }
                EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                    run { checkPin(intermediatePin, false) }
                    run { Utils.Companion.Log(TAG, "Nothing working!!!") }
                }
                else -> {
                    Utils.Companion.Log(TAG, "Nothing working!!!")
                }
            }
            Utils.Companion.Log(TAG, "Pin changed, new length $pinLength with intermediate pin $intermediatePin")
        }
    }

    override fun onAttemptTimer(seconds: String?) {
        runOnUiThread(Runnable {
            try {
                val response = seconds.toDouble()
                Utils.Companion.Log(TAG, "Timer  Attempt $countAttempt Count $count")
                val remain = response / countAttempt * 100
                val result = remain as Int
                Utils.Companion.Log(TAG, "Result $result")
                circleProgressView.setValue(result)
                circleProgressView.setText(seconds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    override fun onAttemptTimerFinish() {
        runOnUiThread(Runnable {
            mPinAction = enumPinPreviousAction
            onDisplayView()
            Utils.Companion.Log(TAG, "onAttemptTimerFinish")
        })
    }

    override fun onNotifier(status: EnumStatus?) {
        when (status) {
            EnumStatus.FINISH -> {
                finish()
            }
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    protected override fun onDestroy() {
        super.onDestroy()
        SingletonManager.Companion.getInstance().setVisitLockScreen(false)
        val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        val action = EnumPinAction.values()[value]
        when (action) {
            EnumPinAction.NONE -> {
                val mUser: User = Utils.Companion.getUserInfo()
                if (mUser != null) {
                    ServiceManager.Companion.getInstance().onStartService()
                    SingletonResetPin.Companion.getInstance().onStop()
                }
            }
            else -> {
                Utils.Companion.Log(TAG, "Nothing to do")
            }
        }
        Utils.Companion.Log(TAG, "onDestroy")
    }

    @OnClick(R.id.btnDone)
    fun onClickedDone() {
        finish()
    }

    fun onDelete(view: View?) {
        Utils.Companion.Log(TAG, "onDelete here")
        if (mPinLockView != null) {
            mPinLockView.onDeleteClicked()
        }
    }

    /*Forgot pin*/
    @OnClick(R.id.llForgotPin)
    fun onForgotPin(view: View?) {
        Navigator.onMoveToForgotPin(this, false)
    }

    fun onSetVisitableForgotPin(value: Int) {
        llForgotPin.setVisibility(value)
    }

    protected override fun onResume() {
        super.onResume()
        SingletonManager.Companion.getInstance().setVisitLockScreen(true)
        Utils.Companion.Log(TAG, "onResume")
        if (mPinLockView != null) {
            mPinLockView.resetPinLockView()
        }
        onSetVisitableForgotPin(View.GONE)
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.startAuth()
        }
        mRealPin = Utils.Companion.getPinFromSharedPreferences()
        mFakePin = Utils.Companion.getFakePinFromSharedPreferences()
        isFakePinEnabled = Utils.Companion.isEnabledFakePin()
    }

    fun onInitPin() {
        mIndicatorDots.setActivity(this)
        mPinLockView.attachIndicatorDots(mIndicatorDots)
        mPinLockView.setPinLockListener(pinLockListener)
        mPinLockView.setPinLength(PIN_LENGTH)
        mIndicatorDots.setIndicatorType(IndicatorType.Companion.FILL_WITH_ANIMATION)
        onInitHiddenCamera()
    }

    protected override fun onPause() {
        super.onPause()
        if (mFingerPrintAuthHelper != null) {
            mFingerPrintAuthHelper.stopAuth()
        }
    }

    private fun setPin(pin: String?) {
        when (mPinAction) {
            EnumPinAction.SET -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        Utils.Companion.writePinToSharedPreferences(pin)
                        when (mPinActionNext) {
                            EnumPinAction.SIGN_UP -> {
                                Navigator.onMoveToSignUp(this)
                            }
                            else -> {
                                Navigator.onMoveToMainTab(this)
                                presenter.onChangeStatus(EnumStatus.SET, EnumPinAction.DONE)
                            }
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
            EnumPinAction.CHANGE -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.Companion.isExistingFakePin(pin, mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            Utils.Companion.writePinToSharedPreferences(pin)
                            presenter.onChangeStatus(EnumStatus.CHANGE, EnumPinAction.DONE)
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
            EnumPinAction.FAKE_PIN -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.Companion.isExistingRealPin(pin, mRealPin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            Utils.Companion.writeFakePinToSharedPreferences(pin)
                            presenter.onChangeStatus(EnumStatus.CREATE_FAKE_PIN, EnumPinAction.DONE)
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
            EnumPinAction.RESET -> {
                if (mFirstPin == "") {
                    mFirstPin = pin
                    mTextTitle.setText(getString(R.string.pinlock_secondPin))
                    mPinLockView.resetPinLockView()
                } else {
                    if (pin == mFirstPin) {
                        if (Utils.Companion.isExistingFakePin(pin, mFakePin)) {
                            onAlertWarning(getString(R.string.pin_lock_replace))
                        } else {
                            when (mPinActionNext) {
                                EnumPinAction.RESTORE -> {
                                    Utils.Companion.writePinToSharedPreferences(pin)
                                    onRestore()
                                }
                                else -> {
                                    Utils.Companion.writePinToSharedPreferences(pin)
                                    Navigator.onMoveToMainTab(this)
                                    presenter.onChangeStatus(EnumStatus.RESET, EnumPinAction.DONE)
                                }
                            }
                        }
                    } else {
                        onAlertWarning(getString(R.string.pinlock_tryagain))
                    }
                }
            }
        }
    }

    fun onRestore() {
        Utils.Companion.onExportAndImportFile(SuperSafeApplication.Companion.getInstance().getSupersafeBackup(), SuperSafeApplication.Companion.getInstance().getSupersafeDataBaseFolder(), object : ServiceManagerSyncDataListener {
            override fun onCompleted() {
                Utils.Companion.Log(TAG, "Exporting successful")
                val mUser: User = SuperSafeApplication.Companion.getInstance().readUseSecret()
                if (mUser != null) {
                    Utils.Companion.setUserPreShare(mUser)
                    Navigator.onMoveToMainTab(this@EnterPinActivity)
                    presenter.onChangeStatus(EnumStatus.RESTORE, EnumPinAction.DONE)
                }
            }

            override fun onError() {
                Utils.Companion.Log(TAG, "Exporting error")
            }

            override fun onCancel() {}
        })
    }

    private fun checkPin(pin: String?, isCompleted: Boolean) {
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                if (SingletonManager.Companion.getInstance().isVisitFakePin()) {
                    if (pin == mFakePin && isFakePinEnabled) {
                        presenter.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE)
                    } else {
                        if (isCompleted) {
                            onTakePicture(pin)
                            onAlertWarning("")
                        }
                    }
                } else {
                    if (pin == mRealPin) {
                        presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE)
                    } else if (pin == mFakePin && isFakePinEnabled) {
                        presenter.onChangeStatus(EnumStatus.FAKE_PIN, EnumPinAction.DONE)
                    } else {
                        if (isCompleted) {
                            onTakePicture(pin)
                            onAlertWarning("")
                        }
                    }
                }
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                if (pin == mRealPin) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.CHANGE)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                if (pin == mRealPin) {
                    presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.FAKE_PIN)
                } else {
                    if (isCompleted) {
                        onTakePicture(pin)
                        onAlertWarning("")
                    }
                }
            }
        }
    }

    private fun shake() {
        val objectAnimator: ObjectAnimator = ObjectAnimator().ofFloat(mPinLockView, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
        objectAnimator.start()
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                count += 1
                onSetVisitableForgotPin(View.VISIBLE)
                if (count >= 3) {
                    countAttempt = count * 10
                    val attemptWaiting = count * 10000.toLong()
                    mPinAction = EnumPinAction.ATTEMPT
                    onDisplayView()
                    SingletonScreenLock.Companion.getInstance().onStartTimer(attemptWaiting)
                }
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                count += 1
                onSetVisitableForgotPin(View.VISIBLE)
                if (count >= 3) {
                    countAttempt = count * 10
                    val attemptWaiting = count * 10000.toLong()
                    mPinAction = EnumPinAction.ATTEMPT
                    onDisplayView()
                    SingletonScreenLock.Companion.getInstance().onStartTimer(attemptWaiting)
                }
            }
        }
        Utils.Companion.Log(TAG, "Visit....$count")
    }

    private fun onAlertWarning(title: String?) {
        when (mPinAction) {
            EnumPinAction.SET -> {
                shake()
                mTextTitle.setText(title)
                mPinLockView.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.CHANGE -> {
                shake()
                mTextTitle.setText(title)
                mPinLockView.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.FAKE_PIN -> {
                shake()
                mTextTitle.setText(title)
                mPinLockView.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.RESET -> {
                shake()
                mTextTitle.setText(title)
                mPinLockView.resetPinLockView()
                mFirstPin = ""
            }
            EnumPinAction.VERIFY -> {
                shake()
                mTextTitle.setText(title)
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView.resetPinLockView()
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                shake()
                mTextTitle.setText(title)
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView.resetPinLockView()
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                shake()
                mTextTitle.setText(title)
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin))
                mPinLockView.resetPinLockView()
            }
        }
    }

    private fun changeLayoutSecretDoor(isVisit: Boolean) {
        if (isVisit) {
            mTextTitle.setVisibility(View.INVISIBLE)
            rlButton.setVisibility(View.INVISIBLE)
            rlDots.setVisibility(View.INVISIBLE)
            mTextAttempts.setVisibility(View.INVISIBLE)
            val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
            if (options) {
                imgLauncher.setVisibility(View.INVISIBLE)
                rlSecretDoor.setVisibility(View.INVISIBLE)
                calculator_holder.setVisibility(View.VISIBLE)
            } else {
                imgLauncher.setVisibility(View.VISIBLE)
                rlSecretDoor.setVisibility(View.VISIBLE)
                calculator_holder.setVisibility(View.INVISIBLE)
            }
        } else {
            mTextTitle.setVisibility(View.VISIBLE)
            rlButton.setVisibility(View.VISIBLE)
            rlDots.setVisibility(View.VISIBLE)
            mTextAttempts.setVisibility(View.VISIBLE)
            mTextAttempts.setText("")
            imgLauncher.setVisibility(View.INVISIBLE)
            rlSecretDoor.setVisibility(View.INVISIBLE)
            calculator_holder.setVisibility(View.INVISIBLE)
            if (Utils.Companion.isSensorAvailable()) {
                val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
                if (isFingerPrintUnLock) {
                    imgSwitchTypeUnClock.setVisibility(View.VISIBLE)
                    isFingerprint = isFingerPrintUnLock
                    onSetVisitFingerprintView(isFingerprint)
                } else {
                    imgSwitchTypeUnClock.setVisibility(View.GONE)
                }
            } else {
                imgSwitchTypeUnClock.setVisibility(View.GONE)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, action: EnumPinAction?) {
        Utils.Companion.Log(TAG, "EnumPinAction 1:...." + action.name)
        when (status) {
            EnumStatus.VERIFY -> {
                mTextAttempts.setText("")
                Utils.Companion.Log(TAG, "Result here")
                mPinAction = action
                when (action) {
                    EnumPinAction.VERIFY_TO_CHANGE -> {
                        initActionBar(false)
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.FAKE_PIN -> {
                        mPinLockView.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.CHANGE -> {
                        mPinLockView.resetPinLockView()
                        onDisplayText()
                        onDisplayView()
                    }
                    EnumPinAction.DONE -> {

                        /*Unlock for real pin*/SingletonManager.Companion.getInstance().setAnimation(false)
                        EventBus.getDefault().post(EnumStatus.UNLOCK)
                        Utils.Companion.onObserveData(100, Listener { finish() })
                        Utils.Companion.Log(TAG, "Action ...................done")
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                    }
                    EnumPinAction.VERIFY -> {
                        finish()
                    }
                }
            }
            EnumStatus.SET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.CHANGE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.RESET -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.RESTORE -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            EnumStatus.FAKE_PIN -> {

                /*UnLock for fake pin*/mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        SingletonManager.Companion.getInstance().setAnimation(false)
                        Utils.Companion.onObserveData(100, Listener {
                            PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                            if (SingletonManager.Companion.getInstance().isVisitFakePin()) {
                                finish()
                            } else {
                                Navigator.onMoveFakePinComponent(this@EnterPinActivity)
                            }
                        })
                    }
                }
            }
            EnumStatus.CREATE_FAKE_PIN -> {
                mPinAction = action
                when (action) {
                    EnumPinAction.DONE -> {
                        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                        finish()
                    }
                }
            }
            else -> {
                Utils.Companion.Log(TAG, "Nothing to do")
            }
        }
    }

    override fun onBackPressed() {
        Utils.Companion.Log(TAG, mPinAction.name)
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                val action = EnumPinAction.values()[value]
                when (action) {
                    EnumPinAction.SCREEN_LOCK -> {
                        EventBus.getDefault().post(EnumStatus.FINISH)
                        Navigator.onMoveToFaceDown(this)
                        Utils.Companion.Log(TAG, "onStillScreenLock ???")
                    }
                }
                super.onBackPressed()
            }
            EnumPinAction.SET -> {
                super.onBackPressed()
            }
            EnumPinAction.CHANGE -> {
                super.onBackPressed()
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                super.onBackPressed()
            }
        }
    }

    fun onDisplayView() {
        Utils.Companion.Log(TAG, "EnumPinAction 2:...." + mPinAction.name)
        when (mPinAction) {
            EnumPinAction.SET -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.CHANGE -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.INIT_PREFERENCE -> {
                rlLockScreen.setVisibility(View.INVISIBLE)
                rlPreference.setVisibility(View.VISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.FAKE_PIN -> {
                rlLockScreen.setVisibility(View.VISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.ATTEMPT -> {
                rlLockScreen.setVisibility(View.INVISIBLE)
                rlPreference.setVisibility(View.INVISIBLE)
                rlAttempt.setVisibility(View.VISIBLE)
                val result: String = kotlin.String.format(getString(R.string.in_correct_pin), count.toString() + "", countAttempt.toString() + "")
                tvAttempt.setText(result)
                Utils.Companion.Log(TAG, mPinAction.name)
            }
        }
    }

    fun onDisplayText() {
        Utils.Companion.Log(TAG, "EnumPinAction 3:...." + mPinAction.name)
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                mTextTitle.setVisibility(View.INVISIBLE)
                imgLauncher.setVisibility(View.VISIBLE)
                imgLauncher.setEnabled(false)
            }
            EnumPinAction.VERIFY_TO_CHANGE -> {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle.setVisibility(View.VISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.CHANGE -> {
                mTextTitle.setText(getString(R.string.pinlock_confirm_create))
                mTextTitle.setVisibility(View.VISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.INIT_PREFERENCE -> {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle.setVisibility(View.VISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.VERIFY_TO_CHANGE_FAKE_PIN -> {
                mTextTitle.setText(getString(R.string.pinlock_confirm_your_pin))
                mTextTitle.setVisibility(View.VISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.FAKE_PIN -> {
                mTextTitle.setText(getString(R.string.pinlock_confirm_create))
                mTextTitle.setVisibility(View.VISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.SET -> {
                mTextTitle.setText(getString(R.string.pinlock_settitle))
                mTextTitle.setVisibility(View.VISIBLE)
                mTextAttempts.setVisibility(View.INVISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
            EnumPinAction.RESET -> {
                mTextTitle.setText(getString(R.string.pinlock_settitle))
                mTextTitle.setVisibility(View.VISIBLE)
                mTextAttempts.setVisibility(View.INVISIBLE)
                imgLauncher.setVisibility(View.INVISIBLE)
            }
        }
    }

    /*Call back finger print*/
    override fun onNoFingerPrintHardwareFound() {}
    override fun onNoFingerPrintRegistered() {}
    override fun onBelowMarshmallow() {}
    override fun onAuthSuccess(cryptoObject: FingerprintManager.CryptoObject?) {
        val isFingerPrintUnLock: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
        isFingerprint = isFingerPrintUnLock
        if (!isFingerPrintUnLock) {
            return
        }
        when (mPinAction) {
            EnumPinAction.VERIFY -> {
                presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.DONE)
            }
        }
    }

    override fun onAuthFailed(errorCode: Int, errorMessage: String?) {}

    /*Call back end at finger print*/
    fun initActionBar(isInit: Boolean) {
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(isInit)
    }

    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    @OnClick(R.id.imgSwitchTypeUnClock)
    fun onClickedSwitchTypeUnlock(view: View?) {
        isFingerprint = if (isFingerprint) {
            false
        } else {
            true
        }
        onSetVisitFingerprintView(isFingerprint)
    }

    fun onSetVisitFingerprintView(isFingerprint: Boolean) {
        if (isFingerprint) {
            mPinLockView.setVisibility(View.INVISIBLE)
            imgFingerprint.setVisibility(View.VISIBLE)
            rlDots.setVisibility(View.INVISIBLE)
            mTextAttempts.setText(getString(R.string.use_your_fingerprint_to_unlock_supersafe))
            mTextAttempts.setVisibility(View.VISIBLE)
            mTextTitle.setText("")
        } else {
            mPinLockView.setVisibility(View.VISIBLE)
            imgFingerprint.setVisibility(View.INVISIBLE)
            rlDots.setVisibility(View.VISIBLE)
            mTextAttempts.setText("")
            mTextTitle.setText(getString(R.string.pinlock_title))
        }
    }

    /*Settings preference*/
    class SettingsFragment : PreferenceFragmentCompat() {
        private var mChangePin: MyPreference? = null
        private var mFaceDown: MySwitchPreference? = null
        private var mFingerPrint: MySwitchPreference? = null
        private fun createChangeListener(): Preference.OnPreferenceChangeListener? {
            return Preference.OnPreferenceChangeListener { preference, newValue ->
                Utils.Companion.Log(TAG, "change $newValue")
                true
            }
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener? {
            return Preference.OnPreferenceClickListener { preference ->
                if (preference is Preference) {
                    if (preference.key == getString(R.string.key_change_pin)) {
                        Utils.Companion.Log(TAG, "Action here!!!")
                        enumPinPreviousAction = EnumPinAction.VERIFY_TO_CHANGE
                        presenter.onChangeStatus(EnumStatus.VERIFY, EnumPinAction.VERIFY_TO_CHANGE)
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Changing pin*/mChangePin = findPreference(getString(R.string.key_change_pin)) as MyPreference?
            mChangePin.setOnPreferenceChangeListener(createChangeListener())
            mChangePin.setOnPreferenceClickListener(createActionPreferenceClickListener())
            /*Face down*/mFaceDown = findPreference(getString(R.string.key_face_down_lock)) as MySwitchPreference?
            val switchFaceDown: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            mFaceDown.setOnPreferenceChangeListener(createChangeListener())
            mFaceDown.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mFaceDown.setDefaultValue(switchFaceDown)
            Utils.Companion.Log(TAG, "default $switchFaceDown")
            /*FingerPrint*/mFingerPrint = findPreference(getString(R.string.key_fingerprint_unlock)) as MySwitchPreference?
            val switchFingerPrint: Boolean = PrefsController.getBoolean(getString(R.string.key_fingerprint_unlock), false)
            mFingerPrint.setOnPreferenceChangeListener(createChangeListener())
            mFingerPrint.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mFingerPrint.setDefaultValue(switchFingerPrint)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_lock_screen)
        }
    }

    fun onLauncherPreferences() {
        var fragment: Fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG)
        if (fragment == null) {
            fragment = Fragment.instantiate(this, co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity.SettingsFragment::class.java.name)
        }
        val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EnumPinAction?>?) {}
    override fun onImageCapture(imageFile: File, pin: String) {
        super.onImageCapture(imageFile, pin)
        val inAlerts = BreakInAlertsModel()
        inAlerts.fileName = imageFile.absolutePath
        inAlerts.pin = pin
        inAlerts.time = System.currentTimeMillis()
        SQLHelper.onInsert(inAlerts)
    }

    override fun onCameraError(errorCode: Int) {
        super.onCameraError(errorCode)
        when (errorCode) {
            CameraError.Companion.ERROR_CAMERA_OPEN_FAILED -> {
            }
            CameraError.Companion.ERROR_IMAGE_WRITE_FAILED -> {
            }
            CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE -> {
            }
            CameraError.Companion.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> showMessage(getString(R.string.error_not_having_camera))
        }
    }

    fun onInitHiddenCamera() {
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
        if (!value) {
            return
        }
        mCameraConfig = CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.Companion.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.Companion.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.Companion.FORMAT_JPEG)
                .setImageRotation(CameraRotation.Companion.ROTATION_270)
                .setCameraFocus(CameraFocus.Companion.AUTO)
                .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig)
        }
    }

    fun onTakePicture(pin: String?) {
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
        if (!value) {
            return
        }
        mCameraConfig.getBuilder(SuperSafeApplication.Companion.getInstance())
                .setPin(pin)
        mCameraConfig.getBuilder(SuperSafeApplication.Companion.getInstance()).setImageFile(SuperSafeApplication.Companion.getInstance().getDefaultStorageFile(CameraImageFormat.Companion.FORMAT_JPEG))
        takePicture()
    }

    /*Calculator action*/
    override fun setValue(value: String?) {
        mResult.setText(value)
    }

    // used only by Robolectric
    override fun setValueDouble(d: Double) {
        mCalc.setValue(Formatter.doubleToString(d))
        mCalc.setLastKey(Constants.DIGIT)
    }

    override fun setFormula(value: String?) {
        mFormula.setText(value)
    }

    @OnClick(R.id.btn_plus)
    fun plusClicked() {
        mCalc.handleOperation(Constants.PLUS)
    }

    @OnClick(R.id.btn_minus)
    fun minusClicked() {
        mCalc.handleOperation(Constants.MINUS)
    }

    @OnClick(R.id.btn_multiply)
    fun multiplyClicked() {
        mCalc.handleOperation(Constants.MULTIPLY)
    }

    @OnClick(R.id.btn_divide)
    fun divideClicked() {
        mCalc.handleOperation(Constants.DIVIDE)
    }

    @OnClick(R.id.btn_modulo)
    fun moduloClicked() {
        mCalc.handleOperation(Constants.MODULO)
    }

    @OnClick(R.id.btn_power)
    fun powerClicked() {
        mCalc.handleOperation(Constants.POWER)
    }

    @OnClick(R.id.btn_root)
    fun rootClicked() {
        mCalc.handleOperation(Constants.ROOT)
    }

    @OnClick(R.id.btn_clear)
    fun clearClicked() {
        mCalc.handleClear()
    }

    @OnLongClick(R.id.btn_clear)
    fun clearLongClicked(): Boolean {
        mCalc.handleReset()
        return true
    }

    @OnClick(R.id.btn_equals)
    fun equalsClicked() {
        mCalc.handleEquals()
    }

    @OnClick(R.id.btn_decimal, R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9)
    fun numpadClick(view: View?) {
        numpadClicked(view.getId())
    }

    fun numpadClicked(id: Int) {
        mCalc.numpadClicked(id)
    }

    companion object {
        val TAG = EnterPinActivity::class.java.simpleName
        private val FRAGMENT_TAG: String? = SettingsActivity::class.java.getSimpleName() + "::fragmentTag"
        val EXTRA_SET_PIN: String? = "SET_PIN"
        val EXTRA_ENUM_ACTION: String? = "ENUM_ACTION"
        private const val PIN_LENGTH = 100
        private var mCalc: CalculatorImpl? = null
        private var mPinAction: EnumPinAction? = null
        private var enumPinPreviousAction: EnumPinAction? = null
        private var mPinActionNext: EnumPinAction? = null
        private var presenter: LockScreenPresenter? = null
        fun getIntent(context: Context?, action: Int, actionNext: Int): Intent? {
            val intent = Intent(context, EnterPinActivity::class.java)
            intent.putExtra(EXTRA_SET_PIN, action)
            intent.putExtra(EXTRA_ENUM_ACTION, actionNext)
            return intent
        }
    }
}