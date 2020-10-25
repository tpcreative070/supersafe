package co.tpcreative.supersafe.ui.theme
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import com.google.gson.Gson
import java.util.*

class ThemeSettingsPresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<ThemeApp>?
    var mThemeApp: ThemeApp? = null
    fun getData() {
        val view: BaseView<EmptyModel>? = view()
        mList = ThemeApp.getInstance()?.getList()
        mThemeApp = ThemeApp.getInstance()?.getThemeInfo()
        if (mThemeApp != null) {
            for (i in mList?.indices!!) {
                if (mThemeApp?.getId() == mList?.get(i)?.getId()) {
                    mList?.get(i)?.isCheck = true
                } else {
                    mList?.get(i)?.isCheck = false
                }
            }
        }
        Utils.Log(TAG, "Value :" + Gson().toJson(mList))
        view?.onSuccessful("Successful", EnumStatus.SHOW_DATA)
    }

    fun getDataReload() {
        val view: BaseView<EmptyModel>? = view()
        mList = ThemeApp.getInstance()?.getList()
        mThemeApp = ThemeApp.getInstance()?.getThemeInfo()
        if (mThemeApp != null) {
            for (i in mList?.indices!!) {
                if (mThemeApp?.getId() == mList?.get(i)?.getId()) {
                    mList?.get(i)?.isCheck = true
                } else {
                    mList?.get(i)?.isCheck = false
                }
            }
        }
        Utils.Log(TAG, "Value :" + Gson().toJson(mList))
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
    }

    companion object {
        private val TAG = ThemeSettingsPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<ThemeApp>()
    }
}