package co.tpcreative.supersafe.model
import java.io.Serializable
class Version : Serializable {
    var title: String? = null
    var release = false
    var version_name: String? = null
    var version_code = 0
    var content: HashMap<Any?, String?>? = null
}