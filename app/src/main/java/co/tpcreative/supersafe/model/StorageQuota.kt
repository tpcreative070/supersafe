package co.tpcreative.supersafe.model
import java.io.Serializable

class StorageQuota : Serializable {
    var limit: Long = 0
    var usage: Long = 0
    var usageInDrive: Long = 0
    var usageInDriveTrash: Long = 0
}