package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.model.EmailToken
import java.io.Serializable

class MicRequest : Serializable {
    var token : String?
    var data : EmailToken?
    /*Send mail*/
    constructor(token : String, data : EmailToken){
        this.token = token
        this.data = data
    }
}