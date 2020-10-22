package co.tpcreative.supersafe.common.controller

import android.content.Context
import android.graphics.Matrix
import android.media.ExifInterface
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.User
import com.google.common.net.MediaType
import com.snatik.storage.Storage
import io.reactivex.Observable
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*
import javax.crypto.Cipher

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
class ServiceManager : BaseServiceView<Any?> {
    private var myService: SuperSafeService? = null
    private var mContext: Context? = null
    private var subscriptions: Disposable? = null
    private val storage: Storage? = Storage(SuperSafeApplication.Companion.getInstance())
    private val mStorage: Storage? = Storage(SuperSafeApplication.Companion.getInstance())
    private val mListExport: MutableList<ExportFiles?>? = ArrayList<ExportFiles?>()
    private var mProgress: String? = null

    /*Improved sync data*/
    private val listImport: MutableList<ImportFilesModel?>? = ArrayList<ImportFilesModel?>()
    private var isDownloadData = false
    private var isUploadData = false
    private var isUpdateItemData = false
    private var isUpdateCategoryData = false
    private var isSyncCategory = false
    private var isGetItemList = false
    private var isImportData = false
    private var isExportData = false
    private var isDownloadToExportFiles = false
    private var isDeleteItemData = false
    private var isDeleteCategoryData = false
    private var isHandleLogic = false
    private var isRequestShareIntent = false

