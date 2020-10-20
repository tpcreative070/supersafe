package co.tpcreative.supersafe.common.viewsimport

import android.app.DownloadManager
import android.app.Fragment
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Message
import android.provider.Settings
import android.util.AttributeSet
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import co.tpcreative.supersafe.common.views.AdvancedWebView
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.util.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
/*
 * Android-AdvancedWebView (https://github.com/delight-im/Android-AdvancedWebView)
 * Copyright (c) delight.im (https://www.delight.im/)
 * Licensed under the MIT License (https://opensource.org/licenses/MIT)
 */ /** Advanced WebView component for Android that works as intended out of the box  */
class AdvancedWebView : WebView {
    interface Listener {
        open fun onPageStarted(url: String?, favicon: Bitmap?)
        open fun onPageFinished(url: String?)
        open fun onPageError(errorCode: Int, description: String?, failingUrl: String?)
        open fun onDownloadRequested(url: String?, suggestedFilename: String?, mimeType: String?, contentLength: Long, contentDisposition: String?, userAgent: String?)
        open fun onExternalPageRequest(url: String?)
    }

    protected var mActivity: WeakReference<Activity?>? = null
    protected var mFragment: WeakReference<Fragment?>? = null
    protected var mListener: AdvancedWebView.Listener? = null
    protected val mPermittedHostnames: MutableList<String?>? = LinkedList()

    /** File upload callback for platform versions prior to Android 5.0  */
    protected var mFileUploadCallbackFirst: ValueCallback<Uri?>? = null

    /** File upload callback for Android 5.0+  */
    protected var mFileUploadCallbackSecond: ValueCallback<Array<Uri?>?>? = null
    protected var mLastError: Long = 0
    protected var mLanguageIso3: String? = null
    protected var mRequestCodeFilePicker = REQUEST_CODE_FILE_PICKER
    protected var mCustomWebViewClient: WebViewClient? = null
    protected var mCustomWebChromeClient: WebChromeClient? = null
    protected var mGeolocationEnabled = false
    protected var mUploadableFileTypes: String? = "*/*"
    protected val mHttpHeaders: MutableMap<String?, String?>? = HashMap()

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun setListener(activity: Activity?, listener: AdvancedWebView.Listener?) {
        setListener(activity, listener, REQUEST_CODE_FILE_PICKER)
    }

    fun setListener(activity: Activity?, listener: AdvancedWebView.Listener?, requestCodeFilePicker: Int) {
        mActivity = if (activity != null) {
            WeakReference<Activity?>(activity)
        } else {
            null
        }
        setListener(listener, requestCodeFilePicker)
    }

    fun setListener(fragment: Fragment?, listener: AdvancedWebView.Listener?) {
        setListener(fragment, listener, REQUEST_CODE_FILE_PICKER)
    }

    fun setListener(fragment: Fragment?, listener: AdvancedWebView.Listener?, requestCodeFilePicker: Int) {
        mFragment = fragment?.let { WeakReference(it) }
        setListener(listener, requestCodeFilePicker)
    }

    protected fun setListener(listener: AdvancedWebView.Listener?, requestCodeFilePicker: Int) {
        mListener = listener
        mRequestCodeFilePicker = requestCodeFilePicker
    }

    override fun setWebViewClient(client: WebViewClient?) {
        mCustomWebViewClient = client
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        mCustomWebChromeClient = client
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setGeolocationEnabled(enabled: Boolean) {
        if (enabled) {
            getSettings().setJavaScriptEnabled(true)
            getSettings().setGeolocationEnabled(true)
            setGeolocationDatabasePath()
        }
        mGeolocationEnabled = enabled
    }

    @SuppressLint("NewApi")
    protected fun setGeolocationDatabasePath() {
        val activity: Activity?
        activity = if (mFragment != null && mFragment.get() != null && Build.VERSION.SDK_INT >= 11 && mFragment.get().getActivity() != null) {
            mFragment.get().getActivity()
        } else if (mActivity != null && mActivity.get() != null) {
            mActivity.get()
        } else {
            return
        }
        getSettings().setGeolocationDatabasePath(activity.getFilesDir().getPath())
    }

    fun setUploadableFileTypes(mimeType: String?) {
        mUploadableFileTypes = mimeType
    }
    /**
     * Loads and displays the provided HTML source text
     *
     * @param html the HTML source text to load
     * @param baseUrl the URL to use as the page's base URL
     * @param historyUrl the URL to use for the page's history entry
     * @param encoding the encoding or charset of the HTML source text
     */
    /**
     * Loads and displays the provided HTML source text
     *
     * @param html the HTML source text to load
     * @param baseUrl the URL to use as the page's base URL
     * @param historyUrl the URL to use for the page's history entry
     */
    /**
     * Loads and displays the provided HTML source text
     *
     * @param html the HTML source text to load
     * @param baseUrl the URL to use as the page's base URL
     */
    /**
     * Loads and displays the provided HTML source text
     *
     * @param html the HTML source text to load
     */
    @JvmOverloads
    fun loadHtml(html: String?, baseUrl: String? = null, historyUrl: String? = null, encoding: String? = "utf-8") {
        loadDataWithBaseURL(baseUrl, html, "text/html", encoding, historyUrl)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.onResume()
        }
        resumeTimers()
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        pauseTimers()
        if (Build.VERSION.SDK_INT >= 11) {
            super.onPause()
        }
    }

