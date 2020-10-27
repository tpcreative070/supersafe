package co.tpcreative.supersafe.ui.checksystem
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RootAPI
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.requestimport.OutlookMailRequest
import co.tpcreative.supersafe.common.requestimport.UserRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.response.UserCloudResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.snatik.storage.security.SecurityUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*

class CheckSystemPresenter : Presenter<BaseView<EmptyModel>>() {
    private val TAG = CheckSystemPresenter::class.java.simpleName
    var mUser: User? = null
    var googleOauth: GoogleOauth? = null
    protected var isUserExisting = false
    fun getIntent(activity: Activity?) {
        try {
            val bundle: Bundle? = activity?.getIntent()?.getExtras()
            googleOauth = bundle?.get(getString(R.string.key_google_oauth)) as GoogleOauth
            Utils.Log(TAG, "≈ " + Gson().toJson(googleOauth))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onCheckUser(email: String?, other_email: String?) {
        Utils.Log(TAG, "onCheckUser")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication?.serverAPI?.onCheckUserId(UserRequest())
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.USER_ID_EXISTING) }
                ?.subscribe({ onResponse: RootResponse ->
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    if (onResponse.error!!) {
                        val request = SignInRequest()
                        request.user_id = email
                        request.password = SecurityUtil.key_password_default_encrypted
                        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                        onSignIn(request)
                    } else {
                        //view.onSuccessful(email,!onResponse.error);
                        view.onSuccessful(email, EnumStatus.USER_ID_EXISTING)
                        val request = SignInRequest()
                        request.user_id = email
                        request.password = SecurityUtil.key_password_default_encrypted
                        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                        onSignIn(request)
                        isUserExisting = !onResponse.error
                    }
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            } else if (code == 403) {
                                val request = SignInRequest()
                                request.user_id = email
                                request.password = SecurityUtil.key_password_default_encrypted
                                request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                                onSignIn(request)
                                Utils.Log(TAG, "Login")
                            }
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call check user" + throwable?.message)
                    }
                    view.onStopLoading(EnumStatus.USER_ID_EXISTING)
                })?.let { subscriptions?.add(it) }
    }

    fun onSignIn(request: SignInRequest) {
        Utils.Log(TAG, "onSignIn")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onSignIn(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.SIGN_IN) }
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.SIGN_IN)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        mUser = mData?.user
                        Utils.setUserPreShare(mUser)
                        var email = mUser?.email
                        if (mUser?.change!!) {
                            email = mUser?.other_email
                        }
                        val emailToken: EmailToken? = EmailToken.getInstance()?.convertObject(mUser!!, EnumStatus.SIGN_IN)
                        if (emailToken != null) {
                            onSendMail(emailToken)
                        }
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            Utils.Log(TAG, "error" + bodys?.string())
                            val msg: String = Gson().toJson(bodys?.string())
                            view.onError(msg, EnumStatus.SIGN_IN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call sign in" + throwable?.message)
                    }
                    view.onStopLoading(EnumStatus.SIGN_IN)
                })?.let { subscriptions?.add(it) }
    }

    fun onVerifyCode(request: VerifyCodeRequest) {
        Utils.Log(TAG, "onVerifyCode")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onVerifyCode(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.VERIFY_CODE) }
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(getString(R.string.the_code_not_signed_up), EnumStatus.VERIFY_CODE)
                    } else {
                        mUser = Utils.getUserInfo()
                        if (mUser != null) {
                            mUser?.verified = true
                            val mData: DataResponse? = onResponse.data
                            if (mData == null) {
                                view.onError(onResponse.message, EnumStatus.VERIFY_CODE)
                                return@subscribe
                            }
                            if (mData.premium != null) {
                                mUser?.premium = mData.premium
                                Utils.Log(TAG, "Saved.............")
                            }
                            SuperSafeApplication.getInstance().writeUserSecret(mUser)
                            Utils.setUserPreShare(mUser)
                            mUser = Utils.getUserInfo()
                        }
                        view.onSuccessful(onResponse.message, EnumStatus.VERIFY_CODE)
                    }
                    Utils.Log(TAG, "Body verify code : " + Gson().toJson(onResponse))
                }, { throwable: Throwable ->
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
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call verify code" + throwable?.message)
                    }
                    view.onStopLoading(EnumStatus.VERIFY_CODE)
                })?.let { subscriptions?.add(it) }
    }

    fun onChangeEmail(request: VerifyCodeRequest) {
        Utils.Log(TAG, "info")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError("No connection", EnumStatus.CHANGE_EMAIL)
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onUpdateUser(ChangeUserIdRequest(request))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.CHANGE_EMAIL) }
                ?.subscribe({ onResponse: RootResponse ->
                    view.onStopLoading(EnumStatus.CHANGE_EMAIL)
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.CHANGE_EMAIL)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        if (mData?.author != null) {
                            if (mUser != null) {
                                mUser?.author = mData.author
                                mUser?.email = request.new_user_id
                                mUser?.other_email = request.other_email
                                mUser?.change = true
                                Utils.setUserPreShare(mUser)
                            } else {
                                Utils.Log(TAG, "User is null")
                            }
                            mUser = Utils.getUserInfo()
                            view.onSuccessful(onResponse.message, EnumStatus.CHANGE_EMAIL)
                        }
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable ->
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
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.CHANGE_EMAIL)
                })?.let { subscriptions?.add(it) }
    }

    fun onResendCode(request: VerifyCodeRequest) {
        Utils.Log(TAG, "info")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication?.serverAPI?.onResendCode(RequestCodeRequest(request.user_id, Utils.getAccessToken(), SuperSafeApplication.getInstance().getDeviceId()))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.RESEND_CODE) }
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.RESEND_CODE)
                        view.onStopLoading(EnumStatus.RESEND_CODE)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        val mData: DataResponse? = onResponse?.data
                        mUser?.code = mData?.requestCode?.code
                        Utils.setUserPreShare(mUser)
                        val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.SIGN_IN) }
                        if (emailToken != null) {
                            onSendMail(emailToken)
                        }
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable ->
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
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.RESEND_CODE)
                })?.let { subscriptions?.add(it) }
    }

    /*Email Verify*/
    fun onSendMail(request: EmailToken) {
        Utils.Log(TAG, "onSendMail.....")
        val view: BaseView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return
        }
        if (subscriptions == null) {
            return
        }
        val response: Call<ResponseBody>? = SuperSafeApplication.serviceGraphMicrosoft?.onSendMail(request?.access_token, request)
        response?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>?) {
                try {
                    val code = response?.code()
                    if (code == 401) {
                        Utils.Log(TAG, "code $code")
                        onRefreshEmailToken(request)
                        val errorMessage = response.errorBody()?.string()
                        Utils.Log(TAG, "error$errorMessage")
                        view.onError(errorMessage, EnumStatus.SEND_EMAIL)
                    } else if (code == 202) {
                        Utils.Log(TAG, "code $code")
                        view.onSuccessful("Successful", EnumStatus.SEND_EMAIL)
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
        val view: BaseView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return
        }
        if (subscriptions == null) {
            return
        }
        val mUser: User? = Utils.getUserInfo()
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[SuperSafeApplication.getInstance().getString(R.string.key_client_id)] = request.client_id
        hash[SuperSafeApplication.getInstance().getString(R.string.key_redirect_uri)] = request.redirect_uri
        hash[SuperSafeApplication.getInstance().getString(R.string.key_grant_type)] = request.grant_type
        hash[SuperSafeApplication.getInstance().getString(R.string.key_refresh_token)] = request.refresh_token
        SuperSafeApplication.serviceGraphMicrosoft?.onRefreshEmailToken(RootAPI.REFRESH_TOKEN, hash)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: EmailToken ->
                    if (onResponse != null) {
                        val token: EmailToken? = mUser?.email_token
                        token?.access_token = onResponse.token_type + " " + onResponse.access_token
                        token?.refresh_token = onResponse.refresh_token
                        token?.token_type = onResponse.token_type
                        Utils.setUserPreShare(mUser)
                        onAddEmailToken()
                    }
                    view.onSuccessful("successful", EnumStatus.REFRESH)
                    Utils.Log(TAG, "Body refresh : " + Gson().toJson(onResponse))
                }, { throwable: Throwable ->
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
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onAddEmailToken() {
        Utils.Log(TAG, "onSignIn.....")
        val view: BaseView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return
        }
        if (subscriptions == null) {
            return
        }
        val mUser: User? = Utils.getUserInfo()
        SuperSafeApplication.serverAPI?.onAddEmailToken(OutlookMailRequest(mUser?.email_token?.refresh_token, mUser?.email_token?.access_token))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: BaseResponse ->
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.SIGN_IN) }
                    if (emailToken != null) {
                        onSendMail(emailToken)
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 403) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
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

    fun onAddUserCloud(cloudRequest: UserCloudRequest) {
        Utils.Log(TAG, "info")
        val view: BaseView<EmptyModel>? = view() ?: return
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication?.serverAPI?.onAddUserCloud(cloudRequest)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.CREATE) }
                ?.subscribe({ onResponse: RootResponse ->
                    Utils.Log(TAG, "Body ???: " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.CREATE)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        val mCloud: UserCloudResponse? = mData?.userCloud
                        mUser = Utils.getUserInfo()
                        mUser?.verified = true
                        mUser?.cloud_id = mCloud?.cloud_id
                        Utils.setUserPreShare(mUser)
                        view.onSuccessful(onResponse.message, EnumStatus.CREATE)
                    }
                    Utils.Log(TAG, "User info " + Gson().toJson(mUser))
                }, { throwable: Throwable ->
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
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call add user cloud" + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.CREATE)
                })?.let { subscriptions?.add(it) }
    }

    fun onUserCloudChecking() {
        Utils.Log(TAG, "onUserCloudChecking")
        val view: BaseView<EmptyModel>? = view() ?: return
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication?.serverAPI?.onCheckUserCloud(UserCloudRequest(mUser?.email, SuperSafeApplication.getInstance().getDeviceId()))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.CLOUD_ID_EXISTING) }
                ?.subscribe({ onResponse: RootResponse ->
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.CLOUD_ID_EXISTING)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        val mCloudData: UserCloudResponse? = mData?.userCloud
                        view.onSuccessful(mCloudData?.cloud_id, EnumStatus.CLOUD_ID_EXISTING)
                    }
                    view.onStopLoading(EnumStatus.CLOUD_ID_EXISTING)
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val body: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "error" + body?.string())
                            val msg: String = Gson().toJson(body?.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call check user cloud " + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.CLOUD_ID_EXISTING)
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    init {
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            mUser = user
        }
    }
}