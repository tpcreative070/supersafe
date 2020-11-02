package co.tpcreative.supersafe.ui.unlockalbum
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toObject
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.requestimport.OutlookMailRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*

class UnlockAllAlbumPresenter : Presenter<BaseView<EmptyModel>>() {
    var mListCategories: MutableList<MainCategoryModel>? = SQLHelper.getListCategories(false)
    fun onVerifyCode(request: VerifyCodeRequest) {
        Utils.Log(TAG, "info")
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("View is null", EnumStatus.VERIFIED_ERROR)
            return
        }
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.VERIFIED_ERROR)
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onVerifyCode(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.VERIFY) }
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(getString(R.string.the_code_not_signed_up), EnumStatus.VERIFY_CODE)
                    } else {
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.VERIFY)
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            mUser.verified = true
                            Utils.setUserPreShare(mUser)
                        }
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val mBody: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val mCode = (throwable as HttpException?)?.response()?.code()
                        try {
                            val mMessage = mBody?.string()
                            val mObject = mMessage?.toObject(BaseResponse::class.java)
                            if (mCode == 401) {
                                Utils.Log(TAG, "code $mCode")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, mObject?.toJson())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    /*Email Verify*/
    fun onSendMail(request: EmailToken) {
        Utils.Log(TAG, "onSendMail.....")
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("Null", EnumStatus.SEND_EMAIL)
            return
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.SEND_EMAIL)
            return
        }
        if (subscriptions == null) {
            return
        }
        val response: Call<ResponseBody>? = SuperSafeApplication.serviceGraphMicrosoft?.onSendMail(request.access_token, request)
        response?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val code = response.code()
                    if (code == 401) {
                        Utils.Log(TAG, "code $code")
                        onRefreshEmailToken(request)
                        val errorMessage = response.errorBody()?.string()
                        Utils.Log(TAG, "error$errorMessage")
                        //view.onError(errorMessage, EnumStatus.SEND_EMAIL);
                    } else if (code == 202) {
                        Utils.Log(TAG, "code $code")
                        view.onSuccessful("Sent email successful", EnumStatus.SEND_EMAIL)
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

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                Utils.Log(TAG, "response failed :" + t?.message)
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
        hash[getString(R.string.key_client_id)] = request.client_id
        hash[getString(R.string.key_redirect_uri)] = request.redirect_uri
        hash[getString(R.string.key_grant_type)] = request.grant_type
        hash[getString(R.string.key_refresh_token)] = request.refresh_token
        SuperSafeApplication.serviceGraphMicrosoft?.onRefreshEmailToken(ApiService.REFRESH_TOKEN, hash)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: EmailToken? ->
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
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val mBody: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val mCode = (throwable as HttpException?)?.response()?.code()
                        try {
                            val mMessage = mBody?.string()
                            if (mCode == 401) {
                                Utils.Log(TAG, "code $mCode")
                            }
                            Utils.Log(TAG, "Error $mMessage")
                            view.onError(mMessage, EnumStatus.SEND_EMAIL)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    private fun onAddEmailToken() {
        Utils.Log(TAG, "onSignIn.....")
        val view: BaseView<EmptyModel> = view() ?: return
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
                    val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.UNLOCK_ALBUMS) }
                    if (emailToken != null) {
                        onSendMail(emailToken)
                    }
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val mBody: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val mCode = (throwable as HttpException?)?.response()?.code()
                        try {
                            val mMessage = mBody?.string()
                            val mObject = mMessage?.toObject(BaseResponse::class.java)
                            if (mCode == 403) {
                                Utils.Log(TAG, "Code $mCode")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, "Error ${mObject?.toJson()}")
                            view.onError(mObject?.toJson(), EnumStatus.ADD_EMAIL_TOKEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    fun onRequestCode(request: VerifyCodeRequest?) {
        Utils.Log(TAG, "info")
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("View is null", EnumStatus.REQUEST_CODE)
            return
        }
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.REQUEST_CODE)
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onResendCode(RequestCodeRequest(request?.user_id, Utils.getAccessToken(), SuperSafeApplication.getInstance().getDeviceId()))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe(Consumer<Disposable> { waiting: Disposable -> view.onStartLoading(EnumStatus.REQUEST_CODE) })
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.REQUEST_CODE)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        val mData: DataResponse? = onResponse.data
                        mUser?.code = mData?.requestCode?.code
                        Utils.setUserPreShare(mUser)
                        val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.UNLOCK_ALBUMS) }
                        if (emailToken != null) {
                            onSendMail(emailToken)
                        }
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.REQUEST_CODE)
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val mBody: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val mCode = (throwable as HttpException?)?.response()?.code()
                        try {
                            val mMessage = mBody?.string()
                            val mObject = mMessage?.toObject(BaseResponse::class.java)
                            if (mCode == 401) {
                                Utils.Log(TAG, "Code $mCode")
                                ServiceManager.getInstance()?.onUpdatedUserToken()
                            }
                            Utils.Log(TAG, mObject?.toJson())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    companion object {
        private val TAG = UnlockAllAlbumPresenter::class.java.simpleName
    }

}