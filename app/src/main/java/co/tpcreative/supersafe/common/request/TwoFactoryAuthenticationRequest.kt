package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.extension.encode
import co.tpcreative.supersafe.common.extension.encryptTextByIdPKCS7
import co.tpcreative.supersafe.common.util.Utils
import javax.crypto.Cipher

class TwoFactoryAuthenticationRequest {
    var user_id : String? = null
    var secret_pin : String? = null
    var new_secret_pin : String? = null
    var isEnabled : Boolean? = null
    constructor(secret_pin : String? = null,new_secret_pin: String?=null,isEnabled:Boolean? = false){
        this.user_id = Utils.getUserId()
        this.secret_pin = secret_pin?.encryptTextByIdPKCS7(Cipher.ENCRYPT_MODE)
        this.new_secret_pin = new_secret_pin?.encryptTextByIdPKCS7(Cipher.ENCRYPT_MODE)
        this.isEnabled = isEnabled
    }
}