package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumDelete
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import kotlinx.coroutines.Dispatchers

class PhotoSlideShowViewModel : BaseViewModel<ItemModel>(){

    override val dataList: MutableList<ItemModel>
        get() = super.dataList

    fun getData(activity: Activity) = liveData(Dispatchers.Main){
        try {
            dataList.clear()
            val bundle: Bundle? = activity.intent?.extras
            val mItem = bundle?.get(SuperSafeApplication.getInstance().getString(R.string.key_items)) as ItemModel
            val mList: MutableList<ItemModel> = bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_list_items)) as MutableList<ItemModel>
            val mMainCategory = bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories)) as MainCategoryModel
            mainCategoryModel = mMainCategory
            val data: MutableList<ItemModel>? = SQLHelper.getListItems(mMainCategory.categories_local_id, isDeleteLocal = false, isExport = false, mMainCategory.isFakePin)
            data?.let {
                val mResult = mList.filter {
                    val formatType = EnumFormatType.values()[it.formatType]
                    formatType != EnumFormatType.FILES
                }
                mResult.forEachIndexed { i, index ->
                    if (mItem.items_id == index.items_id){
                        position = i
                    }
                    dataList.add(index)
                }
            }
            emit(Resource.success(dataList))
        } catch (e: Exception) {
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message?:"",null))
        }
    }

    fun deleteItems(isExport : Boolean? = false) = liveData(Dispatchers.Main){
        isLoading.postValue(true)
        for (index in dataList) {
            if (index.isChecked()!!) {
                if (isExport!!){
                    if (Utils.isRequestDeletedLocal(index)){
                        SQLHelper.deleteItem(index)
                    }else{
                        index.isDeleteLocal = true
                        index.deleteAction = EnumDelete.DELETE_WAITING.ordinal
                        SQLHelper.updatedItem(index)
                    }
                    Utils.deleteFolderOfItemId(index.items_id)
                }else{
                    index.isDeleteLocal = true
                    index.let { SQLHelper.updatedItem(it) }
                }
            }
        }
        isRequestSyncData = true
        isLoading.postValue(false)
        emit(dataList)
    }

    fun getCheckedItems() = liveData(Dispatchers.Main){
        val mData : MutableList<ItemModel> = mutableListOf()
        for (index in dataList) {
            if (index.isChecked()!!) {
                mData.add(index)
            }
        }
        emit(mData)
    }
}