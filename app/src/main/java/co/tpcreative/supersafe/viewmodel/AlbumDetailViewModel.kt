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
}