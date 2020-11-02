package co.tpcreative.supersafe.ui.signup
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toObject
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.SignUpRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

class SignUpPresenter : Presenter<BaseView<User>>() {
    fun onSignUp(request: SignUpRequest) {
        Utils.Log(TAG, "info onSignUp")
        val view: BaseView<User>? = view() ?: return
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            return
        }
        if (subscriptions == null) {
            return
        }
        SuperSafeApplication.serverAPI?.onSignUP(request)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.SIGN_UP) }
                ?.subscribe({ onResponse: RootResponse ->
                    view.onStopLoading(EnumStatus.SIGN_UP)
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.SIGN_UP)
                    } else {
                        val mData: DataResponse? = onResponse.data
                        Utils.setUserPreShare(mData?.user)
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.SIGN_UP, mData?.user)
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                    }
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                }, { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val mBody: ResponseBody? = (throwable as HttpException?)?.response()?.errorBody()
                        try {
                            val mMessage = mBody?.string()
                            val mObject = mMessage?.toObject(BaseResponse::class.java)
                            Utils.Log(TAG, mObject?.toJson())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable?.message)
                    }
                    view.onStopLoading(EnumStatus.SIGN_UP)
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<User>? = view()
        return view?.getContext()?.getString(res)
    }

    companion object {
        private val TAG = SignUpPresenter::class.java.simpleName
    }
}