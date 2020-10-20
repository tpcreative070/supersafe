package co.tpcreative.supersafe.common.utilimport

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import java.io.*

co.tpcreative.supersafe.common.BaseFragmentimport co.tpcreative.supersafe.common.presenter.BaseViewimport butterknife.BindViewimport co.tpcreative.supersafe.Rimport androidx.core.widget.NestedScrollViewimport androidx.appcompat.widget.AppCompatImageViewimport androidx.appcompat.widget.AppCompatTextViewimport android.widget.LinearLayoutimport co.tpcreative.supersafe.ui.me.MePresenterimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.constraintlayout.widget.ConstraintLayoutimport co.tpcreative.supersafe.ui.me.MeFragmentimport co.tpcreative.supersafe.model.ThemeAppimport co.tpcreative.supersafe.model.SyncDataimport android.text.Htmlimport co.tpcreative.supersafe.model.EnumStatusimport butterknife.OnClickimport co.tpcreative.supersafe.common.util.ConvertUtilsimport co.tpcreative.supersafe.common.presenter.Presenterimport com.google.gson.Gsonimport co.tpcreative.supersafe.model.ItemModelimport co.tpcreative.supersafe.common.helper.SQLHelperimport co.tpcreative.supersafe.model.HelpAndSupportimport com.jaychang.srv.SimpleCellimport com.jaychang.srv.SimpleViewHolderimport butterknife.ButterKnifeimport co.tpcreative.supersafe.ui.help.HelpAndSupportCellimport co.tpcreative.supersafe.common.activity.BaseActivityimport co.tpcreative.supersafe.ui.help.HelpAndSupportPresenterimport com.jaychang.srv.SimpleRecyclerViewimport org.greenrobot.eventbus.ThreadModeimport co.tpcreative.supersafe.ui.help.HelpAndSupportActivityimport com.jaychang.srv.decoration.SectionHeaderProviderimport com.jaychang.srv.decoration.SimpleSectionHeaderProviderimport android.widget.TextViewimport android.app.Activityimport co.tpcreative.supersafe.model.EmailTokenimport co.tpcreative.supersafe.common.util.NetworkUtilimport co.tpcreative.supersafe.common.services.SuperSafeApplicationimport okhttp3.ResponseBodyimport co.tpcreative.supersafe.common.api.RootAPIimport io.reactivex.schedulers.Schedulersimport io.reactivex.android.schedulers.AndroidSchedulersimport co.tpcreative.supersafe.common.request.OutlookMailRequestimport co.tpcreative.supersafe.common.api.response.BaseResponseimport android.widget.TextView.OnEditorActionListenerimport co.tpcreative.supersafe.common.views.AdvancedWebViewimport com.rengwuxian.materialedittext.MaterialEditTextimport co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivityimport android.view.inputmethod.EditorInfoimport co.tpcreative.supersafe.common.services.SuperSafeReceiverimport android.text.TextWatcherimport android.text.Editableimport dmax.dialog.SpotsDialogimport co.tpcreative.supersafe.common.adapter.BaseHolderimport co.tpcreative.supersafe.ui.theme.ThemeSettingsAdapterimport com.bumptech.glide.request.RequestOptionsimport com.bumptech.glide.Glideimport androidx.recyclerview.widget.RecyclerViewimport co.tpcreative.supersafe.ui.theme.ThemeSettingsPresenterimport androidx.recyclerview.widget.GridLayoutManagerimport androidx.recyclerview.widget.DefaultItemAnimatorimport android.graphics.PorterDuffimport co.tpcreative.supersafe.common.controller.PrefsControllerimport android.content.Intentimport co.tpcreative.supersafe.ui.trash.TrashAdapterimport co.tpcreative.supersafe.common.entities.ItemEntityimport android.widget.ProgressBarimport co.tpcreative.supersafe.model.EnumStatusProgressimport androidx.appcompat.widget.AppCompatButtonimport android.widget.RelativeLayoutimport co.tpcreative.supersafe.ui.trash.TrashPresenterimport co.tpcreative.supersafe.ui.trash.TrashActivityimport com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallbackimport com.afollestad.materialdialogs.DialogActionimport android.view.MenuInflaterimport android.os.Buildimport androidx.core.content.ContextCompatimport co.tpcreative.supersafe.common.controller.SingletonPrivateFragmentimport co.tpcreative.supersafe.model.EnumDeleteimport co.tpcreative.supersafe.model.MainCategoryModelimport androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallbackimport com.otaliastudios.cameraview.CameraViewimport androidx.appcompat.widget.AppCompatImageButtonimport co.tpcreative.supersafe.common.controller.GalleryCameraMediaManagerimport co.tpcreative.supersafe.ui.camera.CameraActivityimport com.otaliastudios.cameraview.controls.Facingimport com.otaliastudios.cameraview.CameraListenerimport com.otaliastudios.cameraview.CameraOptionsimport com.otaliastudios.cameraview.PictureResultimport android.widget.Toastimport com.otaliastudios.cameraview.controls.Flashimport co.tpcreative.supersafe.ui.player.PlayerAdapterimport co.tpcreative.supersafe.common.activity.BasePlayerActivityimport com.google.android.exoplayer2.ui.PlayerViewimport dyanamitechetan.vusikview.VusikViewimport co.tpcreative.supersafe.ui.player.PlayerPresenterimport com.google.android.exoplayer2.SimpleExoPlayerimport android.view.WindowManagerimport com.snatik.storage.security.SecurityUtilimport com.google.android.exoplayer2.ui.PlayerControlViewimport androidx.recyclerview.widget.LinearLayoutManagerimport co.tpcreative.supersafe.ui.player.PlayerActivityimport com.google.android.exoplayer2.upstream.DefaultBandwidthMeterimport com.google.android.exoplayer2.trackselection.TrackSelectionimport com.google.android.exoplayer2.trackselection.AdaptiveTrackSelectionimport com.google.android.exoplayer2.trackselection.TrackSelectorimport com.google.android.exoplayer2.trackselection.DefaultTrackSelectorimport com.google.android.exoplayer2.ExoPlayerFactoryimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceFactoryimport com.google.android.exoplayer2.extractor.ExtractorsFactoryimport com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.ExtractorMediaSourceimport com.google.android.exoplayer2.source.ConcatenatingMediaSourceimport com.google.android.exoplayer2.Cimport com.google.android.exoplayer2.Playerimport com.google.android.exoplayer2.Timelineimport com.google.android.exoplayer2.source.TrackGroupArrayimport com.google.android.exoplayer2.trackselection.TrackSelectionArrayimport com.google.android.exoplayer2.ExoPlaybackExceptionimport com.google.android.exoplayer2.PlaybackParametersimport android.content.pm.ActivityInfoimport com.google.android.exoplayer2.source.MediaSourceimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideimport com.gc.materialdesign.views.ProgressBarCircularIndeterminateimport co.tpcreative.supersafe.ui.signin.SignInPresenterimport co.tpcreative.supersafe.ui.signin.SignInActivityimport co.tpcreative.supersafe.common.request.SignInRequestimport io.reactivex.disposables.Disposableimport co.tpcreative.supersafe.common.response.RootResponseimport co.tpcreative.supersafe.common.response.DataResponseimport android.text.Spannedimport co.tpcreative.supersafe.ui.signup.SignUpPresenterimport co.tpcreative.supersafe.ui.signup.SignUpActivityimport co.tpcreative.supersafe.common.request.SignUpRequestimport co.tpcreative.supersafe.ui.verify.VerifyPresenterimport co.tpcreative.supersafe.ui.verify.VerifyActivityimport co.tpcreative.supersafe.common.request.VerifyCodeRequestimport co.tpcreative.supersafe.model.EnumPinActionimport co.tpcreative.supersafe.common.request.RequestCodeRequestimport android.widget.CompoundButtonimport androidx.appcompat.widget.SwitchCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapterimport com.bumptech.glide.load.engine.DiskCacheStrategyimport co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePinimport com.leinardi.android.speeddial.SpeedDialViewimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentPresenterimport androidx.appcompat.content.res.AppCompatResourcesimport com.leinardi.android.speeddial.SpeedDialActionItemimport androidx.core.content.res.ResourcesCompatimport co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivityimport com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListenerimport android.text.InputTypeimport com.afollestad.materialdialogs.MaterialDialog.InputCallbackimport com.karumi.dexter.Dexterimport com.karumi.dexter.listener.multi.MultiplePermissionsListenerimport com.karumi.dexter.MultiplePermissionsReportimport com.karumi.dexter.PermissionTokenimport com.karumi.dexter.listener.PermissionRequestErrorListenerimport com.karumi.dexter.listener.DexterErrorimport co.tpcreative.supersafe.common.controller.SingletonFakePinComponentimport co.tpcreative.supersafe.model.ImportFilesModelimport co.tpcreative.supersafe.common.controller.SingletonManagerimport com.anjlab.android.iab.v3.BillingProcessor.IBillingHandlerimport co.tpcreative.supersafe.ui.premium.PremiumPresenterimport com.anjlab.android.iab.v3.BillingProcessorimport co.tpcreative.supersafe.ui.premium.PremiumActivityimport androidx.fragment.app.FragmentFactoryimport androidx.preference.PreferenceFragmentCompatimport com.anjlab.android.iab.v3.TransactionDetailsimport com.anjlab.android.iab.v3.PurchaseInfoimport com.anjlab.android.iab.v3.PurchaseDataimport co.tpcreative.supersafe.model.EnumPurchaseimport com.anjlab.android.iab.v3.SkuDetailsimport co.tpcreative.supersafe.model.CheckoutItemsimport co.tpcreative.supersafe.ui.settings.SettingsActivityimport co.tpcreative.supersafe.common.request.CheckoutRequestimport co.tpcreative.supersafe.ui.restore.RestorePresenterimport co.tpcreative.supersafe.ui.restore.RestoreActivityimport io.reactivex.ObservableOnSubscribeimport io.reactivex.ObservableEmitterimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerSyncDataListenerimport co.tpcreative.supersafe.common.activity.BaseActivityNoneimport co.tpcreative.supersafe.ui.facedown.FaceDownActivityimport co.tpcreative.supersafe.common.activity.BaseGoogleApiimport com.google.android.material.tabs.TabLayoutimport co.tpcreative.supersafe.ui.main_tab.MainViewPagerAdapterimport co.tpcreative.supersafe.ui.main_tab.MainTabPresenterimport co.tpcreative.supersafe.common.views.AnimationsContainer.FramesSequenceAnimationimport com.google.android.gms.ads.InterstitialAdimport co.tpcreative.supersafe.ui.main_tab.MainTabActivityimport co.tpcreative.supersafe.common.controller.PremiumManagerimport co.tpcreative.supersafe.common.views.AnimationsContainerimport com.getkeepsafe.taptargetview.TapTargetViewimport com.getkeepsafe.taptargetview.TapTargetimport android.content.ActivityNotFoundExceptionimport androidx.fragment.app.FragmentPagerAdapterimport co.tpcreative.supersafe.ui.privates.PrivateFragmentimport co.tpcreative.supersafe.ui.privates.PrivateAdapterimport co.tpcreative.supersafe.ui.privates.PrivatePresenterimport co.tpcreative.supersafe.common.dialog.DialogManagerimport co.tpcreative.supersafe.common.dialog.DialogListenerimport android.util.TypedValueimport co.tpcreative.supersafe.common.activity.BaseVerifyPinActivityimport androidx.appcompat.widget.AppCompatEditTextimport co.tpcreative.supersafe.ui.resetpin.ResetPinPresenterimport co.tpcreative.supersafe.ui.resetpin.ResetPinActivityimport co.tpcreative.supersafe.common.controller.SingletonResetPinimport com.github.javiersantos.materialstyleddialogs.MaterialStyledDialogimport androidx.appcompat.app.AppCompatActivityimport android.graphics.drawable.ColorDrawableimport android.content.DialogInterfaceimport android.content.DialogInterface.OnShowListenerimport co.tpcreative.supersafe.ui.settings.AlbumSettingsActivityimport co.tpcreative.supersafe.ui.settings.AlbumSettingsPresenterimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettingsimport co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings.MyPreferenceListenerimport co.tpcreative.supersafe.ui.dashboard.DashBoardActivityimport co.tpcreative.supersafe.common.Encrypterimport co.tpcreative.supersafe.model.EnumEventimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverCellimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverPresenterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverDefaultAdapterimport co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivityimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDotsimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapterimport co.tpcreative.supersafe.ui.lockscreen.PinLockListenerimport co.tpcreative.supersafe.ui.lockscreen.CustomizationOptionsBundleimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnNumberClickListenerimport co.tpcreative.supersafe.ui.lockscreen.PinLockViewimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.OnVerifyClickListenerimport android.content.res.TypedArrayimport co.tpcreative.supersafe.ui.lockscreen.ItemSpaceDecorationimport co.tpcreative.supersafe.ui.lockscreen.ShuffleArrayUtilsimport androidx.annotation .IntDefimport co.tpcreative.supersafe.ui.lockscreen.IndicatorDots.IndicatorTypeimport android.animation.LayoutTransitionimport androidx.annotation .ColorResimport androidx.annotation .DimenResimport androidx.annotation .DrawableResimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.NumberViewHolderimport co.tpcreative.supersafe.ui.lockscreen.PinLockAdapter.VerifyViewHolderimport android.view.animation.Animationimport android.view.animation.ScaleAnimationimport co.tpcreative.supersafe.common.util.Calculatorimport com.multidots.fingerprintauth.FingerPrintAuthCallbackimport co.tpcreative.supersafe.common.controller.SingletonScreenLock.SingletonScreenLockListenerimport com.github.kratorius.circleprogress.CircleProgressViewimport co.tpcreative.supersafe.common.hiddencamera.CameraConfigimport com.multidots.fingerprintauth.FingerPrintAuthHelperimport co.tpcreative.supersafe.ui.lockscreen.EnterPinActivityimport co.tpcreative.supersafe.ui.lockscreen.LockScreenPresenterimport co.tpcreative.supersafe.common.controller.SingletonScreenLockimport android.view.View.OnLongClickListenerimport co.tpcreative.supersafe.common.util.CalculatorImplimport me.grantland.widget.AutofitHelperimport android.hardware.fingerprint.FingerprintManagerimport co.tpcreative.supersafe.common.preference.MyPreferenceimport co.tpcreative.supersafe.common.preference.MySwitchPreferenceimport co.tpcreative.supersafe.model.BreakInAlertsModelimport co.tpcreative.supersafe.common.hiddencamera.CameraErrorimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocusimport androidx.core.app.ActivityCompatimport android.content.pm.PackageManagerimport butterknife.OnLongClickimport androidx.recyclerview.widget.RecyclerView.ItemDecorationimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorActivityimport com.afollestad.materialdialogs.MaterialDialog.ListCallbackimport co.tpcreative.supersafe.ui.secretdoor.SecretDoorSetUpActivityimport android.text.SpannableStringimport android.graphics.Typefaceimport co.tpcreative.supersafe.ui.sharefiles.ShareFilesActivityimport android.os.Parcelableimport co.tpcreative.supersafe.common.util.PathUtilimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapterimport co.tpcreative.supersafe.common.activity.BaseGalleryActivityimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailPresenterimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailVerticalAdapterimport cn.pedant.SweetAlert.SweetAlertDialogimport com.google.android.material.appbar.CollapsingToolbarLayoutimport co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivityimport cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListenerimport co.tpcreative.supersafe.common.views.NpaGridLayoutManagerimport co.tpcreative.supersafe.model.ExportFilesimport co.tpcreative.supersafe.ui.checksystem.CheckSystemPresenterimport co.tpcreative.supersafe.ui.checksystem.CheckSystemActivityimport co.tpcreative.supersafe.common.request.UserCloudRequestimport android.view.View.OnTouchListenerimport android.view.MotionEventimport co.tpcreative.supersafe.model.GoogleOauthimport co.tpcreative.supersafe.common.request.UserRequestimport co.tpcreative.supersafe.common.request.ChangeUserIdRequestimport co.tpcreative.supersafe.common.response.UserCloudResponseimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudPresenterimport co.tpcreative.supersafe.ui.enablecloud.EnableCloudActivityimport android.accounts.AccountManagerimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumPresenterimport co.tpcreative.supersafe.ui.unlockalbum.UnlockAllAlbumActivityimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenterimport co.tpcreative.supersafe.model.DriveAboutimport co.tpcreative.supersafe.ui.cloudmanager.CloudManagerActivityimport co.tpcreative.supersafe.model.GalleryAlbumimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryAdapterimport co.tpcreative.supersafe.common.views.SquaredImageViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryViewimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragmentimport com.google.android.material.bottomsheet.BottomSheetDialogimport com.google.android.material.bottomsheet.BottomSheetBehaviorimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment.OnGalleryAttachedListenerimport co.tpcreative.supersafe.ui.move_gallery.MoveGalleryPresenterimport co.tpcreative.supersafe.common.views.VerticalSpaceItemDecorationimport co.tpcreative.supersafe.model.AlbumMultiItemsimport android.widget.FrameLayoutimport com.google.android.material.snackbar.Snackbarimport android.widget.GridViewimport android.database.ContentObserverimport android.provider.MediaStoreimport android.widget.AdapterViewimport android.util.DisplayMetricsimport co.tpcreative.supersafe.ui.splashscreen.SplashScreenActivityimport co.tpcreative.supersafe.ui.askpermission.AskPermissionActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsAdapterimport com.github.marlonlom.utilities.timeago.TimeAgoMessagesimport com.github.marlonlom.utilities.timeago.TimeAgoimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsPresenterimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsActivityimport co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtilsimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountPresenterimport co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivityimport co.tpcreative.supersafe.common.controller.SingletonManagerProcessingimport co.tpcreative.supersafe.model.AppListsimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerPresenterimport co.tpcreative.supersafe.ui.accountmanager.AccountManagerActivityimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowPresenterimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivity.SamplePagerAdapterimport com.github.chrisbanes.photoview.PhotoViewimport androidx.viewpager.widget.ViewPager.OnPageChangeListenerimport co.tpcreative.supersafe.ui.photosslideshow.PhotoSlideShowActivityimport androidx.viewpager.widget.PagerAdapterimport com.github.chrisbanes.photoview.OnPhotoTapListenerimport android.annotation .SuppressLintimport co.tpcreative.supersafe.model.Authorizationimport co.tpcreative.supersafe.model.Premiumimport co.tpcreative.supersafe.common.entities.MainCategoryEntityimport android.os.Parcelimport co.tpcreative.supersafe.model.ItemEntityModelimport co.tpcreative.supersafe.model.EnumFileTypeimport co.tpcreative.supersafe.common.api.response.BaseResponseDriveimport co.tpcreative.supersafe.model.DriveUserimport co.tpcreative.supersafe.common.response.DriveResponseimport co.tpcreative.supersafe.model.EmailToken.EmailAddressimport co.tpcreative.supersafe.model.EmailToken.EmailObjectimport co.tpcreative.supersafe.model.DriveDescriptionimport co.tpcreative.supersafe.model.MainCategoryEntityModelimport co.tpcreative.supersafe.model.BreakInAlertsEntityModelimport co.tpcreative.supersafe.common.entities.BreakInAlertsEntityimport co.tpcreative.supersafe.model.ErrorResponseimport retrofit2.http.POSTimport co.tpcreative.supersafe.common.request.TrackingRequestimport co.tpcreative.supersafe.common.request.SyncItemsRequestimport co.tpcreative.supersafe.common.request.CategoriesRequestimport retrofit2.http.GETimport retrofit2.http.FormUrlEncodedimport retrofit2.http.Urlimport co.tpcreative.supersafe.common.request.DriveApiRequestimport retrofit2.http.DELETEimport retrofit2.http.Multipartimport okhttp3.MultipartBodyimport retrofit2.http.Streamingimport android.util.Patternsimport com.snatik.storage.helpers.SizeUnitimport android.webkit.MimeTypeMapimport org.apache.commons.io.FilenameUtilsimport com.snatik.storage.helpers.OnStorageListenerimport androidx.annotation .StringResimport android.view.animation.TranslateAnimationimport androidx.core.content.FileProviderimport android.view.Displayimport androidx.core.hardware.fingerprint.FingerprintManagerCompatimport android.os.StatFsimport io.reactivex.Completableimport io.reactivex.CompletableObserverimport androidx.core.content.PermissionCheckerimport android.media.MediaScannerConnectionimport android.annotation .TargetApiimport android.graphics.drawable.AnimatedVectorDrawableimport android.provider.DocumentsContractimport android.content.ContentUrisimport android.net.ConnectivityManagerimport android.net.NetworkInfoimport co.tpcreative.supersafe.common.util.MemoryConstantsimport co.tpcreative.supersafe.common.util.TimeConstantsimport android.graphics.Bitmapimport android.graphics.Bitmap.CompressFormatimport android.graphics.BitmapFactoryimport android.graphics.drawable.BitmapDrawableimport android.graphics.PixelFormatimport co.tpcreative.supersafe.common.views.GestureTap.GestureTapListenerimport android.view.GestureDetector.SimpleOnGestureListenerimport android.widget.Checkableimport android.graphics.PorterDuffXfermodeimport android.view.View.MeasureSpecimport co.tpcreative.supersafe.common.views.MDCheckBoximport android.webkit.WebViewimport android.webkit.ValueCallbackimport android.webkit.WebViewClientimport android.webkit.WebChromeClientimport android.webkit.WebSettingsimport android.webkit.WebResourceResponseimport android.webkit.WebResourceRequestimport android.webkit.SslErrorHandlerimport android.net.http.SslErrorimport android.webkit.ClientCertRequestimport android.webkit.HttpAuthHandlerimport android.webkit.WebChromeClient.FileChooserParamsimport android.webkit.WebChromeClient.CustomViewCallbackimport android.webkit.JsResultimport android.webkit.JsPromptResultimport android.webkit.GeolocationPermissionsimport android.webkit.ConsoleMessageimport android.webkit.WebStorage.QuotaUpdaterimport android.webkit.DownloadListenerimport co.tpcreative.supersafe.common.views.AdvancedWebView.Browsersimport android.content.pm.ApplicationInfoimport android.graphics.RectFimport android.text.TextPaintimport co.tpcreative.supersafe.common.views.AnimationsContainer.OnAnimationStoppedListenerimport com.google.android.material.appbar.AppBarLayoutimport com.google.android.material.appbar.AppBarLayout.Behavior.DragCallbackimport co.tpcreative.supersafe.common.dialog.DialogFragmentAskSignInimport com.google.android.exoplayer2.upstream.TransferListenerimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSourceimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.StreamingCipherInputStreamimport co.tpcreative.supersafe.common.encypt.EncryptedFileDataSource.EncryptedFileDataSourceExceptionimport com.google.android.exoplayer2.upstream.DataSpecimport co.tpcreative.supersafe.common.entities.InstanceGeneratorimport androidx.recyclerview.widget.RecyclerView.AdapterDataObserverimport co.tpcreative.supersafe.common.network.BaseDependenciesimport retrofit2.Retrofitimport co.tpcreative.supersafe.common.network.Dependencies.DependenciesListenerimport okhttp3.OkHttpClientimport com.google.gson.GsonBuilderimport retrofit2.converter.gson.GsonConverterFactoryimport retrofit2.adapter.rxjava2.RxJava2CallAdapterFactoryimport timber.log.Timberimport butterknife.Unbinderimport co.tpcreative.supersafe.common.HomeWatcherimport androidx.annotation .LayoutResimport co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifierimport co.tpcreative.supersafe.common.HomeWatcher.OnHomePressedListenerimport spencerstudios.com.bungeelib.Bungeeimport com.google.android.gms.auth.api.signin.GoogleSignInAccountimport com.google.android.gms.auth.api.signin.GoogleSignInClientimport com.google.android.gms.auth.api.signin.GoogleSignInimport com.google.api.services.drive.DriveScopesimport android.accounts.Accountimport com.google.android.gms.auth.GoogleAuthUtilimport co.tpcreative.supersafe.common.activity.BaseGoogleApi.GetAccessTokenimport android.os.AsyncTaskimport com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredentialimport com.google.android.gms.auth.GoogleAuthExceptionimport com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOExceptionimport com.google.android.gms.tasks.OnCompleteListenerimport com.google.android.gms.tasks.OnFailureListenerimport androidx.annotation .RequiresPermissionimport androidx.room.Daoimport androidx.room.PrimaryKeyimport androidx.room.Databaseimport androidx.room.RoomDatabaseimport co.tpcreative.supersafe.common.entities.ItemsDaoimport co.tpcreative.supersafe.common.entities.MainCategoriesDaoimport co.tpcreative.supersafe.common.entities.BreakInAlertsDaoimport androidx.sqlite.db.SupportSQLiteDatabaseimport androidx.room.Roomimport co.tpcreative.supersafe.common.response.RequestCodeResponseimport co.tpcreative.supersafe.common.api.request.UploadingFileRequestimport org.apache.http.impl .client.DefaultHttpClientimport org.apache.http.client.methods.HttpPostimport org.apache.http.entity.mime.MultipartEntityBuilderimport org.apache.http.entity.mime.HttpMultipartModeimport co.tpcreative.supersafe.common.services.upload.UploadServiceimport org.apache.http.entity.mime.content.FileBodyimport org.apache.http.HttpEntityimport co.tpcreative.supersafe.common.services.upload.ProgressiveEntityimport org.apache.http.util.EntityUtilsimport org.apache.http.client.ClientProtocolExceptionimport org.apache.http.entity.mime.content.ContentBodyimport org.apache.http.entity.mime.MultipartEntityimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.UploadCallbacksimport okhttp3.RequestBodyimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBodyimport okio.BufferedSinkimport android.os.Looperimport co.tpcreative.supersafe.common.services.upload.ProgressRequestBody.ProgressUpdaterimport co.tpcreative.supersafe.common.services.download.ProgressResponseBody.ProgressResponseBodyListenerimport co.tpcreative.supersafe.common.services.download.DownloadService.DownLoadServiceListenerimport co.tpcreative.supersafe.common.api.request.DownloadFileRequestimport okio.Okioimport co.tpcreative.supersafe.common.services.download.ProgressResponseBodyimport co.tpcreative.supersafe.common.services.download.RetrofitInterfaceimport okio.BufferedSourceimport okio.ForwardingSourceimport okhttp3.HttpUrlimport co.tpcreative.supersafe.common.presenter.PresenterServiceimport co.tpcreative.supersafe.common.presenter.BaseServiceViewimport co.tpcreative.supersafe.common.services.SuperSafeReceiver.ConnectivityReceiverListenerimport android.os.IBinderimport co.tpcreative.supersafe.common.services.SuperSafeService.LocalBinderimport co.tpcreative.supersafe.common.services.SuperSafeServiceimport android.content.IntentFilterimport co.tpcreative.supersafe.common.controller.ServiceManager.BaseListenerimport co.tpcreative.supersafe.common.controller.ServiceManager.ServiceManagerInsertItemimport co.tpcreative.supersafe.common.controller.ServiceManager.DownloadServiceListenerimport android.content.BroadcastReceiverimport androidx.multidex.MultiDexApplicationimport android.app.Application.ActivityLifecycleCallbacksimport com.snatik.storage.EncryptConfigurationimport com.google.android.gms.auth.api.signin.GoogleSignInOptionsimport com.google.android.gms.ads.MobileAdsimport com.google.android.gms.ads.initialization.OnInitializationCompleteListenerimport com.google.android.gms.ads.initialization.InitializationStatusimport com.google.firebase.crashlytics.FirebaseCrashlyticsimport com.bumptech.glide.request.target.ImageViewTargetimport co.tpcreative.supersafe.common.services.RetrofitHelperimport android.content.ContextWrapperimport androidx.multidex.MultiDeximport io.reactivex.disposables.CompositeDisposableimport androidx.annotation .CallSuperimport io.reactivex.ObservableSourceimport io.reactivex.Flowableimport io.reactivex.FlowableOnSubscribeimport io.reactivex.FlowableEmitterimport io.reactivex.BackpressureStrategyimport android.content.ServiceConnectionimport android.content.ComponentNameimport id.zelory.compressor.Compressorimport co.tpcreative.supersafe.model.ResponseRXJavaimport android.media.ThumbnailUtilsimport android.content.SharedPreferencesimport android.os.CountDownTimerimport co.tpcreative.supersafe.common.controller.SingletonMultipleListenerimport androidx.preference.PreferenceViewHolderimport co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution.SupportedResolutionimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing.SupportedCameraFacingimport co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat.SupportedImageFormatimport co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation.SupportedRotationimport co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus.SupportedCameraFocusimport android.view.SurfaceViewimport android.view.SurfaceHolderimport co.tpcreative.supersafe.common.hiddencamera.PictureSizeComparatorimport android.hardware.Camera.PictureCallbackimport co.tpcreative.supersafe.common.hiddencamera.CameraError.CameraErrorCodesimport co.tpcreative.supersafe.ui.theme.ThemeSettingsActivityimport co.tpcreative.supersafe.ui.breakinalerts.BreakInAlertsDetailActivityimport co.tpcreative.supersafe.ui.fakepin.FakePinActivityimport co.tpcreative.supersafe.ui.aboutsupersafe.AboutSuperSafeActivityimport co.tpcreative.supersafe.common.JealousSkyimport co.tpcreative.supersafe.common.HomeWatcher.InnerReceiverimport co.tpcreative.supersafe.common.RXJavaCollectionsimport android.hardware.SensorEventListenerimport android.hardware.SensorManagerimport android.hardware.SensorEventimport co.tpcreative.supersafe.common.SensorOrientationChangeNotifier
/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/13
 * desc  : utils about convert
