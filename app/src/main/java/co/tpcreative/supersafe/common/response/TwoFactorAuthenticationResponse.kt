package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.common.api.response.BaseResponse

class TwoFactorAuthenticationResponse  : BaseResponse(){
    var isEnabled : Boolean? = null
}