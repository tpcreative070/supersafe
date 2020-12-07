package co.tpcreative.supersafe.model
import java.io.Serializable

class TwoFactoryAuthenticationModel  : Serializable {
    var user_id : String? = null
    var secret_pin : String? = null
    var new_secret_pin : String? = null
    var isEnable : Boolean? = null
}