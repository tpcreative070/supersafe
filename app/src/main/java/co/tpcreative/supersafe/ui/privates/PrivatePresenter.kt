package co.tpcreative.supersafe.ui.privates
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.snatik.storage.Storage
import java.util.*

class PrivatePresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<MainCategoryModel>?
    protected var storage: Storage? = null
    fun getData() {
        val view: BaseView<EmptyModel>? = view()
        mList = SQLHelper.getList()
        storage = Storage(SuperSafeApplication.getInstance())
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
    }

    fun onDeleteAlbum(position: Int) {
        try {
            val main: MainCategoryModel? = mList?.get(position)
            if (main != null) {
                val mListItems: MutableList<ItemModel>? = SQLHelper.getListItems(main.categories_local_id, false)
                if (mListItems != null) {
                    for (index in mListItems) {
                        index.isDeleteLocal = true
                        SQLHelper.updatedItem(index)
                    }
                }
                main.isDelete = true
                SQLHelper.updateCategory(main)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            getData()
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    fun onEmptyTrash() {
        try {
            val mList: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
            for (i in mList?.indices!!) {
                val formatTypeFile = EnumFormatType.values()[mList[i].formatType]
                if (formatTypeFile == EnumFormatType.AUDIO && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if (formatTypeFile == EnumFormatType.FILES && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if ((mList[i].global_original_id == null) and (mList[i].global_thumbnail_id == null)) {
                    SQLHelper.deleteItem(mList[i])
                } else {
                    mList[i].deleteAction = EnumDelete.DELETE_WAITING.ordinal
                    SQLHelper.updatedItem(mList[i])
                    Utils.Log(TAG, "ServiceManager waiting for delete")
                }
                storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mList[i].items_id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            getData()
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    companion object {
        private val TAG = PrivatePresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<MainCategoryModel>()
    }
}