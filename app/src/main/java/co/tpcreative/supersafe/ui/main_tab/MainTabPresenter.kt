package co.tpcreative.supersafe.ui.main_tab
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.User

class MainTabPresenter : Presenter<BaseView<EmptyModel>>() {
    var mUser: User? = null
    fun onGetUserInfo() {
        mUser = Utils.getUserInfo()
    }
    companion object {
        private val TAG = MainTabPresenter::class.java.simpleName
    }
}