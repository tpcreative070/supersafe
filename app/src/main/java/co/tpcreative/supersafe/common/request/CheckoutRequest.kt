package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.io.Serializable

class CheckoutRequest(var user_id: String?, var autoRenewing: Boolean, var orderId: String?, var sku: String?, state: String?, token: String?) : Serializable {
    var device_id: String?
    var packageName: String?
    var state: String?
    var token: String?
    var device_type: String?
    var manufacturer: String?
    var name_model: String?
    var version: String?
    var versionRelease: String?
    var appVersionRelease: String?

    init {
        device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        device_type = SuperSafeApplication.Companion.getInstance().getString(R.string.device_type)
        manufacturer = SuperSafeApplication.Companion.getInstance().getManufacturer()
        name_model = SuperSafeApplication.Companion.getInstance().getModel()
        version = "" + SuperSafeApplication.Companion.getInstance().getVersion()
        versionRelease = SuperSafeApplication.Companion.getInstance().getVersionRelease()
        appVersionRelease = BuildConfig.VERSION_NAME
        packageName = SuperSafeApplication.Companion.getInstance().getPackageId()
        this.state = state
        this.token = token
    }
}