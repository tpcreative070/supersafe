package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.Authorization
import co.tpcreative.supersafe.model.User
import java.io.Serializable

class UserRequest : Serializable {
    var user_id: String? = null
    var device_id: String? = null
    var session_token: String? = null
    var refresh_token: String? = null
    var public_key: String? = null

    /*Refresh user token*/
    constructor() {
        val mUser: User = Utils.getUserInfo() ?: return
        val mAuthor: Authorization = mUser.author ?: return
        user_id = mUser.email
        refresh_token = mAuthor.refresh_token
        public_key = mAuthor.public_key
        device_id = SuperSafeApplication.getInstance().getDeviceId()
        session_token = Utils.getAccessToken()
    }

    constructor(newUserId: String) {
        val mUser: User = Utils.getUserInfo() ?: return
        val mAuthor: Authorization = mUser.author ?: return
        user_id = newUserId
        refresh_token = mAuthor.refresh_token
        public_key = mAuthor.public_key
        device_id = SuperSafeApplication.getInstance().getDeviceId()
        session_token = Utils.getAccessToken()
    }
}