package co.tpcreative.supersafe.common.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.*

@Entity(tableName = "items")
class ItemEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
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

    //public String createDatetime;
    // public String updatedDateTime;

    @Ignore
    var isOriginalGlobalId = false

    @Ignore
    var date: Date? = null

    @Ignore
    var name: String? = null

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
    }

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
        this.statusAction = enumStatus.ordinal
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
    }

    constructor() {
        thumbnailName = ""
        isSyncCloud = false
        isSyncOwnServer = false
        isOriginalGlobalId = false
        isUpdate = false
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
        this.statusProgress = progress.ordinal
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
        this.statusAction = statusAction.ordinal
    }


    @Ignore
    fun getUUId(): String? {
        return try {
            UUID.randomUUID().toString()
        } catch (e: Exception) {
            "" + System.currentTimeMillis()
        }
    }

    @Ignore
    fun objectToHashMap(items: ItemEntity?): MutableMap<String?, Any?>? {
        val type = object : TypeToken<MutableMap<String?, Any?>?>() {}.type
        return Gson().fromJson(Gson().toJson(items), type)
    }

    @Ignore
    fun getObject(value: String?): ItemEntity? {
        try {
            if (value == null) {
                return null
            }
            val items: ItemEntity = Gson().fromJson(value, ItemEntity::class.java)
            Utils.Companion.Log(TAG, Gson().toJson(items))
            return items
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private var instance: ItemEntity? = null

        @Ignore
        private val TAG = ItemEntity::class.java.simpleName

        @Ignore
        fun getInstance(): ItemEntity? {
            if (instance == null) {
                instance = ItemEntity()
            }
            return instance
        }
    }
}