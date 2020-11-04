package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.util.Utils

class UserCloudRequest {
    var user_id: String?
    var cloud_id: String?
    var device_id: String? = null
    var session_token: String?

    /*Check cloud id*/
    constructor(user_id: String?, cloud_id: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        session_token = Utils.getAccessToken()
    }

    /*Add user cloud*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
        session_token = Utils.getAccessToken()
    }
}