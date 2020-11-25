package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import kotlinx.coroutines.Dispatchers

class PlayerViewModel : BaseViewModel<ItemModel>(){
    var mItems : ItemModel? = null
    fun getData(activity: Activity) = liveData(Dispatchers.Main) {
        val bundle: Bundle? = activity.intent.extras
        try {
            val items : ItemModel = bundle?.get(activity.getString(R.string.key_items)) as ItemModel
            mainCategoryModel = bundle.get(activity.getString(R.string.key_main_categories)) as MainCategoryModel
            mItems = items
            val list: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategoryModel.categories_local_id, items.formatType, false, mainCategoryModel.isFakePin)
            if (list != null) {
                dataList.clear()
                for (index in list) {
                    if (index.items_id != items.items_id) {
                        dataList.add(index)
                    }
                }
                mItems?.isChecked = true
                mItems?.let {
                    dataList.add(0,it)
                }
            }
            emit(dataList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}