package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable

class TrackingSyncRequest() : Serializable{
    var user_id : String?
    var cloud_id: String?
    var device_id: String?
    var channel_code: String?

    init {
        device_id = SuperSafeApplication.getInstance().getDeviceId()
        user_id = Utils.getUserId()
        cloud_id = Utils.getUserCloudId()
        channel_code = "C002"
    }
}