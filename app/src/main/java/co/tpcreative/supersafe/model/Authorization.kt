package co.tpcreative.supersafe.model
import java.io.Serializable

open class Authorization : Serializable {
    var device_id: String? = null
    var device_type: String? = null
    var manufacturer: String? = null
    var name_model: String? = null
    var version: String? = null
    var versionRelease: String? = null
    var session_token: String? = null
    var user_id: String? = null
    var active = false
    var created_date: String? = null
    var updated_date: String? = null
    var date_time: String? = null
    var refresh_token: String? = null
    var public_key: String? = null
}