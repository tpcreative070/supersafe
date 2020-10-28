package co.tpcreative.supersafe.ui.enterpin
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus

class LockScreenPresenter : Presenter<BaseView<EnumPinAction>>() {
    fun onChangeStatus(status: EnumStatus?, action: EnumPinAction?) {
        val view: BaseView<EnumPinAction>? = view()
        view?.onSuccessful("Successful", status, action)
    }
}