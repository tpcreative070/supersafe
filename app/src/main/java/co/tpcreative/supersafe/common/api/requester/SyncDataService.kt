package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SyncDataService(val apiService: ApiService? = null) {
    val TAG = this::class.java.simpleName
    suspend fun onGetListData(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onListFilesSyncCor(request)
                if (mResult?.isSuccessful!!){
                    ResponseHandler.handleSuccess(mResult.body() as RootResponse,mResult.code())
                }else{
                    Utils.Log(TAG,mResult.message())
                    ResponseHandler.handleException(code = mResult.code())
                }
            }
            catch (exception : Exception){
                 ResponseHandler.handleException(exception)
            }
        }
    }
}