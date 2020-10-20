package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.api.response.BaseResponseDrive
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.modelimport.DriveUser
import java.io.Serializable

class DriveAbout : BaseResponseDrive(), Serializable {
    var inAppUsed: Long = 0
    var user: DriveUser? = null
    var storageQuota: StorageQuota? = null

    /*Create folder*/ /*Drive api queries*/
    var files: MutableList<DriveResponse?>? = null
}