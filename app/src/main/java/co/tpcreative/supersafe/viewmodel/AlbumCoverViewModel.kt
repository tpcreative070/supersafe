package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers

class AlbumCoverViewModel : BaseViewModel<AlbumCoverModel>() {

    fun getData(activity: Activity?)  = liveData(Dispatchers.Main) {
        val bundle: Bundle? = activity?.intent?.extras
        try {
            val mainCategories: MainCategoryModel = bundle?.get(MainCategoryModel::class.java.simpleName) as MainCategoryModel
            mainCategoryModel = mainCategories
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(mainCategoryModel)
    }

    fun getData(isCustom : Boolean) = liveData(Dispatchers.Main){
        dataList.clear()
        var isChecked = false
        val oldMainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(mainCategoryModel.mainCategories_Local_Id)
        val mCategoryList = SQLHelper.getCategoriesDefault()
        for (index in mCategoryList) {
            if (oldMainCategories?.mainCategories_Local_Id == index.mainCategories_Local_Id) {
                index.isChecked = true
                isChecked = true
            }
            dataList.add(AlbumCoverModel(null,index,EnumTypeObject.CATEGORY))
        }

        if (isCustom){
            val data: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategoryModel.categories_local_id, EnumFormatType.IMAGE.ordinal, false, mainCategoryModel.isFakePin)
            if (data != null) {
                val oldItem: ItemModel? = SQLHelper.getItemId(mainCategoryModel.items_id)
                for (index in data) {
                    if (oldItem?.items_id == index.items_id && !isChecked) {
                        index.isChecked = true
                        dataList.add(0,AlbumCoverModel(index,null,EnumTypeObject.ITEM))
                    }else{
                        dataList.add(AlbumCoverModel(index,null,EnumTypeObject.ITEM))
                    }
                }
            }
        }
        emit(dataList)
    }
}