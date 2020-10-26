package co.tpcreative.supersafe.common.services
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RootAPI
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseServiceView
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.PresenterService
import co.tpcreative.supersafe.common.request.CategoriesRequest
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.request.TrackingRequest
import co.tpcreative.supersafe.common.requestimport.OutlookMailRequest
import co.tpcreative.supersafe.common.requestimport.UserRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.download.DownloadService
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class SuperSafeService : PresenterService<BaseServiceView<*>?>(), SuperSafeReceiver.ConnectivityReceiverListener {
    private val mBinder: IBinder? = LocalBinder() // Binder given to clients
    private var storage: Storage? = null
    private var androidReceiver: SuperSafeReceiver? = null
    private var downloadService: DownloadService? = null
    private var isCallRefreshToken = false
    override fun onCreate() {
        super.onCreate()
        Utils.Log(TAG, "onCreate")
        downloadService = DownloadService()
        storage = Storage(this)
        onInitReceiver()
        SuperSafeApplication.Companion.getInstance().setConnectivityListener(this)
    }

    fun getStorage(): Storage? {
        return storage
    }

    private fun onInitReceiver() {
        Utils.Log(TAG, "onInitReceiver")
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        androidReceiver = SuperSafeReceiver()
        registerReceiver(androidReceiver, intentFilter)
        SuperSafeApplication.Companion.getInstance().setConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver)
        }
        stopSelf()
        stopForeground(true)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Connected :$isConnected")
        val view: BaseServiceView<*>? = view()
        if (view != null) {
            if (isConnected) {
                view.onSuccessful("Connected network", EnumStatus.CONNECTED)
            } else {
                view.onSuccessful("Disconnected network", EnumStatus.DISCONNECTED)
            }
        }
    }

    override fun onActionScreenOff() {
        val view: BaseServiceView<*>? = view()
        if (view != null) {
            view.onSuccessful("Screen Off", EnumStatus.SCREEN_OFF)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we get killed, after returning from here, restart
        Utils.Log(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        val extras: Bundle? = intent?.getExtras()
        Utils.Log(TAG, "onBind")
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra")
        }
        return mBinder
    }

    fun onGetUserInfo() {
        Utils.Log(TAG, "onGetUserInfo 1")
        val view: BaseServiceView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return
        }
        Utils.Log(TAG, "onGetUserInfo 2")
        if (subscriptions == null) {
            return
        }
        val mUser: User = Utils.getUserInfo() ?: return
        val mAuthor = mUser.author ?: return
        SuperSafeApplication.serverAPI?.onUserInfo(UserRequest())
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.USER_INFO)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        if (mData == null) {
                            view.onError(onResponse.message, EnumStatus.USER_INFO)
                        }
                        if (mData?.premium != null && mData.email_token != null) {
                            mUser?.premium = mData.premium
                            mUser?.email_token = mData.email_token
                            Utils.setUserPreShare(mUser)
                            view.onSuccessful("Successful", EnumStatus.USER_INFO)
                        }
                    }
                    Utils.Log(TAG, "onGetUserInfo 3")
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error " + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onUpdateUserToken() {
        val view: BaseServiceView<*>? = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.UPDATE_USER_TOKEN)) {
            return
        }
        val user: User = Utils.getUserInfo() ?: return
        if (isCallRefreshToken) {
            Utils.Log(TAG, "Refresh token is progressing")
            return
        }
        isCallRefreshToken = true
        val mUserRequest = UserRequest()
        Utils.onWriteLog(Gson().toJson(user), EnumStatus.REFRESH_EMAIL_TOKEN)
        Utils.Log(TAG, "Body request " + Gson().toJson(mUserRequest))
        SuperSafeApplication?.serverAPI?.onUpdateToken(mUserRequest)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view?.onError(onResponse.responseMessage, EnumStatus.UPDATE_USER_TOKEN)
                        isCallRefreshToken = false
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        val mData: DataResponse? = onResponse.data
                        if (mData?.user != null) {
                            if (mData.user?.author != null) {
                                val mAuthorGlobal: Authorization? = mData?.user?.author
                                val mAuthor = mUser?.author
                                mAuthor?.refresh_token = mAuthorGlobal?.refresh_token
                                mAuthor?.session_token = mAuthorGlobal?.session_token
                                mUser?.author = mAuthor
                                Utils.setUserPreShare(mUser)
                                view?.onSuccessful(onResponse.message, EnumStatus.UPDATE_USER_TOKEN)
                                Utils.onWriteLog(Gson().toJson(mUser), EnumStatus.UPDATE_USER_TOKEN)
                                onDeleteOldAccessToken(mUserRequest)
                            }
                        }
                    }
                    Utils.Log(TAG, "Body Update token: " + Gson().toJson(onResponse))
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 403 || code == 400 || code == 401) {
                                val mUserResponse: User? = Utils.getUserInfo()
                                if (mUserResponse != null) {
                                    onSignIn(user)
                                }
                            }
                            val errorMessage: String? = bodys?.string()
                            Utils.Log(TAG, "error update access token $errorMessage")
                            view?.onError(errorMessage, EnumStatus.UPDATE_USER_TOKEN)
                            Utils.onWriteLog(errorMessage, EnumStatus.UPDATE_USER_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                    isCallRefreshToken = false
                })?.let { subscriptions?.add(it) }
    }

    fun onDeleteOldAccessToken(request: UserRequest) {
        val view: BaseServiceView<*>? = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.UPDATE_USER_TOKEN)) {
            isCallRefreshToken = false
            return
        }
        val mUser: User? = Utils.getUserInfo()
        if (mUser == null) {
            isCallRefreshToken = false
            return
        }
        Utils.onWriteLog(Gson().toJson(mUser), EnumStatus.DELETE_OLD_ACCESS_TOKEN)
        Utils.Log(TAG, "Body request " + Gson().toJson(request))
        SuperSafeApplication.serverAPI?.onDeleteOldAccessToken(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view?.onError(onResponse.responseMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                    } else {
                        ServiceManager.Companion.getInstance()?.onPreparingSyncData()
                    }
                    isCallRefreshToken = false
                    Utils.Log(TAG, "Body delele old access token: " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 403 || code == 400 || code == 401) {
                                val user: User? = Utils.getUserInfo()
                                user?.let { onSignIn(it) }
                            }
                            val errorMessage: String? = bodys?.string()
                            Utils.Log(TAG, "error old delete access token $errorMessage")
                            view?.onError(errorMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                            Utils.onWriteLog(errorMessage, EnumStatus.DELETE_OLD_ACCESS_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                    }
                    isCallRefreshToken = false
                })?.let { subscriptions?.add(it) }
    }

    fun onSignIn(request: User?) {
        Utils.Log(TAG, "onSignIn request")
        val view: BaseServiceView<*>? = view()
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.SIGN_IN)) {
            return
        }
        val mRequest = SignInRequest()
        mRequest.user_id = request?.email
        mRequest.password = SecurityUtil.key_password_default_encrypted
        mRequest.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        SuperSafeApplication.serverAPI?.onSignIn(mRequest)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    Utils.Log(TAG, "Body response sign in: " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view?.onError(onResponse.message, EnumStatus.SIGN_IN)
                    } else {
                        val user: User? = Utils.getUserInfo()
                        val mData: DataResponse? = onResponse.data
                        if (mData?.user != null) {
                            user?.author = mData.user?.author
                        }
                        Utils.setUserPreShare(user)
                        ServiceManager.getInstance()?.onPreparingSyncData()
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun getDriveAbout() {
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.GET_DRIVE_ABOUT)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view.onError("User is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        if (user.access_token == null) {
            view.onError("access token is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        view.onSuccessful(access_token, EnumStatus.GET_DRIVE_ABOUT)
        SuperSafeApplication?.serverDriveApi?.onGetDriveAbout(access_token)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: DriveAbout ->
                    if (onResponse.error != null) {
                        view.onError("Error " + Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        mUser?.driveAbout = onResponse
                        Utils.setUserPreShare(mUser)
                        view.onSuccessful(Gson().toJson(onResponse), EnumStatus.GET_DRIVE_ABOUT)
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            val value: String? = bodys?.string()
                            val driveAbout: DriveAbout? = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name + "-" + Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                                }
                            } else {
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name + " - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Utils.Log(TAG, "Exception....")
                            view.onError("Exception " + e.message, EnumStatus.GET_DRIVE_ABOUT)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error ^^:" + throwable.message, EnumStatus.GET_DRIVE_ABOUT)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onGetListFileInApp(view: BaseView<Int>?) {
        Utils.Log(TAG, "onGetListFolderInApp")
        if (isCheckNull<BaseView<Int>?>(view, EnumStatus.GET_LIST_FILES_IN_APP)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        if (!user.driveConnected) {
            view?.onError("No Drive connected", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        SuperSafeApplication.serverDriveApi?.onGetListFileInAppFolder(access_token, SuperSafeApplication.Companion.getInstance().getString(R.string.key_appDataFolder))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe({ ddd: Disposable? -> view?.onStartLoading(EnumStatus.GET_LIST_FILES_IN_APP) })
                ?.subscribe({ onResponse: DriveAbout ->
                    Utils.Log(TAG, "Response data from items " + Gson().toJson(onResponse))
                    view?.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP)
                    if (onResponse.error != null) {
                        Utils.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        view?.onError("Not found this id.... :" + Gson().toJson(onResponse.error), EnumStatus.GET_LIST_FILES_IN_APP)
                    } else {
                        val count = onResponse.files?.size
                        Utils.Log(TAG, "Total count request :$count")
                        view?.onSuccessful("Successful", EnumStatus.GET_LIST_FILES_IN_APP, count)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@Consumer
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            val value: String? = bodys?.string()
                            val driveAbout: DriveAbout? = Gson().fromJson(value, DriveAbout::class.java)
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
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                        view.onError("Error ^^:" + throwable?.message, EnumStatus.GET_LIST_FILES_IN_APP)
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP)
                })?.let { subscriptions?.add(it) }
    }

    /*Network request*/
    fun onCategoriesSync(mainCategories: MainCategoryModel, view: ServiceManager.BaseListener<*>) {
        Utils.Log(TAG, "onCategoriesSync " + Gson().toJson(mainCategories))
        if (isCheckNull<ServiceManager.BaseListener<*>?>(view, EnumStatus.CATEGORIES_SYNC)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.CATEGORIES_SYNC)
            return
        }
        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.CATEGORIES_SYNC)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        val mCategories = CategoriesRequest(user.email, user.cloud_id, SuperSafeApplication.getInstance().getDeviceId(), mainCategories)
        Utils.Log(TAG, "onCategoriesSync " + Gson().toJson(mCategories))
        SuperSafeApplication?.serverAPI?.onCategoriesSync(mCategories)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse?.error!!) {
                        Utils.Log(TAG, "onError 1")
                        Utils.Log(TAG, "onCategoriesSync " + Gson().toJson(onResponse))
                        view.onSuccessful(onResponse?.message!!, EnumStatus.CATEGORIES_SYNC)
                    } else {
                        if (onResponse != null) {
                            val mData: DataResponse? = onResponse.data
                            if (mData?.category != null) {
                                if (mainCategories?.categories_hex_name == mData?.category?.categories_hex_name) {
                                    mainCategories?.categories_id = mData?.category?.categories_id
                                    mainCategories?.isSyncOwnServer = true
                                    mainCategories?.isChange = false
                                    mainCategories?.isDelete = false
                                    SQLHelper.updateCategory(mainCategories)
                                    view.onSuccessful(onResponse.message + " - " + mData?.category?.categories_id + " - ", EnumStatus.CATEGORIES_SYNC)
                                } else {
                                    view.onSuccessful("Not found categories_hex_name - " + mData?.category?.categories_id, EnumStatus.CATEGORIES_SYNC)
                                }
                            }
                        }
                    }
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.CATEGORIES_SYNC)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.CATEGORIES_SYNC)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                        view.onError("Error :" + throwable?.message, EnumStatus.CATEGORIES_SYNC)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onDeleteCategoriesSync(mainCategories: MainCategoryModel, view: ServiceManager.BaseListener<*>?) {
        Utils.Log(TAG, "onDeleteCategoriesSync")
        if (isCheckNull<ServiceManager.BaseListener<*>?>(view, EnumStatus.DELETE_CATEGORIES)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.DELETE_CATEGORIES)
            return
        }
        if (user.access_token == null) {
            view?.onError("no access_token", EnumStatus.DELETE_CATEGORIES)
            return
        }
        val mCategories = CategoriesRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), mainCategories.categories_id)
        Utils.Log(TAG, "onDeleteCategoriesSync " + Gson().toJson(mCategories))
        SuperSafeApplication.serverAPI?.onDeleteCategories(mCategories)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        view?.onError("View is null", EnumStatus.DELETE_CATEGORIES)
                        return@subscribe
                    }
                    if (onResponse.error!!) {
                        Utils.Log(TAG, "onError 1")
                        view.onError(onResponse.message!!, EnumStatus.DELETE_CATEGORIES)
                    } else {
                        Utils.Log(TAG, "onDeleteCategoriesSync response" + Gson().toJson(onResponse))
                        view.onSuccessful(onResponse.message!!, EnumStatus.DELETE_CATEGORIES)
                    }
                }, Consumer subscribe@{ throwable: Throwable? ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        view?.onError("View is null", EnumStatus.DELETE_CATEGORIES)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.DELETE_CATEGORIES)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.DELETE_CATEGORIES)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                        view.onError("Error :" + throwable?.message, EnumStatus.DELETE_CATEGORIES)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onUpdateItems(mItem: ItemModel, view: ServiceManager.BaseListener<*>?) {
        Utils.Log(TAG, "onUpdateItems")
        if (isCheckNull<ServiceManager.BaseListener<*>?>(view, EnumStatus.UPDATE)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.UPDATE)
            return
        }
        if (!user.driveConnected) {
            view?.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        if (!Utils.isNotEmptyOrNull(mItem.categories_id)) {
            view?.onError("Categories id is null", EnumStatus.UPDATE)
            Utils.Log(TAG, " Updated => Warning categories id is null")
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        SuperSafeApplication.serverAPI?.onSyncData(SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.getInstance().getDeviceId(), mItem))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        mItem.isUpdate = true
                        view.onSuccessful(EnumStatus.UPDATED_ITEM_SUCCESSFULLY.name, EnumStatus.UPDATED_ITEM_SUCCESSFULLY)
                        SQLHelper.updatedItem(mItem)
                        view.onError("Queries add items is failed :" + onResponse.message, EnumStatus.UPDATE)
                    } else {
                        mItem.isUpdate = false
                        SQLHelper.updatedItem(mItem)
                        view.onSuccessful(EnumStatus.UPDATED_ITEM_SUCCESSFULLY.name, EnumStatus.UPDATED_ITEM_SUCCESSFULLY)
                    }
                    Utils.Log(TAG, "Adding item Response " + Gson().toJson(onResponse))
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            Utils.Log(TAG, "Adding item Response error" + bodys?.string())
                            view.onError("" + bodys?.string(), EnumStatus.UPDATE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.UPDATE)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.UPDATE)
                    }
                })?.let { subscriptions?.add(it) }
    }

    /*Date for Categories*/
    fun onAddItems(items: ItemModel, drive_id: String, view: ServiceManager.ServiceManagerInsertItem?) {
        Utils.Log(TAG, "onAddItems")
        if (isCheckNull<ServiceManager.ServiceManagerInsertItem?>(view, EnumStatus.ADD_ITEMS)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.ADD_ITEMS)
            return
        }
        if (!user.driveConnected) {
            view?.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        items.isSyncOwnServer = true
        Utils.Log(TAG, "system access token : " + Utils.getAccessToken())
        val entityModel: ItemModel? = SQLHelper.getItemById(items.items_id)
        if (entityModel==null){
            view?.onError("no user", EnumStatus.ADD_ITEMS)
            return
        }
        if (items.isOriginalGlobalId) {
            if (!Utils.isNotEmptyOrNull(entityModel.global_thumbnail_id)) {
                entityModel.global_thumbnail_id = "null"
            }
            entityModel.originalSync = true
            entityModel.global_original_id = drive_id
        } else {
            if (!Utils.isNotEmptyOrNull(entityModel.global_original_id)) {
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
        val mRequest = SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.getInstance().getDeviceId(), entityModel)
        Utils.Log(TAG, "onAddItems request " + Gson().toJson(mRequest))
        SuperSafeApplication.serverAPI?.onSyncData(mRequest)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError:" + Gson().toJson(onResponse))
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS)
                    } else {
                        /*Check saver space*/
                        checkSaverSpace(entityModel, items.isOriginalGlobalId)
                        SQLHelper.updatedItem(entityModel)
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS)
                    }
                    Utils.Log(TAG, "Adding item Response " + Gson().toJson(onResponse))
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            Utils.Log(TAG, "Adding item Response error" + bodys?.string())
                            view.onError("" + bodys?.string(), EnumStatus.ADD_ITEMS)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.ADD_ITEMS)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.ADD_ITEMS)
                    }
                })?.let { subscriptions?.add(it) }
    }

    /*Check saver space*/
    fun checkSaverSpace(itemModel: ItemModel, isOriginalGlobalId: Boolean) {
        val mType = EnumFormatType.values()[itemModel?.formatType]
        if (mType == EnumFormatType.IMAGE) {
            if (Utils.getSaverSpace()) {
                itemModel.isSaver = true
                Utils.checkSaverToDelete(itemModel.originalPath, isOriginalGlobalId)
            }
        }
    }

    /*Check imported data after sync data*/
    fun checkImportedDataBeforeSyncData(itemModel: ItemModel) {
        val categoryModel: MainCategoryModel? = SQLHelper.getCategoriesLocalId(itemModel?.categories_local_id)
        Utils.Log(TAG, "checkImportedDataBeforeSyncData " + Gson().toJson(categoryModel))
        if (categoryModel != null) {
            if (!Utils.isNotEmptyOrNull(itemModel.categories_id)) {
                itemModel.categories_id = categoryModel.categories_id
                Utils.Log(TAG, "checkImportedDataBeforeSyncData ==> isNotEmptyOrNull")
            }
        }
    }

    /*Get List Categories*/
    fun onDeleteCloudItems(items: ItemModel, view: ServiceManager.BaseListener<*>?) {
        Utils.Log(TAG, "onDeleteCloudItems")
        if (isCheckNull<ServiceManager.BaseListener<*>?>(view, EnumStatus.DELETE_SYNC_CLOUD_DATA)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.DELETE_SYNC_CLOUD_DATA)
            return
        }
        if (!user.driveConnected) {
            view?.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        SuperSafeApplication.serverDriveApi?.onDeleteCloudItem(access_token, items.global_id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer { onResponse: Response<DriveAbout> ->
                    Utils.Log(TAG, "Deleted cloud response code " + onResponse.code())
                    if (onResponse.code() == 204) {
                        val delete = EnumDelete.values()[items.deleteAction]
                        if (delete == EnumDelete.DELETE_DONE) {
                            view?.onSuccessful("Deleted successfully", EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                        }
                        view?.onSuccessful("Deleted Successful : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    } else if (onResponse.code() == 404) {
                        val delete = EnumDelete.values()[items.deleteAction]
                        if (delete == EnumDelete.DELETE_DONE) {
                            view?.onSuccessful("Deleted successfully", EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                        }
                        val value = onResponse.errorBody()?.string()
                        val driveAbout: DriveAbout = Gson().fromJson(value, DriveAbout::class.java)
                        view?.onError("Not found file :" + Gson().toJson(driveAbout.error) + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    } else {
                        view?.onError("Another cases : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA)
                    }
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            val value: String? = bodys?.string()
                            val driveAbout: DriveAbout? = Gson().fromJson(value, DriveAbout::class.java)
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
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error 0:" + throwable.message, EnumStatus.DELETED_CLOUD_ITEM_SUCCESSFULLY)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onDeleteOwnSystem(items: ItemModel, view: ServiceManager.BaseListener<*>?) {
        Utils.Log(TAG, "onDeleteOwnSystem")
        if (isCheckNull<ServiceManager.BaseListener<*>?>(view, EnumStatus.DELETE_SYNC_OWN_DATA)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.DELETE_SYNC_OWN_DATA)
            return
        }
        if (!user.driveConnected) {
            view?.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        val mItem = SyncItemsRequest(user.email, user.cloud_id, items.items_id)
        Utils.Log(TAG, "onDeleteOwnSystem " + Gson().toJson(mItem))
        SuperSafeApplication?.serverAPI?.onDeleteOwnItems(mItem)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer<RootResponse> subscribe@{ onResponse: RootResponse ->
                    Utils.Log(TAG, "Response data from items " + Gson().toJson(onResponse))
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (onResponse.error) {
                        view.onError(onResponse.message!!, EnumStatus.DELETED_ITEM_SUCCESSFULLY)
                    } else {
                        view.onSuccessful(onResponse.message!!, EnumStatus.DELETED_ITEM_SUCCESSFULLY)
                    }
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            val value: String? = bodys?.string()
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
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.DELETE_SYNC_OWN_DATA)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onGetListSync(nextPage: String, view: ServiceManager.BaseListener<ItemModel>?) {
        Utils.Log(TAG, "onGetListSync")
        if (isCheckNull<ServiceManager.BaseListener<ItemModel>?>(view, EnumStatus.GET_LIST_FILE)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("no user", EnumStatus.GET_LIST_FILE)
            return
        }
        if (user.access_token == null) {
            view?.onError("no access_token", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        if (!user.driveConnected) {
            view?.onError("no driveConnected", EnumStatus.REQUEST_ACCESS_TOKEN)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        SuperSafeApplication.serverAPI?.onListFilesSync(SyncItemsRequest(user.email, user.cloud_id, SuperSafeApplication.Companion.getInstance().getDeviceId(), true, nextPage))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    Utils.Log(TAG, "onGetListSync " + Gson().toJson(onResponse))
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        view?.onError("View is null", EnumStatus.GET_LIST_FILE)
                        return@subscribe
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError 1")
                        view.onError(onResponse.message!!, EnumStatus.GET_LIST_FILE)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        val listCategories: MutableList<MainCategoryModel>? = mData?.categoriesList
                        val mListItemResponse: MutableList<ItemModel>? = mData?.itemsList
                        if (mData?.nextPage == null) {
                            if (mData?.syncData != null) {
                                user.syncData = mData.syncData
                            }
                            Utils.setUserPreShare(user)
                            if (listCategories != null) {
                                for (index in listCategories) {
                                    val main: MainCategoryModel? = SQLHelper.getCategoriesId(index.categories_id, false)
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
                                            mMain.categories_local_id = Utils.getUUId()
                                            mMain.items_id = Utils.getUUId()
                                            mMain.isSyncOwnServer = true
                                            mMain.isChange = false
                                            mMain.isDelete = false
                                            mMain.pin = ""
                                            val count: Int = SQLHelper.getLatestItem()
                                            mMain.categories_max = count.toLong()
                                            SQLHelper.insertCategory(mMain)
                                            Utils.Log(TAG, "Adding new main categories.......................................2")
                                        }
                                    }
                                }
                            }
                            view.onSuccessful(mData?.nextPage!!, EnumStatus.SYNC_READY)
                        } else {
                            val mList: MutableList<ItemModel> = ArrayList()
                            if (mListItemResponse != null) {
                                for (index in mListItemResponse) {
                                    mList.add(ItemModel(index, EnumStatus.DOWNLOAD))
                                }
                            }
                            view.onShowListObjects(mList)
                            view.onSuccessful(mData.nextPage!!, EnumStatus.LOAD_MORE)
                        }
                    }
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        view?.onError("View is null", EnumStatus.GET_LIST_FILE)
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                            view.onError("" + msg, EnumStatus.GET_LIST_FILE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            view.onError("" + e.message, EnumStatus.GET_LIST_FILE)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        view.onError("Error :" + throwable.message, EnumStatus.GET_LIST_FILE)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onDownloadFile(items: ItemModel, isDownloadToExport: Boolean, listener: ServiceManager.DownloadServiceListener?) {
        Utils.Log(TAG, "onDownloadFile !!!!")
        val mUser: User? = Utils.getUserInfo()
        if (mUser==null){
            return
        }
        if (!mUser.driveConnected) {
            listener?.onError("No Drive api connected", EnumStatus.DOWNLOAD)
            return
        }
        if (mUser.access_token == null) {
            listener?.onError("No Access token", EnumStatus.DOWNLOAD)
        }
        val request = DownloadFileRequest()
        var id : String? = ""
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
        Utils.Log(TAG, "onDownloadFile request " + Gson().toJson(items))
        if (!Utils.isNotEmptyOrNull(id)) {
            listener?.onError("Error upload", EnumStatus.REQUEST_NEXT_DOWNLOAD)
            return
        }
        items.originalPath = Utils.getOriginalPath(items.originalName, items.items_id)
        request.path_folder_output = Utils.createDestinationDownloadItem(items.items_id)
        downloadService?.onProgressingDownload(object : DownloadService.DownLoadServiceListener {
            override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?) {
                Utils.Log(TAG, "onDownLoadCompleted " + file_name?.getAbsolutePath())
                if (request != null) {
                    listener?.onDownLoadCompleted(file_name, request)
                }
                val entityModel: ItemModel? = SQLHelper.getItemById(items.items_id)
                val categoryModel: MainCategoryModel? = SQLHelper.getCategoriesId(items.categories_id, false)
                if (entityModel != null) {
                    if (categoryModel != null) {
                        entityModel.categories_local_id = categoryModel.categories_local_id
                    }
                    entityModel.isSaver = false
                    if (items.isOriginalGlobalId) {
                        entityModel.originalSync = true
                        entityModel.global_original_id = request?.id
                    } else {
                        entityModel.thumbnailSync = true
                        entityModel.global_thumbnail_id = request?.id
                    }
                    if (entityModel.originalSync && entityModel.thumbnailSync) {
                        entityModel.isSyncCloud = true
                        entityModel.isSyncOwnServer = true
                        entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
                        Utils.Log(TAG, "Synced already....")
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
                Utils.Log(TAG, "onDownLoadError $error")
                if (listener != null) {
                    listener.onError("Error download ", EnumStatus.REQUEST_NEXT_DOWNLOAD)
                }
            }

            override fun onProgressingDownloading(percent: Int) {
                listener?.onProgressDownload(percent)
                Utils.Log(TAG, "Progressing downloaded $percent%")
            }

            override fun onAttachmentElapsedTime(elapsed: Long) {}
            override fun onAttachmentAllTimeForDownloading(all: Long) {}
            override fun onAttachmentRemainingTime(all: Long) {}
            override fun onAttachmentSpeedPerSecond(all: Double) {}
            override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {}
            override fun onSavedCompleted() {
                Utils.Log(TAG, "onSavedCompleted ")
            }

            override fun onErrorSave(name: String?) {
                Utils.Log(TAG, "onErrorSave")
                if (listener != null) {
                    listener.onError("Error download save ", EnumStatus.DOWNLOAD)
                }
            }

            override fun onCodeResponse(code: Int, request: DownloadFileRequest?) {
                if (listener != null) {
                    val mItem: ItemModel? = request?.items
                    if (mItem != null) {
                        /*Not Found file*/
                        if (code == 404) {
                            Utils.Log(TAG, "isDelete local id error")
                            SQLHelper.deleteItem(items)
                        }
                    }
                }
            }

            override fun onHeader(): MutableMap<String, String>? {
                return HashMap()
            }
        })
        downloadService?.downloadFileFromGoogleDrive(request)
    }

    fun onUploadFileInAppFolder(items: ItemModel, listener: ServiceManager.UploadServiceListener?) {
        Utils.Log(TAG, "onUploadFileInAppFolder")
        val mUser: User? = Utils.getUserInfo()
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
        if (!storage!!.isFileExist(file.absolutePath)) {
            SQLHelper.deleteItem(items)
            listener?.onError("This path is not found", EnumStatus.UPLOAD)
            return
        }
        if (!Utils.isNotEmptyOrNull(items.categories_id)) {
            listener?.onError("Error upload", EnumStatus.REQUEST_NEXT_UPLOAD)
            return
        }
        contentEvent.items_id = items.items_id
        val hex: String? = DriveEvent.getInstance()?.convertToHex(Gson().toJson(contentEvent))
        content[getString(R.string.key_name)] = hex
        val list: MutableList<String?> = ArrayList()
        list.add(getString(R.string.key_appDataFolder))
        content[getString(R.string.key_parents)] = list
        val metaPart: MultipartBody.Part = MultipartBody.Part.create(RequestBody.create(contentType, Gson().toJson(content)))
        val fileBody = ProgressRequestBody(file, object : ProgressRequestBody.UploadCallbacks {
            override fun onProgressUpdate(percentage: Int) {
                Utils.Log(TAG, "Progressing uploaded $percentage%")
                listener?.onProgressUpdate(percentage)
            }
            override fun onError() {
                Utils.Log(TAG, "onError")
                listener?.onError("Error upload", EnumStatus.REQUEST_NEXT_UPLOAD)
            }

            override fun onFinish() {
                listener?.onFinish()
                Utils.Log(TAG, "onFinish")
            }
        })
        fileBody.setContentType(items.mimeType)
        val dataPart: MultipartBody.Part = MultipartBody.Part.create(fileBody)
        val request: Call<DriveResponse>? = SuperSafeApplication.serverDriveApi?.uploadFileMultipleInAppFolder(mUser?.access_token, metaPart, dataPart, items.mimeType)
        request?.enqueue(object : Callback<DriveResponse?> {
            override fun onResponse(call: Call<DriveResponse?>?, response: Response<DriveResponse?>?) {
                Utils.Log(TAG, "response successful :" + Gson().toJson(response?.body()))
                response?.body()?.let { listener?.onResponseData(it) }
            }

            override fun onFailure(call: Call<DriveResponse?>?, t: Throwable?) {
                Utils.Log(TAG, "response failed :" + t?.message)
                listener?.onError("Error upload" + t?.message, EnumStatus.REQUEST_NEXT_UPLOAD)
            }
        })
    }

    fun getDriveAbout(view: BaseView<*>?) {
        Utils.Log(TAG, "getDriveAbout")
        if (isCheckNull<BaseView<*>?>(view, EnumStatus.GET_DRIVE_ABOUT)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view?.onError("User is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        if (user.access_token == null) {
            view?.onError("Access token is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        val access_token = user.access_token
        Utils.Log(TAG, "access_token : $access_token")
        view?.onSuccessful(access_token)
        SuperSafeApplication?.serverDriveApi?.onGetDriveAbout(access_token)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe({ m: Disposable? -> view?.onStartLoading(EnumStatus.GET_DRIVE_ABOUT) })
                ?.subscribe(Consumer subscribe@{ onResponse: DriveAbout ->
                    if (view == null) {
                        view?.onError("View is disable", EnumStatus.GET_DRIVE_ABOUT)
                        return@subscribe
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT)
                    if (onResponse.error != null) {
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = false
                            Utils.setUserPreShare(user)
                        }
                        view.onError(Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = true
                            Utils.setUserPreShare(user)
                            view.onSuccessful("Successful", EnumStatus.GET_DRIVE_ABOUT)
                        }
                    }
                }, Consumer subscribe@{ throwable: Throwable ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            if (view == null) {
                                return@subscribe
                            }
                            val value: String? = bodys?.string()
                            val driveAbout: DriveAbout? = Gson().fromJson(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    val mUser: User? = Utils.getUserInfo()
                                    if (mUser != null) {
                                        user.driveConnected = false
                                        Utils.setUserPreShare(user)
                                    }
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name + "-" + Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN)
                                }
                            } else {
                                val mUser: User? = Utils.getUserInfo()
                                if (mUser != null) {
                                    user.driveConnected = false
                                    Utils.setUserPreShare(user)
                                }
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name + " - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            val mUser: User? = Utils.getUserInfo()
                            if (mUser != null) {
                                user.driveConnected = false
                                Utils.setUserPreShare(user)
                            }
                            view.onError("Error IOException " + e.message, EnumStatus.GET_DRIVE_ABOUT)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            user.driveConnected = false
                            Utils.setUserPreShare(user)
                        }
                        view.onError("Error else :" + throwable.message, EnumStatus.GET_DRIVE_ABOUT)
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT)
                })?.let { subscriptions?.add(it) }
    }

    /*TrackHandler*/
    fun onCheckVersion() {
        Utils.Log(TAG, "onCheckVersion")
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.CHECK_VERSION)) {
            return
        }
        SuperSafeApplication.serverAPI?.onCheckVersion()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: BaseResponse ->
                    if (onResponse.version != null) {
                        view.onSuccessful("Successful", EnumStatus.CHECK_VERSION)
                        val user: User? = Utils.getUserInfo()
                        user?.version = onResponse.version
                        Utils.setUserPreShare(user)
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onSyncAuthorDevice() {
        Utils.Log(TAG, "onSyncAuthorDevice")
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.AUTHOR_SYNC)) {
            return
        }
        val user: User? = Utils.getUserInfo()
        var user_id: String? = "null@gmail.com"
        if (user != null) {
            user_id = user.email
        }
        SuperSafeApplication.serverAPI?.onTracking(TrackingRequest(user_id, SuperSafeApplication.getInstance().getDeviceId()))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: RootResponse ->
                    if (!onResponse.error) {
                        Utils.Log(TAG, "Tracking response " + Gson().toJson(onResponse))
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "Author error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Author Can not call" + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    /*Email token*/
    fun onSendMail(request: EmailToken) {
        Utils.Log(TAG, "onSendMail.....")
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.SEND_EMAIL)) {
            return
        }
        val mUser: User = Utils.getUserInfo() ?: return
        val response: Call<ResponseBody>? = SuperSafeApplication.serviceGraphMicrosoft?.onSendMail(request.access_token, request)
        response?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>?) {
                try {
                    val code = response?.code()
                    if (code == 401) {
                        Utils.Log(TAG, "code $code")
                        onRefreshEmailToken(request)
                        val errorMessage = response.errorBody()?.string()
                        Utils.Log(TAG, "error$errorMessage")
                        view.onError(errorMessage, EnumStatus.SEND_EMAIL)
                        mUser.isWaitingSendMail = false
                        Utils.setUserPreShare(mUser)
                    } else if (code == 202) {
                        Utils.Log(TAG, "code $code")
                        view.onSuccessful("successful", EnumStatus.SEND_EMAIL)
                        val mUser: User? = Utils.getUserInfo()
                        mUser?.isWaitingSendMail = false
                        Utils.setUserPreShare(mUser)
                        ServiceManager.Companion.getInstance()?.onDismissServices()
                        Utils.Log(TAG, "Body : Send email Successful")
                    } else {
                        Utils.Log(TAG, "code $code")
                        Utils.Log(TAG, "Nothing to do")
                        view.onError("Null", EnumStatus.SEND_EMAIL)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                Utils.Log(TAG, "response failed :" + t.message)
            }
        })
    }

    fun onRefreshEmailToken(request: EmailToken) {
        Utils.Log(TAG, "onRefreshEmailToken.....")
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.REFRESH)) {
            return
        }
        val mUser: User? = Utils.getUserInfo()
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[getString(R.string.key_client_id)] = request.client_id
        hash[getString(R.string.key_redirect_uri)] = request.redirect_uri
        hash[getString(R.string.key_grant_type)] = request.grant_type
        hash[getString(R.string.key_refresh_token)] = request.refresh_token
        SuperSafeApplication.serviceGraphMicrosoft?.onRefreshEmailToken(RootAPI.REFRESH_TOKEN, hash)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: EmailToken ->
                    if (onResponse != null) {
                        val token = mUser?.email_token
                        token?.access_token = onResponse.token_type + " " + onResponse.access_token
                        token?.refresh_token = onResponse.refresh_token
                        token?.token_type = onResponse.token_type
                        Utils.setUserPreShare(mUser)
                        onAddEmailToken()
                    }
                    view.onSuccessful("successful", EnumStatus.REFRESH)
                    Utils.Log(TAG, "Body refresh : " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            view.onError(msg, EnumStatus.SEND_EMAIL)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onAddEmailToken() {
        Utils.Log(TAG, "onSignIn.....")
        val view: BaseServiceView<*> = view()!!
        if (isCheckNull<BaseServiceView<*>?>(view, EnumStatus.ADD_EMAIL_TOKEN)) {
            return
        }
        val mUser: User? = Utils.getUserInfo()
        SuperSafeApplication.serverAPI?.onAddEmailToken(OutlookMailRequest(mUser?.email_token?.refresh_token, mUser?.email_token?.access_token))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: BaseResponse ->
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.RESET) }
                    emailToken?.let {
                        onSendMail(it)
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.Companion.getInstance()?.onUpdatedUserToken()
                            }
                            val errorMessage: String? = bodys?.string()
                            Utils.Log(TAG, "error$errorMessage")
                            view.onError(errorMessage, EnumStatus.ADD_EMAIL_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
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

    fun <T> isCheckNull(view: T?, status: EnumStatus): Boolean {
        if (subscriptions == null) {
            Utils.Log(TAG, "Subscriptions is null " + status.name)
            return true
        } else if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "No connection " + status.name)
            return true
        } else if (view == null) {
            Utils.Log(TAG, "View is null " + status.name)
            return true
        }
        return false
    }

    companion object {
        private val TAG = SuperSafeService::class.java.simpleName
    }
}