</pre> *
 */
class ConvertUtils private constructor() {
    /**
     * Output stream to input stream.
     *
     * @param out The output stream.
     * @return input stream
     */
    fun output2InputStream(out: OutputStream?): ByteArrayInputStream? {
        return if (out == null) null else ByteArrayInputStream((out as ByteArrayOutputStream?).toByteArray())
    }

    companion object {
        private val hexDigits: CharArray? = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        /**
         * Bytes to bits.
         *
         * @param bytes The bytes.
         * @return bits
         */
        fun bytes2Bits(bytes: ByteArray?): String? {
            if (bytes == null || bytes.size == 0) return ""
            val sb = StringBuilder()
            for (aByte in bytes) {
                for (j in 7 downTo 0) {
                    sb.append(if (aByte shr j and 0x01 == 0) '0' else '1')
                }
            }
            return sb.toString()
        }

        /**
         * Bits to bytes.
         *
         * @param bits The bits.
         * @return bytes
         */
        fun bits2Bytes(bits: String?): ByteArray? {
            var bits = bits
            val lenMod = bits.length % 8
            var byteLen = bits.length / 8
            // add "0" until length to 8 times
            if (lenMod != 0) {
                for (i in lenMod..7) {
                    bits = "0$bits"
                }
                byteLen++
            }
            val bytes = ByteArray(byteLen)
            for (i in 0 until byteLen) {
                for (j in 0..7) {
                    bytes[i] = bytes[i] shl 1
                    bytes[i] = bytes[i] or bits.get(i * 8 + j) - '0'
                }
            }
            return bytes
        }

        /**
         * Bytes to chars.
         *
         * @param bytes The bytes.
         * @return chars
         */
        fun bytes2Chars(bytes: ByteArray?): CharArray? {
            if (bytes == null) return null
            val len = bytes.size
            if (len <= 0) return null
            val chars = CharArray(len)
            for (i in 0 until len) {
                chars[i] = (bytes[i] and 0xff) as Char
            }
            return chars
        }

        /**
         * Chars to bytes.
         *
         * @param chars The chars.
         * @return bytes
         */
        fun chars2Bytes(chars: CharArray?): ByteArray? {
            if (chars == null || chars.size <= 0) return null
            val len = chars.size
            val bytes = ByteArray(len)
            for (i in 0 until len) {
                bytes[i] = chars[i] as Byte
            }
            return bytes
        }

        /**
         * Bytes to hex string.
         *
         * e.g. bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns "00A8"
         *
         * @param bytes The bytes.
         * @return hex string
         */
        fun bytes2HexString(bytes: ByteArray?): String? {
            if (bytes == null) return ""
            val len = bytes.size
            if (len <= 0) return ""
            val ret = CharArray(len shl 1)
            var i = 0
            var j = 0
            while (i < len) {
                ret[j++] = hexDigits.get(bytes[i] shr 4 and 0x0f)
                ret[j++] = hexDigits.get(bytes[i] and 0x0f)
                i++
            }
            return String(ret)
        }

        /**
         * Hex string to bytes.
         *
         * e.g. hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
         *
         * @param hexString The hex string.
         * @return the bytes
         */
        fun hexString2Bytes(hexString: String?): ByteArray? {
            var hexString = hexString
            if (isSpace(hexString)) return null
            var len = hexString.length
            if (len % 2 != 0) {
                hexString = "0$hexString"
                len = len + 1
            }
            val hexBytes = hexString.toUpperCase().toCharArray()
            val ret = ByteArray(len shr 1)
            var i = 0
            while (i < len) {
                ret[i shr 1] = (hex2Int(hexBytes[i]) shl 4 or hex2Int(hexBytes[i + 1])) as Byte
                i += 2
            }
            return ret
        }

        private fun hex2Int(hexChar: Char): Int {
            return if (hexChar >= '0' && hexChar <= '9') {
                hexChar - '0'
            } else if (hexChar >= 'A' && hexChar <= 'F') {
                hexChar - 'A' + 10
            } else {
                throw IllegalArgumentException()
            }
        }

        /**
         * Size of memory in unit to size of byte.
         *
         * @param memorySize Size of memory.
         * @param unit       The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of byte
         */
        fun memorySize2Byte(memorySize: Long,
                            @MemoryConstants.Unit unit: Int): Long {
            return if (memorySize < 0) -1 else memorySize * unit
        }

        /**
         * Size of byte to size of memory in unit.
         *
         * @param byteSize Size of byte.
         * @param unit     The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of memory in unit
         */
        fun byte2MemorySize(byteSize: Long,
                            @MemoryConstants.Unit unit: Int): Double {
            return if (byteSize < 0) -1 else byteSize as Double / unit
        }

        /**
         * Size of byte to fit size of memory.
         *
         * to three decimal places
         *
         * @param byteSize Size of byte.
         * @return fit size of memory
         */
        @SuppressLint("DefaultLocale")
        fun byte2FitMemorySize(byteSize: Long): String? {
            return if (byteSize < 0) {
                "shouldn't be less than zero!"
            } else if (byteSize < MemoryConstants.KB) {
                String.format("%.1f B", byteSize as Double)
            } else if (byteSize < MemoryConstants.MB) {
                String.format("%.1f KB", byteSize as Double / MemoryConstants.KB)
            } else if (byteSize < MemoryConstants.GB) {
                String.format("%.1f MB", byteSize as Double / MemoryConstants.MB)
            } else {
                String.format("%.1f GB", byteSize as Double / MemoryConstants.GB)
            }
        }

        /**
         * Time span in unit to milliseconds.
         *
         * @param timeSpan The time span.
         * @param unit     The unit of time span.
         *
         *  * [TimeConstants.MSEC]
         *  * [TimeConstants.SEC]
         *  * [TimeConstants.MIN]
         *  * [TimeConstants.HOUR]
         *  * [TimeConstants.DAY]
         *
         * @return milliseconds
         */
        fun timeSpan2Millis(timeSpan: Long, @TimeConstants.Unit unit: Int): Long {
            return timeSpan * unit
        }

        /**
         * Milliseconds to time span in unit.
         *
         * @param millis The milliseconds.
         * @param unit   The unit of time span.
         *
         *  * [TimeConstants.MSEC]
         *  * [TimeConstants.SEC]
         *  * [TimeConstants.MIN]
         *  * [TimeConstants.HOUR]
         *  * [TimeConstants.DAY]
         *
         * @return time span in unit
         */
        fun millis2TimeSpan(millis: Long, @TimeConstants.Unit unit: Int): Long {
            return millis / unit
        }

        /**
         * Milliseconds to fit time span.
         *
         * @param millis    The milliseconds.
         *
         * millis &lt;= 0, return null
         * @param precision The precision of time span.
         *
         *  * precision = 0, return null
         *  * precision = 1, return 天
         *  * precision = 2, return 天, 小时
         *  * precision = 3, return 天, 小时, 分钟
         *  * precision = 4, return 天, 小时, 分钟, 秒
         *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
         *
         * @return fit time span
         */
        @SuppressLint("DefaultLocale")
        fun millis2FitTimeSpan(millis: Long, precision: Int): String? {
            var millis = millis
            var precision = precision
            if (millis <= 0 || precision <= 0) return null
            val sb = StringBuilder()
            val units = arrayOf<String?>("天", "小时", "分钟", "秒", "毫秒")
            val unitLen = intArrayOf(86400000, 3600000, 60000, 1000, 1)
            precision = Math.min(precision, 5)
            for (i in 0 until precision) {
                if (millis >= unitLen[i]) {
                    val mode = millis / unitLen[i]
                    millis -= mode * unitLen[i]
                    sb.append(mode).append(units[i])
                }
            }
            return sb.toString()
        }

        /**
         * Input stream to output stream.
         *
         * @param is The input stream.
         * @return output stream
         */
        fun input2OutputStream(`is`: InputStream?): ByteArrayOutputStream? {
            return if (`is` == null) null else try {
                val os = ByteArrayOutputStream()
                val b = ByteArray(MemoryConstants.KB)
                var len: Int
                while (`is`.read(b, 0, MemoryConstants.KB).also { len = it } != -1) {
                    os.write(b, 0, len)
                }
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Input stream to bytes.
         *
         * @param is The input stream.
         * @return bytes
         */
        fun inputStream2Bytes(`is`: InputStream?): ByteArray? {
            return if (`is` == null) null else input2OutputStream(`is`).toByteArray()
        }

        /**
         * Bytes to input stream.
         *
         * @param bytes The bytes.
         * @return input stream
         */
        fun bytes2InputStream(bytes: ByteArray?): InputStream? {
            return if (bytes == null || bytes.size <= 0) null else ByteArrayInputStream(bytes)
        }

        /**
         * Output stream to bytes.
         *
         * @param out The output stream.
         * @return bytes
         */
        fun outputStream2Bytes(out: OutputStream?): ByteArray? {
            return if (out == null) null else (out as ByteArrayOutputStream?).toByteArray()
        }

        /**
         * Bytes to output stream.
         *
         * @param bytes The bytes.
         * @return output stream
         */
        fun bytes2OutputStream(bytes: ByteArray?): OutputStream? {
            if (bytes == null || bytes.size <= 0) return null
            var os: ByteArrayOutputStream? = null
            return try {
                os = ByteArrayOutputStream()
                os.write(bytes)
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Input stream to string.
         *
         * @param is          The input stream.
         * @param charsetName The name of charset.
         * @return string
         */
        fun inputStream2String(`is`: InputStream?, charsetName: String?): String? {
            return if (`is` == null || isSpace(charsetName)) "" else try {
                String(inputStream2Bytes(`is`), charsetName)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to input stream.
         *
         * @param string      The string.
         * @param charsetName The name of charset.
         * @return input stream
         */
        fun string2InputStream(string: String?, charsetName: String?): InputStream? {
            return if (string == null || isSpace(charsetName)) null else try {
                ByteArrayInputStream(string.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Output stream to string.
         *
         * @param out         The output stream.
         * @param charsetName The name of charset.
         * @return string
         */
        fun outputStream2String(out: OutputStream?, charsetName: String?): String? {
            return if (out == null || isSpace(charsetName)) "" else try {
                String(outputStream2Bytes(out), charsetName)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to output stream.
         *
         * @param string      The string.
         * @param charsetName The name of charset.
         * @return output stream
         */
        fun string2OutputStream(string: String?, charsetName: String?): OutputStream? {
            return if (string == null || isSpace(charsetName)) null else try {
                bytes2OutputStream(string.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Bitmap to bytes.
         *
         * @param bitmap The bitmap.
         * @param format The format of bitmap.
         * @return bytes
         */
        fun bitmap2Bytes(bitmap: Bitmap?, format: CompressFormat?): ByteArray? {
            if (bitmap == null) return null
            val baos = ByteArrayOutputStream()
            bitmap.compress(format, 100, baos)
            return baos.toByteArray()
        }

        /**
         * Bytes to bitmap.
         *
         * @param bytes The bytes.
         * @return bitmap
         */
        fun bytes2Bitmap(bytes: ByteArray?): Bitmap? {
            return if (bytes == null || bytes.size == 0) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        /**
         * Drawable to bitmap.
         *
         * @param drawable The drawable.
         * @return bitmap
         */
        fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
            if (drawable is BitmapDrawable) {
                val bitmapDrawable: BitmapDrawable? = drawable as BitmapDrawable?
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap()
                }
            }
            val bitmap: Bitmap
            bitmap = if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                Bitmap.createBitmap(1, 1,
                        if (drawable.getOpacity() != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            } else {
                Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        if (drawable.getOpacity() != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        /**
         * Bitmap to drawable.
         *
         * @param bitmap The bitmap.
         * @return drawable
         */
        fun bitmap2Drawable(bitmap: Bitmap?): Drawable? {
            return if (bitmap == null) null else BitmapDrawable(SuperSafeApplication.Companion.getInstance().getResources(), bitmap)
        }

        /**
         * Drawable to bytes.
         *
         * @param drawable The drawable.
         * @param format   The format of bitmap.
         * @return bytes
         */
        fun drawable2Bytes(drawable: Drawable?,
                           format: CompressFormat?): ByteArray? {
            return if (drawable == null) null else bitmap2Bytes(drawable2Bitmap(drawable), format)
        }

        /**
         * Bytes to drawable.
         *
         * @param bytes The bytes.
         * @return drawable
         */
        fun bytes2Drawable(bytes: ByteArray?): Drawable? {
            return if (bytes == null) null else bitmap2Drawable(bytes2Bitmap(bytes))
        }

        /**
         * View to bitmap.
         *
         * @param view The view.
         * @return bitmap
         */
        fun view2Bitmap(view: View?): Bitmap? {
            if (view == null) return null
            val ret: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(ret)
            val bgDrawable = view.background
            if (bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            view.draw(canvas)
            return ret
        }

        /**
         * Value of dp to value of px.
         *
         * @param dpValue The value of dp.
         * @return value of px
         */
        fun dp2px(dpValue: Float): Int {
            val scale: Float = SuperSafeApplication.Companion.getInstance().getResources().getDisplayMetrics().density
            return (dpValue * scale + 0.5f) as Int
        }

        /**
         * Value of px to value of dp.
         *
         * @param pxValue The value of px.
         * @return value of dp
         */
        fun px2dp(pxValue: Float): Int {
            val scale: Float = SuperSafeApplication.Companion.getInstance().getResources().getDisplayMetrics().density
            return (pxValue / scale + 0.5f) as Int
        }

        /**
         * Value of sp to value of px.
         *
         * @param spValue The value of sp.
         * @return value of px
         */
        fun sp2px(spValue: Float): Int {
            val fontScale: Float = SuperSafeApplication.Companion.getInstance().getResources().getDisplayMetrics().scaledDensity
            return (spValue * fontScale + 0.5f) as Int
        }

        /**
         * Value of px to value of sp.
         *
         * @param pxValue The value of px.
         * @return value of sp
         */
        fun px2sp(pxValue: Float): Int {
            val fontScale: Float = SuperSafeApplication.Companion.getInstance().getResources().getDisplayMetrics().scaledDensity
            return (pxValue / fontScale + 0.5f) as Int
        }

        ///////////////////////////////////////////////////////////////////////////
        // other utils methods
        ///////////////////////////////////////////////////////////////////////////
        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            var i = 0
            val len = s.length
            while (i < len) {
                if (!Character.isWhitespace(s[i])) {
                    return false
                }
                ++i
            }
            return true
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}