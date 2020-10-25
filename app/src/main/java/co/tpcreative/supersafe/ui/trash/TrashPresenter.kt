package co.tpcreative.supersafe.ui.trash
import android.app.Activity
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.snatik.storage.Storage
import java.util.*

class TrashPresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<ItemModel>?
    protected var storage: Storage?
    var videos = 0
    var photos = 0
    var audios = 0
    var others = 0
    fun getData(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        mList?.clear()
        try {
            val data: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
            if (data != null) {
                mList = data
                onCalculate()
            }
            Utils.Log(TAG, Gson().toJson(data))
            view?.onSuccessful("successful", EnumStatus.RELOAD)
        } catch (e: Exception) {
            Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
        }
    }

    fun onCalculate() {
        photos = 0
        videos = 0
        audios = 0
        others = 0
        for (index in mList!!) {
            val enumTypeFile = EnumFormatType.values()[index.formatType]
            when (enumTypeFile) {
                EnumFormatType.IMAGE -> {
                    photos += 1
                }
                EnumFormatType.VIDEO -> {
                    videos += 1
                }
                EnumFormatType.AUDIO -> {
                    audios += 1
                }
                EnumFormatType.FILES -> {
                    others += 1
                }
            }
        }
    }

    fun onDeleteAll(isEmpty: Boolean) {
        val view: BaseView<EmptyModel>? = view()
        for (i in mList?.indices!!) {
            if (isEmpty) {
                val formatTypeFile = EnumFormatType.values()[mList?.get(i)?.formatType!!]
                if (formatTypeFile == EnumFormatType.AUDIO && mList?.get(i)?.global_original_id == null) {
                    SQLHelper.deleteItem(mList?.get(i)!!)
                } else if (formatTypeFile == EnumFormatType.FILES && mList?.get(i)?.global_original_id == null) {
                    SQLHelper.deleteItem(mList?.get(i)!!)
                } else if ((mList?.get(i)?.global_original_id == null) and (mList?.get(i)?.global_thumbnail_id == null)) {
                    SQLHelper.deleteItem(mList?.get(i)!!)
                } else {
                    mList?.get(i)?.deleteAction = EnumDelete.DELETE_WAITING.ordinal
                    SQLHelper.updatedItem(mList?.get(i)!!)
                    Utils.Log(TAG, "ServiceManager waiting for delete")
                }
                storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mList?.get(i)?.items_id)
            } else {
                val items: ItemModel? = mList?.get(i)
                items?.isDeleteLocal = false
                if (mList?.get(i)?.isChecked!!) {
                    val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesLocalId(items?.categories_local_id)
                    if (mainCategories != null) {
                        mainCategories.isDelete = false
                        SQLHelper.updateCategory(mainCategories)
                    }
                    SQLHelper.updatedItem(items!!)
                }
            }
        }
        view?.onSuccessful("Done", EnumStatus.DONE)
    }

    companion object {
        private val TAG = TrashPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<ItemModel>()
        storage = Storage(SuperSafeApplication.getInstance())
    }
}