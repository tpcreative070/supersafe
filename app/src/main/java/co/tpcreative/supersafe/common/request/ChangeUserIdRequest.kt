package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable

class ChangeUserIdRequest : Serializable {
    var user_id: String?
    var new_user_id: String?
    var _id: String?
    var session_token: String?

    constructor(request : VerifyCodeRequest){
        this.user_id = request.user_id
        this.new_user_id = request.new_user_id
        this._id = request._id
        this.session_token = Utils.getAccessToken()
    }

    /*Changed email*/
    constructor(newUser : String){
        val mUser = Utils.getUserInfo()
        this.user_id = mUser?.email
        this.new_user_id = newUser
        this._id = mUser?._id
        this.session_token = Utils.getAccessToken()
    }
}