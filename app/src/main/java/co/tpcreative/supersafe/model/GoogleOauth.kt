package co.tpcreative.supersafe.model
import java.io.Serializable

class GoogleOauth : Serializable {
    var email: String? = null
    var isEnableSync = false
}