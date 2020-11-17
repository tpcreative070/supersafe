package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers
import java.util.ArrayList

class TrashViewModel : BaseViewModel(){
    var mList: MutableList<ItemModel>?
    val videos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val photos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val audios : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val others : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val count : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    init {
        mList = ArrayList<ItemModel>()
    }

    fun getData() = liveData(Dispatchers.IO){
        mList?.clear()
        try {
            val data: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
            if (data != null) {
                mList = data
                onCalculate()
            }
            emit(Resource.success(mList))
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
        for (index in mList!!) {
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
                Utils.deleteFolderOfItemId(SuperSafeApplication.getInstance().getSuperSafePrivate() + mList?.get(i)?.items_id)
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
        emit(mList)
        isLoading.postValue(false)
    }
    companion object {
        private val TAG = TrashViewModel::class.java.simpleName
    }
}