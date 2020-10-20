package co.tpcreative.supersafe.ui.albumdetailimport

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.Image
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapter
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Priority
import com.karumi.dexter.listener.PermissionRequest
import com.snatik.storage.Storage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class AlbumDetailActivity : BaseGalleryActivity(), BaseView<Int?>, AlbumDetailAdapter.ItemSelectedListener, AlbumDetailVerticalAdapter.ItemSelectedListener {
    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.speedDial)
    var mSpeedDialView: SpeedDialView? = null

    @BindView(R.id.backdrop)
    var backdrop: AppCompatImageView? = null

    @BindView(R.id.tv_Photos)
    var tv_Photos: AppCompatTextView? = null

    @BindView(R.id.tv_Videos)
    var tv_Videos: AppCompatTextView? = null

    @BindView(R.id.tv_Audios)
    var tv_Audios: AppCompatTextView? = null

    @BindView(R.id.tv_Others)
    var tv_Others: AppCompatTextView? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.llBottom)
    var llBottom: LinearLayout? = null
    private var presenter: AlbumDetailPresenter? = null
    private var adapter: AlbumDetailAdapter? = null
    private var verticalAdapter: AlbumDetailVerticalAdapter? = null
    private var isReload = false
    private var storage: Storage? = null
    private var actionMode: ActionMode? = null
    private var countSelected = 0
    private var isSelectAll = false
    private var dialog: AlertDialog? = null
    var mDialogProgress: SweetAlertDialog? = null
    private var menuItem: MenuItem? = null
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH)

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        storage = Storage(this)
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
        initSpeedDial(true)
        presenter = AlbumDetailPresenter()
        presenter.bindView(this)
        onInit()
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        val collapsingToolbar: CollapsingToolbarLayout = findViewById<CollapsingToolbarLayout?>(R.id.collapsing_toolbar)
        collapsingToolbar.setTitle(presenter.mainCategories.categories_name)
        val mList: MutableList<ItemModel?> = SQLHelper.getListItems(presenter.mainCategories.categories_local_id, presenter.mainCategories.isFakePin)
        val items: ItemModel = SQLHelper.getItemId(presenter.mainCategories.items_id)
        if (items != null && mList != null && mList.size > 0) {
            val formatTypeFile = EnumFormatType.values()[items.formatType]
            when (formatTypeFile) {
                EnumFormatType.AUDIO -> {
                    try {
                        val myColor = Color.parseColor(presenter.mainCategories.image)
                        backdrop.setBackgroundColor(myColor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                EnumFormatType.FILES -> {
                    try {
                        val myColor = Color.parseColor(presenter.mainCategories.image)
                        backdrop.setBackgroundColor(myColor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> {
                    if (storage.isFileExist(items.thumbnailPath)) {
                        backdrop.setRotation(items.degrees.toFloat())
                        Glide.with(this)
                                .load(storage.readFile(items.thumbnailPath))
                                .apply(options)
                                .into(backdrop)
                    } else {
                        backdrop.setImageResource(0)
                        val myColor = Color.parseColor(presenter.mainCategories.image)
                        backdrop.setBackgroundColor(myColor)
                    }
                }
            }
        } else {
            backdrop.setImageResource(0)
            val mainCategories: MainCategoryModel = SQLHelper.getCategoriesPosition(presenter.mainCategories.mainCategories_Local_Id)
            if (mainCategories != null) {
                try {
                    val myColor = Color.parseColor(mainCategories.image)
                    backdrop.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val myColor = Color.parseColor(presenter.mainCategories.image)
                    backdrop.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        llBottom.setVisibility(View.GONE)
        /*Root Fragment*/attachFragment(R.id.gallery_root)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Utils.Companion.Log(TAG, "Scrolling change listener")
                if (actionMode != null) {
                    mSpeedDialView.setVisibility(View.INVISIBLE)
                }
            }
        })
    }

    fun onInit() {
        presenter.getData(this)
        initRecycleView(getLayoutInflater())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REFRESH -> {
                presenter.getData(this)
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.UPDATED_VIEW_DETAIL_ALBUM -> {
                try {
                    this.runOnUiThread(Runnable { presenter.getData(EnumStatus.RELOAD) })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                            runOnUiThread(Runnable { Toast.makeText(this@AlbumDetailActivity, "Exported at " + SuperSafeApplication.Companion.getInstance().getSupersafePicture(), Toast.LENGTH_LONG).show() })
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
                        var i = 0
                        while (i < presenter.mList.size) {
                            val items: ItemModel = presenter.mList.get(i)
                            if (items.isChecked) {
                                items.isSaver = false
                            }
                            i++
                        }
                        onClickedExport()
                    }
                })
                Utils.Companion.Log(TAG, "already sync ")
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
        ServiceManager.Companion.getInstance().setRequestShareIntent(false)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter.unbindView()
        if (isReload) {
            ServiceManager.Companion.getInstance().onPreparingSyncData()
        }
        storage.deleteDirectory(SuperSafeApplication.Companion.getInstance().getSupersafeShare())
    }

    protected override fun onStop() {
        super.onStop()
        Utils.Companion.Log(TAG, "onStop Album")
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (toolbar == null) {
            return false
        }
        toolbar.inflateMenu(R.menu.menu_album_detail)
        menuItem = toolbar.getMenu().getItem(0)
        val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
        if (isVertical) {
            menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_view_comfy_white_48))
        } else {
            menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_format_list_bulleted_white_48))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.action_album_settings -> {
                Navigator.onAlbumSettings(this, presenter.mainCategories)
                return true
            }
            R.id.action_select_items -> {
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback)
                }
                countSelected = 0
                actionMode.setTitle(countSelected.toString() + " " + getString(R.string.selected))
                Utils.Companion.Log(TAG, "Action here")
                return true
            }
            R.id.action_view -> {
                if (menuItem != null) {
                    val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                    if (isVertical) {
                        menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_format_list_bulleted_white_48))
                        PrefsController.putBoolean(getString(R.string.key_vertical_adapter), false)
                        onInit()
                    } else {
                        menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_view_comfy_white_48))
                        PrefsController.putBoolean(getString(R.string.key_vertical_adapter), true)
                        onInit()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickItem(position: Int) {
        Utils.Companion.Log(TAG, "On clicked item")
        if (position >= presenter.mList.size) {
            return
        }
        if (actionMode != null) {
            toggleSelection(position)
            actionMode.setTitle(countSelected.toString() + " " + getString(R.string.selected))
        } else {
            try {
                val formatType = EnumFormatType.values()[presenter.mList.get(position).formatType]
                when (formatType) {
                    EnumFormatType.FILES -> {
                        Toast.makeText(getContext(), "Can not support to open type of this file", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Navigator.onPhotoSlider(this, presenter.mList.get(position), presenter.mList, presenter.mainCategories)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onLongClickItem(position: Int) {
        Utils.Companion.Log(TAG, "On long clicked item")
        if (position >= presenter.mList.size) {
            return
        }
        if (actionMode == null) {
            actionMode = toolbar.startActionMode(callback)
        }
        toggleSelection(position)
        actionMode.setTitle(countSelected.toString() + " " + getString(R.string.selected))
    }

    @OnClick(R.id.imgShare)
    fun onClickedShare(view: View?) {
        if (countSelected > 0) {
            storage.createDirectory(SuperSafeApplication.Companion.getInstance().getSupersafeShare())
            presenter.status = EnumStatus.SHARE
            onShowDialog(presenter.status)
        }
    }

    @OnClick(R.id.imgExport)
    fun onClickedExport() {
        if (countSelected > 0) {
            storage.createDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePicture())
            presenter.status = EnumStatus.EXPORT
            var isSaver = false
            var spaceAvailable: Long = 0
            for (i in presenter.mList.indices) {
                val items: ItemModel = presenter.mList.get(i)
                if (items.isSaver && items.isChecked) {
                    isSaver = true
                    spaceAvailable += items.size.toLong()
                }
            }
            val availableSpaceOS: Long = Utils.Companion.getAvailableSpaceInBytes()
            if (availableSpaceOS < spaceAvailable) {
                val request_spaces = spaceAvailable - availableSpaceOS
                val result: String = ConvertUtils.Companion.byte2FitMemorySize(request_spaces)
                val message: String = kotlin.String.format(getString(R.string.your_space_is_not_enough_to), "export. ", "Request spaces: $result")
                Utils.Companion.showDialog(this, message)
            } else {
                if (isSaver) {
                    onEnableSyncData()
                } else {
                    onShowDialog(presenter.status)
                }
            }
        }
    }

    @OnClick(R.id.imgDelete)
    fun onClickedDelete(view: View?) {
        if (countSelected > 0) {
            presenter.status = EnumStatus.DELETE
            onShowDialog(presenter.status)
        }
    }

    @OnClick(R.id.imgMove)
    fun onClickedMove(view: View?) {
        openAlbum()
    }

    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    val themeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
                    dialog = SpotsDialog.Builder()
                            .setContext(this@AlbumDetailActivity)
                            .setDotColor(themeApp.getAccentColor())
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
                    if (actionMode != null) {
                        actionMode.finish()
                    }
                }
            })
        } catch (e: Exception) {
            Utils.Companion.Log(TAG, e.message)
        }
    }

    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    /*Init Floating View*/
    private fun initSpeedDial(addActionItems: Boolean) {
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
            mSpeedDialView.setMainFabAnimationRotateAngle(180f)
        }

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                return false // True to keep the Speed Dial open
            }

            override fun onToggleChanged(isOpen: Boolean) {
                Utils.Companion.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
            }
        })

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(object : OnActionSelectedListener {
            override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
                when (actionItem.getId()) {
                    R.id.fab_album -> return false // false will close it without animation
                    R.id.fab_photo -> {
                        Navigator.onMoveToAlbum(this@AlbumDetailActivity)
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

    override fun onBackPressed() {
        if (mSpeedDialView.isOpen()) {
            mSpeedDialView.close()
        } else if (actionMode != null) {
            actionMode.finish()
        } else {
            super.onBackPressed()
        }
    }

    /*Init grant permission*/
    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            Navigator.onMoveCamera(this@AlbumDetailActivity, presenter.mainCategories)
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

    fun initRecycleView(layoutInflater: LayoutInflater?) {
        try {
            val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
            if (isVertical) {
                recyclerView.getRecycledViewPool().clear()
                verticalAdapter = AlbumDetailVerticalAdapter(getLayoutInflater(), this, this)
                val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(getApplicationContext())
                recyclerView.setLayoutManager(mLayoutManager)
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0)
                }
                recyclerView.addItemDecoration(DividerItemDecoration(this, 0))
                recyclerView.setAdapter(verticalAdapter)
                verticalAdapter.setDataSource(presenter.mList)
            } else {
                recyclerView.getRecycledViewPool().clear()
                adapter = AlbumDetailAdapter(layoutInflater, getApplicationContext(), this)
                val mLayoutManager: RecyclerView.LayoutManager = NpaGridLayoutManager(getApplicationContext(), 3)
                recyclerView.setLayoutManager(mLayoutManager)
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0)
                }
                recyclerView.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
                recyclerView.setAdapter(adapter)
                adapter.setDataSource(presenter.mList)
            }
        } catch (e: Exception) {
            e.message
        }
    }

    fun onShowDialog(status: EnumStatus?) {
        var content: String? = ""
        when (status) {
            EnumStatus.EXPORT -> {
                content = kotlin.String.format(getString(R.string.export_items), "" + countSelected)
            }
            EnumStatus.SHARE -> {
                content = kotlin.String.format(getString(R.string.share_items), "" + countSelected)
            }
            EnumStatus.DELETE -> {
                content = kotlin.String.format(getString(R.string.move_items_to_trash), "" + countSelected)
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
                        presenter.status = EnumStatus.CANCEL
                    }
                })
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val mListExporting: MutableList<ExportFiles?> = ArrayList<ExportFiles?>()
                        when (status) {
                            EnumStatus.SHARE -> {
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter.mListShare.clear()
                                var i = 0
                                while (i < presenter.mList.size) {
                                    val index: ItemModel = presenter.mList.get(i)
                                    if (index.isChecked) {
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                    mListExporting.add(exportFiles)
                                                }
                                            }
                                        }
                                    }
                                    i++
                                }
                                onStartProgressing()
                                ServiceManager.Companion.getInstance().setmListExport(mListExporting)
                                ServiceManager.Companion.getInstance().onExportingFiles()
                            }
                            EnumStatus.EXPORT -> {
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter.mListShare.clear()
                                var i = 0
                                while (i < presenter.mList.size) {
                                    val index: ItemModel = presenter.mList.get(i)
                                    if (index.isChecked) {
                                        val formatType = EnumFormatType.values()[index.formatType]
                                        when (formatType) {
                                            EnumFormatType.AUDIO -> {
                                                val input = File(index.originalPath)
                                                Utils.Companion.Log(TAG, "Name :" + index.originalName)
                                                var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                                if (storage.isFileExist(output.getAbsolutePath())) {
                                                    output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                                }
                                                if (storage.isFileExist(input.absolutePath)) {
                                                    presenter.mListShare.add(output)
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                    mListExporting.add(exportFiles)
                                                }
                                            }
                                            EnumFormatType.FILES -> {
                                                val input = File(index.originalPath)
                                                Utils.Companion.Log(TAG, "Name :" + index.originalName)
                                                var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                                if (storage.isFileExist(output.getAbsolutePath())) {
                                                    output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                                }
                                                if (storage.isFileExist(input.absolutePath)) {
                                                    presenter.mListShare.add(output)
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                    mListExporting.add(exportFiles)
                                                }
                                                Utils.Companion.Log(TAG, "Exporting file " + input.absolutePath)
                                            }
                                        }
                                    }
                                    i++
                                }
                                onStartProgressing()
                                ServiceManager.Companion.getInstance().setmListExport(mListExporting)
                                ServiceManager.Companion.getInstance().onExportingFiles()
                            }
                            EnumStatus.DELETE -> {
                                presenter.onDelete()
                            }
                        }
                    }
                })
        builder.show()
    }

    fun onDialogDownloadFile() {
        mDialogProgress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading))
        mDialogProgress.show()
        mDialogProgress.setCancelable(false)
    }

    /*Download file*/
    fun onEnableSyncData() {
        val mUser: User = Utils.Companion.getUserInfo()
        if (mUser != null) {
            if (mUser.verified) {
                if (!mUser.driveConnected) {
                    Navigator.onCheckSystem(this, null)
                } else {
                    onDialogDownloadFile()
                    ServiceManager.Companion.getInstance().onPreparingEnableDownloadData(Utils.Companion.getCheckedList(presenter.mList))
                    //ServiceManager.getInstance().getObservableDownload();
                }
            } else {
                Navigator.onVerifyAccount(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Companion.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    presenter.getData(EnumStatus.RELOAD)
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    presenter.getData(EnumStatus.RELOAD)
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
                            if (presenter.mainCategories == null) {
                                Utils.Companion.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val importFiles = ImportFilesModel(presenter.mainCategories, mimeTypeFile, path, i, false)
                            mListImportFiles.add(importFiles)
                            isReload = true
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
            Navigator.SHARE -> {
                if (actionMode != null) {
                    actionMode.finish()
                }
                Utils.Companion.Log(TAG, "share action")
            }
            else -> {
                Utils.Companion.Log(TAG, "Nothing to do")
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter.photos)
                tv_Photos.setText(photos)
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter.videos)
                tv_Videos.setText(videos)
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter.audios)
                tv_Audios.setText(audios)
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter.others)
                tv_Others.setText(others)
                if (actionMode != null) {
                    countSelected = 0
                    actionMode.finish()
                    llBottom.setVisibility(View.GONE)
                    isReload = true
                }
                val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                if (isVertical) {
                    verticalAdapter.setDataSource(presenter.mList)
                } else {
                    adapter.setDataSource(presenter.mList)
                }
            }
            EnumStatus.REFRESH -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter.photos)
                tv_Photos.setText(photos)
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter.videos)
                tv_Videos.setText(videos)
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter.audios)
                tv_Audios.setText(audios)
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter.others)
                tv_Others.setText(others)
                val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                if (isVertical) {
                    verticalAdapter.getDataSource().clear()
                    verticalAdapter.getDataSource().addAll(presenter.mList)
                } else {
                    adapter.getDataSource().clear()
                    adapter.getDataSource().addAll(presenter.mList)
                }
            }
            EnumStatus.DELETE -> {
                SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                if (actionMode != null) {
                    actionMode.finish()
                }
                isReload = true
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Int?) {
        when (status) {
            EnumStatus.DELETE -> {
                Utils.Companion.Log(TAG, "Position $`object`")
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER, `object`)
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}

    /*Action mode*/
    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater = mode.getMenuInflater()
            menuInflater.inflate(R.menu.menu_select, menu)
            actionMode = mode
            countSelected = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = getWindow()
                window.statusBarColor = ContextCompat.getColor(getContext(), R.color.material_orange_900)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item.getItemId()
            if (i == R.id.menu_item_select_all) {
                isSelectAll = !isSelectAll
                selectAll()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (countSelected > 0) {
                deselectAll()
            }
            actionMode = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val themeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
                if (themeApp != null) {
                    val window: Window = getWindow()
                    window.statusBarColor = ContextCompat.getColor(getContext(), themeApp.getPrimaryDarkColor())
                }
            }
        }
    }

    private fun toggleSelection(position: Int) {
        presenter.mList.get(position).isChecked = !presenter.mList.get(position).isChecked
        if (presenter.mList.get(position).isChecked) {
            countSelected++
        } else {
            countSelected--
        }
        onShowUI()
        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, position)
    }

    private fun deselectAll() {
        var isExport = false
        val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
        var i = 0
        val l: Int = presenter.mList.size
        while (i < l) {
            when (presenter.status) {
                EnumStatus.EXPORT -> {
                    if (presenter.mList.get(i).isChecked) {
                        presenter.mList.get(i).isExport = true
                        presenter.mList.get(i).isDeleteLocal = true
                        SQLHelper.updatedItem(presenter.mList.get(i))
                    }
                    isExport = true
                }
                EnumStatus.CANCEL -> {
                    val items: ItemModel = presenter.mList.get(i)
                    if (items.isChecked) {
                        items.isChecked = false
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
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, i)
                    }
                }
                else -> {
                    if (presenter.mList.get(i).isChecked) {
                        presenter.mList.get(i).isChecked = false
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, i)
                    }
                }
            }
            i++
        }
        countSelected = 0
        onShowUI()
        if (isExport) {
            onCheckDelete()
        }
    }

    fun onCheckDelete() {
        val mList: MutableList<ItemModel?> = presenter.mList
        var i = 0
        val l = mList.size
        while (i < l) {
            if (presenter.mList.get(i).isChecked) {
                val formatTypeFile = EnumFormatType.values()[mList[i].formatType]
                if (formatTypeFile == EnumFormatType.AUDIO && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if (formatTypeFile == EnumFormatType.FILES && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if (mList[i].global_original_id == null and mList[i].global_thumbnail_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else {
                    mList[i].deleteAction = EnumDelete.DELETE_WAITING.ordinal
                    SQLHelper.updatedItem(mList[i])
                    Utils.Companion.Log(TAG, "ServiceManager waiting for delete")
                }
                storage.deleteDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePrivate() + mList[i].items_id)
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER, i)
            }
            i++
        }
        presenter.getData(EnumStatus.REFRESH)
    }

    fun selectAll() {
        try {
            var countSelect = 0
            for (i in presenter.mList.indices) {
                presenter.mList.get(i).isChecked = isSelectAll
                if (presenter.mList.get(i).isChecked) {
                    countSelect++
                }
            }
            countSelected = countSelect
            onShowUI()
            onUpdateAdapter(EnumStatus.UPDATE_ENTIRE_ADAPTER, 0)
            actionMode.setTitle(countSelected.toString() + " " + getString(R.string.selected))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onShowUI() {
        try {
            runOnUiThread(Runnable {
                if (countSelected == 0) {
                    llBottom.setVisibility(View.GONE)
                    mSpeedDialView.setVisibility(View.VISIBLE)
                } else {
                    llBottom.setVisibility(View.VISIBLE)
                    mSpeedDialView.setVisibility(View.INVISIBLE)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*Gallery action*/
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
                    .setFakePIN(presenter.mainCategories.isFakePin)
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

    override fun onMoveAlbumSuccessful() {}
    override fun getListItems(): MutableList<ItemModel?>? {
        return presenter.mList
    }

    fun onUpdateAdapter(status: EnumStatus?, position: Int) {
        val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
        when (status) {
            EnumStatus.UPDATE_ENTIRE_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter.getDataSource() != null) {
                            verticalAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter.getDataSource() != null) {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            EnumStatus.REMOVE_AT_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter.getDataSource() != null) {
                            if (verticalAdapter.getDataSource().size > position) {
                                verticalAdapter.removeAt(position)
                            }
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter.getDataSource() != null) {
                            if (adapter.getDataSource().size > position) {
                                adapter.removeAt(position)
                            }
                        }
                    }
                }
            }
            EnumStatus.UPDATE_AT_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter.getDataSource() != null) {
                            if (verticalAdapter.getDataSource().size > position) {
                                verticalAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter.getDataSource() != null) {
                            if (adapter.getDataSource().size > position) {
                                adapter.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = AlbumDetailActivity::class.java.simpleName
    }
}