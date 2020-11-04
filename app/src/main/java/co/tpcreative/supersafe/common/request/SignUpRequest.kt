package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.model.Authorization

class SignUpRequest : Authorization() {
    var email: String? = null
    var password: String? = null
    var name: String? = null
}