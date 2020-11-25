package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import kotlinx.coroutines.Dispatchers

class MeViewModel : BaseViewModel<EmptyModel>(){
    override val photos: MutableLiveData<Int>
        get() = super.photos

    override val videos: MutableLiveData<Int>
        get() = super.videos

    override val audios: MutableLiveData<Int>
        get() = super.audios

    override val others: MutableLiveData<Int>
        get() = super.others

    fun getData()  = liveData(Dispatchers.Main){
        var mPhotos = 0
        var mVideos = 0
        var mAudios = 0
        var mOthers = 0
        val mList: MutableList<ItemModel>? = SQLHelper.getListAllItems(isDeleteLocal = false, isFakePin = false)
        mList?.let {
            for (index in it) {
                when (EnumFormatType.values()[index.formatType]) {
                    EnumFormatType.IMAGE -> {
                        mPhotos += 1
                    }
                    EnumFormatType.VIDEO -> {
                        mVideos += 1
                    }
                    EnumFormatType.AUDIO -> {
                        mAudios += 1
                    }
                    EnumFormatType.FILES -> {
                        mOthers += 1
                    }
                }
            }
            photos.postValue(mPhotos)
            videos.postValue(mVideos)
            audios.postValue(mAudios)
            others.postValue(mOthers)
        }
        emit(true)
    }
}