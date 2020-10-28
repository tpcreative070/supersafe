package co.tpcreative.supersafe.ui.fakepin
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import com.google.gson.Gson
import com.snatik.storage.Storage
import java.util.*

class FakePinComponentPresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<MainCategoryModel>?
    protected var storage: Storage? = null
    fun getData() {
        try {
            val view: BaseView<EmptyModel>? = view()
            mList = SQLHelper.getListFakePin()
            storage = Storage(SuperSafeApplication.getInstance())
            view?.onSuccessful("Successful", EnumStatus.RELOAD)
            Utils.Log(TAG, Gson().toJson(mList))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDeleteAlbum(position: Int) {
        try {
            val main: MainCategoryModel? = mList?.get(position)
            if (main != null) {
                val mListItems: MutableList<ItemModel>? = SQLHelper.getListItems(main.categories_local_id, true)
                if (mListItems != null) {
                    for (index in mListItems) {
                        SQLHelper.deleteItem(index)
                        storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + index.items_id)
                    }
                }
                SQLHelper.deleteCategory(main)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            getData()
        }
    }

    companion object {
        private val TAG = FakePinComponentPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<MainCategoryModel>()
    }
}