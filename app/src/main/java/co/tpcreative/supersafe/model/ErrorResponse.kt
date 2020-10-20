package co.tpcreative.supersafe.model
import java.io.Serializable

class ErrorResponse : Serializable {
    var code = 0
    var message: String? = null
}