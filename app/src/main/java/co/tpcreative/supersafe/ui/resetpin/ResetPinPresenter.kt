package co.tpcreative.supersafe.ui.resetpin
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toObject
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class ResetPinPresenter : Presenter<BaseView<EmptyModel>>() {
    var mUser: User?
    fun onVerifyCode(request: VerifyCodeRequest?) {
        Utils.Log(TAG, "info")
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("View is null", EnumStatus.VERIFY)
            return
        }
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError("NO internet", EnumStatus.VERIFY)
            return
        }
        if (subscriptions == null) {
            return
        }
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[getString(R.string.key_user_id)] = request?.user_id
        hash[getString(R.string.key_id)] = request?._id
        hash[getString(R.string.key_device_id)] = SuperSafeApplication.getInstance().getDeviceId()
        hash[getString(R.string.key_code)] = request?.code
        hash[getString(R.string.key_appVersionRelease)] = SuperSafeApplication.getInstance().getAppVersionRelease()
        SuperSafeApplication.serverAPI?.onVerifyCode(request!!)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe({ waiting: Disposable -> view.onStartLoading(EnumStatus.VERIFY) })
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(getString(R.string.the_code_not_signed_up), EnumStatus.VERIFY_CODE)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            mUser.verified = true
                            Utils.setUserPreShare(mUser)
                        }
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.VERIFY)
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

    fun onRequestCode(request: VerifyCodeRequest?) {
        Utils.Log(TAG, "info")
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("View is null", EnumStatus.REQUEST_CODE)
            return
        }
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError("NO internet", EnumStatus.REQUEST_CODE)
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onResendCode(RequestCodeRequest(request?.user_id, Utils.getAccessToken(), SuperSafeApplication.getInstance().getDeviceId()))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.OTHER) }
                ?.subscribe({ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.REQUEST_CODE)
                    } else {
                        val mUser: User? = Utils.getUserInfo()
                        val mData: DataResponse? = onResponse.data
                        mUser?.code = mData?.requestCode?.code
                        this.mUser = mUser
                        Utils.setUserPreShare(mUser)
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.REQUEST_CODE)
                        Utils.Log(TAG, Gson().toJson(mUser))
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
                            view.onError(mObject?.toJson(), EnumStatus.REQUEST_CODE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable?.message)
                    }
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        return SuperSafeApplication.getInstance().getString(res)
    }

    companion object {
        private val TAG = ResetPinPresenter::class.java.simpleName
    }

    init {
        mUser = User()
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            mUser = user
        } else {
            mUser = SuperSafeApplication.getInstance().readUseSecret()
            Utils.setUserPreShare(mUser)
        }
    }
}