package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.request.TrackingSyncRequest
import co.tpcreative.supersafe.common.response.RootResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemService() {
    suspend fun getListData(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onListFilesSyncCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun syncData(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onSyncDataCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun deleteOwnItems(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onDeleteOwnItemsCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun trackingSync(request : TrackingSyncRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.trackingSyncCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }
}