package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable
import java.util.*

class ItemModel : Serializable {
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
    /*Migration folder- Android 11*/
    private var thumbnailPath: String? = null
    private var originalPath: String? = null
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
    var isRequestChecking : Boolean

    /*Added more custom fields*/
    var isChecked = false
    var isDeleted = false
    var isOriginalGlobalId = false
    var date: Date? = null
    var name: String? = null

    /*Added more condition fields*/
    var global_id: String? = null
    var unique_id: String? = null

    /*Get data list from local db*/
    constructor(items: ItemEntityModel) {
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
        unique_id = Utils.getUUId()
        isRequestChecking = items.isRequestChecking
    }

    /*Get data list from local db*/
    constructor(items: ItemEntityModel, categories_id: String?) {
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
        this.categories_id = categories_id
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
        unique_id = Utils.getUUId()
        isRequestChecking = items.isRequestChecking
    }

    /*Added item to list from fetch data*/
    constructor(items: ItemModel, enumStatus: EnumStatus) {
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
        statusAction = enumStatus.ordinal
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
        unique_id = Utils.getUUId()
        isRequestChecking = items.isRequestChecking
        val formatTypeFile = EnumFormatType.values()[formatType]
        when (formatTypeFile) {
            EnumFormatType.AUDIO -> {
                originalSync = false
                thumbnailSync = true
            }
            EnumFormatType.FILES -> {
                originalSync = false
                thumbnailSync = true
            }
            else -> {
                originalSync = false
                thumbnailSync = false
            }
        }
    }

    constructor(isSyncCloud: Boolean, isSyncOwnServer: Boolean, originalSync: Boolean, thumbnailSync: Boolean, degrees: Int, fileType: Int, formatType: Int, title: String?, originalName: String?, thumbnailName: String?, items_id: String?, originalPath: String?, thumbnailPath: String?, global_original_id: String?, global_thumbnail_id: String?, categories_id: String?, categories_local_id: String?, mimeType: String?, fileExtension: String?, enumStatus: EnumStatus, size: String?, statusProgress: Int, isDeleteLocal: Boolean, isDeleteGlobal: Boolean, deleteAction: Int, isFakePin: Boolean, isSaver: Boolean, isExport: Boolean, isWaitingForExporting: Boolean, custom_items: Int, isUpdate: Boolean) {
        this.originalName = originalName
        this.thumbnailName = thumbnailName
        this.items_id = items_id
        this.fileType = fileType
        this.formatType = formatType
        this.title = title
        this.isSyncCloud = isSyncCloud
        this.isSyncOwnServer = isSyncOwnServer
        this.thumbnailSync = thumbnailSync
        this.originalSync = originalSync
        this.degrees = degrees
        this.global_original_id = global_original_id
        this.global_thumbnail_id = global_thumbnail_id
        this.categories_id = categories_id
        this.categories_local_id = categories_local_id
        this.originalPath = originalPath
        this.thumbnailPath = thumbnailPath
        this.mimeType = mimeType
        this.fileExtension = fileExtension
        statusAction = enumStatus.ordinal
        this.size = size
        this.statusProgress = statusProgress
        this.isDeleteLocal = isDeleteLocal
        this.isDeleteGlobal = isDeleteGlobal
        this.deleteAction = deleteAction
        this.isFakePin = isFakePin
        this.isSaver = isSaver
        this.isExport = isExport
        this.isWaitingForExporting = isWaitingForExporting
        this.custom_items = custom_items
        this.isUpdate = isUpdate
        this.isRequestChecking  = false
    }

    fun isChecked() : Boolean? {
        return isChecked
    }

    constructor() {
        thumbnailName = ""
        isSyncCloud = false
        isSyncOwnServer = false
        isOriginalGlobalId = false
        isUpdate = false
        isRequestChecking = false
    }

    constructor(fileExtension: String?,
                originalPath: String?,
                thumbnailPath: String?,
                categories_id: String?,
                categories_local_id: String?,
                mimeType: String?,
                uuId: String?,
                formatType: EnumFormatType,
                degrees: Int,
                thumbnailSync: Boolean,
                originalSync: Boolean,
                global_original_id: String?,
                global_thumbnail_id: String?,
                fileType: EnumFileType,
                originalName: String?,
                name: String?,
                thumbnailName: String?,
                size: String?,
                progress: EnumStatusProgress,
                isDeleteLocal: Boolean,
                isDeleteGlobal: Boolean,
                deleteAction: EnumDelete,
                isFakePin: Boolean,
                isSaver: Boolean,
                isExport: Boolean,
                isWaitingForExporting: Boolean,
                custom_items: Int,
                isSyncCloud: Boolean,
                isSyncOwnServer: Boolean,
                isUpdate: Boolean,
                statusAction: EnumStatus
    ) {
        this.fileExtension = fileExtension
        this.originalPath = originalPath
        this.thumbnailPath = thumbnailPath
        this.categories_id = categories_id
        this.categories_local_id = categories_local_id
        this.mimeType = mimeType
        items_id = uuId
        this.formatType = formatType.ordinal
        this.degrees = degrees
        this.thumbnailSync = thumbnailSync
        this.originalSync = originalSync
        this.global_original_id = global_original_id
        this.global_thumbnail_id = global_thumbnail_id
        this.fileType = fileType.ordinal
        this.originalName = originalName
        title = name
        this.thumbnailName = thumbnailName
        this.size = size
        statusProgress = progress.ordinal
        this.isDeleteLocal = isDeleteLocal
        this.isDeleteGlobal = isDeleteGlobal
        this.deleteAction = deleteAction.ordinal
        this.isFakePin = isFakePin
        this.isSaver = isSaver
        this.isExport = isExport
        this.isWaitingForExporting = isWaitingForExporting
        this.custom_items = custom_items
        this.isSyncCloud = isSyncCloud
        this.isSyncOwnServer = isSyncOwnServer
        this.isUpdate = isUpdate
        this.isRequestChecking = false
        this.statusAction = statusAction.ordinal
    }

    /*Merged request list for sync data*/
    constructor(items: ItemModel, isOriginalGlobalId: Boolean) {
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
        /*Added more condition fields*/unique_id = Utils.getUUId()
        global_id = if (isOriginalGlobalId) {
            items.global_original_id
        } else {
            items.global_thumbnail_id
        }
        this.isOriginalGlobalId = isOriginalGlobalId
    }

    /*Get dynamic path of thumbnail*/
    fun getThumbnail() : String{
        if ((SuperSafeApplication.getInstance().getStorage()?.isFileExist(thumbnailPath)!!)){
            return this.thumbnailPath!!
        }
        return SuperSafeApplication.getInstance().getSuperSafePrivate()+ "/"+ items_id +"/"+thumbnailName
    }

    fun setThumbnail(path : String?){
        this.thumbnailPath = path
    }

    /*Get dynamic path of original*/
    fun getOriginal() : String{
        if ((SuperSafeApplication.getInstance().getStorage()?.isFileExist(originalPath)!!)){
            return this.originalPath!!
        }
        return SuperSafeApplication.getInstance().getSuperSafePrivate()+ "/"+ items_id +"/"+originalName
    }

    fun setOriginal(path : String?){
        this.originalPath = path
    }

    /*This is view Area*/
    val titleView : String
    get() {
        return title ?: ""
    }

    val createdDateTimeView : String
    get() {
        val value: String? = size?.toLong()?.let { ConvertUtils.byte2FitMemorySize(it) }
        return value + " created " + Utils.getCurrentDate(originalName)
    }
}