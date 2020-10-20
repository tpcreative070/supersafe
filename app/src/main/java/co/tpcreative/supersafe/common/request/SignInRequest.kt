package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.model.Authorization
import java.io.Serializable

class SignInRequest : Authorization(), Serializable {
    var email: String? = null
    var password: String? = null
}