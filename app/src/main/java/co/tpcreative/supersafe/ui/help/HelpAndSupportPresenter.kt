package co.tpcreative.supersafe.ui.help
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.requestimport.OutlookMailRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.*
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

class HelpAndSupportPresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<HelpAndSupport>? = ArrayList<HelpAndSupport>()
    var content: HelpAndSupport?
    fun onGetList() {
        mList?.clear()
        var categories = Categories(0, getString(R.string.faq))
        mList?.add(HelpAndSupport(categories, getString(R.string.i_have_a_new_phone), getString(R.string.i_have_a_new_phone_content), null))
        mList?.add(HelpAndSupport(categories, getString(R.string.what_about_google_drive), getString(R.string.what_about_google_drive_content), null))
        mList?.add(HelpAndSupport(categories, getString(R.string.how_do_export_my_files), getString(R.string.how_do_export_my_files_content), null))
        mList?.add(HelpAndSupport(categories, getString(R.string.how_do_i_recover_items_from_trash), getString(R.string.how_do_i_recover_items_from_trash_content), null))
        mList?.add(HelpAndSupport(categories, getString(R.string.i_forgot_the_password_how_to_unlock_my_albums), getString(R.string.i_forgot_the_password_how_to_unlock_my_albums_content), null))
        mList?.add(HelpAndSupport(categories, getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it), getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it_content), null))
        categories = Categories(1, getString(R.string.contact_support))
        mList?.add(HelpAndSupport(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null))
    }

    fun onGetDataIntent(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        try {
            val bundle: Bundle? = activity?.getIntent()?.getExtras()
            content = bundle?.get(HelpAndSupport::class.java.getSimpleName()) as HelpAndSupport
            view?.onSuccessful("Successful", EnumStatus.RELOAD)
            Utils.Log(TAG, Gson().toJson(content))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getString(res: Int): String {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)!!
    }

    fun onSendMail(request: EmailToken, content: String?) {
        Utils.Log(TAG, "onSendMail.....")
        val view: BaseView<EmptyModel>? = view() ?: return
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
                        onRefreshEmailToken(request, content)
                        val errorMessage = response.errorBody()?.string()
                        Utils.Log(TAG, "error$errorMessage")
                        view?.onError(errorMessage, EnumStatus.SEND_EMAIL)
                    } else if (code == 202) {
                        Utils.Log(TAG, "code $code")
                        view?.onSuccessful("Successful", EnumStatus.SEND_EMAIL)
                        Utils.Log(TAG, "Body : Send email Successful")
                    } else {
                        Utils.Log(TAG, "code $code")
                        Utils.Log(TAG, "Nothing to do")
                        view?.onError("Null", EnumStatus.SEND_EMAIL)
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

    fun onRefreshEmailToken(request: EmailToken, content: String?) {
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
        SuperSafeApplication.serviceGraphMicrosoft?.onRefreshEmailToken(ApiService.REFRESH_TOKEN, hash)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ onResponse: EmailToken ->
                    if (onResponse != null) {
                        val token: EmailToken? = mUser?.email_token
                        token?.access_token = onResponse?.token_type + " " + onResponse.access_token
                        token?.refresh_token = onResponse.refresh_token
                        token?.token_type = onResponse.token_type
                        Utils.setUserPreShare(mUser)
                        onAddEmailToken(content)
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

    fun onAddEmailToken(content: String?) {
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
                    val emailToken: EmailToken? = EmailToken.getInstance()?.convertTextObject(mUser!!, content!!)
                    if (emailToken != null) {
                        onSendMail(emailToken, content)
                    }
                }, { throwable: Throwable ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        val code = (throwable as HttpException?)?.response()?.code()
                        try {
                            if (code == 403) {
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

    companion object {
        private val TAG = HelpAndSupportPresenter::class.java.simpleName
    }

    init {
        content = HelpAndSupport()
    }
}