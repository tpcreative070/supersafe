package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.util.Utils
import com.google.gson.Gson
import java.io.Serializable

class DriveDescription : Serializable {
    var categories_local_id: String? = null
    var categories_id: String? = null
    var fileExtension: String? = null
    var thumbnailPath: String? = null
    var originalPath: String? = null
    var title: String? = null
    var global_original_id: String? = null
    var global_thumbnail_id: String? = null
    var mimeType: String? = null
    var thumbnailName: String? = null
    var originalName: String? = null
    var items_id: String? = null
    var degrees = 0
    var formatType = 0
    var thumbnailSync = false
    var originalSync = false
    var fileType = 0
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
    fun convertToHex(value: String?): String? {
        try {
            return Utils.stringToHex(value)
        } catch (e: Exception) {
            Utils.Log(TAG, "Error :" + e.message)
            e.printStackTrace()
            Utils.onWriteLog(TAG + "-" + e.message, EnumStatus.WRITE_FILE)
        }
        return null
    }

    fun hexToObject(value: String?): DriveDescription? {
        try {
            val result: String? = Utils.hexToString(value)
            return Gson().fromJson(result, DriveDescription::class.java)
        } catch (e: Exception) {
            Utils.Log(TAG, "Error :" + e.message)
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val TAG = DriveDescription::class.java.simpleName
        private var instance: DriveDescription? = null
        fun getInstance(): DriveDescription? {
            if (instance == null) {
                instance = DriveDescription()
            }
            return instance
        }
    }
}