    /*Using item_id as key for hash map*/
    private var mMapDeleteItem: MutableMap<String?, ItemModel?>? = HashMap<String?, ItemModel?>()
    private var mMapDeleteCategory: MutableMap<String?, MainCategoryModel?>? = HashMap<String?, MainCategoryModel?>()
    private var mMapUpdateCategory: MutableMap<String?, MainCategoryModel?>? = HashMap<String?, MainCategoryModel?>()
    private var mMapSyncCategory: MutableMap<String?, MainCategoryModel?>? = HashMap<String?, MainCategoryModel?>()
    private var mMapDownload: MutableMap<String?, ItemModel?>? = HashMap<String?, ItemModel?>()
    private var mMapDownloadToExportFiles: MutableMap<String?, ItemModel?>? = HashMap<String?, ItemModel?>()
    private var mMapUpload: MutableMap<String?, ItemModel?>? = HashMap<String?, ItemModel?>()
    private var mMapUpdateItem: MutableMap<String?, ItemModel?>? = HashMap<String?, ItemModel?>()
    private var mMapImporting: MutableMap<String?, ImportFilesModel?>? = HashMap<String?, ImportFilesModel?>()
    private val mDownloadList: MutableList<ItemModel?>? = ArrayList<ItemModel?>()
    private var mStart = 20
    var myConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "connected")
            myService = (binder as LocalBinder?).getService()
            myService.bindView(this@ServiceManager)
            storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
            mStorage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
            ServiceManager.Companion.getInstance().onGetUserInfo()
            ServiceManager.Companion.getInstance().onSyncAuthorDevice()
            ServiceManager.Companion.getInstance().onGetDriveAbout()
            Utils.Companion.onScanFile(SuperSafeApplication.Companion.getInstance(), "scan.log")
            PremiumManager.Companion.getInstance().onStartInAppPurchase()
        }

        //binder comes from server to communicate with method's of
        override fun onServiceDisconnected(className: ComponentName?) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "disconnected")
            myService = null
        }
    }

    fun onInitConfigurationFile() {
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
        mStorage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
    }

    private var mCiphers: Cipher? = null
    private var isWaitingSendMail = false
    fun setListImport(mListImport: MutableList<ImportFilesModel?>?) {
        if (!isImportData) {
            listImport.clear()
            listImport.addAll(mListImport)
        }
    }

    /*Preparing sync data*/
    fun onPreparingSyncData() {
        if (Utils.Companion.getUserId() == null) {
            return
        }
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData...???")
        if (!Utils.Companion.isAllowSyncData()) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is unauthorized $isDownloadData")
            Utils.Companion.onWriteLog(EnumStatus.AUTHOR_SYNC, EnumStatus.AUTHOR_SYNC, "onPreparingSyncData is unauthorized")
            return
        }
        if (isGetItemList) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is getting item list. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.GET_LIST_FILE, EnumStatus.ERROR, "onPreparingSyncData is getting item list. Please wait")
            return
        }
        if (isDownloadData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is downloading. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onPreparingSyncData is downloading. Please wait")
            return
        }
        if (isUploadData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is uploading. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.ERROR, "onPreparingSyncData is uploading. Please wait")
            return
        }
        if (isDeleteItemData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is deleting. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.ERROR, "onPreparingSyncData is deleting. Please wait")
            return
        }
        if (isDeleteCategoryData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is deleting category. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.DELETE_CATEGORIES, EnumStatus.ERROR, "onPreparingSyncData is deleting category. Please wait")
            return
        }
        if (isImportData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is importing. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.IMPORTING, EnumStatus.ERROR, "onPreparingSyncData is importing. Please wait")
            return
        }
        if (isDownloadToExportFiles) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is downloading files. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.DOWNLOADING, EnumStatus.ERROR, "onPreparingSyncData is downloading to export files. Please wait")
            return
        }
        if (isUpdateItemData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is updating item. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.UPDATE_ITEM, EnumStatus.ERROR, "onPreparingSyncData is updating item.. Please wait")
            return
        }
        if (isUpdateCategoryData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is updating category. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.ERROR, "onPreparingSyncData is updating category.. Please wait")
            return
        }
        if (isHandleLogic) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is handle logic. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.ERROR, "onPreparingSyncData is handle logic. Please wait")
            return
        }
        if (isSyncCategory) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData is sync category. Please wait")
            Utils.Companion.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.ERROR, "onPreparingSyncData is sync category. Please wait")
            return
        }
        mDownloadList.clear()
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncData...onGetItemList")
        ServiceManager.Companion.getInstance().onGetItemList("0")
    }

    private fun onGetItemList(next: String?) {
        if (myService != null) {
            isGetItemList = true
            /*Stop multiple request*/isHandleLogic = true
            myService.onGetListSync(next, object : BaseListener<ItemModel?> {
                override fun onShowListObjects(list: MutableList<ItemModel?>?) {
                    mDownloadList.addAll(list)
                }

                override fun onShowObjects(`object`: ItemModel?) {}
                override fun onError(message: String?, status: EnumStatus?) {
                    isGetItemList = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    if (status == EnumStatus.LOAD_MORE) {
                        isGetItemList = true
                        ServiceManager.Companion.getInstance().onGetItemList(message)
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Continue load more $message")
                    } else if (status == EnumStatus.SYNC_READY) {
                        /*Start sync*/
                        isGetItemList = false
                        SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                        onPreparingSyncCategoryData()
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Start to sync data.......")
                    }
                }
            })
        }
    }

    /*Preparing sync category*/
    fun onPreparingSyncCategoryData() {
        val mResult: MutableList<MainCategoryModel?> = SQLHelper.requestSyncCategories(false, false)
        if (mResult.size > 0) {
            mMapSyncCategory.clear()
            mMapSyncCategory = Utils.Companion.mergeListToCategoryHashMap(mResult)
            val itemModel: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapSyncCategory)
            if (itemModel != null) {
                Utils.Companion.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.PROGRESS, "Total updating " + mMapSyncCategory.size)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingSyncCategoryData ==> total: " + mMapSyncCategory.size)
                onSyncCategoryData(itemModel)
            }
        } else {
            onPreparingUpdateCategoryData()
        }
    }

    /*Sync category data*/
    fun onSyncCategoryData(categoryModel: MainCategoryModel?) {
        if (myService != null) {
            isSyncCategory = true
            myService.onCategoriesSync(categoryModel, object : BaseListener<Any?> {
                override fun onShowListObjects(list: MutableList<*>?) {}
                override fun onShowObjects(`object`: Any?) {}
                override fun onError(message: String?, status: EnumStatus?) {
                    isSyncCategory = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    isSyncCategory = false
                    if (Utils.Companion.deletedIndexOfCategoryHashMap(categoryModel, mMapSyncCategory)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapSyncCategory)
                        if (mUpdatedItem != null) {
                            onSyncCategoryData(mUpdatedItem)
                            isSyncCategory = true
                            Utils.Companion.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.DONE, Gson().toJson(categoryModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Update completely...............")
                            Utils.Companion.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.DONE, Gson().toJson(categoryModel))
                            Utils.Companion.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapSyncCategory.size)
                            onPreparingUpdateCategoryData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing update category*/
    fun onPreparingUpdateCategoryData() {
        val mResult: MutableList<MainCategoryModel?> = SQLHelper.getRequestUpdateCategoryList()
        if (mResult.size > 0) {
            mMapUpdateCategory.clear()
            mMapUpdateCategory = Utils.Companion.mergeListToCategoryHashMap(mResult)
            val itemModel: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapUpdateCategory)
            if (itemModel != null) {
                Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.PROGRESS, "Total updating " + mMapUpdateItem.size)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingUpdateCategoryData ==> total: " + mMapUpdateItem.size)
                onUpdateCategoryData(itemModel)
            }
        } else {
            onPreparingDeleteCategoryData()
        }
    }

    fun onUpdateCategoryData(itemModel: MainCategoryModel?) {
        if (myService != null) {
            isUpdateCategoryData = true
            myService.onCategoriesSync(itemModel, object : BaseListener<Any?> {
                override fun onShowListObjects(list: MutableList<*>?) {}
                override fun onShowObjects(`object`: Any?) {}
                override fun onError(message: String?, status: EnumStatus?) {
                    isUpdateCategoryData = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    isUpdateCategoryData = false
                    if (Utils.Companion.deletedIndexOfCategoryHashMap(itemModel, mMapUpdateCategory)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapUpdateCategory)
                        if (mUpdatedItem != null) {
                            onUpdateCategoryData(mUpdatedItem)
                            isUpdateCategoryData = true
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Update completely...............")
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapUpdateCategory.size)
                            isUpdateCategoryData = false
                            Utils.Companion.onPushEventBus(EnumStatus.UPDATED_COMPLETED)
                            Utils.Companion.onPushEventBus(EnumStatus.DONE)
                            onPreparingDeleteCategoryData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing to delete category from system server*/
    private fun onPreparingDeleteCategoryData() {
        val listRequestDelete: MutableList<MainCategoryModel?> = SQLHelper.getDeleteCategoryRequest()
        mMapDeleteCategory.clear()
        mMapDeleteCategory = Utils.Companion.mergeListToCategoryHashMap(listRequestDelete)
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDeleteCategoryData preparing to delete " + mMapDeleteCategory.size)
        val categoryModel: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapDeleteCategory)
        if (categoryModel != null) {
            Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.PROGRESS, "Total Delete category " + mMapDeleteCategory.size)
            onDeleteCategoryData(categoryModel)
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDeleteCategoryData to delete data " + mMapDeleteCategory.size)
        } else {
            onPreparingDownloadData(mDownloadList)
        }
    }

    /*Delete category from Google drive and server*/
    fun onDeleteCategoryData(mainCategoryModel: MainCategoryModel?) {
        if (myService == null) {
            return
        }
        isDeleteCategoryData = true
        myService.onDeleteCategoriesSync(mainCategoryModel, object : BaseListener<Any?> {
            override fun onShowListObjects(list: MutableList<*>?) {}
            override fun onShowObjects(`object`: Any?) {}
            override fun onError(message: String?, status: EnumStatus?) {
                isDeleteCategoryData = false
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {
                if (Utils.Companion.deletedIndexOfCategoryHashMap(mainCategoryModel, mMapDeleteCategory)) {
                    /*Delete local db and folder name*/
                    SQLHelper.deleteCategory(mainCategoryModel)
                    val mDeleteItem: MainCategoryModel = Utils.Companion.getArrayOfIndexCategoryHashMap(mMapDeleteCategory)
                    if (mDeleteItem != null) {
                        isDeleteCategoryData = true
                        Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(mainCategoryModel))
                        onDeleteCategoryData(mDeleteItem)
                    } else {
                        isDeleteCategoryData = false
                        Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(mainCategoryModel))
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Deleted completely...............")
                        Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DELETED_CATEGORY_SUCCESSFULLY, "Total uploading " + mMapDeleteCategory.size)
                        onPreparingDownloadData(mDownloadList)
                    }
                }
            }
        })
    }

    /*Preparing to download data from Google drive and system server*/
    private fun onPreparingDownloadData(globalList: MutableList<ItemModel?>?) {
        val mListLocal: MutableList<ItemModel?> = SQLHelper.getItemListDownload()
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDownloadData ==> Local original list " + Gson().toJson(mListLocal))
        if (mListLocal != null) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDownloadData ==> Local list " + Gson().toJson(mListLocal))
            if (globalList != null && mListLocal != null) {
                val mergeList: MutableList<ItemModel?> = Utils.Companion.clearListFromDuplicate(globalList, mListLocal)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDownloadData ==> clear duplicated data " + Gson().toJson(mergeList))
                if (mergeList != null) {
                    if (mergeList.size > 0) {
                        mMapDownload.clear()
                        mMapDownload = Utils.Companion.mergeListToHashMap(mergeList)
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDownloadData ==> clear merged data " + Gson().toJson(mMapDownload))
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDownloadData ==> merged data " + Gson().toJson(mergeList))
                        val itemModel: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownload)
                        if (itemModel != null) {
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Total downloading " + mMapDownload.size)
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Preparing to download " + Gson().toJson(itemModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Preparing to download total  " + mMapDownload.size)
                            onDownLoadData(itemModel)
                        }
                    } else {
                        /*Preparing upload file to Google drive*/
                        onPreparingUploadData()
                    }
                }
            }
        }
    }

    /*Download file from Google drive*/
    private fun onDownLoadData(itemModel: ItemModel?) {
        if (myService != null) {
            isDownloadData = true
            mStart = 20
            Utils.Companion.onPushEventBus(EnumStatus.DOWNLOAD)
            myService.onDownloadFile(itemModel, false, object : DownloadServiceListener {
                override fun onProgressDownload(percentage: Int) {
                    isDownloadData = true
                    if (mStart == percentage) {
                        Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?) {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onDownLoadCompleted ==> onDownLoadCompleted:" + file_name.getAbsolutePath())
                    if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapDownload)) {
                        /*Delete local db and folder name*/
                        val mDownloadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownload)
                        if (mDownloadItem != null) {
                            onDownLoadData(mDownloadItem)
                            isDownloadData = true
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Download completely...............")
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownload.size)
                            isDownloadData = false
                            onPreparingUploadData()
                            /*Download done for main tab*/Utils.Companion.onPushEventBus(EnumStatus.DONE)
                        }
                    }
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onDownLoadData ==> onError:$message")
                    Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onDownLoadData ==> onError $message")
                    isDownloadData = false
                    if (status == EnumStatus.NO_SPACE_LEFT) {
                        Utils.Companion.onPushEventBus(EnumStatus.NO_SPACE_LEFT)
                        Utils.Companion.onDeleteItemFolder(itemModel.items_id)
                        onPreparingDeleteData()
                        /*Download done for main tab*/Utils.Companion.onPushEventBus(EnumStatus.DONE)
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD) {
                        if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapDownload)) {
                            /*Delete local db and folder name*/
                            val mDownloadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownload)
                            if (mDownloadItem != null) {
                                onDownLoadData(mDownloadItem)
                                isDownloadData = true
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                            } else {
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Download completely...............")
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownload.size)
                                isDownloadData = false
                                onPreparingUploadData()
                                /*Download done for main tab*/Utils.Companion.onPushEventBus(EnumStatus.DONE)
                            }
                        }
                    }
                }
            })
        }
    }

    /*Preparing upload data*/
    private fun onPreparingUploadData() {
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingUploadData")
        if (!Utils.Companion.isCheckAllowUpload()) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingUploadData ==> Left 0. Please wait for next month or upgrade to premium version")
            Utils.Companion.onPushEventBus(EnumStatus.DONE)
            Utils.Companion.onPushEventBus(EnumStatus.REFRESH)
            onPreparingUpdateItemData()
            return
        }
        /*Preparing upload file to Google drive*/
        val mResult: MutableList<ItemModel?> = SQLHelper.getItemListUpload()
        val listUpload: MutableList<ItemModel?> = Utils.Companion.getMergedOriginalThumbnailList(true, mResult)
        if (listUpload.size > 0) {
            mMapUpload.clear()
            mMapUpload = Utils.Companion.mergeListToHashMap(listUpload)
            val itemModel: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapUpload)
            if (itemModel != null) {
                Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.PROGRESS, "Total uploading " + mMapUpload.size)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingUploadData ==> total: " + mMapUpload.size)
                onUploadData(itemModel)
            }
        } else {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to upload")
            onPreparingUpdateItemData()
        }
    }

    /*Upload file to Google drive*/
    private fun onUploadData(itemModel: ItemModel?) {
        if (myService != null) {
            mStart = 20
            Utils.Companion.onPushEventBus(EnumStatus.UPLOAD)
            myService.onUploadFileInAppFolder(itemModel, object : ServiceManager.UploadServiceListener {
                override fun onProgressUpdate(percentage: Int) {
                    isUploadData = true
                    if (mStart == percentage) {
                        Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onFinish() {}
                override fun onResponseData(response: DriveResponse?) {
                    if (response == null) {
                        isUploadData = false
                        return
                    }
                    ServiceManager.Companion.getInstance().onInsertItem(itemModel, response.id, object : ServiceManagerInsertItem {
                        override fun onCancel() {
                            isUploadData = false
                        }

                        override fun onError(message: String?, status: EnumStatus?) {
                            isUploadData = false
                            Utils.Companion.onPushEventBus(EnumStatus.DONE)
                        }

                        override fun onSuccessful(message: String?, status: EnumStatus?) {
                            if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapUpload)) {
                                val mUploadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapUpload)
                                if (mUploadItem != null) {
                                    onUploadData(mUploadItem)
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Next upload item..............." + Gson().toJson(mUploadItem))
                                    isUploadData = true
                                } else {
                                    isUploadData = false
                                    onPreparingUpdateItemData()
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Upload completely...............")
                                    Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                    Utils.Companion.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                                    Utils.Companion.onPushEventBus(EnumStatus.DONE)
                                    Utils.Companion.onPushEventBus(EnumStatus.REFRESH)
                                    Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.UPLOAD_COMPLETED, "Total uploading " + mMapUpload.size)
                                }
                            }
                        }
                    })
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    isUploadData = false
                    Utils.Companion.onPushEventBus(EnumStatus.DONE)
                    Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.ERROR, "onUploadLoadData ==> onError $message")
                    if (status == EnumStatus.NO_SPACE_LEFT_CLOUD) {
                        Utils.Companion.onPushEventBus(EnumStatus.NO_SPACE_LEFT_CLOUD)
                        onPreparingDeleteData()
                        Utils.Companion.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                    }
                    if (status == EnumStatus.REQUEST_NEXT_UPLOAD) {
                        if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapUpload)) {
                            val mUploadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapUpload)
                            if (mUploadItem != null) {
                                onUploadData(mUploadItem)
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Next upload item..............." + Gson().toJson(mUploadItem))
                                isUploadData = true
                            } else {
                                isUploadData = false
                                onPreparingUpdateItemData()
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Upload completely...............")
                                Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                                Utils.Companion.onPushEventBus(EnumStatus.DONE)
                                Utils.Companion.onPushEventBus(EnumStatus.REFRESH)
                                Utils.Companion.onWriteLog(EnumStatus.UPLOAD, EnumStatus.UPLOAD_COMPLETED, "Total uploading " + mMapUpload.size)
                            }
                        }
                    }
                }
            })
        }
    }

    /*Preparing update item*/
    fun onPreparingUpdateItemData() {
        val mResult: MutableList<ItemModel?> = SQLHelper.getRequestUpdateItemList()
        if (mResult.size > 0) {
            mMapUpdateItem.clear()
            mMapUpdateItem = Utils.Companion.mergeListToHashMap(mResult)
            val itemModel: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapUpdateItem)
            if (itemModel != null) {
                Utils.Companion.onWriteLog(EnumStatus.UPDATE, EnumStatus.PROGRESS, "Total updating " + mMapUpdateItem.size)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingUpdateItemData ==> total: " + mMapUpdateItem.size)
                onUpdateItemData(itemModel)
            }
        } else {
            onPreparingDeleteData()
        }
    }

    /*Update item*/
    fun onUpdateItemData(itemModel: ItemModel?) {
        if (myService != null) {
            isUpdateItemData = true
            myService.onUpdateItems(itemModel, object : BaseListener<Any?> {
                override fun onShowListObjects(list: MutableList<*>?) {}
                override fun onShowObjects(`object`: Any?) {}
                override fun onError(message: String?, status: EnumStatus?) {
                    isUpdateItemData = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    isUpdateItemData = false
                    if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapUpdateItem)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapUpdateItem)
                        if (mUpdatedItem != null) {
                            onUpdateItemData(mUpdatedItem)
                            isUpdateItemData = true
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Update completely...............")
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.onWriteLog(EnumStatus.UPDATE, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapUpdateItem.size)
                            isUpdateItemData = false
                            Utils.Companion.onPushEventBus(EnumStatus.UPDATED_COMPLETED)
                            Utils.Companion.onPushEventBus(EnumStatus.DONE)
                            onPreparingDeleteData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing to delete item from system server*/
    fun onPreparingDeleteData() {
        val mResult: MutableList<ItemModel?> = SQLHelper.getDeleteItemRequest()
        /*Merged original and thumbnail*/
        val listRequestDelete: MutableList<ItemModel?> = Utils.Companion.getMergedOriginalThumbnailList(false, mResult)
        mMapDeleteItem.clear()
        mMapDeleteItem = Utils.Companion.mergeListToHashMap(listRequestDelete)
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingDeleteData preparing to delete " + mMapDeleteItem.size)
        val itemModel: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDeleteItem)
        if (itemModel != null) {
            Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.PROGRESS, "Total Delete category " + mMapDeleteItem.size)
            onDeleteData(itemModel)
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Preparing to delete data " + mMapDeleteItem.size)
        } else {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to upload")
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to delete ")
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Sync items completely======>ready to test")
            isDeleteItemData = false
            isHandleLogic = false
            Utils.Companion.onPushEventBus(EnumStatus.DONE)
            Utils.Companion.checkRequestUploadItemData()
            SingletonPrivateFragment.Companion.getInstance().onUpdateView()
        }
    }

    /*Delete item from Google drive and server*/
    fun onDeleteData(itemModel: ItemModel?) {
        if (myService == null) {
            return
        }
        isDeleteItemData = true
        /*Request delete item from cloud*/myService.onDeleteCloudItems(itemModel, object : BaseListener<Any?> {
            override fun onShowListObjects(list: MutableList<*>?) {}
            override fun onShowObjects(`object`: Any?) {}
            override fun onError(message: String?, status: EnumStatus?) {}
            override fun onSuccessful(message: String?, status: EnumStatus?) {
                /*Request delete item from system*/
                myService.onDeleteOwnSystem(itemModel, object : BaseListener<Any?> {
                    override fun onShowListObjects(list: MutableList<*>?) {}
                    override fun onShowObjects(`object`: Any?) {}
                    override fun onError(message: String?, status: EnumStatus?) {
                        isDeleteItemData = false
                    }

                    override fun onSuccessful(message: String?, status: EnumStatus?) {
                        if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapDeleteItem)) {
                            /*Delete local db and folder name*/
                            Utils.Companion.onDeleteItemFolder(itemModel.items_id)
                            SQLHelper.deleteItem(itemModel)
                            val mDeleteItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDeleteItem)
                            if (mDeleteItem != null) {
                                isDeleteItemData = true
                                Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(itemModel))
                                onDeleteData(mDeleteItem)
                            } else {
                                isDeleteItemData = false
                                Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Deleted completely...............")
                                Utils.Companion.onWriteLog(EnumStatus.DELETE, EnumStatus.DELETED_ITEM_SUCCESSFULLY, "Total deleted " + mMapDeleteItem.size)
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to upload")
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to delete ")
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Sync items completely======>ready to test")
                                isHandleLogic = false
                                Utils.Companion.onPushEventBus(EnumStatus.DONE)
                                Utils.Companion.checkRequestUploadItemData()
                                SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                            }
                        }
                    }
                })
            }
        })
    }

    fun onInsertItem(itemRequest: ItemModel?, drive_id: String?, ls: ServiceManagerInsertItem?) {
        if (myService != null) {
            myService.onAddItems(itemRequest, drive_id, object : ServiceManagerInsertItem {
                override fun onCancel() {
                    ls.onCancel()
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    ls.onError(message, status)
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    ls.onSuccessful(message, status)
                }
            })
        }
    }

    /*Preparing import data*/
    fun onPreparingImportData() {
        if (isImportData) {
            return
        }
        /*Preparing upload file to Google drive*/if (listImport.size > 0) {
            mMapImporting.clear()
            mMapImporting = Utils.Companion.mergeListToHashMapImport(listImport)
            val itemModel: ImportFilesModel = Utils.Companion.getArrayOfIndexHashMapImport(mMapImporting)
            if (itemModel != null) {
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Preparing to import " + Gson().toJson(itemModel))
                Utils.Companion.onPushEventBus(EnumStatus.IMPORTING)
                onImportData(itemModel)
            }
        } else {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Not found item to import")
        }
    }

    /*Import data from gallery*/
    private fun onImportData(importFiles: ImportFilesModel?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val mMimeTypeFile: MimeTypeFile = importFiles.mimeTypeFile
            val enumTypeFile = mMimeTypeFile.formatType
            val mPath: String = importFiles.path
            val mMimeType = mMimeTypeFile.mimeType
            val mMainCategories: MainCategoryModel = importFiles.mainCategories
            val categories_id: String = mMainCategories.categories_id
            val categories_local_id: String = mMainCategories.categories_local_id
            val isFakePin: Boolean = mMainCategories.isFakePin
            val uuId: String = importFiles.unique_id
            var thumbnail: Bitmap? = null
            when (enumTypeFile) {
                EnumFormatType.IMAGE -> {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Start RXJava Image Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
                        val currentTime: String = Utils.Companion.getCurrentDateTime()
                        val pathContent = "$rootPath$uuId/"
                        storage.createDirectory(pathContent)
                        val thumbnailPath = pathContent + "thumbnail_" + currentTime
                        val originalPath = pathContent + currentTime
                        val itemsPhoto = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, Utils.Companion.getSaverSpace(), false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        val file: File = Compressor(SuperSafeApplication.Companion.getInstance())
                                .setMaxWidth(1032)
                                .setMaxHeight(774)
                                .setQuality(85)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(File(mPath))
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "start compress")
                        val createdThumbnail = storage.createFile(File(thumbnailPath), file, Cipher.ENCRYPT_MODE)
                        val createdOriginal = storage.createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "start end")
                        val response = ResponseRXJava()
                        response.items = itemsPhoto
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdThumbnail && createdOriginal) {
                            response.isWorking = true
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                        Utils.Companion.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber.onNext(response)
                        subscriber.onComplete()
                    } finally {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
                    }
                }
                EnumFormatType.VIDEO -> {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Start RXJava Video Progressing")
                    try {
                        try {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(mPath,
                                    MediaStore.Video.Thumbnails.MINI_KIND)
                            val exifInterface = ExifInterface(mPath)
                            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                            Utils.Companion.Log("EXIF", "Exif: $orientation")
                            val matrix = Matrix()
                            if (orientation == 6) {
                                matrix.postRotate(90f)
                            } else if (orientation == 3) {
                                matrix.postRotate(180f)
                            } else if (orientation == 8) {
                                matrix.postRotate(270f)
                            }
                            thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true) // rotating bitmap
                        } catch (e: Exception) {
                            thumbnail = BitmapFactory.decodeResource(SuperSafeApplication.Companion.getInstance().getResources(),
                                    R.drawable.ic_default_video)
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                        }
                        val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
                        val currentTime: String = Utils.Companion.getCurrentDateTime()
                        val pathContent = "$rootPath$uuId/"
                        storage.createDirectory(pathContent)
                        val thumbnailPath = pathContent + "thumbnail_" + currentTime
                        val originalPath = pathContent + currentTime
                        val itemsVideo = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.VIDEO, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Call thumbnail")
                        val createdThumbnail: Boolean = storage.createFile(thumbnailPath, thumbnail)
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage.createLargeFile(File(originalPath), File(mPath), mCiphers)
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Call original")
                        val response = ResponseRXJava()
                        response.items = itemsVideo
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdThumbnail && createdOriginal) {
                            response.isWorking = true
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                        Utils.Companion.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber.onNext(response)
                        subscriber.onComplete()
                    } finally {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
                    }
                }
                EnumFormatType.AUDIO -> {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Start RXJava Audio Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
                        val currentTime: String = Utils.Companion.getCurrentDateTime()
                        val pathContent = "$rootPath$uuId/"
                        storage.createDirectory(pathContent)
                        val originalPath = pathContent + currentTime
                        val itemsAudio = ItemModel(mMimeTypeFile.extension, originalPath, "null", categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.AUDIO, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage.createLargeFile(File(originalPath), File(mPath), mCiphers)
                        val response = ResponseRXJava()
                        response.items = itemsAudio
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdOriginal) {
                            response.isWorking = true
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                        Utils.Companion.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber.onNext(response)
                        subscriber.onComplete()
                    } finally {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
                    }
                }
                EnumFormatType.FILES -> {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Start RXJava Files Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
                        val currentTime: String = Utils.Companion.getCurrentDateTime()
                        val pathContent = "$rootPath$uuId/"
                        storage.createDirectory(pathContent)
                        val originalPath = pathContent + currentTime
                        val itemsFile = ItemModel(mMimeTypeFile.extension, originalPath, "null", categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.FILES, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage.createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                        val response = ResponseRXJava()
                        response.items = itemsFile
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdOriginal) {
                            response.isWorking = true
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber.onNext(response)
                            subscriber.onComplete()
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                        Utils.Companion.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber.onNext(response)
                        subscriber.onComplete()
                    } finally {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
                    }
                }
            }
            Utils.Companion.Log(ServiceManager.Companion.TAG, "End up RXJava")
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mResponse: ResponseRXJava? = response as ResponseRXJava?
                    try {
                        if (mResponse.isWorking) {
                            val items: ItemModel = mResponse.items
                            var mb: Long
                            val enumFormatType = EnumFormatType.values()[items.formatType]
                            when (enumFormatType) {
                                EnumFormatType.AUDIO -> {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = +storage.getSize(File(items.originalPath), SizeUnit.B) as Long
                                        items.size = "" + mb
                                        SQLHelper.insertedItem(items)
                                    }
                                }
                                EnumFormatType.FILES -> {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = +storage.getSize(File(items.originalPath), SizeUnit.B) as Long
                                        items.size = "" + mb
                                        SQLHelper.insertedItem(items)
                                    }
                                }
                                else -> {
                                    if (storage.isFileExist(items.originalPath) && storage.isFileExist(items.thumbnailPath)) {
                                        mb = +storage.getSize(File(items.originalPath), SizeUnit.B) as Long
                                        if (storage.isFileExist(items.thumbnailPath)) {
                                            mb += +storage.getSize(File(items.thumbnailPath), SizeUnit.B) as Long
                                        }
                                        items.size = "" + mb
                                        SQLHelper.insertedItem(items)
                                        if (!mResponse.categories.isCustom_Cover) {
                                            if (enumFormatType == EnumFormatType.IMAGE) {
                                                val main: MainCategoryModel = mResponse.categories
                                                main.items_id = items.items_id
                                                SQLHelper.updateCategory(main)
                                            }
                                        }
                                    }
                                }
                            }
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Write file successful ")
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Write file Failed ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (mResponse.isWorking) {
                            val items: ItemModel = mResponse.items
                            GalleryCameraMediaManager.Companion.getInstance().setProgressing(false)
                            EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                            if (items.isFakePin) {
                                SingletonFakePinComponent.Companion.getInstance().onUpdateView()
                            } else {
                                SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                            }
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Original path :" + mResponse.originalPath)
                            val storage = Storage(SuperSafeApplication.Companion.getInstance())
                            if (!getRequestShareIntent()) {
                                Utils.Companion.onDeleteFile(mResponse.originalPath)
                            }
                            if (Utils.Companion.deletedIndexOfHashMapImport(importFiles, mMapImporting)) {
                                val mImportItem: ImportFilesModel = Utils.Companion.getArrayOfIndexHashMapImport(mMapImporting)
                                if (mImportItem != null) {
                                    onImportData(mImportItem)
                                    isImportData = true
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Next import data completely")
                                } else {
                                    isImportData = false
                                    listImport.clear()
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Imported data completely")
                                    Utils.Companion.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY)
                                    ServiceManager.Companion.getInstance().onPreparingSyncData()
                                }
                            }
                        } else {
                            val mImportItem: ImportFilesModel = Utils.Companion.getArrayOfIndexHashMapImport(mMapImporting)
                            if (mImportItem != null) {
                                onImportData(mImportItem)
                                isImportData = true
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Next import data completely")
                            } else {
                                isImportData = false
                                listImport.clear()
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Imported data completely")
                                Utils.Companion.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY)
                                ServiceManager.Companion.getInstance().onPreparingSyncData()
                            }
                        }
                    }
                }
    }

    fun setIsWaitingSendMail(isWaitingSendMail: Boolean) {
        this.isWaitingSendMail = isWaitingSendMail
    }

    fun setProgress(mProgress: String?) {
        this.mProgress = mProgress
    }

    fun getProgress(): String? {
        return mProgress
    }

    fun setmListExport(mListExport: MutableList<ExportFiles?>?) {
        if (!isExportData) {
            this.mListExport.clear()
            this.mListExport.addAll(mListExport)
        }
    }

    fun setExporting(exporting: Boolean) {
        isExportData = exporting
    }

    fun setUpdate(update: Boolean) {
        isUpdateItemData = update
    }

    fun onPickUpNewEmailNoTitle(context: Activity?, account: String?) {
        try {
            val account1 = Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), null, null, null, null)
            intent.putExtra("overrideTheme", 1)
            //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun onPickUpExistingEmail(context: Activity?, account: String?) {
        try {
            val value = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.choose_an_account), account)
            val account1 = Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), value, null, null, null)
            intent.putExtra("overrideTheme", 1)
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun onPickUpNewEmail(context: Activity?) {
        try {
            if (Utils.Companion.getUserId() == null) {
                return
            }
            val value = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.choose_an_new_account))
            val account1 = Account(Utils.Companion.getUserId(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), value, null, null, null)
            intent.putExtra("overrideTheme", 1)
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun setContext(mContext: Context?) {
        this.mContext = mContext
    }

    private fun doBindService() {
        if (myService != null) {
            return
        }
        var intent: Intent? = null
        intent = Intent(mContext, SuperSafeService::class.java)
        intent.putExtra(ServiceManager.Companion.TAG, "Message")
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onStartService")
    }

    fun onStartService() {
        if (myService == null) {
            doBindService()
            Utils.Companion.Log(ServiceManager.Companion.TAG, "start services now")
        }
    }

    fun onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection)
            myService = null
            Utils.Companion.Log(ServiceManager.Companion.TAG, "stop services now")
        }
    }

    fun onSendEmail() {
        if (myService != null) {
            val mUser: User = Utils.Companion.getUserInfo()
            val emailToken: EmailToken = EmailToken.Companion.getInstance().convertObject(mUser, EnumStatus.RESET)
            myService.onSendMail(emailToken)
        }
    }

    fun getMyService(): SuperSafeService? {
        return myService
    }

    private fun getString(res: Int): String? {
        return SuperSafeApplication.Companion.getInstance().getString(res)
    }

    /*User info*/
    fun onGetUserInfo() {
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onGetUserInfo")
        if (myService != null) {
            myService.onGetUserInfo()
        } else {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "My services is null")
            onStartService()
        }
    }

    /*Update user token*/
    fun onUpdatedUserToken() {
        Utils.Companion.onWriteLog("onUpdatedUserToken", EnumStatus.UPDATE_USER_TOKEN)
        if (myService != null) {
            myService.onUpdateUserToken()
        } else {
            ServiceManager.Companion.getInstance().onStartService()
            Utils.Companion.Log(ServiceManager.Companion.TAG, "My services is null")
        }
    }

    /*Response Network*/
    fun onGetDriveAbout() {
        if (myService != null) {
            myService.getDriveAbout()
        }
    }

    /*Sync Author Device*/
    fun onSyncAuthorDevice() {
        if (myService != null) {
            myService.onSyncAuthorDevice()
        }
    }

    /*--------------Camera action-----------------*/
    fun onSaveDataOnCamera(mData: ByteArray?, mainCategories: MainCategoryModel?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val mMainCategories: MainCategoryModel? = mainCategories
            val categories_id: String = mMainCategories.categories_id
            val categories_local_id: String = mMainCategories.categories_local_id
            val isFakePin: Boolean = mMainCategories.isFakePin
            try {
                val rootPath: String = SuperSafeApplication.Companion.getInstance().getSupersafePrivate()
                val currentTime: String = Utils.Companion.getCurrentDateTime()
                val uuId: String = Utils.Companion.getUUId()
                val pathContent = "$rootPath$uuId/"
                storage.createDirectory(pathContent)
                val thumbnailPath = pathContent + "thumbnail_" + currentTime
                val originalPath = pathContent + currentTime
                val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
                val items = ItemModel(getString(R.string.key_jpg), originalPath, thumbnailPath, categories_id, categories_local_id, MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype(), uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, currentTime + getString(R.string.key_jpg), "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, isSaver, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                storage.createFileByteDataNoEncrypt(SuperSafeApplication.Companion.getInstance(), mData, object : OnStorageListener {
                    override fun onSuccessful() {}
                    override fun onSuccessful(path: String?) {
                        try {
                            val file: File = Compressor(SuperSafeApplication.Companion.getInstance())
                                    .setMaxWidth(1032)
                                    .setMaxHeight(774)
                                    .setQuality(85)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .compressToFile(File(path))
                            val createdThumbnail = storage.createFile(File(thumbnailPath), file, Cipher.ENCRYPT_MODE)
                            val createdOriginal = storage.createFile(originalPath, mData, Cipher.ENCRYPT_MODE)
                            val response = ResponseRXJava()
                            response.items = items
                            response.categories = mMainCategories
                            if (createdThumbnail && createdOriginal) {
                                response.isWorking = true
                                subscriber.onNext(response)
                                subscriber.onComplete()
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile successful")
                            } else {
                                response.isWorking = false
                                subscriber.onNext(response)
                                subscriber.onComplete()
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "CreatedFile failed")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val response = ResponseRXJava()
                            response.isWorking = false
                            subscriber.onNext(response)
                            subscriber.onComplete()
                        }
                    }

                    override fun onFailed() {
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber.onNext(response)
                        subscriber.onComplete()
                    }

                    override fun onSuccessful(position: Int) {}
                })
            } catch (e: Exception) {
                val response = ResponseRXJava()
                response.isWorking = false
                subscriber.onNext(response)
                subscriber.onComplete()
                Utils.Companion.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
            } finally {
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mResponse: ResponseRXJava? = response as ResponseRXJava?
                    try {
                        if (mResponse.isWorking) {
                            val mItem: ItemModel = mResponse.items
                            var mb: Long
                            if (storage.isFileExist(mItem.originalPath) && storage.isFileExist(mItem.thumbnailPath)) {
                                mb = +storage.getSize(File(mItem.originalPath), SizeUnit.B) as Long
                                if (storage.isFileExist(mItem.thumbnailPath)) {
                                    mb += +storage.getSize(File(mItem.thumbnailPath), SizeUnit.B) as Long
                                }
                                mItem.size = "" + mb
                                SQLHelper.insertedItem(mItem)
                                if (!mResponse.categories.isCustom_Cover) {
                                    val main: MainCategoryModel = mResponse.categories
                                    main.items_id = mItem.items_id
                                    SQLHelper.updateCategory(main)
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Special main categories " + Gson().toJson(main))
                                }
                            }
                        }
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Insert Successful")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (mResponse.isWorking) {
                            val mItem: ItemModel = mResponse.items
                            GalleryCameraMediaManager.Companion.getInstance().setProgressing(false)
                            EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                            if (mItem.isFakePin) {
                                SingletonFakePinComponent.Companion.getInstance().onUpdateView()
                            } else {
                                SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                                ServiceManager.Companion.getInstance().onPreparingSyncData()
                            }
                        }
                    }
                }
    }

    fun onExportingFiles() {
        Utils.Companion.Log(ServiceManager.Companion.TAG, "Export amount files :" + mListExport.size)
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            setExporting(true)
            var isWorking = false
            var exportFiles: ExportFiles? = null
            var position = 0
            for (i in mListExport.indices) {
                if (!mListExport.get(i).isExport) {
                    exportFiles = mListExport.get(i)
                    isWorking = true
                    position = i
                    break
                }
            }
            if (isWorking) {
                val mInput: File = exportFiles.input
                val mOutPut: File = exportFiles.output
                try {
                    val storage = Storage(SuperSafeApplication.Companion.getInstance())
                    storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
                    val mCipher = storage.getCipher(Cipher.DECRYPT_MODE)
                    val formatType = EnumFormatType.values()[exportFiles.formatType]
                    if (formatType == EnumFormatType.VIDEO || formatType == EnumFormatType.AUDIO) {
                        storage.createLargeFile(mOutPut, mInput, mCipher, position, object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                Utils.Companion.onWriteLog("Exporting failed", EnumStatus.EXPORT)
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Exporting failed")
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {
                                try {
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Exporting large file...............................Successful $position")
                                    mListExport.get(position).isExport = true
                                    onExportingFiles()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    } else {
                        storage.createFile(mOutPut, mInput, Cipher.DECRYPT_MODE, position, object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                Utils.Companion.onWriteLog("Exporting failed", EnumStatus.EXPORT)
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Exporting failed")
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {
                                try {
                                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Exporting file...............................Successful $position")
                                    mListExport.get(position).isExport = true
                                    onExportingFiles()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                } catch (e: Exception) {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Cannot write to $e")
                } finally {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "Finally")
                }
            } else {
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Exporting file............................Done")
                EventBus.getDefault().post(EnumStatus.STOP_PROGRESS)
                setExporting(false)
                mListExport.clear()
                ServiceManager.Companion.getInstance().onPreparingSyncData()
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? -> }
    }

    override fun onError(message: String?, status: EnumStatus?) {
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onError response :" + message + " - " + status.name)
        if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
            EventBus.getDefault().post(EnumStatus.REQUEST_ACCESS_TOKEN)
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Request token on global")
        }
    }

    override fun getContext(): Context? {
        return mContext
    }

    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SCREEN_OFF -> {
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                val action = EnumPinAction.values()[value]
                when (action) {
                    EnumPinAction.NONE -> {
                        val key: String = SuperSafeApplication.Companion.getInstance().readKey()
                        if ("" != key) {
                            Utils.Companion.onHomePressed()
                        }
                    }
                    else -> {
                        Utils.Companion.Log(ServiceManager.Companion.TAG, "Nothing to do ???")
                    }
                }
            }
            EnumStatus.GET_DRIVE_ABOUT -> {
            }
            EnumStatus.CONNECTED -> {
                EventBus.getDefault().post(EnumStatus.CONNECTED)
                ServiceManager.Companion.getInstance().onGetUserInfo()
                PremiumManager.Companion.getInstance().onStartInAppPurchase()
            }
            EnumStatus.DISCONNECTED -> {
                onDefaultValue()
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Disconnect")
            }
            EnumStatus.USER_INFO -> {
                Utils.Companion.Log(ServiceManager.Companion.TAG, "Get info successful")
                ServiceManager.Companion.getInstance().onPreparingSyncData()
                val mUser: User = Utils.Companion.getUserInfo()
                if (mUser.isWaitingSendMail) {
                    ServiceManager.Companion.getInstance().onSendEmail()
                }
            }
            EnumStatus.UPDATE_USER_TOKEN -> {
                ServiceManager.Companion.getInstance().onPreparingSyncData()
            }
        }
    }

    fun onPreparingEnableDownloadData(globalList: MutableList<ItemModel?>?) {
        if (isDownloadToExportFiles) {
            return
        }
        val mergeList: MutableList<ItemModel?> = Utils.Companion.getMergedOriginalThumbnailList(false, globalList)
        Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingEnableDownloadData ==> clear duplicated data " + Gson().toJson(mergeList))
        if (mergeList != null) {
            if (mergeList.size > 0) {
                mMapDownloadToExportFiles.clear()
                mMapDownloadToExportFiles = Utils.Companion.mergeListToHashMap(mergeList)
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingEnableDownloadData ==> clear merged data " + Gson().toJson(mMapDownloadToExportFiles))
                Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingEnableDownloadData ==> merged data " + Gson().toJson(mergeList))
                val itemModel: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                if (itemModel != null) {
                    Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Total downloading " + mMapDownloadToExportFiles.size)
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingEnableDownloadData to download " + Gson().toJson(itemModel))
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onPreparingEnableDownloadData to download total  " + mMapDownloadToExportFiles.size)
                    onDownLoadDataToExportFiles(itemModel)
                }
            }
        }
    }

    /*Download file from Google drive*/
    private fun onDownLoadDataToExportFiles(itemModel: ItemModel?) {
        if (myService != null) {
            isDownloadToExportFiles = true
            mStart = 20
            myService.onDownloadFile(itemModel, true, object : DownloadServiceListener {
                override fun onProgressDownload(percentage: Int) {
                    isDownloadToExportFiles = true
                    if (mStart == percentage) {
                        Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?) {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onDownLoadCompleted ==> onDownLoadCompleted:" + file_name.getAbsolutePath())
                    if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapDownloadToExportFiles)) {
                        /*Delete local db and folder name*/
                        val mDownloadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                        if (mDownloadItem != null) {
                            onDownLoadDataToExportFiles(mDownloadItem)
                            isDownloadToExportFiles = true
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                        } else {
                            Utils.Companion.Log(ServiceManager.Companion.TAG, "Download completely...............")
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownloadToExportFiles.size)
                            isDownloadToExportFiles = false
                            Utils.Companion.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED)
                        }
                    }
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    Utils.Companion.Log(ServiceManager.Companion.TAG, "onDownLoadData ==> onError:$message")
                    Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onDownLoadData ==> onError $message")
                    isDownloadToExportFiles = false
                    if (status == EnumStatus.NO_SPACE_LEFT) {
                        Utils.Companion.onPushEventBus(EnumStatus.NO_SPACE_LEFT)
                        Utils.Companion.onDeleteItemFolder(itemModel.items_id)
                        onPreparingDeleteData()
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD) {
                        if (Utils.Companion.deletedIndexOfHashMap(itemModel, mMapDownloadToExportFiles)) {
                            /*Delete local db and folder name*/
                            val mDownloadItem: ItemModel = Utils.Companion.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                            if (mDownloadItem != null) {
                                onDownLoadDataToExportFiles(mDownloadItem)
                                isDownloadToExportFiles = true
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                            } else {
                                Utils.Companion.Log(ServiceManager.Companion.TAG, "Download completely...............")
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Companion.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownloadToExportFiles.size)
                                isDownloadToExportFiles = false
                                /*Download completed for export files*/Utils.Companion.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED)
                            }
                        }
                    }
                }
            })
        }
    }

    fun onDefaultValue() {
        isDownloadData = false
        isUploadData = false
        isExportData = false
        isImportData = false
        isUpdateItemData = false
        isUpdateCategoryData = false
        isDeleteItemData = false
        isDeleteCategoryData = false
        isDownloadToExportFiles = false
        isHandleLogic = false
        isSyncCategory = false
        isGetItemList = false
        isWaitingSendMail = false
    }

    fun onDismissServices() {
        if (isDownloadData || isUploadData || isDownloadToExportFiles || isExportData || isImportData || isDeleteItemData || isDeleteCategoryData || isWaitingSendMail || isUpdateItemData || isHandleLogic || isSyncCategory || isGetItemList || isUpdateCategoryData) {
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Progress....................!!!!:")
        } else {
            onDefaultValue()
            if (myService != null) {
                myService.unbindView()
            }
            if (subscriptions != null) {
                subscriptions.dispose()
            }
            onStopService()
            Utils.Companion.Log(ServiceManager.Companion.TAG, "Dismiss Service manager")
        }
    }

    fun getRequestShareIntent(): Boolean {
        return isRequestShareIntent
    }

    fun setRequestShareIntent(requestShareIntent: Boolean) {
        isRequestShareIntent = requestShareIntent
    }

    interface ServiceManagerSyncDataListener {
        open fun onCompleted()
        open fun onError()
        open fun onCancel()
    }

    interface ServiceManagerGalleySyncDataListener {
        open fun onCompleted(importFiles: ImportFilesModel?)
        open fun onFailed(importFiles: ImportFilesModel?)
    }

    /*Upload Service*/
    interface UploadServiceListener {
        open fun onProgressUpdate(percentage: Int)
        open fun onFinish()
        open fun onResponseData(response: DriveResponse?)
        open fun onError(message: String?, status: EnumStatus?)
    }

    interface DownloadServiceListener {
        open fun onProgressDownload(percentage: Int)
        open fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?)
        open fun onError(message: String?, status: EnumStatus?)
    }

    interface BaseListener<T> {
        open fun onShowListObjects(list: MutableList<T>?)
        open fun onShowObjects(`object`: T?)
        open fun onError(message: String?, status: EnumStatus?)
        open fun onSuccessful(message: String?, status: EnumStatus?)
    }

    interface ServiceManagerInsertItem {
        open fun onCancel()
        open fun onError(message: String?, status: EnumStatus?)
        open fun onSuccessful(message: String?, status: EnumStatus?)
    }

    companion object {
        private val TAG = ServiceManager::class.java.simpleName
        private val instance: ServiceManager? = null
        fun getInstance(): ServiceManager? {
            if (ServiceManager.Companion.instance == null) {
                ServiceManager.Companion.instance = ServiceManager()
            }
            return ServiceManager.Companion.instance
        }
    }
}