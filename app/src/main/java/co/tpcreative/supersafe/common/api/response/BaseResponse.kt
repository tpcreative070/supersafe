package co.tpcreative.supersafe.common.api.response
import co.tpcreative.supersafe.model.Version
import com.anjlab.android.iab.v3.PurchaseData
import com.google.gson.Gson
import java.io.Serializable

open class BaseResponse : Serializable {
    var responseMessage: String? = null
    var responseCode : Int? = 0
    var error = false
    var purchase: PurchaseData? = null
    var version: Version? = null
}