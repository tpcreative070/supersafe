package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import retrofit2.HttpException
import retrofit2.Response

class ApiHelper() {
    suspend fun onListFilesSyncCor(data : SyncItemsRequest) =   SuperSafeApplication.serverApiCor?.onListFilesSyncCor(data)
    companion object {
        private var mInstance : ApiHelper? = null
        fun getInstance() : ApiHelper? {
            if (mInstance==null){
                mInstance = ApiHelper()
            }
            return mInstance
        }
    }
}