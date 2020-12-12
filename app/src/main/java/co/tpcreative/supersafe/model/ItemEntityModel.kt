package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.entities.ItemEntity

class ItemEntityModel {
    var id = 0
    var originalName: String? = null
    var thumbnailName: String?
    var isSyncCloud: Boolean
    var isSyncOwnServer: Boolean
    var global_original_id: String? = null
    var global_thumbnail_id: String? = null
    var categories_local_id: String? = null
    var categories_id: String? = null
    var formatType = 0
    var thumbnailPath: String? = null
    var originalPath: String? = null
    var mimeType: String? = null
    var fileExtension: String? = null
    var statusAction = 0
    var items_id: String? = null
    var degrees = 0
    var thumbnailSync = false
    var originalSync = false
    var fileType = 0
    var title: String? = null
    var size: String? = null
    var isDeleteLocal = false
    var isDeleteGlobal = false
    var statusProgress = 0
    var deleteAction = 0
    var isFakePin = false
    var isSaver = false
    var isExport = false
    var isWaitingForExporting = false
    var custom_items = 0
    var isUpdate: Boolean
    var isRequestChecking: Boolean

    /*Push data to local db*/
    constructor(items: ItemModel) {
        id = items.id
        originalName = items.originalName
        thumbnailName = items.thumbnailName
        items_id = items.items_id
        fileType = items.fileType
        formatType = items.formatType
        title = items.title
        isSyncCloud = items.isSyncCloud
        isSyncOwnServer = items.isSyncOwnServer
        thumbnailSync = items.thumbnailSync
        originalSync = items.originalSync
        degrees = items.degrees
        global_original_id = items.global_original_id
        global_thumbnail_id = items.global_thumbnail_id
        categories_id = items.categories_id
        categories_local_id = items.categories_local_id
        originalPath = items.getOriginal()
        thumbnailPath = items.getThumbnail()
        mimeType = items.mimeType
        fileExtension = items.fileExtension
        statusAction = items.statusAction
        size = items.size
        statusProgress = items.statusProgress
        isDeleteLocal = items.isDeleteLocal
        isDeleteGlobal = items.isDeleteGlobal
        deleteAction = items.deleteAction
        isFakePin = items.isFakePin
        isSaver = items.isSaver
        isExport = items.isExport
        isWaitingForExporting = items.isWaitingForExporting
        custom_items = items.custom_items
        isUpdate = items.isUpdate
        isRequestChecking = items.isRequestChecking
    }

    constructor(items: ItemEntity) {
        id = items.id
        originalName = items.originalName
        thumbnailName = items.thumbnailName
        items_id = items.items_id
        fileType = items.fileType
        formatType = items.formatType
        title = items.title
        isSyncCloud = items.isSyncCloud
        isSyncOwnServer = items.isSyncOwnServer
        thumbnailSync = items.thumbnailSync
        originalSync = items.originalSync
        degrees = items.degrees
        global_original_id = items.global_original_id
        global_thumbnail_id = items.global_thumbnail_id
        categories_id = items.categories_id
        categories_local_id = items.categories_local_id
        originalPath = items.originalPath
        thumbnailPath = items.thumbnailPath
        mimeType = items.mimeType
        fileExtension = items.fileExtension
        statusAction = items.statusAction
        size = items.size
        statusProgress = items.statusProgress
        isDeleteLocal = items.isDeleteLocal
        isDeleteGlobal = items.isDeleteGlobal
        deleteAction = items.deleteAction
        isFakePin = items.isFakePin
        isSaver = items.isSaver
        isExport = items.isExport
        isWaitingForExporting = items.isWaitingForExporting
        custom_items = items.custom_items
        isUpdate = items.isUpdate
        isRequestChecking = items.isRequestChecking
    }

    constructor() {
        thumbnailName = ""
        isSyncCloud = false
        isSyncOwnServer = false
        isUpdate = false
        isRequestChecking = false
    }
}