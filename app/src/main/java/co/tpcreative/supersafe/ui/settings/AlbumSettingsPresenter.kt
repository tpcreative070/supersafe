package co.tpcreative.supersafe.ui.settings
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import com.google.gson.Gson

class AlbumSettingsPresenter : Presenter<BaseView<EmptyModel>>() {
    var mMainCategories: MainCategoryModel?
    fun getData(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        val bundle: Bundle? = activity?.getIntent()?.getExtras()
        try {
            val mainCategories: MainCategoryModel? = bundle?.get(activity.getString(R.string.key_main_categories)) as MainCategoryModel
            if (mainCategories != null) {
                mMainCategories = mainCategories
                view?.onSuccessful("Successful", EnumStatus.RELOAD)
            }
            Utils.Log(TAG, Gson().toJson(mMainCategories))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getData() {
        val view: BaseView<EmptyModel>? = view()
        if (mMainCategories == null) {
            return
        }
        try {
            val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesLocalId(mMainCategories?.categories_local_id, mMainCategories?.isFakePin!!)
            Utils.Log(TAG, "Reload " + Gson().toJson(mainCategories))
            if (mainCategories != null) {
                mMainCategories = mainCategories
                view?.onSuccessful("Successful", EnumStatus.RELOAD)
            }
            Utils.Log(TAG, Gson().toJson(mMainCategories))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = AlbumSettingsPresenter::class.java.simpleName
    }

    init {
        mMainCategories = MainCategoryModel()
    }
}