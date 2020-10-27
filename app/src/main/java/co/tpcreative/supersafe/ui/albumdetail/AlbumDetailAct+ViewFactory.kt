package co.tpcreative.supersafe.ui.albumdetail
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import kotlinx.android.synthetic.main.footer_items_detail_album.*

fun AlbumDetailAct.initUI(){
    TAG = this::class.java.simpleName
    imgShare.setOnClickListener {
        if (countSelected > 0) {
            storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafeShare())
            presenter?.status = EnumStatus.SHARE
            onShowDialog(presenter?.status)
        }
    }

    imgExport.setOnClickListener {
        onExport()
    }

    imgDelete.setOnClickListener {
        if (countSelected > 0) {
            presenter?.status = EnumStatus.DELETE
            onShowDialog(presenter?.status)
        }
    }

   imgMove.setOnClickListener {
       openAlbum()
   }
}

fun AlbumDetailAct.onExport(){
    if (countSelected > 0) {
        storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture())
        presenter?.status = EnumStatus.EXPORT
        var isSaver = false
        var spaceAvailable: Long = 0
        for (i in presenter?.mList?.indices!!) {
            val items: ItemModel? = presenter?.mList?.get(i)
            if (items?.isSaver!! && items?.isChecked) {
                isSaver = true
                spaceAvailable += items.size?.toLong()!!
            }
        }
        val availableSpaceOS: Long = Utils.getAvailableSpaceInBytes()
        if (availableSpaceOS < spaceAvailable) {
            val request_spaces = spaceAvailable - availableSpaceOS
            val result: String? = ConvertUtils.byte2FitMemorySize(request_spaces)
            val message: String = kotlin.String.format(getString(R.string.your_space_is_not_enough_to), "export. ", "Request spaces: $result")
            Utils.showDialog(this, message)
        } else {
            if (isSaver) {
                onEnableSyncData()
            } else {
                onShowDialog(presenter?.status)
            }
        }
    }
}