package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.model.ItemModel
import java.io.Serializable

class SyncItemModel(itemModel: ItemModel, isOriginalGlobalId: Boolean) : Serializable {
    var id: Int
    var originalName: String?
    var thumbnailName: String?
    var isSyncCloud: Boolean
    var isSyncOwnServer: Boolean
    var global_original_id: String?
    var global_thumbnail_id: String?
    var categories_local_id: String?
    var categories_id: String?
    var formatType: Int
    var thumbnailPath: String?
    var originalPath: String?
    var mimeType: String?
    var fileExtension: String?
    var statusAction: Int
    var items_id: String?
    var degrees: Int
    var thumbnailSync: Boolean
    var originalSync: Boolean
    var fileType: Int
    var title: String?
    var size: String?
    var isDeleteLocal: Boolean
    var isDeleteGlobal: Boolean
    var statusProgress: Int
    var deleteAction: Int
    var isFakePin: Boolean
    var isSaver: Boolean
    var isExport: Boolean
    var isWaitingForExporting: Boolean
    var custom_items: Int
    var isUpdate: Boolean

    /*Added more condition fields*/
    var global_id: String? = null
    var isOriginalGlobalId: Boolean

    init {
        id = itemModel.id
        originalName = itemModel.originalName
        thumbnailName = itemModel.thumbnailName
        isSyncCloud = itemModel.isSyncCloud
        isSyncOwnServer = itemModel.isSyncOwnServer
        global_original_id = itemModel.global_original_id
        global_thumbnail_id = itemModel.global_thumbnail_id
        categories_local_id = itemModel.categories_local_id
        categories_id = itemModel.categories_id
        formatType = itemModel.formatType
        thumbnailPath = itemModel.thumbnailPath
        originalPath = itemModel.originalPath
        mimeType = itemModel.mimeType
        fileExtension = itemModel.fileExtension
        statusAction = itemModel.statusAction
        items_id = itemModel.items_id
        degrees = itemModel.degrees
        thumbnailSync = itemModel.thumbnailSync
        originalSync = itemModel.originalSync
        fileType = itemModel.fileType
        title = itemModel.title
        size = itemModel.size
        isDeleteLocal = itemModel.isDeleteLocal
        isDeleteGlobal = itemModel.isDeleteGlobal
        statusProgress = itemModel.statusProgress
        deleteAction = itemModel.deleteAction
        isFakePin = itemModel.isFakePin
        isSaver = itemModel.isSaver
        isExport = itemModel.isExport
        isWaitingForExporting = itemModel.isWaitingForExporting
        custom_items = itemModel.custom_items
        isUpdate = itemModel.isUpdate

        /*Added more condition fields*/global_id = if (isOriginalGlobalId) {
            itemModel.global_original_id
        } else {
            itemModel.global_thumbnail_id
        }
        this.isOriginalGlobalId = isOriginalGlobalId
    }
}