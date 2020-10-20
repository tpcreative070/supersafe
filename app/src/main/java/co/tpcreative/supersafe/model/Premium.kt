package co.tpcreative.supersafe.model
import java.io.Serializable

class Premium : Serializable {
    var status = false
    var message: String? = null
    var current_milliseconds: Long = 0
    var past_milliseconds: Long = 0
    var device_milliseconds: Long = 0
}