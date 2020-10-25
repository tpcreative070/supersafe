package co.tpcreative.supersafe.ui.restore
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.User

class RestorePresenter : Presenter<BaseView<EmptyModel>>() {
    var mUser: User? = null
    fun onGetData() {
        mUser = SuperSafeApplication.getInstance().readUseSecret()
    }
    fun onRestoreData() {}
}