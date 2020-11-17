package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers

class TrashViewModel : BaseViewModel<ItemModel>(){
    override val videos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    override val photos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    override val audios : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    override val others : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    override val count : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    override val dataList: MutableList<ItemModel>
        get() = super.dataList

    init {

    }

    fun getData() = liveData(Dispatchers.IO){
        try {
            dataList.clear()
            val data: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
            if (data != null) {
                dataList.addAll(data)
                onCalculate()
            }
            emit(Resource.success(data))
        } catch (e: Exception) {
            Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message?:"",null))
        }
    }

    private fun onCalculate() {
        var photos = 0
        var videos = 0
        var audios = 0
        var others = 0
        for (index in dataList) {
            when (EnumFormatType.values()[index.formatType]) {
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
        this.photos.postValue(photos)
        this.videos.postValue(videos)
        this.audios.postValue(audios)
        this.others.postValue(others)
    }

    fun onDeleteAll(isEmpty: Boolean) = liveData(Dispatchers.IO) {
        isLoading.postValue(true)
        for (i in dataList.indices) {
            if (isEmpty) {
                val formatTypeFile = EnumFormatType.values()[dataList[i].formatType]
                if (formatTypeFile == EnumFormatType.AUDIO && dataList[i].global_original_id == null) {
                    SQLHelper.deleteItem(dataList.get(i))
                } else if (formatTypeFile == EnumFormatType.FILES && dataList[i].global_original_id == null) {
                    SQLHelper.deleteItem(dataList.get(i))
                } else if ((dataList[i].global_original_id == null) and (dataList[i].global_thumbnail_id == null)) {
                    SQLHelper.deleteItem(dataList[i])
                } else {
                    dataList[i].deleteAction = EnumDelete.DELETE_WAITING.ordinal
                    SQLHelper.updatedItem(dataList.get(i))
                    Utils.Log(TAG, "ServiceManager waiting for delete")
                }
                Utils.deleteFolderOfItemId(SuperSafeApplication.getInstance().getSuperSafePrivate() + dataList[i].items_id)
            } else {
                val items: ItemModel? = dataList[i]
                items?.isDeleteLocal = false
                if (dataList[i].isChecked) {
                    val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesLocalId(items?.categories_local_id)
                    if (mainCategories != null) {
                        mainCategories.isDelete = false
                        SQLHelper.updateCategory(mainCategories)
                    }
                    SQLHelper.updatedItem(items!!)
                }
            }
        }
        emit(dataList)
        isLoading.postValue(false)
    }
    companion object {
        private val TAG = TrashViewModel::class.java.simpleName
    }
}