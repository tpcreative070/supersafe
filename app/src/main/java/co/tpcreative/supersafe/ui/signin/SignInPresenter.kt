package co.tpcreative.supersafe.ui.signin
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RootAPI
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.requestimport.OutlookMailRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EmailToken
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*

class SignInPresenter : Presenter<BaseView<User>>() {
    fun onSignIn(request: SignInRequest) {
        Utils.Log(TAG, "onSignIn " + Gson().toJson(request))
        val view: BaseView<User>? = view() ?: return
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onSignIn(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe({ view.onStartLoading(EnumStatus.SIGN_IN) })
                ?.subscribe({ onResponse: RootResponse ->
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view.onError(getString(R.string.signed_in_failed), EnumStatus.SIGN_IN)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        Utils.setUserPreShare(mData?.user)
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                        view?.onSuccessful(onResponse.message, EnumStatus.SIGN_IN, mData?.user)
                    }
                }, { throwable: Throwable? ->
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
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                    }
                    view.onStopLoading(EnumStatus.OTHER)
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<User>? = view()
        return view?.getContext()?.getString(res)
    }

    /*Email token*/
    fun onSendMail(request: EmailToken) {
        Utils.Log(TAG, "onSendMail....." + Gson().toJson(request))
        val view: BaseView<User> = view() ?: return
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
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
                        view.onError(errorMessage, EnumStatus.SEND_EMAIL)
                    } else if (code == 202) {
                        Utils.Log(TAG, "code $code")
                        view.onSuccessful("successful", EnumStatus.SEND_EMAIL)
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
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.Companion.getInstance())) {
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
        Utils.Log(TAG, "Refresh token : " + Gson().toJson(hash))
        SuperSafeApplication.Companion.serviceGraphMicrosoft?.onRefreshEmailToken(RootAPI.REFRESH_TOKEN, hash)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: EmailToken? ->
                    if (onResponse != null) {
                        val token: EmailToken? = mUser?.email_token
                        token?.access_token = onResponse?.token_type + " " + onResponse.access_token
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
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.Companion.getInstance())) {
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

    companion object {
        private val TAG = SignInPresenter::class.java.simpleName
    }
}