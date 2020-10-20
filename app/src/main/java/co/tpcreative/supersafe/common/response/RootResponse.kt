package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.responseimport.DataResponse
import java.io.Serializable

class RootResponse : BaseResponse(), Serializable {
    var data: DataResponse? = null
}