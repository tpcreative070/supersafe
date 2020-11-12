package co.tpcreative.supersafe.ui.move_gallery
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import java.util.*

class MoveGalleryPresenter : Presenter<MoveGalleryView>() {
    var mList: MutableList<GalleryAlbum>? = ArrayList<GalleryAlbum>()
    protected var videos = 0
    protected var photos = 0
    protected var audios = 0
    protected var others = 0
    fun getData(categories_local_id: String?, isFakePIN: Boolean) {
        mList?.clear()
        val view: MoveGalleryView? = view()
        val list: MutableList<MainCategoryModel>? = SQLHelper.getListMoveGallery(categories_local_id, isFakePIN)
        if (isFakePIN) {
            val main: MainCategoryModel? = SQLHelper.getMainItemFakePin()
            if (main != null) {
                if (main.categories_local_id != categories_local_id) {
                    list?.add(main)
                    Collections.sort(list, Comparator { lhs, rhs ->
                        val count_1 = lhs.categories_max.toInt()
                        val count_2 = rhs.categories_max.toInt()
                        count_1 - count_2
                    })
                }
            }
        }
        if (list != null) {
            for (index in list) {
                val mListItem: MutableList<ItemModel>? = SQLHelper.getListItems(index.categories_local_id, false, isFakePIN)
                photos = 0
                videos = 0
                audios = 0
                others = 0
                if (mListItem != null) {
                    for (i in mListItem) {
                        val enumTypeFile = EnumFormatType.values()[i.formatType]
                        when (enumTypeFile) {
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
                mList?.add(GalleryAlbum(index, photos, videos, audios, others))
            }
        }
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
    }

    fun onMoveItemsToAlbum(position: Int) {
        val view: MoveGalleryView? = view()
        val gallery: GalleryAlbum? = mList?.get(position)
        val mList: MutableList<ItemModel>? = view?.getListItems()
        if (mList != null) {
            for (i in mList.indices) {
                val item: ItemModel? = mList[i]
                if (item?.isChecked!!) {
                    item.categories_local_id = gallery?.main?.categories_local_id
                    item.categories_id = gallery?.main?.categories_id
                    if (item.isSyncCloud && item.isSyncOwnServer) {
                        item.isUpdate = true
                    }
                    Utils.Log(TAG, "Warning " + item.isUpdate + "; isSyncCloud " + item.isSyncCloud + "; isSyncOwnServer " + item.isSyncOwnServer)
                    SQLHelper.updatedItem(item)
                }
            }
            ServiceManager.getInstance()?.onPreparingSyncData()
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            view.onSuccessful("Successful", EnumStatus.MOVE)
        } else {
            Utils.Log(TAG, "Nulll")
        }
    }

    companion object {
        private val TAG = MoveGalleryPresenter::class.java.simpleName
    }

}