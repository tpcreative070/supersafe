package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.network.Result
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.util.Utils

class SyncDataService(private val apiService: ApiService) {
    suspend fun <T : Any>onListFilesSyncT(request:SyncItemsRequest, onComplete: (result:T) -> Unit?, onException: (error:Throwable?) -> Unit){
        val mResponse = apiService.onListFilesSyncT<T>(request)
        mResponse?.let {
            when(it){
                is Result.Ok ->{
                    onComplete(it.value)
                    Utils.Log("","Ok")
                }
                is Result.Error ->{
                    onException(it.exception)
                    Utils.Log("","Error")
                }
                is Result.Exception -> {
                    onException(it.exception)
                    Utils.Log("","Exception")
                }
            }
        }
    }
}