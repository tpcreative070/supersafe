package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication

class ApiHelper() {
    suspend fun onListFilesSyncCor(data : SyncItemsRequest)   = SuperSafeApplication.serverAPI?.onListFilesSyncCor(data)
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