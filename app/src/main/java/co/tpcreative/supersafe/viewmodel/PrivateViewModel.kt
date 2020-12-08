package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.deleteDirectory
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumDelete
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import kotlinx.coroutines.Dispatchers

class PrivateViewModel : BaseViewModel<MainCategoryModel>(){
    val TAG = this::class.java.simpleName
    fun getData() = liveData(Dispatchers.Main) {
        dataList.clear()
        SQLHelper.getList().let {
            it.sortByDescending {sorted ->
                Utils.getMilliseconds(sorted.updated_date,Utils.FORMAT_SERVER_DATE_TIME)
            }
            for (index in it){
                if (index.categories_hex_name == SQLHelper.main_hex){
                    dataList.add(0,index)
                }else{
                    dataList.add(index)
                }
            }
        }
        emit(dataList)
    }

    fun onDeleteAlbum(position: Int)  = liveData(Dispatchers.Main){
        try {
            val main: MainCategoryModel = dataList[position]
            main.let {
                val mListItems: MutableList<ItemModel>? = SQLHelper.getListItems(it.categories_local_id, false)
                mListItems?.let {item ->
                    for (index in item) {
                        index.isDeleteLocal = true
                        SQLHelper.updatedItem(index)
                    }
                    it.isDelete = true
                    SQLHelper.updateCategory(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ServiceManager.getInstance()?.onPreparingSyncData()
            emit(dataList)
        }
    }

    fun onEmptyTrash() = liveData(Dispatchers.Main){
        try {
            val mList: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
            mList?.let {
                for (index in it) {
                    val formatTypeFile = EnumFormatType.values()[index.formatType]
                    if (formatTypeFile == EnumFormatType.AUDIO && index.global_original_id == null) {
                        SQLHelper.deleteItem(index)
                    } else if (formatTypeFile == EnumFormatType.FILES && index.global_original_id == null) {
                        SQLHelper.deleteItem(index)
                    } else if ((index.global_original_id == null) and (index.global_thumbnail_id == null)) {
                        SQLHelper.deleteItem(index)
                    } else {
                        index.deleteAction = EnumDelete.DELETE_WAITING.ordinal
                        SQLHelper.updatedItem(index)
                        Utils.Log(TAG, "ServiceManager waiting for delete")
                    }
                    val itemId = SuperSafeApplication.getInstance().getSuperSafePrivate() + index.items_id
                    itemId.deleteDirectory()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            ServiceManager.getInstance()?.onPreparingSyncData()
            emit(dataList)
        }
    }
}