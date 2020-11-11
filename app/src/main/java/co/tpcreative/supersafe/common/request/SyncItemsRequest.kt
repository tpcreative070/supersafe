package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.DriveEvent
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import java.io.Serializable

class SyncItemsRequest : Serializable {
    var user_id: String?
    var device_id: String?
    var size: Long = 0
    var cloud_id: String?
    var kind: String? = null
    var items_id: String? = null
    var name: String? = null
    var mimeType: String? = null
    var global_original_id: String? = null
    var global_thumbnail_id: String? = null
    var categories_id: String? = null
    var isSyncCloud = false
    var isSyncOwnServer = false
    var thumbnailSync = false
    var originalSync = false
    var isDeleteLocal = false
    var isDeleteGlobal = false
    var originalName: String? = null
    var thumbnailName: String? = null
    var formatType = 0
    var thumbnailPath: String? = null
    var originalPath: String? = null
    var fileExtension: String? = null
    var statusAction = 0
    var degrees = 0
    var fileType = 0
    var title: String? = null
    var nextPage: String? = null

    /*Sync items*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?, items: ItemModel) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
        size = items.size!!.toLong()
        kind = SuperSafeApplication.getInstance().getString(R.string.key_drive_file)
        items_id = items.items_id
        mimeType = items.mimeType
        global_original_id = items.global_original_id
        global_thumbnail_id = items.global_thumbnail_id
        categories_id = items.categories_id
        isSyncCloud = items.isSyncCloud
        isSyncOwnServer = items.isSyncOwnServer
        thumbnailSync = items.thumbnailSync
        originalSync = items.originalSync
        isDeleteLocal = items.isDeleteLocal
        isDeleteGlobal = items.isDeleteGlobal
        originalName = items.originalName
        thumbnailName = items.thumbnailName
        formatType = items.formatType
        thumbnailPath = items.getThumbnail()
        originalPath = items.getOriginal()
        fileExtension = items.fileExtension
        statusAction = items.statusAction
        degrees = items.degrees
        fileType = items.fileType
        title = items.title
        val contentTitle = DriveEvent()
        contentTitle.items_id = items.items_id
        val hex: String? = DriveEvent.getInstance()?.convertToHex(Gson().toJson(contentTitle))
        name = hex
    }

    /*Delete item*/
    constructor(user_id: String?, cloud_id: String?, items_id: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.items_id = items_id
        device_id = SuperSafeApplication.getInstance().getDeviceId()
    }

    /*Delete item from system*/
    constructor(item: ItemModel?) {
        this.user_id = Utils.getUserId()
        this.cloud_id = Utils.getUserCloudId()
        this.items_id = item?.items_id
        device_id = SuperSafeApplication.getInstance().getDeviceId()
    }

    /*Get items list*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?, isSyncCloud: Boolean, nextPage: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
        this.isSyncCloud = isSyncCloud
        this.nextPage = nextPage
    }

    /*Get Item List*/
    constructor(nextPage: String? = "0"){
        val mUser = Utils.getUserInfo()
        this.user_id = mUser?.email
        this.cloud_id = mUser?.cloud_id
        this.device_id = SuperSafeApplication.getInstance().getDeviceId()
        this.isSyncCloud = true
        this.nextPage = nextPage
    }
}