package co.tpcreative.supersafe.ui.premium
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toObject
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.request.CheckoutRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.User
import com.anjlab.android.iab.v3.PurchaseData
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class PremiumPresenter : Presenter<BaseView<EmptyModel>>() {
    protected var mUser: User?
    protected var mList: MutableList<ItemModel>?
    protected var spaceAvailable: Long = 0
    protected var isSaver = false
    fun onUpdatedItems() {
        if (mList == null) {
            mList = SQLHelper.getListAllItemsSaved(true, true)
            if (mList == null) {
                mList = ArrayList<ItemModel>()
            }
        }
        for (i in mList?.indices!!) {
            val index: ItemModel? = mList?.get(i)
            index?.isSaver = false
            SQLHelper.updatedItem(index!!)
        }
    }

    fun onAddCheckout(purchase: PurchaseData?) {
        val view: BaseView<EmptyModel>? = view()
        if (view == null) {
            view?.onError("no view", EnumStatus.CHECKOUT)
            return
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.CHECKOUT)
            return
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.CHECKOUT)
            return
        }
        val user: User? = Utils.getUserInfo()
        if (user == null) {
            view.onError("no user", EnumStatus.CHECKOUT)
            return
        }
        if (purchase == null) {
            return
        }
        val mCheckout = CheckoutRequest(mUser?.email, purchase.autoRenewing, purchase.orderId, purchase.productId, purchase.purchaseState.name, purchase.purchaseToken)
        Utils.onWriteLog(Gson().toJson(mCheckout), EnumStatus.CHECKOUT)
        SuperSafeApplication.serverAPI?.onCheckout(mCheckout)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { view.onStartLoading(EnumStatus.CHECKOUT) }
                ?.subscribe(Consumer subscribe@{ onResponse: RootResponse ->
                    if (onResponse.error) {
                        view.onError("Error", EnumStatus.CHECKOUT)
                    } else {
                        view.onSuccessful("Successful", EnumStatus.CHECKOUT)
                    }
                    Utils.onWriteLog(Gson().toJson(onResponse), EnumStatus.CHECKOUT)
                }, Consumer subscribe@{ throwable: Throwable? ->
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
                            Utils.Log(TAG,mObject?.toJson())
                            view.onError(mObject?.toJson(), EnumStatus.CHECKOUT)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Utils.onWriteLog("Line 2" + e.message, EnumStatus.CHECKOUT)
                            view.onError("" + e.message, EnumStatus.CHECKOUT)
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable?.message)
                        Utils.onWriteLog("Line 3 " + throwable?.message, EnumStatus.CHECKOUT)
                        view.onError("Error :" + throwable?.message, EnumStatus.CHECKOUT)
                    }
                    view.onStopLoading(EnumStatus.CHECKOUT)
                })?.let { subscriptions?.add(it) }
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    companion object {
        private val TAG = PremiumPresenter::class.java.simpleName
    }

    init {
        mUser = Utils.getUserInfo()
        mList = SQLHelper.getListAllItemsSaved(true, true)
        if (mList == null) {
            mList = ArrayList<ItemModel>()
        }
        if (mList?.size!! > 0) {
            spaceAvailable = 0
            for (i in mList?.indices!!) {
                val items: ItemModel? = mList?.get(i)
                items?.isChecked = true
                spaceAvailable += items?.size?.toLong()!!
            }
        }
        Utils.Log(TAG, Gson().toJson(mList))
    }
}