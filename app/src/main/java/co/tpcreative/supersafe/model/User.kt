package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.api.response.BaseResponse
import java.io.Serializable

class User : BaseResponse(), Serializable {
    var email: String? = null
    var _id: String? = null
    var other_email: String? = null
    var name: String? = null
    var change = false
    var code: String? = null
    var cloud_id: String? = null
    var author: Authorization? = null
    var verified = false
    var access_token: String? = null
    var driveConnected = false
    var driveAbout: DriveAbout? = null
    var isWaitingSendMail = false
    var premium: Premium? = null
    var email_token: EmailToken? = null
    var syncData: SyncData? = null
    companion object {
    }
}