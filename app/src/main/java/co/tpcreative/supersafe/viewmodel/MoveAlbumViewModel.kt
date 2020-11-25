package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.Comparator

class MoveAlbumViewModel : BaseViewModel<GalleryAlbum>() {

    val TAG = this::class.java.simpleName
    override val photos: MutableLiveData<Int>
        get() = super.photos

    override val videos: MutableLiveData<Int>
        get() = super.videos

    override val audios: MutableLiveData<Int>
        get() = super.audios

    override val others: MutableLiveData<Int>
        get() = super.others

    fun getData(categories_local_id: String?, isFakePIN: Boolean)  = liveData(Dispatchers.Main){
        dataList.clear()
        val list: MutableList<MainCategoryModel>? = SQLHelper.getListMoveGallery(categories_local_id, isFakePIN)
        list?.let {
            if (isFakePIN) {
                val main: MainCategoryModel? = SQLHelper.getMainItemFakePin()
                if (main != null) {
                    if (main.categories_local_id != categories_local_id) {
                        it.add(main)
                        list.sortWith(Comparator { lhs, rhs ->
                            lhs.categories_max.toInt() - rhs.categories_max.toInt()
                        })
                    }
                }
            }
            for (index in list) {
                val mListItem: MutableList<ItemModel>? = SQLHelper.getListItems(index.categories_local_id, false, isFakePIN)
                var photos = 0
                var videos = 0
                var audios = 0
                var others = 0
                if (mListItem != null) {
                    for (i in mListItem) {
                        when (EnumFormatType.values()[i.formatType]) {
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
                }
                dataList.add(GalleryAlbum(index, photos, videos, audios, others))
            }
        }
        emit(dataList)
    }


    fun onMoveItemsToAlbum(position: Int, dataRequest : MutableList<ItemModel>) = liveData(Dispatchers.Main) {
        val gallery: GalleryAlbum = dataList[position]
        for (i in dataRequest.indices) {
            val item: ItemModel = dataRequest[i]
            if (item.isChecked) {
                item.categories_local_id = gallery.main?.categories_local_id
                item.categories_id = gallery.main?.categories_id
                if (item.isSyncCloud && item.isSyncOwnServer) {
                    item.isUpdate = true
                }
                SQLHelper.updatedItem(item)
            }
        }
        emit(true)
    }
}