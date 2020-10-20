package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.util.Utils

class VerifyCodeRequest {
    var code: String? = null
    var user_id: String? = null
    var new_user_id: String? = null

    /*Supersafe _id*/
    var _id: String? = null
    var other_email: String? = null
    var device_id: String? = null
    var session_token: String?

    init {
        session_token = Utils.getAccessToken()
    }
}