package co.tpcreative.supersafe.common.response

import java.io.Serializable

class TrackingSyncResponse() : Serializable {
    var device_id  : String?
    var user_id  : String?
    var cloud_id : String?
    var channel_code : String?
    var channel_name : String?
    var created_date : String?
    var updated_date : String?
    var date_time: String?
    var lastAccessTime : String?

    init {
        device_id = ""
        user_id = ""
        cloud_id = ""
        channel_code = ""
        channel_name = ""
        created_date = ""
        updated_date = ""
        date_time = ""
        lastAccessTime = ""
    }
}