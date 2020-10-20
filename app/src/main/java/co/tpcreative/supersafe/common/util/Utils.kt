package co.tpcreative.supersafe.common.util

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import co.tpcreative.supersafe.BuildConfigimport
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.User
import com.afollestad.materialdialogs.MaterialDialog
import com.google.api.client.util.Base64
import com.google.common.base.Charsets
import com.google.gson.reflect.TypeToken
import com.snatik.storage.Storage
import org.greenrobot.eventbus.EventBus
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
/**
 * Created by pc on 07/16/2017.
 */
class Utils private constructor() {
    fun onChangeCategories(mainCategories: MainCategoryModel?): Boolean {
        try {
            val hex_name = getHexCode(mainCategories.categories_name)
            val mIsFakePin: Boolean = mainCategories.isFakePin
            val response: MainCategoryModel = SQLHelper.getCategoriesItemId(hex_name, mIsFakePin)
            if (response == null) {
                mainCategories.categories_hex_name = hex_name
                mainCategories.isChange = true
                mainCategories.isSyncOwnServer = false
                SQLHelper.updateCategory(mainCategories)
                return true
            }
            Log(TAG, "value changed :" + Gson().toJson(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    companion object {
        val GOOGLE_CONSOLE_KEY: String? = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk+6HXAFTNx3LbODafbpgsLqkdyMqMEvIYt55lqTjLIh0PkoAX7oSAD0fY7BXW0Czuys13hNNdyzmDjQe76xmUWTNfXM1vp0JQtStl7tRqNaFuaRje59HKRLpRTW1MGmgKw/19/18EalWTjbGOW7C2qZ5eGIOvGfQvvlraAso9lCTeEwze3bmGTc7B8MOfDqZHETdavSVgVjGJx/K10pzAauZFGvZ+ryZtU0u+9ZSyGx1CgHysmtfcZFKqZLbtOxUQHpBMeJf2M1LReqbR1kvJiAeLYqdOMWzmmNcsEoG6g/e+F9ZgjZjoQzqhWsrTE2IQZAaiwU4EezdqqruNXx6uwIDAQAB"

        // utility function
        var FORMAT_TIME: String? = "yyyy-MM-dd HH:mm:ss"
        var FORMAT_TIME_FILE_NAME: String? = "yyyyMMdd_HHmmss"
        const val COUNT_RATE = 9
        const val START_TIMER: Long = 5000
        private val storage: Storage? = Storage(SuperSafeApplication.Companion.getInstance())
        private val TAG = Utils::class.java.simpleName
        private fun bytesToHexString(bytes: ByteArray?): String? {
            // http://stackoverflow.com/questions/332079
            val sb = StringBuffer()
            for (i in bytes.indices) {
                val hex = Integer.toHexString(0xFF and bytes.get(i))
                if (hex.length == 1) {
                    sb.append('0')
                }
                sb.append(hex)
            }
            return sb.toString()
        }

        fun isValidEmail(target: CharSequence?): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }

        fun isValid(target: CharSequence?): Boolean {
            return !TextUtils.isEmpty(target)
        }

        fun showDialog(activity: Activity?, message: String?) {
            val builder = MaterialDialog.Builder(activity)
            builder.title(R.string.confirm)
            builder.content(message)
            builder.positiveText(R.string.ok)
            builder.show()
        }

        fun showDialog(activity: Activity?, message: String?, ls: ServiceManagerSyncDataListener?) {
            val builder = MaterialDialog.Builder(activity)
            builder.title(R.string.confirm)
            builder.content(message)
            builder.positiveText(R.string.ok)
            builder.negativeText(R.string.cancel)
            builder.onNegative(object : SingleButtonCallback {
                override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    ls.onCancel()
                }
            })
            builder.onPositive(object : SingleButtonCallback {
                override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    ls.onCompleted()
                }
            })
            builder.show()
        }

        fun mCreateAndSaveFileOverride(fileName: String?, path_folder_name: String?, responseJson: String?, append: Boolean): Boolean {
            Log(TAG, "path $path_folder_name")
            val newLine = System.getProperty("line.separator")
            return try {
                val root = File("$path_folder_name/$fileName")
                val saved = storage.getSize(root, SizeUnit.MB)
                if (saved >= 1) {
                    storage.deleteFile(root.absolutePath)
                }
                if (!root.exists()) {
                    val parentFolder = File(path_folder_name)
                    if (!parentFolder.exists()) {
                        parentFolder.mkdirs()
                    }
                    root.createNewFile()
                }
                val file = FileWriter(root, append)
                file.write("\r\n")
                file.write(responseJson)
                file.write("\r\n")
                file.flush()
                file.close()
                true
            } catch (e: IOException) {
                Log(TAG, e.message)
                false
            }
        }

        fun hideSoftKeyboard(context: Activity?) {
            val view: View = context.getCurrentFocus()
            if (view != null) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun hideKeyboard(view: View?) {
            // Check if no view has focus:
            if (view != null) {
                val inputManager = SuperSafeApplication.Companion.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        fun dpToPx(dp: Int): Int {
            val r: Resources = SuperSafeApplication.Companion.getInstance().getResources()
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(), r.displayMetrics) as Int
        }

        fun showKeyboard(view: View?) {
            // Check if no view has focus:
            if (view != null) {
                val inputManager = SuperSafeApplication.Companion.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun getPackagePath(context: Context?): File? {
            return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    ".temporary.jpg")
        }

        fun getMimeType(url: String?): String? {
            var type: String? = null
            val extension: String = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }

        fun getFileExtension(url: String?): String? {
            return FilenameUtils.getExtension(url).toLowerCase()
        }

        fun Log(TAG: String?, message: String?) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d(TAG, message)
            }
        }

        fun getUUId(): String? {
            return try {
                UUID.randomUUID().toString()
            } catch (e: Exception) {
                "" + System.currentTimeMillis()
            }
        }

        fun getCurrentDateTime(): String? {
            val date = Date()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun getCurrentDateTime(formatName: String?): String? {
            val date = Date()
            val dateFormat = SimpleDateFormat(formatName, Locale.getDefault())
            return dateFormat.format(date)
        }

        fun getCurrentDate(value: String?): String? {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            try {
                val mDate = sdf.parse(value)
                val dateFormat = SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault())
                return dateFormat.format(mDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return ""
        }

        fun getCurrentDateTimeFormat(): String? {
            val date = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun getHexCode(value: String?): String? {
            return Base64.encodeBase64String(value.toUpperCase().toByteArray(Charsets.UTF_8))
        }

        fun onExportAndImportFile(input: String?, output: String?, ls: ServiceManagerSyncDataListener?) {
            val storage = Storage(SuperSafeApplication.Companion.getInstance())
            val mFile = storage.getFiles(input)
            try {
                for (index in mFile) {
                    if (storage.isFileExist(index.absolutePath)) {
                        storage.createFile(File(output + index.name), File(index.absolutePath), object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                ls.onError()
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {}
                        })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ls.onCompleted()
            }
        }

        fun showToast(context: Context?, @StringRes text: Int, isLong: Boolean) {
            showToast(context, context.getString(text), isLong)
        }

        fun showToast(context: Context?, text: String?, isLong: Boolean) {
            Toast.makeText(context, text, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }

        fun showInfoSnackbar(view: View?, @StringRes text: Int, isLong: Boolean) {
            Handler().postDelayed({
                multilineSnackbar(
                        Snackbar.make(
                                view, text,
                                if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
                ).show()
            }, 100)
        }

        fun showGotItSnackbar(view: View?, @StringRes text: Int) {
            Handler().postDelayed({
                multilineSnackbar(
                        Snackbar.make(
                                view, text, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, View.OnClickListener { })
                ).show()
            }, 200)
        }

        fun showGotItSnackbar(view: View?, @StringRes text: String?) {
            onObserveData(START_TIMER, Listener {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, View.OnClickListener { })
                ).show()
            })
        }

        fun showGotItSnackbar(view: View?, @StringRes text: Int, ls: ServiceManagerSyncDataListener?) {
            onObserveData(START_TIMER, Listener {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, View.OnClickListener { ls.onCompleted() })
                ).show()
            })
        }

        private fun multilineSnackbar(snackbar: Snackbar?): Snackbar? {
            val textView: TextView = snackbar.getView().findViewById(R.id.snackbar_text) as TextView
            textView.setMaxLines(5)
            return snackbar
        }

        fun slideToRight(view: View?) {
            val animate = TranslateAnimation(0, view.getWidth(), 0, 0)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
            view.setVisibility(View.GONE)
        }

        // To animate view slide out from right to left
        fun slideToLeft(view: View?) {
            val animate = TranslateAnimation(0, -view.getWidth(), 0, 0)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
            view.setVisibility(View.GONE)
        }

        // To animate view slide out from top to bottom
        fun slideToBottomHeader(view: View?) {
            val animate = TranslateAnimation(0, 0, -view.getHeight(), 0)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from bottom to top
        fun slideToTopHeader(view: View?) {
            Log(TAG, " " + view.getHeight())
            val animate = TranslateAnimation(0, 0, 0, -view.getHeight())
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from top to bottom
        fun slideToBottomFooter(view: View?) {
            val animate = TranslateAnimation(0, 0, 0, view.getHeight())
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from bottom to top
        fun slideToTopFooter(view: View?) {
            Log(TAG, " " + view.getHeight())
            val animate = TranslateAnimation(0, 0, view.getHeight(), 0)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        fun stringToHex(content: String?): String? {
            return Base64.encodeBase64String(content.toByteArray(Charsets.UTF_8))
        }

        fun hexToString(hex: String?): String? {
            try {
                val data = Base64.decodeBase64(hex.toByteArray())
                return String(data, Charsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun mediaTypeSupport(): HashMap<String?, MimeTypeFile?>? {
            val hashMap = HashMap<String?, MimeTypeFile?>()
            hashMap["mp4"] = MimeTypeFile(".mp4", EnumFormatType.VIDEO, "video/mp4")
            hashMap["3gp"] = MimeTypeFile(".3gp", EnumFormatType.VIDEO, "video/3gp")
            hashMap["wmv"] = MimeTypeFile(".wmv", EnumFormatType.VIDEO, "video/wmv")
            hashMap["mkv"] = MimeTypeFile(".mkv", EnumFormatType.VIDEO, "video/mkv")
            hashMap["m4a"] = MimeTypeFile(".m4a", EnumFormatType.AUDIO, "audio/m4a")
            hashMap["aac"] = MimeTypeFile(".aac", EnumFormatType.AUDIO, "audio/aac")
            hashMap["mp3"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mp3")
            hashMap["wav"] = MimeTypeFile(".wav", EnumFormatType.AUDIO, "audio/wav")
            hashMap["jpg"] = MimeTypeFile(".jpg", EnumFormatType.IMAGE, "image/jpeg")
            hashMap["jpeg"] = MimeTypeFile(".jpeg", EnumFormatType.IMAGE, "image/jpeg")
            hashMap["png"] = MimeTypeFile(".png", EnumFormatType.IMAGE, "image/png")
            hashMap["gif"] = MimeTypeFile(".gif", EnumFormatType.IMAGE, "image/gif")
            return hashMap
        }

        fun mimeTypeSupport(): HashMap<String?, MimeTypeFile?>? {
            val hashMap = HashMap<String?, MimeTypeFile?>()
            hashMap["video/mp4"] = MimeTypeFile(".mp4", EnumFormatType.VIDEO, "video/mp4")
            hashMap["video/3gp"] = MimeTypeFile(".3gp", EnumFormatType.VIDEO, "video/3gp")
            hashMap["video/wmv"] = MimeTypeFile(".wmv", EnumFormatType.VIDEO, "video/wmv")
            hashMap["video/mkv"] = MimeTypeFile(".mkv", EnumFormatType.VIDEO, "video/mkv")
            hashMap["audio/m4a"] = MimeTypeFile(".m4a", EnumFormatType.AUDIO, "audio/m4a")
            hashMap["audio/aac"] = MimeTypeFile(".aac", EnumFormatType.AUDIO, "audio/aac")
            hashMap["audio/mp3"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mp3")
            hashMap["audio/mpeg"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mpeg")
            hashMap["audio/wav"] = MimeTypeFile(".wav", EnumFormatType.AUDIO, "audio/wav")
            hashMap["image/jpeg"] = MimeTypeFile(".jpg", EnumFormatType.IMAGE, "image/jpeg")
            hashMap["image/png"] = MimeTypeFile(".png", EnumFormatType.IMAGE, "image/png")
            hashMap["image/gif"] = MimeTypeFile(".gif", EnumFormatType.IMAGE, "image/gif")
            hashMap["application/msword"] = MimeTypeFile(".doc", EnumFormatType.FILES, "application/msword")
            hashMap["application/vnd.openxmlformats-officedocument.wordprocessingml.document"] = MimeTypeFile(".docx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            hashMap["application/vnd.openxmlformats-officedocument.wordprocessingml.template"] = MimeTypeFile(".dotx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            hashMap["application/vnd.ms-word.document.macroEnabled.12"] = MimeTypeFile(".dotm", EnumFormatType.FILES, "application/vnd.ms-word.document.macroEnabled.12")
            hashMap["application/vnd.ms-excel"] = MimeTypeFile(".xls", EnumFormatType.FILES, "application/vnd.ms-excel")
            hashMap["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"] = MimeTypeFile(".xlsx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            hashMap["application/vnd.openxmlformats-officedocument.spreadsheetml.template"] = MimeTypeFile(".xltx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
            hashMap["application/vnd.ms-excel.sheet.macroEnabled.12"] = MimeTypeFile(".xlsm", EnumFormatType.FILES, "application/vnd.ms-excel.sheet.macroEnabled.12")
            hashMap["application/vnd.ms-excel.template.macroEnabled.12"] = MimeTypeFile(".xltm", EnumFormatType.FILES, "application/vnd.ms-excel.template.macroEnabled.12")
            hashMap["application/vnd.ms-excel.addin.macroEnabled.12"] = MimeTypeFile(".xlam", EnumFormatType.FILES, "application/vnd.ms-excel.addin.macroEnabled.12")
            hashMap["application/vnd.ms-excel.sheet.binary.macroEnabled.12"] = MimeTypeFile(".xlsb", EnumFormatType.FILES, "application/vnd.ms-excel.sheet.binary.macroEnabled.12")
            hashMap["application/vnd.ms-powerpoint"] = MimeTypeFile(".ppt", EnumFormatType.FILES, "application/vnd.ms-powerpoint")
            hashMap["application/vnd.openxmlformats-officedocument.presentationml.presentation"] = MimeTypeFile(".pptx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            hashMap["application/vnd.openxmlformats-officedocument.presentationml.template"] = MimeTypeFile(".potx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.presentationml.template")
            hashMap["application/vnd.ms-powerpoint.addin.macroEnabled.12"] = MimeTypeFile(".ppsx", EnumFormatType.FILES, "application/vnd.ms-powerpoint.addin.macroEnabled.12")
            hashMap["application/vnd.ms-powerpoint.presentation.macroEnabled.12t"] = MimeTypeFile(".pptm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            hashMap["application/vnd.ms-powerpoint.template.macroEnabled.12"] = MimeTypeFile(".potm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.template.macroEnabled.12")
            hashMap["application/vnd.ms-powerpoint.slideshow.macroEnabled.12"] = MimeTypeFile(".ppsm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.slideshow.macroEnabled.12")
            hashMap["application/vnd.ms-access"] = MimeTypeFile(".mdb", EnumFormatType.FILES, "application/vnd.ms-access")
            return hashMap
        }

        fun DeviceInfo(): String? {
            try {
                val manufacturer: String = Build.MANUFACTURER
                val model: String = Build.MODEL
                val version: Int = Build.VERSION.SDK_INT
                val versionRelease: String = Build.VERSION.RELEASE
                return """manufacturer $manufacturer 
 model $model 
 version $version 
 versionRelease $versionRelease 
 app version name ${BuildConfig.VERSION_NAME}"""
            } catch (e: Exception) {
                onWriteLog(e.message, EnumStatus.DEVICE_ABOUT)
            }
            return "Exception"
        }

        fun onWriteLog(message: String?, status: EnumStatus?) {
            if (!BuildConfig.DEBUG) {
                return
            }
            if (status == null) {
                mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.Companion.getInstance().getSupersafeLog(), "----Time----" + getCurrentDateTimeFormat() + " ----Content--- :" + message, true)
            } else {
                mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.Companion.getInstance().getSupersafeLog(), "----Time----" + getCurrentDateTimeFormat() + " ----Status---- :" + status.name + " ----Content--- :" + message, true)
            }
        }

        private fun appendLog(text: String?) {
            val logFile = File(SuperSafeApplication.Companion.getInstance().getFileLogs())
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append("""
    $text
    
    """.trimIndent())
                buf.newLine()
                buf.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }

        fun onWriteLog(action: EnumStatus?, status: EnumStatus?, value: String?) {
            if (!BuildConfig.DEBUG) {
                return
            }
            onCheck()
            appendLog("Version " + BuildConfig.VERSION_NAME.toString() + " ; created date time :" + getCurrentDateTime(FORMAT_TIME).toString() + " ; Action :" + action.name.toString() + " ; Status: " + status.name.toString() + " ; message log: " + value)
        }

        fun onCheck() {
            val file = File(SuperSafeApplication.Companion.getInstance().getFileLogs())
            if (file.exists()) {
                val mSize = +SuperSafeApplication.Companion.getInstance().getStorage().getSize(file, SizeUnit.MB) as Long
                if (mSize > 2) {
                    SuperSafeApplication.Companion.getInstance().getStorage().deleteFile(file.absolutePath)
                }
            }
        }

        fun shareMultiple(files: MutableList<File?>?, context: Activity?) {
            val uris = ArrayList<Uri?>()
            for (file in files) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uri: Uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
                    uris.add(uri)
                } else {
                    uris.add(Uri.fromFile(file))
                }
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.setType("*/*")
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            context.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.share)), Navigator.SHARE)
        }

        private fun getScreenSize(activity: Context?): Point? {
            val display: Display = (activity as Activity?).getWindowManager().getDefaultDisplay()
            val size = Point()
            display.getSize(size)
            return size
        }

        fun getScreenWidth(activity: Context?): Int {
            return getScreenSize(activity).x
        }

        fun getScreenHeight(activity: Context?): Int {
            return getScreenSize(activity).y
        }

        fun isSensorAvailable(): Boolean {
            try {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.checkSelfPermission(SuperSafeApplication.Companion.getInstance(), Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                            SuperSafeApplication.Companion.getInstance().getSystemService(FingerprintManager::class.java).isHardwareDetected()
                } else {
                    FingerprintManagerCompat.from(SuperSafeApplication.Companion.getInstance()).isHardwareDetected()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun getFontString(content: Int, value: String?): String? {
            val themeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
            return SuperSafeApplication.Companion.getInstance().getString(content, "<font color='" + themeApp.getAccentColorHex() + "'>" + "<b>" + value + "</b>" + "</font>")
        }

        fun getFontString(content: Int, value: String?, fontSize: Int): String? {
            val themeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
            return SuperSafeApplication.Companion.getInstance().getString(content, "<font size='" + fontSize + "' color='" + themeApp.getAccentColorHex() + "'>" + "<b>" + value + "</b>" + "</font>")
        }

        fun appInstalledOrNot(uri: String?): Boolean {
            val pm: PackageManager = SuperSafeApplication.Companion.getInstance().getPackageManager()
            try {
                pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return false
        }

        fun objectToHashMap(items: PurchaseData?): MutableMap<String?, Any?>? {
            val type = object : TypeToken<MutableMap<String?, Any?>?>() {}.type
            return Gson().fromJson(Gson().toJson(items), type)
        }

        fun onDeleteTemporaryFile() {
            try {
                val rootDataDir: File = SuperSafeApplication.Companion.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val list = rootDataDir.listFiles()
                for (i in list.indices) {
                    Log(TAG, "File list :" + list[i].absolutePath)
                    SuperSafeApplication.Companion.getInstance().getStorage().deleteFile(list[i].absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun isLandscape(activity: AppCompatActivity?): Boolean {
            val landscape: Boolean
            val displaymetrics = DisplayMetrics()
            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics)
            val width: Int = displaymetrics.widthPixels
            val height: Int = displaymetrics.heightPixels
            landscape = if (width < height) {
                false
            } else {
                true
            }
            return landscape
        }

        /**
         * @return Number of bytes available on External storage
         */
        fun getAvailableSpaceInBytes(): Long {
            var availableSpace = -1L
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            availableSpace = stat.getAvailableBlocks() as Long * stat.getBlockSize() as Long
            return availableSpace
        }

        fun writePinToSharedPreferences(pin: String?) {
            //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
            SuperSafeApplication.Companion.getInstance().writeKey(pin)
        }

        fun getPinFromSharedPreferences(): String? {
            //PrefsController.getString(getString(R.string.key_pin), "");
            return SuperSafeApplication.Companion.getInstance().readKey()
        }

        fun writeFakePinToSharedPreferences(pin: String?) {
            //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
            SuperSafeApplication.Companion.getInstance().writeFakeKey(pin)
        }

        fun getFakePinFromSharedPreferences(): String? {
            //PrefsController.getString(getString(R.string.key_pin), "");
            return SuperSafeApplication.Companion.getInstance().readFakeKey()
        }

        fun isEnabledFakePin(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.Companion.getInstance().getString(R.string.key_fake_pin), false)
        }

        fun isExistingFakePin(pin: String?, currentPin: String?): Boolean {
            return if (pin == currentPin) {
                true
            } else false
        }

        fun isExistingRealPin(pin: String?, currentPin: String?): Boolean {
            return if (pin == currentPin) {
                true
            } else false
        }

        fun onCheckNewVersion() {
            val current_code_version: Int = PrefsController.getInt(SuperSafeApplication.Companion.getInstance().getString(R.string.current_code_version), 0)
            if (current_code_version == BuildConfig.VERSION_CODE) {
                Log(TAG, "Already install this version")
                return
            } else {
                PrefsController.putInt(SuperSafeApplication.Companion.getInstance().getString(R.string.current_code_version), BuildConfig.VERSION_CODE)
                PrefsController.putBoolean(SuperSafeApplication.Companion.getInstance().getString(R.string.we_are_a_team), false)
                Log(TAG, "New install this version")
            }
        }

        fun onUpdatedCountRate() {
            val count: Int = PrefsController.getInt(SuperSafeApplication.Companion.getInstance().getString(R.string.key_count_to_rate), 0)
            if (count > 999) {
                PrefsController.putInt(SuperSafeApplication.Companion.getInstance().getString(R.string.key_count_to_rate), 0)
            } else {
                PrefsController.putInt(SuperSafeApplication.Companion.getInstance().getString(R.string.key_count_to_rate), count + 1)
            }
        }

        fun onObserveData(second: Long, ls: Listener?) {
            Completable.timer(second, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable?) {}
                        override fun onComplete() {
                            Log(TAG, "Completed")
                            ls.onStart()
                        }

                        override fun onError(e: Throwable?) {}
                    })
        }

        fun onHomePressed() {
            PrefsController.putInt(SuperSafeApplication.Companion.getInstance().getString(R.string.key_screen_status), EnumPinAction.SCREEN_LOCK.ordinal)
            Log(TAG, "Pressed home button")
            if (!SingletonManager.Companion.getInstance().isVisitLockScreen()) {
                Navigator.onMoveToVerifyPin(SuperSafeApplication.Companion.getInstance().getActivity(), EnumPinAction.NONE)
                SingletonManager.Companion.getInstance().setVisitLockScreen(true)
                Log(TAG, "Verify pin")
            } else {
                Log(TAG, "Verify pin already")
            }
        }

        fun getUserInfo(): User? {
            try {
                val value: String = PrefsController.getString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_user), null)
                if (value != null) {
                    val mUser: User = Gson().fromJson(value, User::class.java)
                    if (mUser != null) {
                        return mUser
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun getUserId(): String? {
            try {
                val mUser = getUserInfo()
                if (mUser != null) {
                    return mUser.email
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /*Checking allow sync data*/
        fun isAllowSyncData(): Boolean {
            return isAllowRequestDriveApis()
        }

        fun isPauseSync(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.Companion.getInstance().getString(R.string.key_pause_cloud_sync), false)
        }

        fun isCheckSyncSuggestion(): Boolean {
            val name: String = SuperSafeApplication.Companion.getInstance().getString(R.string.key_count_sync)
            val mCount: Int = PrefsController.getInt(name, 0)
            val mSynced = getUserInfo().driveConnected
            if (!mSynced) {
                if (mCount == 5) {
                    PrefsController.putInt(name, 0)
                    return true
                } else {
                    PrefsController.putInt(name, mCount + 1)
                }
            }
            return false
        }

        fun getAccessToken(): String? {
            try {
                val user = getUserInfo()
                if (user != null) {
                    return user.author.session_token
                }
            } catch (e: Exception) {
            }
            return SecurityUtil.DEFAULT_TOKEN
        }

        fun onPushEventBus(status: EnumStatus?) {
            EventBus.getDefault().post(status)
        }

        /*Improved sync data*/ /*Filter only item already synced*/
        fun filterOnlyGlobalOriginalId(list1: MutableList<ItemModel?>?): MutableList<ItemModel?>? {
            val mList: MutableList<ItemModel?> = ArrayList<ItemModel?>()
            for (index in list1) {
                if (index.isSyncCloud) {
                    mList.add(index)
                }
            }
            return mList
        }

        /*Remove duplicated item for download id*/
        fun clearListFromDuplicate(globalList: MutableList<ItemModel?>?, localList: MutableList<ItemModel?>?): MutableList<ItemModel?>? {
            val modelMap: MutableMap<String?, ItemModel?> = HashMap<String?, ItemModel?>()
            val mList: MutableList<ItemModel?> = ArrayList<ItemModel?>()
            if (globalList != null) {
                if (globalList.size == 0) {
                    return mList
                }
            }

            /*Merged local data*/
            val mLocalList: MutableList<ItemModel?>? = getMergedOriginalThumbnailList(false, localList)
            for (index in mLocalList) {
                modelMap[index.global_id] = index
            }

            /*Merged global data*/
            val mGlobalList: MutableList<ItemModel?>? = getMergedOriginalThumbnailList(true, globalList)
            Log(TAG, "onPreparingSyncData ==> Index download globalList " + Gson().toJson(globalList))
            Log(TAG, "onPreparingSyncData ==> Index download map " + Gson().toJson(modelMap))
            Log(TAG, "onPreparingSyncData ==> Index download list " + Gson().toJson(mGlobalList))
            for (index in mGlobalList) {
                val item: ItemModel? = modelMap[index.global_id]
                if (item != null) {
                    if (index.global_id != item.global_id) {
                        mList.add(index)
                        Log(TAG, "onPreparingSyncData ==> Index download" + Gson().toJson(index))
                    }
                } else {
                    mList.add(index)
                    Log(TAG, "onPreparingSyncData ==> Index download add " + Gson().toJson(index))
                }
            }
            return mList
        }

        /*Merge list to hash map for upload, download and delete*/
        fun mergeListToHashMap(mList: MutableList<ItemModel?>?): MutableMap<String?, ItemModel?>? {
            val map: MutableMap<String?, ItemModel?> = HashMap<String?, ItemModel?>()
            for (index in mList) {
                map[index.unique_id] = index
            }
            return map
        }

        /*Get the first of item data*/
        fun getArrayOfIndexHashMap(mMapDelete: MutableMap<String?, ItemModel?>?): ItemModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: ItemModel = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Get the first of category data*/
        fun getArrayOfIndexCategoryHashMap(mMapDelete: MutableMap<String?, MainCategoryModel?>?): MainCategoryModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: MainCategoryModel = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Delete hash map after delete Google drive or Server system*/
        fun deletedIndexOfCategoryHashMap(itemModel: MainCategoryModel?, map: MutableMap<String?, MainCategoryModel?>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        map.remove(itemModel.unique_id)
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

        /*Merge list to hash map for upload, download and delete*/
        fun mergeListToCategoryHashMap(mList: MutableList<MainCategoryModel?>?): MutableMap<String?, MainCategoryModel?>? {
            val map: MutableMap<String?, MainCategoryModel?> = HashMap<String?, MainCategoryModel?>()
            for (index in mList) {
                map[index.unique_id] = index
            }
            return map
        }

        /*Merge list original and thumbnail as list*/
        fun getMergedOriginalThumbnailList(isNotSync: Boolean, mDataList: MutableList<ItemModel?>?): MutableList<ItemModel?>? {
            val mList: MutableList<ItemModel?> = ArrayList<ItemModel?>()
            for (index in mDataList) {
                if (isNotSync) {
                    if (!index.originalSync) {
                        mList.add(ItemModel(index, true))
                    }
                    val mType = EnumFormatType.values()[index.formatType]
                    if (EnumFormatType.IMAGE == mType || EnumFormatType.VIDEO == mType) {
                        if (!index.thumbnailSync) {
                            mList.add(ItemModel(index, false))
                        }
                    }
                } else {
                    if (index.originalSync) {
                        mList.add(ItemModel(index, true))
                    }
                    val mType = EnumFormatType.values()[index.formatType]
                    if (EnumFormatType.IMAGE == mType || EnumFormatType.VIDEO == mType) {
                        if (index.thumbnailSync) {
                            mList.add(ItemModel(index, false))
                        }
                    }
                }
            }
            return mList
        }

        /*Delete hash map after delete Google drive and Server system*/
        fun deletedIndexOfHashMap(itemModel: ItemModel?, map: MutableMap<String?, ItemModel?>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        map.remove(itemModel.unique_id)
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

        /*------------------------Import area-------------------*/ /*Add list to hash map for import*/
        fun mergeListToHashMapImport(mList: MutableList<ImportFilesModel?>?): MutableMap<String?, ImportFilesModel?>? {
            val map: MutableMap<String?, ImportFilesModel?> = HashMap<String?, ImportFilesModel?>()
            for (index in mList) {
                map[index.unique_id] = index
            }
            return map
        }

        /*Get the first of data for import*/
        fun getArrayOfIndexHashMapImport(mMapDelete: MutableMap<String?, ImportFilesModel?>?): ImportFilesModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: ImportFilesModel = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Delete hash map after delete Google drive and Server system for import*/
        fun deletedIndexOfHashMapImport(itemModel: ImportFilesModel?, map: MutableMap<String?, ImportFilesModel?>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        map.remove(itemModel.unique_id)
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

        /*Check saver space*/
        fun getSaverSpace(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.Companion.getInstance().getString(R.string.key_saving_space), false)
        }

        /*Delete folder*/
        fun onDeleteItemFolder(item_id: String?) {
            val path: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate() + item_id
            Log(TAG, "Delete folder $path")
            SuperSafeApplication.Companion.getInstance().getStorage().deleteDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePrivate() + item_id)
        }

        fun onDeleteFile(file_path: String?) {
            SuperSafeApplication.Companion.getInstance().getStorage().deleteFile(file_path)
        }

        /*Create folder*/
        fun createDestinationDownloadItem(items_id: String?): String? {
            val path: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
            return "$path$items_id/"
        }

        fun getOriginalPath(currentTime: String?, items_id: String?): String? {
            val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
            val pathContent = "$rootPath$items_id/"
            createDirectory(pathContent)
            return pathContent + currentTime
        }

        /*Create folder*/
        fun createDirectory(path: String?): Boolean {
            val directory = File(path)
            if (directory.exists()) {
                android.util.Log.w(TAG, "Directory '$path' already exists")
                return false
            }
            return directory.mkdirs()
        }

        fun isNotEmptyOrNull(value: String?): Boolean {
            return if (value == null || value == "" || value == "null") {
                false
            } else true
        }

        fun getCheckedList(mList: MutableList<ItemModel?>?): MutableList<ItemModel?>? {
            val mResult: MutableList<ItemModel?> = ArrayList<ItemModel?>()
            for (index in mList) {
                if (index.isChecked) {
                    mResult.add(index)
                }
            }
            return mResult
        }

        fun checkSaverToDelete(originalPath: String?, isOriginalGlobalId: Boolean) {
            if (getSaverSpace()) {
                if (SuperSafeApplication.Companion.getInstance().getStorage().isFileExist(originalPath)) {
                    if (isOriginalGlobalId) {
                        onDeleteFile(originalPath)
                    }
                }
            }
        }

        fun setUserPreShare(user: User?) {
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_user), Gson().toJson(user))
        }

        fun onScanFile(activity: Context?, nameLogs: String?) {
            if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                Log(TAG, "Granted permission....")
                val storage: Storage = SuperSafeApplication.Companion.getInstance().getStorage()
                if (storage != null) {
                    val file = File(storage.externalStorageDirectory + "/" + nameLogs)
                    MediaScannerConnection.scanFile(activity, arrayOf(file.absolutePath), null, null)
                    MediaScannerConnection.scanFile(activity, arrayOf(storage.externalStorageDirectory), null, null)
                    storage.createFile(storage.externalStorageDirectory + "/" + nameLogs, "")
                }
            } else {
                Log(TAG, "No permission")
            }
        }

        fun checkRequestUploadItemData() {
            val mResult: MutableList<ItemModel?> = SQLHelper.getItemListUpload()
            if (mResult != null) {
                if (mResult.size > 0 && isCheckAllowUpload()) {
                    ServiceManager.Companion.getInstance().onPreparingSyncData()
                    return
                }
            }
            ServiceManager.Companion.getInstance().onDefaultValue()
            Log(TAG, "All items already synced...........")
        }

        fun isRealCheckedOut(orderId: String?): Boolean {
            return if (orderId.contains("GPA")) {
                true
            } else false
        }

        fun setCheckoutItems(checkoutItems: CheckoutItems?) {
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_checkout_items), Gson().toJson(checkoutItems))
        }

        fun getCheckoutItems(): CheckoutItems? {
            val value: String = PrefsController.getString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_checkout_items), null)
            if (value != null) {
                val mResult: CheckoutItems = Gson().fromJson(value, CheckoutItems::class.java)
                if (mResult != null) {
                    return mResult
                }
            }
            return null
        }

        fun isPremium(): Boolean {
//        if (BuildConfig.DEBUG){
//            return false;
//        }
            val mCheckout: CheckoutItems? = getCheckoutItems()
            if (mCheckout != null) {
                if (mCheckout.isPurchasedLifeTime || mCheckout.isPurchasedOneYears || mCheckout.isPurchasedSixMonths) {
                    return true
                }
            }
            return false
        }

        fun isCheckAllowUpload(): Boolean {
            val mUser = getUserInfo() ?: return false
            val syncData: SyncData? = mUser.syncData
            if (!isPremium()) {
                if (syncData != null) {
                    if (syncData.left == 0) {
                        return false
                    }
                }
            }
            return true
        }

        fun isAllowRequestDriveApis(): Boolean {
            val mUser = getUserInfo()
            if (mUser != null) {
                if (mUser.driveConnected) {
                    if (mUser.access_token != null && mUser.access_token != "") {
                        if (mUser.cloud_id != null && mUser.cloud_id != "" && !isPauseSync()) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

    init {
        throw AssertionError()
    }
}