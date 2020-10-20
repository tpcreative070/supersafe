package co.tpcreative.supersafe.common.servicesimport

import android.app.Service
import android.os.Binder
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.services.download.DownloadService
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.snatik.storage.Storage
import io.reactivex.functions.Consumer
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class SuperSafeService : PresenterService<BaseServiceView<*>?>(), ConnectivityReceiverListener {
    private val mBinder: IBinder? = LocalBinder() // Binder given to clients
    protected var storage: Storage? = null
    private var androidReceiver: SuperSafeReceiver? = null
    private var downloadService: DownloadService? = null
    private var isCallRefreshToken = false
    override fun onCreate() {
        super.onCreate()
        Utils.Companion.Log(TAG, "onCreate")
        downloadService = DownloadService()
        storage = Storage(this)
        onInitReceiver()
        SuperSafeApplication.Companion.getInstance().setConnectivityListener(this)
    }

    fun getStorage(): Storage? {
        return storage
    }

    fun onInitReceiver() {
        Utils.Companion.Log(TAG, "onInitReceiver")
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        androidReceiver = SuperSafeReceiver()
        registerReceiver(androidReceiver, intentFilter)
        SuperSafeApplication.Companion.getInstance().setConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "onDestroy")
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver)
        }
        stopSelf()
        stopForeground(true)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Companion.Log(TAG, "Connected :$isConnected")
        val view: BaseServiceView<*> = view()
        if (view != null) {
            if (isConnected) {
                view.onSuccessful("Connected network", EnumStatus.CONNECTED)
            } else {
                view.onSuccessful("Disconnected network", EnumStatus.DISCONNECTED)
            }
        }
    }

    override fun onActionScreenOff() {
        val view: BaseServiceView<*> = view()
        if (view != null) {
            view.onSuccessful("Screen Off", EnumStatus.SCREEN_OFF)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we get killed, after returning from here, restart
        Utils.Companion.Log(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        val extras: Bundle = intent.getExtras()
        Utils.Companion.Log(TAG, "onBind")
        // Get messager from the Activity
        if (extras != null) {
            Utils.Companion.Log("service", "onBind with extra")
        }
        return mBinder
    }

    fun onGetUserInfo() {
        Utils.Companion.Log(TAG, "onGetUserInfo 1")
        val view: BaseServiceView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.Companion.getInstance())) {
            return
        }
        Utils.Companion.Log(TAG, "onGetUserInfo 2")
        if (subscriptions == null) {
            return
        }
        val mUser: User = Utils.Companion.getUserInfo() ?: return
        val mAuthor = mUser.author ?: return
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onUserInfo(UserRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.USER_INFO)
                    } else {
                        val mData: DataResponse = onResponse.data
                        if (mData == null) {
                            view.onError(onResponse.message, EnumStatus.USER_INFO)
                            return@subscribe
                        }
                        if (mData.premium != null && mData.email_token != null) {
                            mUser.premium = mData.premium
                            mUser.email_token = mData.email_token
                            Utils.Companion.setUserPreShare(mUser)
                            view.onSuccessful("Successful", EnumStatus.USER_INFO)
                        }
                    }
                    Utils.Companion.Log(TAG, "onGetUserInfo 3")
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error " + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    fun onUpdateUserToken() {
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.UPDATE_USER_TOKEN)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo() ?: return
        if (isCallRefreshToken) {
            Utils.Companion.Log(TAG, "Refresh token is progressing")
            return
        }
        isCallRefreshToken = true
        val mUserRequest = UserRequest()
        Utils.Companion.onWriteLog(Gson().toJson(user), EnumStatus.REFRESH_EMAIL_TOKEN)
        Utils.Companion.Log(TAG, "Body request " + Gson().toJson(mUserRequest))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onUpdateToken(mUserRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.UPDATE_USER_TOKEN)
                        isCallRefreshToken = false
                    } else {
                        val mUser: User = Utils.Companion.getUserInfo()
                        val mData: DataResponse = onResponse.data
                        if (mData.user != null) {
                            if (mData.user.author != null) {
                                val mAuthorGlobal: Authorization = mData.user.author
                                val mAuthor = mUser.author
                                mAuthor.refresh_token = mAuthorGlobal.refresh_token
                                mAuthor.session_token = mAuthorGlobal.session_token
                                mUser.author = mAuthor
                                Utils.Companion.setUserPreShare(mUser)
                                view.onSuccessful(onResponse.message, EnumStatus.UPDATE_USER_TOKEN)
                                Utils.Companion.onWriteLog(Gson().toJson(mUser), EnumStatus.UPDATE_USER_TOKEN)
                                onDeleteOldAccessToken(mUserRequest)
                            }
                        }
                    }
                    Utils.Companion.Log(TAG, "Body Update token: " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 403 || code == 400 || code == 401) {
                                val mUserResponse: User = Utils.Companion.getUserInfo()
                                if (mUserResponse != null) {
                                    onSignIn(user)
                                }
                            }
                            val errorMessage: String = bodys.string()
                            Utils.Companion.Log(TAG, "error update access token $errorMessage")
                            view.onError(errorMessage, EnumStatus.UPDATE_USER_TOKEN)
                            Utils.Companion.onWriteLog(errorMessage, EnumStatus.UPDATE_USER_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                    isCallRefreshToken = false
                }))
    }

    fun onDeleteOldAccessToken(request: UserRequest?) {
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.UPDATE_USER_TOKEN)) {
            isCallRefreshToken = false
            return
        }
        val mUser: User = Utils.Companion.getUserInfo()
        if (mUser == null) {
            isCallRefreshToken = false
            return
        }
        Utils.Companion.onWriteLog(Gson().toJson(mUser), EnumStatus.DELETE_OLD_ACCESS_TOKEN)
        Utils.Companion.Log(TAG, "Body request " + Gson().toJson(request))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onDeleteOldAccessToken(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                    } else {
                        ServiceManager.Companion.getInstance().onPreparingSyncData()
                    }
                    isCallRefreshToken = false
                    Utils.Companion.Log(TAG, "Body delele old access token: " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 403 || code == 400 || code == 401) {
                                val user: User = Utils.Companion.getUserInfo()
                                user?.let { onSignIn(it) }
                            }
                            val errorMessage: String = bodys.string()
                            Utils.Companion.Log(TAG, "error old delete access token $errorMessage")
                            view.onError(errorMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                            Utils.Companion.onWriteLog(errorMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                    isCallRefreshToken = false
                }))
    }

    fun onSignIn(request: User?) {
        Utils.Companion.Log(TAG, "onSignIn request")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.SIGN_IN)) {
            return
        }
        val mRequest = SignInRequest()
        mRequest.user_id = request.email
        mRequest.password = SecurityUtil.key_password_default_encrypted
        mRequest.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onSignIn(mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    Utils.Companion.Log(TAG, "Body response sign in: " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.SIGN_IN)
                    } else {
                        val user: User = Utils.Companion.getUserInfo()
                        val mData: DataResponse = onResponse.data
                        if (mData.user != null) {
                            user.author = mData.user.author
                        }
                        Utils.Companion.setUserPreShare(user)
                        ServiceManager.Companion.getInstance().onPreparingSyncData()
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    fun getDriveAbout() {
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.GET_DRIVE_ABOUT)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("User is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        if (user.access_token == null) {
            view.onError("access token is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        view.onSuccessful(access_token, EnumStatus.GET_DRIVE_ABOUT)
        subscriptions.add(SuperSafeApplication.Companion.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { onResponse: DriveAbout? ->
                    if (view == null) {
                        view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT)
                        return@subscribe
                    }
                    if (onResponse.error != null) {
                        view.onError("Error " + Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                    } else {
                        val mUser: User = Utils.Companion.getUserInfo()
                        mUser.driveAbout = onResponse
                        Utils.Companion.setUserPreShare(mUser)
                        view.onSuccessful(Gson().toJson(onResponse), EnumStatus.GET_DRIVE_ABOUT)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name + "-" + Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                                }
                            } else {
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name + " - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Utils.Companion.Log(TAG, "Exception....")
                            view.onError("Exception " + e.message, EnumStatus.GET_DRIVE_ABOUT)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error ^^:" + throwable.message, EnumStatus.GET_DRIVE_ABOUT)
                    }
                }))
    }

    fun onGetListFileInApp(view: BaseView<Int?>?) {
        Utils.Companion.Log(TAG, "onGetListFolderInApp")
        if (isCheckNull<BaseView<Int?>?>(view, EnumStatus.GET_LIST_FILES_IN_APP)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        subscriptions.add(SuperSafeApplication.Companion.serverDriveApi.onGetListFileInAppFolder(access_token, SuperSafeApplication.Companion.getInstance().getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> view.onStartLoading(EnumStatus.GET_LIST_FILES_IN_APP) })
                .subscribe(Consumer { onResponse: DriveAbout? ->
                    Utils.Companion.Log(TAG, "Response data from items " + Gson().toJson(onResponse))
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP)
                    if (onResponse.error != null) {
                        Utils.Companion.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        view.onError("Not found this id.... :" + Gson().toJson(onResponse.error), EnumStatus.GET_LIST_FILES_IN_APP)
                    } else {
                        val count = onResponse.files.size
                        Utils.Companion.Log(TAG, "Total count request :$count")
                        view.onSuccessful("Successful", EnumStatus.GET_LIST_FILES_IN_APP, count)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(EnumStatus.GET_LIST_FILES_IN_APP.name + "-" + Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                                }
                            } else {
                                view.onError(EnumStatus.GET_LIST_FILES_IN_APP.name + " - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("Exception " + e.message, EnumStatus.GET_LIST_FILES_IN_APP)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error ^^:" + throwable.message, EnumStatus.GET_LIST_FILES_IN_APP)
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP)
                }))
    }

    /*Network request*/
    fun onCategoriesSync(mainCategories: MainCategoryModel?, view: BaseListener<*>?) {
        Utils.Companion.Log(TAG, "onCategoriesSync " + Gson().toJson(mainCategories))
        if (isCheckNull<BaseListener<*>?>(view, EnumStatus.CATEGORIES_SYNC)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.CATEGORIES_SYNC)
            return
        }
        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.CATEGORIES_SYNC)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        val mCategories = CategoriesRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), mainCategories)
        Utils.Companion.Log(TAG, "onCategoriesSync " + Gson().toJson(mCategories))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onCategoriesSync(mCategories)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC)
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Companion.Log(TAG, "onError 1")
                        Utils.Companion.Log(TAG, "onCategoriesSync " + Gson().toJson(onResponse))
                        view.onSuccessful(onResponse.message, EnumStatus.CATEGORIES_SYNC)
                    } else {
                        if (onResponse != null) {
                            val mData: DataResponse = onResponse.data
                            if (mData.category != null) {
                                if (mainCategories.categories_hex_name == mData.category.categories_hex_name) {
                                    mainCategories.categories_id = mData.category.categories_id
                                    mainCategories.isSyncOwnServer = true
                                    mainCategories.isChange = false
                                    mainCategories.isDelete = false
                                    SQLHelper.updateCategory(mainCategories)
                                    view.onSuccessful(onResponse.message + " - " + mData.category.categories_id + " - ", EnumStatus.CATEGORIES_SYNC)
                                } else {
                                    view.onSuccessful("Not found categories_hex_name - " + mData.category.categories_id, EnumStatus.CATEGORIES_SYNC)
                                }
                            }
                        }
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.CATEGORIES_SYNC)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.CATEGORIES_SYNC)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.CATEGORIES_SYNC)
                    }
                }))
    }

    fun onDeleteCategoriesSync(mainCategories: MainCategoryModel?, view: BaseListener<*>?) {
        Utils.Companion.Log(TAG, "onDeleteCategoriesSync")
        if (isCheckNull<BaseListener<*>?>(view, EnumStatus.DELETE_CATEGORIES)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_CATEGORIES)
            return
        }
        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.DELETE_CATEGORIES)
            return
        }
        val mCategories = CategoriesRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), mainCategories.categories_id)
        Utils.Companion.Log(TAG, "onDeleteCategoriesSync " + Gson().toJson(mCategories))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onDeleteCategories(mCategories)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES)
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Companion.Log(TAG, "onError 1")
                        view.onError(onResponse.message, EnumStatus.DELETE_CATEGORIES)
                    } else {
                        Utils.Companion.Log(TAG, "onDeleteCategoriesSync response" + Gson().toJson(onResponse))
                        view.onSuccessful(onResponse.message, EnumStatus.DELETE_CATEGORIES)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.DELETE_CATEGORIES)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.DELETE_CATEGORIES)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.DELETE_CATEGORIES)
                    }
                }))
    }

    fun onUpdateItems(mItem: ItemModel?, view: BaseListener<*>?) {
        Utils.Companion.Log(TAG, "onUpdateItems")
        if (isCheckNull<BaseListener<*>?>(view, EnumStatus.UPDATE)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.UPDATE)
            return
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        if (!Utils.Companion.isNotEmptyOrNull(mItem.categories_id)) {
            view.onError("Categories id is null", EnumStatus.UPDATE)
            Utils.Companion.Log(TAG, " Updated => Warning categories id is null")
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onSyncData(SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), mItem))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Companion.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        mItem.isUpdate = true
                        view.onSuccessful(EnumStatus.UPDATED_ITEM_SUCCESSFULLY.name, EnumStatus.UPDATED_ITEM_SUCCESSFULLY)
                        SQLHelper.updatedItem(mItem)
                        view.onError("Queries add items is failed :" + onResponse.message, EnumStatus.UPDATE)
                    } else {
                        mItem.isUpdate = false
                        SQLHelper.updatedItem(mItem)
                        view.onSuccessful(EnumStatus.UPDATED_ITEM_SUCCESSFULLY.name, EnumStatus.UPDATED_ITEM_SUCCESSFULLY)
                    }
                    Utils.Companion.Log(TAG, "Adding item Response " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            Utils.Companion.Log(TAG, "Adding item Response error" + bodys.string())
                            view.onError("" + bodys.string(), EnumStatus.UPDATE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.UPDATE)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.UPDATE)
                    }
                }))
    }

    /*Date for Categories*/
    fun onAddItems(items: ItemModel?, drive_id: String?, view: ServiceManagerInsertItem?) {
        Utils.Companion.Log(TAG, "onAddItems")
        if (isCheckNull<ServiceManagerInsertItem?>(view, EnumStatus.ADD_ITEMS)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.ADD_ITEMS)
            return
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        items.isSyncOwnServer = true
        Utils.Companion.Log(TAG, "system access token : " + Utils.Companion.getAccessToken())
        val entityModel: ItemModel = SQLHelper.getItemById(items.items_id)
        if (items.isOriginalGlobalId) {
            if (!Utils.Companion.isNotEmptyOrNull(entityModel.global_thumbnail_id)) {
                entityModel.global_thumbnail_id = "null"
            }
            entityModel.originalSync = true
            entityModel.global_original_id = drive_id
        } else {
            if (!Utils.Companion.isNotEmptyOrNull(entityModel.global_original_id)) {
                entityModel.global_original_id = "null"
            }
            entityModel.thumbnailSync = true
            entityModel.global_thumbnail_id = drive_id
        }
        if (entityModel.originalSync && entityModel.thumbnailSync) {
            entityModel.isSyncCloud = true
            entityModel.isSyncOwnServer = true
            entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
        }
        val mFormat = EnumFormatType.values()[entityModel.formatType]
        if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
            entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
        }
        /*Check imported data before sync data*/checkImportedDataBeforeSyncData(entityModel)
        val mRequest = SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), entityModel)
        Utils.Companion.Log(TAG, "onAddItems request " + Gson().toJson(mRequest))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onSyncData(mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Companion.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS)
                    } else {
                        /*Check saver space*/
                        checkSaverSpace(entityModel, items.isOriginalGlobalId)
                        SQLHelper.updatedItem(entityModel)
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS)
                    }
                    Utils.Companion.Log(TAG, "Adding item Response " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            Utils.Companion.Log(TAG, "Adding item Response error" + bodys.string())
                            view.onError("" + bodys.string(), EnumStatus.ADD_ITEMS)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.ADD_ITEMS)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.ADD_ITEMS)
                    }
                }))
    }

    /*Check saver space*/
    fun checkSaverSpace(itemModel: ItemModel?, isOriginalGlobalId: Boolean) {
        val mType = EnumFormatType.values()[itemModel.formatType]
        if (mType == EnumFormatType.IMAGE) {
            if (Utils.Companion.getSaverSpace()) {
                itemModel.isSaver = true
                Utils.Companion.checkSaverToDelete(itemModel.originalPath, isOriginalGlobalId)
            }
        }
    }

    /*Check imported data after sync data*/
    fun checkImportedDataBeforeSyncData(itemModel: ItemModel?) {
        val categoryModel: MainCategoryModel = SQLHelper.getCategoriesLocalId(itemModel.categories_local_id)
        Utils.Companion.Log(TAG, "checkImportedDataBeforeSyncData " + Gson().toJson(categoryModel))
        if (categoryModel != null) {
            if (!Utils.Companion.isNotEmptyOrNull(itemModel.categories_id)) {
                itemModel.categories_id = categoryModel.categories_id
                Utils.Companion.Log(TAG, "checkImportedDataBeforeSyncData ==> isNotEmptyOrNull")
            }
        }
    }

    /*Get List Categories*/
    fun onDeleteCloudItems(items: ItemModel?, view: BaseListener<*>?) {
        Utils.Companion.Log(TAG, "onDeleteCloudItems")
        if (isCheckNull<BaseListener<*>?>(view, EnumStatus.DELETE_SYNC_CLOUD_DATA)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_SYNC_CLOUD_DATA)
            return
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        subscriptions.add(SuperSafeApplication.Companion.serverDriveApi.onDeleteCloudItem(access_token, items.global_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { onResponse: Response<DriveAbout?>? ->
                    Utils.Companion.Log(TAG, "Deleted cloud response code " + onResponse.code())
                    if (onResponse.code() == 204) {
                        val delete = EnumDelete.values()[items.deleteAction]
                        if (delete == EnumDelete.DELETE_DONE) {
                            view.onSuccessful("Deleted successfully", EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                        }
                        view.onSuccessful("Deleted Successful : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    } else if (onResponse.code() == 404) {
                        val delete = EnumDelete.values()[items.deleteAction]
                        if (delete == EnumDelete.DELETE_DONE) {
                            view.onSuccessful("Deleted successfully", EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                        }
                        val value = onResponse.errorBody().string()
                        val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                        view.onError("Not found file :" + Gson().toJson(driveAbout.error) + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    } else {
                        view.onError("Another cases : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(Gson().toJson(driveAbout.error), EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                                }
                            } else {
                                view.onError("Error null 1 ", EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("Exception " + e.message, EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error 0:" + throwable.message, EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                    }
                }))
    }

    fun onDeleteOwnSystem(items: ItemModel?, view: BaseListener<*>?) {
        Utils.Companion.Log(TAG, "onDeleteOwnSystem")
        if (isCheckNull<BaseListener<*>?>(view, EnumStatus.DELETE_SYNC_OWN_DATA)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_SYNC_OWN_DATA)
            return
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        val mItem = SyncItemsRequest(user.email, user.cloud_id, items.items_id)
        Utils.Companion.Log(TAG, "onDeleteOwnSystem " + Gson().toJson(mItem))
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onDeleteOwnItems(mItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    Utils.Companion.Log(TAG, "Response data from items " + Gson().toJson(onResponse))
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.DELETED_ITEM_SUCCESSFULLY)
                    } else {
                        view.onSuccessful(onResponse.message, EnumStatus.DELETED_ITEM_SUCCESSFULLY)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            val value: String = bodys.string()
                            if (value != null) {
                                view.onError("Error $value", EnumStatus.DELETE_SYNC_OWN_DATA)
                            } else {
                                view.onError("Error null ", EnumStatus.DELETE_SYNC_OWN_DATA)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("Exception " + e.message, EnumStatus.DELETE_SYNC_OWN_DATA)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.DELETE_SYNC_OWN_DATA)
                    }
                }))
    }

    fun onGetListSync(nextPage: String?, view: BaseListener<ItemModel?>?) {
        Utils.Companion.Log(TAG, "onGetListSync")
        if (isCheckNull<BaseListener<ItemModel?>?>(view, EnumStatus.GET_LIST_FILE)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.GET_LIST_FILE)
            return
        }
        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        if (!user.driveConnected) {
            view.onError("no driveConnected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onListFilesSync(SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), true, nextPage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    Utils.Companion.Log(TAG, "onGetListSync " + Gson().toJson(onResponse))
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.GET_LIST_FILE)
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Companion.Log(TAG, "onError 1")
                        view.onError(onResponse.message, EnumStatus.GET_LIST_FILE)
                    } else {
                        val mData: DataResponse = onResponse.data
                        val listCategories: MutableList<MainCategoryModel?> = mData.categoriesList
                        val mListItemResponse: MutableList<ItemModel?> = mData.itemsList
                        if (mData.nextPage == null) {
                            if (mData.syncData != null) {
                                user.syncData = mData.syncData
                            }
                            Utils.Companion.setUserPreShare(user)
                            for (index in listCategories) {
                                val main: MainCategoryModel = SQLHelper.getCategoriesId(index.categories_id, false)
                                if (main != null) {
                                    if (!main.isChange && !main.isDelete) {
                                        main.isSyncOwnServer = true
                                        main.categories_name = index.categories_name
                                        SQLHelper.updateCategory(main)
                                    }
                                } else {
                                    var mMain: MainCategoryModel? = SQLHelper.getCategoriesItemId(index.categories_hex_name, false)
                                    if (mMain != null) {
                                        if (!mMain.isDelete && !mMain.isChange) {
                                            mMain.isSyncOwnServer = true
                                            mMain.isChange = false
                                            mMain.isDelete = false
                                            mMain.categories_id = index.categories_id
                                            SQLHelper.updateCategory(mMain)
                                        }
                                    } else {
                                        mMain = index
                                        mMain.categories_local_id = Utils.Companion.getUUId()
                                        mMain.items_id = Utils.Companion.getUUId()
                                        mMain.isSyncOwnServer = true
                                        mMain.isChange = false
                                        mMain.isDelete = false
                                        mMain.pin = ""
                                        val count: Int = SQLHelper.getLatestItem()
                                        mMain.categories_max = count.toLong()
                                        SQLHelper.insertCategory(mMain)
                                        Utils.Companion.Log(TAG, "Adding new main categories.......................................2")
                                    }
                                }
                            }
                            view.onSuccessful(mData.nextPage, EnumStatus.SYNC_READY)
                        } else {
                            val mList: MutableList<ItemModel?> = ArrayList()
                            for (index in mListItemResponse) {
                                mList.add(ItemModel(index, EnumStatus.DOWNLOAD))
                            }
                            view.onShowListObjects(mList)
                            view.onSuccessful(mData.nextPage, EnumStatus.LOAD_MORE)
                        }
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        view.onError("View is null", EnumStatus.GET_LIST_FILE)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.GET_LIST_FILE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.GET_LIST_FILE)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.GET_LIST_FILE)
                    }
                }))
    }

    fun onDownloadFile(items: ItemModel?, isDownloadToExport: Boolean, listener: DownloadServiceListener?) {
        Utils.Companion.Log(TAG, "onDownloadFile !!!!")
        val mUser: User = Utils.Companion.getUserInfo()
        if (!mUser.driveConnected) {
            listener.onError("No Drive api connected", EnumStatus.DOWNLOAD)
            return
        }
        if (mUser.access_token == null) {
            listener.onError("No Access token", EnumStatus.DOWNLOAD)
        }
        val request = DownloadFileRequest()
        var id = ""
        if (items.isOriginalGlobalId) {
            id = items.global_id
            request.file_name = items.originalName
        } else {
            id = items.global_id
            request.file_name = items.thumbnailName
        }
        request.items = items
        request.Authorization = mUser.access_token
        request.id = id
        Utils.Companion.Log(TAG, "onDownloadFile request " + Gson().toJson(items))
        if (!Utils.Companion.isNotEmptyOrNull(id)) {
            listener.onError("Error upload", EnumStatus.REQUEST_NEXT_DOWNLOAD)
            return
        }
        items.originalPath = Utils.Companion.getOriginalPath(items.originalName, items.items_id)
        request.path_folder_output = Utils.Companion.createDestinationDownloadItem(items.items_id)
        downloadService.onProgressingDownload(object : DownLoadServiceListener {
            override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?) {
                Utils.Companion.Log(TAG, "onDownLoadCompleted " + file_name.getAbsolutePath())
                listener.onDownLoadCompleted(file_name, request)
                val entityModel: ItemModel = SQLHelper.getItemById(items.items_id)
                val categoryModel: MainCategoryModel = SQLHelper.getCategoriesId(items.categories_id, false)
                if (entityModel != null) {
                    if (categoryModel != null) {
                        entityModel.categories_local_id = categoryModel.categories_local_id
                    }
                    entityModel.isSaver = false
                    if (items.isOriginalGlobalId) {
                        entityModel.originalSync = true
                        entityModel.global_original_id = request.id
                    } else {
                        entityModel.thumbnailSync = true
                        entityModel.global_thumbnail_id = request.id
                    }
                    if (entityModel.originalSync && entityModel.thumbnailSync) {
                        entityModel.isSyncCloud = true
                        entityModel.isSyncOwnServer = true
                        entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
                        Utils.Companion.Log(TAG, "Synced already....")
                    }
                    val mFormat = EnumFormatType.values()[entityModel.formatType]
                    if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
                        entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
                    }
                    /*Check saver space*/if (!isDownloadToExport) {
                        checkSaverSpace(entityModel, items.isOriginalGlobalId)
                    }
                    SQLHelper.updatedItem(entityModel)
                } else {
                    if (categoryModel != null) {
                        items.categories_local_id = categoryModel.categories_local_id
                    }
                    if (items.isOriginalGlobalId) {
                        items.originalSync = true
                    } else {
                        items.thumbnailSync = true
                    }
                    val mFormat = EnumFormatType.values()[items.formatType]
                    if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
                        items.statusProgress = EnumStatusProgress.DONE.ordinal
                    }
                    /*Check saver space*/if (!isDownloadToExport) {
                        checkSaverSpace(items, items.isOriginalGlobalId)
                    }
                    SQLHelper.insertedItem(items)
                }
            }

            override fun onDownLoadError(error: String?) {
                Utils.Companion.Log(TAG, "onDownLoadError $error")
                if (listener != null) {
                    listener.onError("Error download ", EnumStatus.REQUEST_NEXT_DOWNLOAD)
                }
            }

            override fun onProgressingDownloading(percent: Int) {
                listener.onProgressDownload(percent)
                Utils.Companion.Log(TAG, "Progressing downloaded $percent%")
            }

            override fun onAttachmentElapsedTime(elapsed: Long) {}
            override fun onAttachmentAllTimeForDownloading(all: Long) {}
            override fun onAttachmentRemainingTime(all: Long) {}
            override fun onAttachmentSpeedPerSecond(all: Double) {}
            override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {}
            override fun onSavedCompleted() {
                Utils.Companion.Log(TAG, "onSavedCompleted ")
            }

            override fun onErrorSave(name: String?) {
                Utils.Companion.Log(TAG, "onErrorSave")
                if (listener != null) {
                    listener.onError("Error download save ", EnumStatus.DOWNLOAD)
                }
            }

            override fun onCodeResponse(code: Int, request: DownloadFileRequest?) {
                if (listener != null) {
                    val mItem: ItemModel = request.items
                    if (mItem != null) {
                        /*Not Found file*/
                        if (code == 404) {
                            Utils.Companion.Log(TAG, "isDelete local id error")
                            SQLHelper.deleteItem(items)
                        }
                    }
                }
            }

            override fun onHeader(): MutableMap<String?, String?>? {
                return HashMap()
            }
        })
        downloadService.downloadFileFromGoogleDrive(request)
    }

    fun onUploadFileInAppFolder(items: ItemModel?, listener: ServiceManager.UploadServiceListener?) {
        Utils.Companion.Log(TAG, "onUploadFileInAppFolder")
        val mUser: User = Utils.Companion.getUserInfo()
        val contentType = MediaType.parse("application/json; charset=UTF-8")
        val content = HashMap<String?, Any?>()
        val contentEvent = DriveEvent()
        var file: File? = null
        if (items.isOriginalGlobalId) {
            contentEvent.fileType = EnumFileType.ORIGINAL.ordinal
            file = File(items.originalPath)
        } else {
            contentEvent.fileType = EnumFileType.THUMBNAIL.ordinal
            file = File(items.thumbnailPath)
        }
        if (!storage.isFileExist(file.absolutePath)) {
            SQLHelper.deleteItem(items)
            listener.onError("This path is not found", EnumStatus.UPLOAD)
            return
        }
        if (!Utils.Companion.isNotEmptyOrNull(items.categories_id)) {
            listener.onError("Error upload", EnumStatus.REQUEST_NEXT_UPLOAD)
            return
        }
        contentEvent.items_id = items.items_id
        val hex: String = DriveEvent.Companion.getInstance().convertToHex(Gson().toJson(contentEvent))
        content[getString(R.string.key_name)] = hex
        val list: MutableList<String?> = ArrayList()
        list.add(getString(R.string.key_appDataFolder))
        content[getString(R.string.key_parents)] = list
        val metaPart: MultipartBody.Part = MultipartBody.Part.create(RequestBody.create(contentType, Gson().toJson(content)))
        val fileBody = ProgressRequestBody(file, object : UploadCallbacks {
            override fun onProgressUpdate(percentage: Int) {
                Utils.Companion.Log(TAG, "Progressing uploaded $percentage%")
                listener.onProgressUpdate(percentage)
            }

            override fun onError() {
                Utils.Companion.Log(TAG, "onError")
                listener?.onError("Error upload", EnumStatus.REQUEST_NEXT_UPLOAD)
            }

            override fun onFinish() {
                listener.onFinish()
                Utils.Companion.Log(TAG, "onFinish")
            }
        })
        fileBody.setContentType(items.mimeType)
        val dataPart: MultipartBody.Part = MultipartBody.Part.create(fileBody)
        val request: Call<DriveResponse?> = SuperSafeApplication.Companion.serverDriveApi.uploadFileMultipleInAppFolder(mUser.access_token, metaPart, dataPart, items.mimeType)
        request.enqueue(object : Callback<DriveResponse?> {
            override fun onResponse(call: Call<DriveResponse?>?, response: Response<DriveResponse?>?) {
                Utils.Companion.Log(TAG, "response successful :" + Gson().toJson(response.body()))
                listener.onResponseData(response.body())
            }

            override fun onFailure(call: Call<DriveResponse?>?, t: Throwable?) {
                Utils.Companion.Log(TAG, "response failed :" + t.message)
                listener.onError("Error upload" + t.message, EnumStatus.REQUEST_NEXT_UPLOAD)
            }
        })
    }

    fun getDriveAbout(view: BaseView<*>?) {
        Utils.Companion.Log(TAG, "getDriveAbout")
        if (isCheckNull<BaseView<*>?>(view, EnumStatus.GET_DRIVE_ABOUT)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        if (user == null) {
            view.onError("User is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        if (user.access_token == null) {
            view.onError("Access token is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        val access_token = user.access_token
        Utils.Companion.Log(TAG, "access_token : $access_token")
        view.onSuccessful(access_token)
        subscriptions.add(SuperSafeApplication.Companion.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> view.onStartLoading(EnumStatus.GET_DRIVE_ABOUT) })
                .subscribe(Consumer { onResponse: DriveAbout? ->
                    if (view == null) {
                        view.onError("View is disable", EnumStatus.GET_DRIVE_ABOUT)
                        return@subscribe
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT)
                    if (onResponse.error != null) {
                        val mUser: User = Utils.Companion.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = false
                            Utils.Companion.setUserPreShare(user)
                        }
                        view.onError(Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                    } else {
                        val mUser: User = Utils.Companion.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = true
                            Utils.Companion.setUserPreShare(user)
                            view.onSuccessful("Successful", EnumStatus.GET_DRIVE_ABOUT)
                        }
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Companion.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            if (view == null) {
                                return@subscribe
                            }
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    val mUser: User = Utils.Companion.getUserInfo()
                                    if (mUser != null) {
                                        user.driveConnected = false
                                        Utils.Companion.setUserPreShare(user)
                                    }
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name + "-" + Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                                }
                            } else {
                                val mUser: User = Utils.Companion.getUserInfo()
                                if (mUser != null) {
                                    user.driveConnected = false
                                    Utils.Companion.setUserPreShare(user)
                                }
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name + " - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            val mUser: User = Utils.Companion.getUserInfo()
                            if (mUser != null) {
                                user.driveConnected = false
                                Utils.Companion.setUserPreShare(user)
                            }
                            view.onError("Error IOException " + e.message, EnumStatus.GET_DRIVE_ABOUT)
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                        val mUser: User = Utils.Companion.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = false
                            Utils.Companion.setUserPreShare(user)
                        }
                        view.onError("Error else :" + throwable.message, EnumStatus.GET_DRIVE_ABOUT)
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT)
                }))
    }

    /*TrackHandler*/
    fun onCheckVersion() {
        Utils.Companion.Log(TAG, "onCheckVersion")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.CHECK_VERSION)) {
            return
        }
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onCheckVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<BaseResponse?> { onResponse: BaseResponse? ->
                    if (onResponse != null) {
                        if (onResponse.version != null) {
                            view.onSuccessful("Successful", EnumStatus.CHECK_VERSION)
                            val user: User = Utils.Companion.getUserInfo()
                            user.version = onResponse.version
                            Utils.Companion.setUserPreShare(user)
                        }
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call" + throwable.message)
                    }
                }))
    }

    fun onSyncAuthorDevice() {
        Utils.Companion.Log(TAG, "onSyncAuthorDevice")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.AUTHOR_SYNC)) {
            return
        }
        val user: User = Utils.Companion.getUserInfo()
        var user_id: String? = "null@gmail.com"
        if (user != null) {
            user_id = user.email
        }
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onTracking(TrackingRequest(user_id, SuperSafeApplication.Companion.getInstance().getDeviceId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    if (!onResponse.error) {
                        Utils.Companion.Log(TAG, "Tracking response " + Gson().toJson(onResponse))
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            Utils.Companion.Log(TAG, "Author error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Companion.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Author Can not call" + throwable.message)
                    }
                }))
    }

    /*Email token*/
    fun onSendMail(request: EmailToken?) {
        Utils.Companion.Log(TAG, "onSendMail.....")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.SEND_EMAIL)) {
            return
        }
        val mUser: User = Utils.Companion.getUserInfo() ?: return
        val response: Call<ResponseBody?> = SuperSafeApplication.Companion.serviceGraphMicrosoft.onSendMail(request.access_token, request)
        response.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>?) {
                try {
                    val code = response.code()
                    if (code == 401) {
                        Utils.Companion.Log(TAG, "code $code")
                        onRefreshEmailToken(request)
                        val errorMessage = response.errorBody().string()
                        Utils.Companion.Log(TAG, "error$errorMessage")
                        view.onError(errorMessage, EnumStatus.SEND_EMAIL)
                        mUser.isWaitingSendMail = false
                        Utils.Companion.setUserPreShare(mUser)
                    } else if (code == 202) {
                        Utils.Companion.Log(TAG, "code $code")
                        view.onSuccessful("successful", EnumStatus.SEND_EMAIL)
                        val mUser: User = Utils.Companion.getUserInfo()
                        mUser.isWaitingSendMail = false
                        Utils.Companion.setUserPreShare(mUser)
                        ServiceManager.Companion.getInstance().onDismissServices()
                        Utils.Companion.Log(TAG, "Body : Send email Successful")
                    } else {
                        Utils.Companion.Log(TAG, "code $code")
                        Utils.Companion.Log(TAG, "Nothing to do")
                        view.onError("Null", EnumStatus.SEND_EMAIL)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                Utils.Companion.Log(TAG, "response failed :" + t.message)
            }
        })
    }

    fun onRefreshEmailToken(request: EmailToken?) {
        Utils.Companion.Log(TAG, "onRefreshEmailToken.....")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.REFRESH)) {
            return
        }
        val mUser: User = Utils.Companion.getUserInfo()
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[getString(R.string.key_client_id)] = request.client_id
        hash[getString(R.string.key_redirect_uri)] = request.redirect_uri
        hash[getString(R.string.key_grant_type)] = request.grant_type
        hash[getString(R.string.key_refresh_token)] = request.refresh_token
        subscriptions.add(SuperSafeApplication.Companion.serviceGraphMicrosoft.onRefreshEmailToken(RootAPI.Companion.REFRESH_TOKEN, hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { onResponse: EmailToken? ->
                    if (onResponse != null) {
                        val token = mUser.email_token
                        token.access_token = onResponse.token_type + " " + onResponse.access_token
                        token.refresh_token = onResponse.refresh_token
                        token.token_type = onResponse.token_type
                        Utils.Companion.setUserPreShare(mUser)
                        onAddEmailToken()
                    }
                    view.onSuccessful("successful", EnumStatus.REFRESH)
                    Utils.Companion.Log(TAG, "Body refresh : " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                            }
                            Utils.Companion.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            view.onError(msg, EnumStatus.SEND_EMAIL)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    fun onAddEmailToken() {
        Utils.Companion.Log(TAG, "onSignIn.....")
        val view: BaseServiceView<*> = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.ADD_EMAIL_TOKEN)) {
            return
        }
        val mUser: User = Utils.Companion.getUserInfo()
        subscriptions.add(SuperSafeApplication.Companion.serverAPI.onAddEmailToken(OutlookMailRequest(mUser.email_token.refresh_token, mUser.email_token.access_token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<BaseResponse?> { onResponse: BaseResponse? ->
                    Utils.Companion.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    val emailToken: EmailToken = EmailToken.Companion.getInstance().convertObject(mUser, EnumStatus.RESET)
                    onSendMail(emailToken)
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Companion.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance().onUpdatedUserToken()
                            }
                            val errorMessage: String = bodys.string()
                            Utils.Companion.Log(TAG, "error$errorMessage")
                            view.onError(errorMessage, EnumStatus.ADD_EMAIL_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Companion.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): SuperSafeService? {
            // Return this instance of SignalRService so clients can call public methods
            return this@SuperSafeService
        }

        fun setIntent(intent: Intent?) {}
    }

    fun <T> isCheckNull(view: T?, status: EnumStatus?): Boolean {
        if (subscriptions == null) {
            Utils.Companion.Log(TAG, "Subscriptions is null " + status.name)
            return true
        } else if (NetworkUtil.pingIpAddress(SuperSafeApplication.Companion.getInstance())) {
            Utils.Companion.Log(TAG, "No connection " + status.name)
            return true
        } else if (view == null) {
            Utils.Companion.Log(TAG, "View is null " + status.name)
            return true
        }
        return false
    }

    companion object {
        private val TAG = SuperSafeService::class.java.simpleName
    }
}