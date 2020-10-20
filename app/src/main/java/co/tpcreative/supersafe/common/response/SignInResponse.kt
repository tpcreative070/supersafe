package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.model.User

class SignInResponse : BaseResponse() {
    var user: User? = null
}