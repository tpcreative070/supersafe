package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.network.Result
import co.tpcreative.supersafe.common.network.awaitResult
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.requestimport.UserRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.util.Utils
import retrofit2.awaitResponse

class SyncDataService(private val apiService: ApiService) {
    suspend fun onListFilesSync(request:SyncItemsRequest, onComplete: (result:RootResponse) -> Unit?, onException: (error:Throwable?) -> Unit){
        val mResponse = apiService.onListFilesSyncT(request)?.awaitResult()
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

    suspend fun onUserInfo(request:UserRequest, onComplete: (result:RootResponse) -> Unit?, onException: (error:Throwable?) -> Unit){
        val mResponse = apiService.onUserInfoT(request)?.awaitResult()
        try{
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
        }catch (exception : Exception){
            onException(exception)
        }
    }

}