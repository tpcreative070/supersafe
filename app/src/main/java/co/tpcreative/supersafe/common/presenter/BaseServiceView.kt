package co.tpcreative.supersafe.common.presenter
import android.content.Context
import co.tpcreative.supersafe.model.EnumStatus

interface BaseServiceView<T> {
    fun onError(message: String?, status: EnumStatus)
    fun onSuccessful(message: String?, status: EnumStatus)
    fun getContext(): Context?
}