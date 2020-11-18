package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import co.tpcreative.supersafe.model.ItemModel

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

    init {

    }

}