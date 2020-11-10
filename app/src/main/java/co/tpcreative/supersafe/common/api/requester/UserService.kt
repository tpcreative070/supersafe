package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.UserRequest
import co.tpcreative.supersafe.common.response.RootResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService (val apiHelper: ApiHelper? = null){
    suspend fun onUpdateToken(request : UserRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.updateTokenCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }
}