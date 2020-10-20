package co.tpcreative.supersafe.common.api.response
import co.tpcreative.supersafe.model.Version
import com.anjlab.android.iab.v3.PurchaseData
import com.google.gson.Gson
import java.io.Serializable

open class BaseResponse : Serializable {
    var message: String? = null
    var responseMessage: String? = null
    var error = false
    var purchase: PurchaseData? = null
    var version: Version? = null
    fun toFormResponse(): String? {
        return Gson().toJson(this)
    }
}