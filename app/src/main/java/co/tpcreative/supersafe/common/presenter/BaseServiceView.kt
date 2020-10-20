package co.tpcreative.supersafe.common.presenter
import android.content.Context
import co.tpcreative.supersafe.model.EnumStatus

interface BaseServiceView<T> {
    open fun onError(message: String?, status: EnumStatus)
    open fun onSuccessful(message: String?, status: EnumStatus)
    open fun getContext(): Context?
}