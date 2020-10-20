package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.util.Utils
import com.google.gson.Gson
import java.io.Serializable

class DriveEvent : Serializable {
    var items_id: String? = null
    var fileType = 0
    fun convertToHex(value: String?): String? {
        try {
            return Utils.stringToHex(value)
        } catch (e: Exception) {
            Utils.Log(DriveEvent.Companion.TAG, "Error :" + e.message)
            Utils.onWriteLog(DriveEvent.Companion.TAG + "-" + e.message, EnumStatus.WRITE_FILE)
            e.printStackTrace()
        }
        return null
    }

    fun hexToObject(value: String?): DriveEvent? {
        try {
            val result: String? = Utils.hexToString(value)
            return Gson().fromJson(result, DriveEvent::class.java)
        } catch (e: Exception) {
            Utils.Log(DriveEvent.Companion.TAG, "Error :" + e.message)
            Utils.onWriteLog(DriveEvent.Companion.TAG + "-" + e.message, EnumStatus.WRITE_FILE)
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private var instance: DriveEvent? = null
        private val TAG = DriveEvent::class.java.simpleName
        fun getInstance(): DriveEvent? {
            if (instance == null) {
                instance = DriveEvent()
            }
            return instance
        }
    }
}