package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.BuildConfig
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
        device_type = SuperSafeApplication.getInstance().getString(R.string.device_type)
        manufacturer = SuperSafeApplication.getInstance().getManufacturer()
        name_model = SuperSafeApplication.getInstance().getModel()
        version = "" + SuperSafeApplication.getInstance().getVersion()
        versionRelease = SuperSafeApplication.getInstance().getVersionRelease()
        appVersionRelease = BuildConfig.VERSION_NAME
        app_id = SuperSafeApplication.getInstance().getPackageId()
        channel_code = "C002"
    }
}