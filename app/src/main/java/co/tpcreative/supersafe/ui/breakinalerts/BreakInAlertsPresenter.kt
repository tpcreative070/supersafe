package co.tpcreative.supersafe.ui.breakinalerts
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.BreakInAlertsModel
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import com.google.gson.Gson
import java.util.*

class BreakInAlertsPresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<BreakInAlertsModel>?
    fun onGetData() {
        val view: BaseView<EmptyModel>? = view()
        mList = SQLHelper.getBreakInAlertsList()
        mList?.sortByDescending {
            it.id
        }
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
        Utils.Log(TAG, "Result :" + Gson().toJson(mList))
    }

    fun onDeleteAll() {
        val view: BaseView<EmptyModel>? = view()
        for (index in mList!!) {
            Utils.onDeleteFile(index.fileName)
            SQLHelper.onDelete(index)
        }
        view?.onSuccessful("Deleted successful", EnumStatus.DELETE)
    }

    companion object {
        private val TAG = BreakInAlertsPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<BreakInAlertsModel>()
    }
}