package co.tpcreative.supersafe.modelimport
import java.io.Serializable

class DriveUser : Serializable {
    var kind: String? = null
    var displayName: String? = null
    var photoLink: String? = null
    var me = false
    var permissionId: String? = null
    var emailAddress: String? = null
}