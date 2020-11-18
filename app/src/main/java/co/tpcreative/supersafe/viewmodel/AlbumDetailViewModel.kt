package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers

class AlbumDetailViewModel : BaseViewModel<ItemModel>() {
    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    override val photos: MutableLiveData<Int>
        get() = super.photos

    override val videos: MutableLiveData<Int>
        get() = super.videos

    override val audios: MutableLiveData<Int>
        get() = super.audios

    override val others: MutableLiveData<Int>
        get() = super.others

    override val count : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    override val dataList: MutableList<ItemModel>
        get() = super.dataList

    override var isRequestSyncData: Boolean = false

    override val isSelectAll: MutableLiveData<Boolean>
        get() = super.isSelectAll

    fun getData(activity: Activity) = liveData(Dispatchers.Main){
        try {
            dataList.clear()
            val bundle: Bundle? = activity.intent?.extras
            val mMainCategory = bundle?.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories)) as MainCategoryModel
            val data: MutableList<ItemModel>? = SQLHelper.getListItems(mMainCategory.categories_local_id, isDeleteLocal = false, isExport = false, mMainCategory.isFakePin)
            data?.let {
                dataList.addAll(it)
                onCalculate()
            }
            emit(Resource.success(dataList))
        } catch (e: Exception) {
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message?:"",null))
        }
    }

    fun onCalculate() {
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

    fun deleteItems() = liveData(Dispatchers.Main){
        isLoading.postValue(true)
        for (i in dataList.indices) {
            if (dataList[i].isChecked()!!) {
                dataList[i].isDeleteLocal = true
                dataList[i].let { SQLHelper.updatedItem(it) }
            }
        }
        emit(dataList)
        isLoading.postValue(false)
    }
}