    fun onDestroy() {
        // try to remove this view from its parent first
        try {
            (getParent() as ViewGroup?).removeView(this)
        } catch (ignored: Exception) {
        }

        // then try to remove all child views from this view
        try {
            removeAllViews()
        } catch (ignored: Exception) {
        }

        // and finally destroy this view
        destroy()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == mRequestCodeFilePicker) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(intent.getData())
                        mFileUploadCallbackFirst = null
                    } else if (mFileUploadCallbackSecond != null) {
                        var dataUris: Array<Uri?>? = null
                        try {
                            if (intent.getDataString() != null) {
                                dataUris = arrayOf(Uri.parse(intent.getDataString()))
                            } else {
                                if (Build.VERSION.SDK_INT >= 16) {
                                    if (intent.getClipData() != null) {
                                        val numSelectedFiles: Int = intent.getClipData().getItemCount()
                                        dataUris = arrayOfNulls<Uri?>(numSelectedFiles)
                                        for (i in 0 until numSelectedFiles) {
                                            dataUris[i] = intent.getClipData().getItemAt(i).getUri()
                                        }
                                    }
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                        mFileUploadCallbackSecond.onReceiveValue(dataUris)
                        mFileUploadCallbackSecond = null
                    }
                }
            } else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst.onReceiveValue(null)
                    mFileUploadCallbackFirst = null
                } else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond.onReceiveValue(null)
                    mFileUploadCallbackSecond = null
                }
            }
        }
    }

    /**
     * Adds an additional HTTP header that will be sent along with every HTTP `GET` request
     *
     * This does only affect the main requests, not the requests to included resources (e.g. images)
     *
     * If you later want to delete an HTTP header that was previously added this way, call `removeHttpHeader()`
     *
     * The `WebView` implementation may in some cases overwrite headers that you set or unset
     *
     * @param name the name of the HTTP header to add
     * @param value the value of the HTTP header to send
     */
    fun addHttpHeader(name: String?, value: String?) {
        mHttpHeaders[name] = value
    }

    /**
     * Removes one of the HTTP headers that have previously been added via `addHttpHeader()`
     *
     * If you want to unset a pre-defined header, set it to an empty string with `addHttpHeader()` instead
     *
     * The `WebView` implementation may in some cases overwrite headers that you set or unset
     *
     * @param name the name of the HTTP header to remove
     */
    fun removeHttpHeader(name: String?) {
        mHttpHeaders.remove(name)
    }

    fun addPermittedHostname(hostname: String?) {
        mPermittedHostnames.add(hostname)
    }

    fun addPermittedHostnames(collection: MutableCollection<out String?>?) {
        mPermittedHostnames.addAll(collection)
    }

    fun getPermittedHostnames(): MutableList<String?>? {
        return mPermittedHostnames
    }

    fun removePermittedHostname(hostname: String?) {
        mPermittedHostnames.remove(hostname)
    }

    fun clearPermittedHostnames() {
        mPermittedHostnames.clear()
    }

    fun onBackPressed(): Boolean {
        return if (canGoBack()) {
            goBack()
            false
        } else {
            true
        }
    }

    fun setCookiesEnabled(enabled: Boolean) {
        CookieManager.getInstance().setAcceptCookie(enabled)
    }

    @SuppressLint("NewApi")
    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, enabled)
        }
    }

    fun setMixedContentAllowed(allowed: Boolean) {
        setMixedContentAllowed(getSettings(), allowed)
    }

    @SuppressLint("NewApi")
    protected fun setMixedContentAllowed(webSettings: WebSettings?, allowed: Boolean) {
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(if (allowed) WebSettings.MIXED_CONTENT_ALWAYS_ALLOW else WebSettings.MIXED_CONTENT_NEVER_ALLOW)
        }
    }

    fun setDesktopMode(enabled: Boolean) {
        val webSettings: WebSettings = getSettings()
        val newUserAgent: String
        newUserAgent = if (enabled) {
            webSettings.getUserAgentString().replace("Mobile", "eliboM").replace("Android", "diordnA")
        } else {
            webSettings.getUserAgentString().replace("eliboM", "Mobile").replace("diordnA", "Android")
        }
        webSettings.setUserAgentString(newUserAgent)
        webSettings.setUseWideViewPort(enabled)
        webSettings.setLoadWithOverviewMode(enabled)
        webSettings.setSupportZoom(enabled)
        webSettings.setBuiltInZoomControls(enabled)
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected fun init(context: Context?) {
        // in IDE's preview mode
        if (isInEditMode()) {
            // do not run the code from this method
            return
        }
        if (context is Activity) {
            mActivity = WeakReference<Activity?>(context as Activity?)
        }
        mLanguageIso3 = getLanguageIso3()
        setFocusable(true)
        setFocusableInTouchMode(true)
        setSaveEnabled(true)
        val filesDir = context.getFilesDir().path
        val databaseDir = filesDir.substring(0, filesDir.lastIndexOf("/")) + DATABASES_SUB_FOLDER
        val webSettings: WebSettings = getSettings()
        webSettings.setAllowFileAccess(false)
        setAllowAccessFromFileUrls(webSettings, false)
        webSettings.setBuiltInZoomControls(false)
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        if (Build.VERSION.SDK_INT < 18) {
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        }
        webSettings.setDatabaseEnabled(true)
        if (Build.VERSION.SDK_INT < 19) {
            webSettings.setDatabasePath(databaseDir)
        }
        setMixedContentAllowed(webSettings, true)
        setThirdPartyCookiesEnabled(true)
        super.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!hasError()) {
                    if (mListener != null) {
                        mListener.onPageStarted(url, favicon)
                    }
                }
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onPageStarted(view, url, favicon)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (!hasError()) {
                    if (mListener != null) {
                        mListener.onPageFinished(url)
                    }
                }
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onPageFinished(view, url)
                }
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                setLastError()
                if (mListener != null) {
                    mListener.onPageError(errorCode, description, failingUrl)
                }
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onReceivedError(view, errorCode, description, failingUrl)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (!isPermittedUrl(url)) {
                    // if a listener is available
                    if (mListener != null) {
                        // inform the listener about the request
                        mListener.onExternalPageRequest(url)
                    }

                    // cancel the original request
                    return true
                }

                // if there is a user-specified handler available
                if (mCustomWebViewClient != null) {
                    // if the user-specified handler asks to override the request
                    if (mCustomWebViewClient.shouldOverrideUrlLoading(view, url)) {
                        // cancel the original request
                        return true
                    }
                }

                // route the request through the custom URL loading method
                view.loadUrl(url)

                // cancel the original request
                return true
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onLoadResource(view, url)
                } else {
                    super.onLoadResource(view, url)
                }
            }

            @SuppressLint("NewApi")
            override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                return if (Build.VERSION.SDK_INT >= 11) {
                    if (mCustomWebViewClient != null) {
                        mCustomWebViewClient.shouldInterceptRequest(view, url)
                    } else {
                        super.shouldInterceptRequest(view, url)
                    }
                } else {
                    null
                }
            }

            @SuppressLint("NewApi")
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return if (Build.VERSION.SDK_INT >= 21) {
                    if (mCustomWebViewClient != null) {
                        mCustomWebViewClient.shouldInterceptRequest(view, request)
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }
                } else {
                    null
                }
            }

            override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onFormResubmission(view, dontResend, resend)
                } else {
                    super.onFormResubmission(view, dontResend, resend)
                }
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.doUpdateVisitedHistory(view, url, isReload)
                } else {
                    super.doUpdateVisitedHistory(view, url, isReload)
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onReceivedSslError(view, handler, error)
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }

            @SuppressLint("NewApi")
            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                if (Build.VERSION.SDK_INT >= 21) {
                    if (mCustomWebViewClient != null) {
                        mCustomWebViewClient.onReceivedClientCertRequest(view, request)
                    } else {
                        super.onReceivedClientCertRequest(view, request)
                    }
                }
            }

            override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm)
                } else {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm)
                }
            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                return if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.shouldOverrideKeyEvent(view, event)
                } else {
                    super.shouldOverrideKeyEvent(view, event)
                }
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onUnhandledKeyEvent(view, event)
                } else {
                    super.onUnhandledKeyEvent(view, event)
                }
            }

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                if (mCustomWebViewClient != null) {
                    mCustomWebViewClient.onScaleChanged(view, oldScale, newScale)
                } else {
                    super.onScaleChanged(view, oldScale, newScale)
                }
            }

            @SuppressLint("NewApi")
            override fun onReceivedLoginRequest(view: WebView?, realm: String?, account: String?, args: String?) {
                if (Build.VERSION.SDK_INT >= 12) {
                    if (mCustomWebViewClient != null) {
                        mCustomWebViewClient.onReceivedLoginRequest(view, realm, account, args)
                    } else {
                        super.onReceivedLoginRequest(view, realm, account, args)
                    }
                }
            }
        })
        super.setWebChromeClient(object : WebChromeClient() {
            // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
            // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
            // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
            @JvmOverloads
            fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String? = null, capture: String? = null) {
                openFileInput(uploadMsg, null, false)
            }

            // file upload callback (Android 5.0 (API level 21) -- current) (public method)
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>?, fileChooserParams: FileChooserParams?): Boolean {
                return if (Build.VERSION.SDK_INT >= 21) {
                    val allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE
                    openFileInput(null, filePathCallback, allowMultiple)
                    true
                } else {
                    false
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onProgressChanged(view, newProgress)
                } else {
                    super.onProgressChanged(view, newProgress)
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onReceivedTitle(view, title)
                } else {
                    super.onReceivedTitle(view, title)
                }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onReceivedIcon(view, icon)
                } else {
                    super.onReceivedIcon(view, icon)
                }
            }

            override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed)
                } else {
                    super.onReceivedTouchIconUrl(view, url, precomposed)
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onShowCustomView(view, callback)
                } else {
                    super.onShowCustomView(view, callback)
                }
            }

            @SuppressLint("NewApi")
            override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
                if (Build.VERSION.SDK_INT >= 14) {
                    if (mCustomWebChromeClient != null) {
                        mCustomWebChromeClient.onShowCustomView(view, requestedOrientation, callback)
                    } else {
                        super.onShowCustomView(view, requestedOrientation, callback)
                    }
                }
            }

            override fun onHideCustomView() {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onHideCustomView()
                } else {
                    super.onHideCustomView()
                }
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                } else {
                    super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }
            }

            override fun onRequestFocus(view: WebView?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onRequestFocus(view)
                } else {
                    super.onRequestFocus(view)
                }
            }

            override fun onCloseWindow(window: WebView?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onCloseWindow(window)
                } else {
                    super.onCloseWindow(window)
                }
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onJsAlert(view, url, message, result)
                } else {
                    super.onJsAlert(view, url, message, result)
                }
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onJsConfirm(view, url, message, result)
                } else {
                    super.onJsConfirm(view, url, message, result)
                }
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onJsPrompt(view, url, message, defaultValue, result)
                } else {
                    super.onJsPrompt(view, url, message, defaultValue, result)
                }
            }

            override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onJsBeforeUnload(view, url, message, result)
                } else {
                    super.onJsBeforeUnload(view, url, message, result)
                }
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                if (mGeolocationEnabled) {
                    callback.invoke(origin, true, false)
                } else {
                    if (mCustomWebChromeClient != null) {
                        mCustomWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback)
                    } else {
                        super.onGeolocationPermissionsShowPrompt(origin, callback)
                    }
                }
            }

            override fun onGeolocationPermissionsHidePrompt() {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onGeolocationPermissionsHidePrompt()
                } else {
                    super.onGeolocationPermissionsHidePrompt()
                }
            }

            @SuppressLint("NewApi")
            override fun onPermissionRequest(request: PermissionRequest?) {
                if (Build.VERSION.SDK_INT >= 21) {
                    if (mCustomWebChromeClient != null) {
                        mCustomWebChromeClient.onPermissionRequest(request)
                    } else {
                        super.onPermissionRequest(request)
                    }
                }
            }

            @SuppressLint("NewApi")
            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                if (Build.VERSION.SDK_INT >= 21) {
                    if (mCustomWebChromeClient != null) {
                        mCustomWebChromeClient.onPermissionRequestCanceled(request)
                    } else {
                        super.onPermissionRequestCanceled(request)
                    }
                }
            }

            override fun onJsTimeout(): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onJsTimeout()
                } else {
                    super.onJsTimeout()
                }
            }

            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onConsoleMessage(message, lineNumber, sourceID)
                } else {
                    super.onConsoleMessage(message, lineNumber, sourceID)
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onConsoleMessage(consoleMessage)
                } else {
                    super.onConsoleMessage(consoleMessage)
                }
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.getDefaultVideoPoster()
                } else {
                    super.getDefaultVideoPoster()
                }
            }

            override fun getVideoLoadingProgressView(): View? {
                return if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.getVideoLoadingProgressView()
                } else {
                    super.getVideoLoadingProgressView()
                }
            }

            override fun getVisitedHistory(callback: ValueCallback<Array<String?>?>?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.getVisitedHistory(callback)
                } else {
                    super.getVisitedHistory(callback)
                }
            }

            override fun onExceededDatabaseQuota(url: String?, databaseIdentifier: String?, quota: Long, estimatedDatabaseSize: Long, totalQuota: Long, quotaUpdater: QuotaUpdater?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater)
                } else {
                    super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater)
                }
            }

            override fun onReachedMaxAppCacheSize(requiredStorage: Long, quota: Long, quotaUpdater: QuotaUpdater?) {
                if (mCustomWebChromeClient != null) {
                    mCustomWebChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
                } else {
                    super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
                }
            }
        })
        setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
                val suggestedFilename = URLUtil.guessFileName(url, contentDisposition, mimeType)
                if (mListener != null) {
                    mListener.onDownloadRequested(url, suggestedFilename, mimeType, contentLength, contentDisposition, userAgent)
                }
            }
        })
    }

    override fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String?, String?>?) {
        var additionalHttpHeaders = additionalHttpHeaders
        if (additionalHttpHeaders == null) {
            additionalHttpHeaders = mHttpHeaders
        } else if (mHttpHeaders.size > 0) {
            additionalHttpHeaders.putAll(mHttpHeaders)
        }
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadUrl(url: String?) {
        if (mHttpHeaders.size > 0) {
            super.loadUrl(url, mHttpHeaders)
        } else {
            super.loadUrl(url)
        }
    }

    fun loadUrl(url: String?, preventCaching: Boolean) {
        var url = url
        if (preventCaching) {
            url = makeUrlUnique(url)
        }
        loadUrl(url)
    }

    fun loadUrl(url: String?, preventCaching: Boolean, additionalHttpHeaders: MutableMap<String?, String?>?) {
        var url = url
        if (preventCaching) {
            url = makeUrlUnique(url)
        }
        loadUrl(url, additionalHttpHeaders)
    }

    fun isPermittedUrl(url: String?): Boolean {
        // if the permitted hostnames have not been restricted to a specific set
        if (mPermittedHostnames.size == 0) {
            // all hostnames are allowed
            return true
        }
        val parsedUrl = Uri.parse(url)

        // get the hostname of the URL that is to be checked
        val actualHost = parsedUrl.host ?: return false

        // if the hostname could not be determined, usually because the URL has been invalid

        // if the host contains invalid characters (e.g. a backslash)
        if (!actualHost.matches("^[a-zA-Z0-9._!~*')(;:&=+$,%\\[\\]-]*$")) {
            // prevent mismatches between interpretations by `Uri` and `WebView`, e.g. for `http://evil.example.com\.good.example.com/`
            return false
        }

        // get the user information from the authority part of the URL that is to be checked
        val actualUserInformation = parsedUrl.userInfo

        // if the user information contains invalid characters (e.g. a backslash)
        if (actualUserInformation != null && !actualUserInformation.matches("^[a-zA-Z0-9._!~*')(;:&=+$,%-]*$")) {
            // prevent mismatches between interpretations by `Uri` and `WebView`, e.g. for `http://evil.example.com\@good.example.com/`
            return false
        }

        // for every hostname in the set of permitted hosts
        for (expectedHost in mPermittedHostnames) {
            // if the two hostnames match or if the actual host is a subdomain of the expected host
            if (actualHost == expectedHost || actualHost.endsWith(".$expectedHost")) {
                // the actual hostname of the URL to be checked is allowed
                return true
            }
        }

        // the actual hostname of the URL to be checked is not allowed since there were no matches
        return false
    }

    @Deprecated("use `isPermittedUrl` instead")
    protected fun isHostnameAllowed(url: String?): Boolean {
        return isPermittedUrl(url)
    }

    protected fun setLastError() {
        mLastError = System.currentTimeMillis()
    }

    protected fun hasError(): Boolean {
        return mLastError + 500 >= System.currentTimeMillis()
    }

    /**
     * Provides localizations for the 25 most widely spoken languages that have a ISO 639-2/T code
     *
     * @return the label for the file upload prompts as a string
     */
    protected fun getFileUploadPromptLabel(): String? {
        try {
            if (mLanguageIso3 == "zho") return decodeBase64("6YCJ5oup5LiA5Liq5paH5Lu2") else if (mLanguageIso3 == "spa") return decodeBase64("RWxpamEgdW4gYXJjaGl2bw==") else if (mLanguageIso3 == "hin") return decodeBase64("4KSP4KSVIOCkq+CkvOCkvuCkh+CksiDgpJrgpYHgpKjgpYfgpII=") else if (mLanguageIso3 == "ben") return decodeBase64("4KaP4KaV4Kaf4Ka/IOCmq+CmvuCmh+CmsiDgpqjgpr/gprDgp43gpqzgpr7gpprgpqg=") else if (mLanguageIso3 == "ara") return decodeBase64("2KfYrtiq2YrYp9ixINmF2YTZgSDZiNin2K3Yrw==") else if (mLanguageIso3 == "por") return decodeBase64("RXNjb2xoYSB1bSBhcnF1aXZv") else if (mLanguageIso3 == "rus") return decodeBase64("0JLRi9Cx0LXRgNC40YLQtSDQvtC00LjQvSDRhNCw0LnQuw==") else if (mLanguageIso3 == "jpn") return decodeBase64("MeODleOCoeOCpOODq+OCkumBuOaKnuOBl+OBpuOBj+OBoOOBleOBhA==") else if (mLanguageIso3 == "pan") return decodeBase64("4KiH4Kmx4KiVIOCoq+CovuCoh+CosiDgqJrgqYHgqKPgqYs=") else if (mLanguageIso3 == "deu") return decodeBase64("V8OkaGxlIGVpbmUgRGF0ZWk=") else if (mLanguageIso3 == "jav") return decodeBase64("UGlsaWggc2lqaSBiZXJrYXM=") else if (mLanguageIso3 == "msa") return decodeBase64("UGlsaWggc2F0dSBmYWls") else if (mLanguageIso3 == "tel") return decodeBase64("4LCS4LCVIOCwq+CxhuCxluCwsuCxjeCwqOCxgSDgsI7gsILgsJrgsYHgsJXgsYvgsILgsKHgsL8=") else if (mLanguageIso3 == "vie") return decodeBase64("Q2jhu41uIG3hu5l0IHThuq1wIHRpbg==") else if (mLanguageIso3 == "kor") return decodeBase64("7ZWY64KY7J2YIO2MjOydvOydhCDshKDtg50=") else if (mLanguageIso3 == "fra") return decodeBase64("Q2hvaXNpc3NleiB1biBmaWNoaWVy") else if (mLanguageIso3 == "mar") return decodeBase64("4KSr4KS+4KSH4KSyIOCkqOCkv+CkteCkoeCkvg==") else if (mLanguageIso3 == "tam") return decodeBase64("4K6S4K6w4K+BIOCuleCvh+CuvuCuquCvjeCuquCviCDgrqTgr4fgrrDgr43grrXgr4E=") else if (mLanguageIso3 == "urd") return decodeBase64("2KfbjNqpINmB2KfYptmEINmF24zauiDYs9uSINin2YbYqtiu2KfYqCDaqdix24zaug==") else if (mLanguageIso3 == "fas") return decodeBase64("2LHYpyDYp9mG2KrYrtin2Kgg2qnZhtuM2K8g24zaqSDZgdin24zZhA==") else if (mLanguageIso3 == "tur") return decodeBase64("QmlyIGRvc3lhIHNlw6dpbg==") else if (mLanguageIso3 == "ita") return decodeBase64("U2NlZ2xpIHVuIGZpbGU=") else if (mLanguageIso3 == "tha") return decodeBase64("4LmA4Lil4Li34Lit4LiB4LmE4Lif4Lil4LmM4Lir4LiZ4Li24LmI4LiH") else if (mLanguageIso3 == "guj") return decodeBase64("4KqP4KqVIOCqq+CqvuCqh+CqsuCqqOCrhyDgqqrgqrjgqoLgqqY=")
        } catch (ignored: Exception) {
        }

        // return English translation by default
        return "Choose a file"
    }

    @SuppressLint("NewApi")
    protected fun openFileInput(fileUploadCallbackFirst: ValueCallback<Uri?>?, fileUploadCallbackSecond: ValueCallback<Array<Uri?>?>?, allowMultiple: Boolean) {
        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst.onReceiveValue(null)
        }
        mFileUploadCallbackFirst = fileUploadCallbackFirst
        if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond.onReceiveValue(null)
        }
        mFileUploadCallbackSecond = fileUploadCallbackSecond
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        if (allowMultiple) {
            if (Build.VERSION.SDK_INT >= 18) {
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        i.setType(mUploadableFileTypes)
        if (mFragment != null && mFragment.get() != null && Build.VERSION.SDK_INT >= 11) {
            mFragment.get().startActivityForResult(Intent.createChooser(i, getFileUploadPromptLabel()), mRequestCodeFilePicker)
        } else if (mActivity != null && mActivity.get() != null) {
            mActivity.get().startActivityForResult(Intent.createChooser(i, getFileUploadPromptLabel()), mRequestCodeFilePicker)
        }
    }

    /** Wrapper for methods related to alternative browsers that have their own rendering engines  */
    object Browsers {
        /** Package name of an alternative browser that is installed on this device  */
        private var mAlternativePackage: String? = null

        /**
         * Returns whether there is an alternative browser with its own rendering engine currently installed
         *
         * @param context a valid `Context` reference
         * @return whether there is an alternative browser or not
         */
        fun hasAlternative(context: Context?): Boolean {
            return getAlternative(context) != null
        }

        /**
         * Returns the package name of an alternative browser with its own rendering engine or `null`
         *
         * @param context a valid `Context` reference
         * @return the package name or `null`
         */
        fun getAlternative(context: Context?): String? {
            if (mAlternativePackage != null) {
                return mAlternativePackage
            }
            val alternativeBrowsers = Arrays.asList(*ALTERNATIVE_BROWSERS)
            val apps: MutableList<ApplicationInfo?> = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                if (!app.enabled) {
                    continue
                }
                if (alternativeBrowsers.contains(app.packageName)) {
                    mAlternativePackage = app.packageName
                    return app.packageName
                }
            }
            return null
        }
        /**
         * Opens the given URL in an alternative browser
         *
         * @param context a valid `Activity` reference
         * @param url the URL to open
         * @param withoutTransition whether to switch to the browser `Activity` without a transition
         */
        /**
         * Opens the given URL in an alternative browser
         *
         * @param context a valid `Activity` reference
         * @param url the URL to open
         */
        @JvmOverloads
        fun openUrl(context: Activity?, url: String?, withoutTransition: Boolean = false) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage(getAlternative(context))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            if (withoutTransition) {
                context.overridePendingTransition(0, 0)
            }
        }
    }

    companion object {
        val PACKAGE_NAME_DOWNLOAD_MANAGER: String? = "com.android.providers.downloads"
        protected const val REQUEST_CODE_FILE_PICKER = 51426
        protected val DATABASES_SUB_FOLDER: String? = "/databases"
        protected val LANGUAGE_DEFAULT_ISO3: String? = "eng"
        protected val CHARSET_DEFAULT: String? = "UTF-8"

        /** Alternative browsers that have their own rendering engine and *may* be installed on this device  */
        protected val ALTERNATIVE_BROWSERS: Array<String?>? = arrayOf("org.mozilla.firefox", "com.android.chrome", "com.opera.browser", "org.mozilla.firefox_beta", "com.chrome.beta", "com.opera.browser.beta")

        @SuppressLint("NewApi")
        protected fun setAllowAccessFromFileUrls(webSettings: WebSettings?, allowed: Boolean) {
            if (Build.VERSION.SDK_INT >= 16) {
                webSettings.setAllowFileAccessFromFileURLs(allowed)
                webSettings.setAllowUniversalAccessFromFileURLs(allowed)
            }
        }

        protected fun makeUrlUnique(url: String?): String? {
            val unique = StringBuilder()
            unique.append(url)
            if (url.contains("?")) {
                unique.append('&')
            } else {
                if (url.lastIndexOf('/') <= 7) {
                    unique.append('/')
                }
                unique.append('?')
            }
            unique.append(System.currentTimeMillis())
            unique.append('=')
            unique.append(1)
            return unique.toString()
        }

        protected fun getLanguageIso3(): String? {
            return try {
                Locale.getDefault().isO3Language.toLowerCase(Locale.US)
            } catch (e: MissingResourceException) {
                LANGUAGE_DEFAULT_ISO3
            }
        }

        @Throws(IllegalArgumentException::class, UnsupportedEncodingException::class)
        protected fun decodeBase64(base64: String?): String? {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            return String(bytes, CHARSET_DEFAULT)
        }

        /**
         * Returns whether file uploads can be used on the current device (generally all platform versions except for 4.4)
         *
         * @return whether file uploads can be used
         */
        fun isFileUploadAvailable(): Boolean {
            return isFileUploadAvailable(false)
        }

        /**
         * Returns whether file uploads can be used on the current device (generally all platform versions except for 4.4)
         *
         * On Android 4.4.3/4.4.4, file uploads may be possible but will come with a wrong MIME type
         *
         * @param needsCorrectMimeType whether a correct MIME type is required for file uploads or `application/octet-stream` is acceptable
         * @return whether file uploads can be used
         */
        fun isFileUploadAvailable(needsCorrectMimeType: Boolean): Boolean {
            return if (Build.VERSION.SDK_INT == 19) {
                val platformVersion = if (Build.VERSION.RELEASE == null) "" else Build.VERSION.RELEASE
                !needsCorrectMimeType && (platformVersion.startsWith("4.4.3") || platformVersion.startsWith("4.4.4"))
            } else {
                true
            }
        }

        /**
         * Handles a download by loading the file from `fromUrl` and saving it to `toFilename` on the external storage
         *
         * This requires the two permissions `android.permission.INTERNET` and `android.permission.WRITE_EXTERNAL_STORAGE`
         *
         * Only supported on API level 9 (Android 2.3) and above
         *
         * @param context a valid `Context` reference
         * @param fromUrl the URL of the file to download, e.g. the one from `AdvancedWebView.onDownloadRequested(...)`
         * @param toFilename the name of the destination file where the download should be saved, e.g. `myImage.jpg`
         * @return whether the download has been successfully handled or not
         * @throws IllegalStateException if the storage or the target directory could not be found or accessed
         */
        @SuppressLint("NewApi")
        fun handleDownload(context: Context?, fromUrl: String?, toFilename: String?): Boolean {
            if (Build.VERSION.SDK_INT < 9) {
                throw RuntimeException("Method requires API level 9 or above")
            }
            val request = DownloadManager.Request(Uri.parse(fromUrl))
            if (Build.VERSION.SDK_INT >= 11) {
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, toFilename)
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            return try {
                try {
                    dm.enqueue(request)
                } catch (e: SecurityException) {
                    if (Build.VERSION.SDK_INT >= 11) {
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    }
                    dm.enqueue(request)
                }
                true
            } // if the download manager app has been disabled on the device
            catch (e: IllegalArgumentException) {
                // show the settings screen where the user can enable the download manager app again
                openAppSettings(context, PACKAGE_NAME_DOWNLOAD_MANAGER)
                false
            }
        }

        @SuppressLint("NewApi")
        private fun openAppSettings(context: Context?, packageName: String?): Boolean {
            if (Build.VERSION.SDK_INT < 9) {
                throw RuntimeException("Method requires API level 9 or above")
            }
            return try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.parse("package:$packageName"))
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}