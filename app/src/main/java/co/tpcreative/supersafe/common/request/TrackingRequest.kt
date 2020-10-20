package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.io.Serializable
class TrackingRequest(var user_id: String?, var device_id: String?) : Serializable {
    var device_type: String?
    var manufacturer: String?
    var name_model: String?
    var version: String?
    var versionRelease: String?
    var appVersionRelease: String?
    var app_id: String?
    var channel_code: String?

    init {
        device_type = SuperSafeApplication.Companion.getInstance().getString(R.string.device_type)
        manufacturer = SuperSafeApplication.Companion.getInstance().getManufacturer()
        name_model = SuperSafeApplication.Companion.getInstance().getModel()
        version = "" + SuperSafeApplication.Companion.getInstance().getVersion()
        versionRelease = SuperSafeApplication.Companion.getInstance().getVersionRelease()
        appVersionRelease = BuildCon.VERSION_NAME
        app_id = SuperSafeApplication.Companion.getInstance().getPackageId()
        channel_code = "C002"
    }
}