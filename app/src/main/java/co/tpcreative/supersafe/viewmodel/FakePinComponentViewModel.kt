package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.extension.deleteDirectory
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import kotlinx.coroutines.Dispatchers

class FakePinComponentViewModel : BaseViewModel<MainCategoryModel>() {

    override val dataList: MutableList<MainCategoryModel>
        get() = super.dataList

    fun getData() = liveData(Dispatchers.Main){
        try {
            dataList.clear()
            SQLHelper.getListFakePin()?.let {
                dataList.addAll(it)
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDeleteAlbum(position: Int)  = liveData(Dispatchers.Main){
        try {
            val main: MainCategoryModel = dataList[position]
            val mListItems: MutableList<ItemModel>? = SQLHelper.getListItems(main.categories_local_id, true)
            if (mListItems != null) {
                for (index in mListItems) {
                    SQLHelper.deleteItem(index)
                    val itemId = SuperSafeApplication.getInstance().getSuperSafePrivate() + index.items_id
                    itemId.deleteDirectory()
                }
            }
            SQLHelper.deleteCategory(main)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
           emit(true)
        }
    }
}