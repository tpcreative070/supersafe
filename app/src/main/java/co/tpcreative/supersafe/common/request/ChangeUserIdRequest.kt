package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable

class ChangeUserIdRequest(request: VerifyCodeRequest) : Serializable {
    var user_id: String?
    var new_user_id: String?
    var _id: String?
    var session_token: String?

    init {
        user_id = request.user_id
        new_user_id = request.new_user_id
        _id = request._id
        session_token = Utils.getAccessToken()
    }
}