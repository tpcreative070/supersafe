package co.tpcreative.supersafe.ui.enablecloud
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.UserCloudRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

class EnableCloudPresenter : Presenter<BaseView<EmptyModel>>() {
    var mUser: User?
    fun onUserInfo() {
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            mUser = user
        }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    fun onAddUserCloud(cloudRequest: UserCloudRequest?) {
        Utils.Log(TAG, "info")
        val view: BaseView<*> = view() ?: return
        if (view.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
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
                    Utils.Log(TAG, "Body : " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.CREATE)
                    } else {
                        view.onSuccessful(mUser?.cloud_id, EnumStatus.CREATE)
                    }
                    view.onStopLoading(EnumStatus.CREATE)
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
                    view.onStopLoading(EnumStatus.CREATE)
                })?.let { subscriptions?.add(it) }
    }

    companion object {
        private val TAG = EnableCloudPresenter::class.java.simpleName
    }

    init {
        mUser = User()
    }
}