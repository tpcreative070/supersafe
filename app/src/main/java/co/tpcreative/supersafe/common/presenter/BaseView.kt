package co.tpcreative.supersafe.common.presenter
import android.app.Activity
import android.content.Context
import co.tpcreative.supersafe.model.EnumStatus

interface BaseView<T> {
     fun onStartLoading(status: EnumStatus)
     fun onStopLoading(status: EnumStatus)
     fun onError(message: String?)
     fun onError(message: String?, status: EnumStatus?)
     fun onSuccessful(message: String?)
     fun onSuccessful(message: String?, status: EnumStatus?)
     fun onSuccessful(message: String?, status: EnumStatus?, `object`: T?)
     fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<T>?)
     fun getContext(): Context?
     fun getActivity(): Activity?
}