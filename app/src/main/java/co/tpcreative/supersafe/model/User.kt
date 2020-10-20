package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.api.response.BaseResponse
import java.io.Serializable

class User : BaseResponse(), Serializable {
    var email: String? = null
    var _id: String? = null
    var other_email: String? = null
    var current_milliseconds: Long = 0
    var name: String? = null
    var role = 0
    var change = false
    var active = false
    var code: String? = null
    var cloud_id: String? = null
    var author: Authorization? = null
    var verified = false
    var access_token: String? = null
    var driveConnected = false
    var driveAbout: DriveAbout? = null
    var isRefresh = false
    var isDownLoad = false
    var isUpload = false
    var isRequestSync = false
    var isUpdateView = false
    var isWaitingSendMail = false
    var premium: Premium? = null
    var email_token: EmailToken? = null
    var syncData: SyncData? = null

    companion object {
        private val instance: User? = null
    }
}