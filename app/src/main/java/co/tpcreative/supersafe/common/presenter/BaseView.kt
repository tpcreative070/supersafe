package co.tpcreative.supersafe.common.presenter
import android.app.Activity
import android.content.Context
import co.tpcreative.supersafe.model.EnumStatus

interface BaseView<T> {
    open fun onStartLoading(status: EnumStatus)
    open fun onStopLoading(status: EnumStatus)
    open fun onError(message: String?)
    open fun onError(message: String?, status: EnumStatus?)
    open fun onSuccessful(message: String?)
    open fun onSuccessful(message: String?, status: EnumStatus?)
    open fun onSuccessful(message: String?, status: EnumStatus?, `object`: T?)
    open fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<T?>?)
    open fun getContext(): Context?
    open fun getActivity(): Activity?
}