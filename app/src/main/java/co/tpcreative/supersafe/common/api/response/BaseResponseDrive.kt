package co.tpcreative.supersafe.common.api.response
import co.tpcreative.supersafe.model.ErrorResponse
import java.io.Serializable

open class BaseResponseDrive : Serializable {
    var error: ErrorResponse? = null
}