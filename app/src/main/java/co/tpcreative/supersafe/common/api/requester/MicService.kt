package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.MicRequest
import co.tpcreative.supersafe.common.request.OutlookMailRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MicService (){
    suspend fun sendMail(request : MicRequest) : Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                ApiHelper.getInstance()?.onSendMailCor(request.token,request.data!!)
                ResponseHandler.handleSuccess("Sent mail successfully")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun refreshEmailToken(request : MutableMap<String?,Any?>) : Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                ApiHelper.getInstance()?.onRefreshEmailTokenCor(ApiService.REFRESH_TOKEN,request)
                ResponseHandler.handleSuccess("Sent mail successfully")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun refreshEmailToken(request : OutlookMailRequest) : Resource<BaseResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onAddEmailTokenCor(request)
                ResponseHandler.handleSuccess(mResult as BaseResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }
}