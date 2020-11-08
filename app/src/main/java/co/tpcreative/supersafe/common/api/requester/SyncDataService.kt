package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.*
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.request.UserRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncDataService(val apiService: ApiService? = null) {
    suspend fun onListFilesSync(request:SyncItemsRequest, onComplete: (result:RootResponse) -> Unit?, onException: (error:Throwable?) -> Unit){
        val mResponse = apiService?.onListFilesSyncT(request)?.awaitResult()
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

    suspend fun onUserInfo(request: UserRequest, onComplete: (result:RootResponse) -> Unit?, onException: (error:Throwable?) -> Unit){
        val mResponse = apiService?.onUserInfoT(request)?.awaitResult()
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

    suspend fun onGetListData(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            val mResult = ApiHelper.getInstance()?.onListFilesSyncCor(request)?.awaitResult()
            try {
                mResult?.let { mResponse ->
                    when(mResponse){
                        is Result.Ok ->{
                            Resource.success(mResponse.value,mResponse.response.code())
                        }
                        is Result.Error -> {
                            Resource.error(null,"",mResponse.response.code())
                        }
                        is Result.Exception -> {
                            Resource.error(null,mResponse.exception.message,1111)
                        }
                    }
                }
                Resource.error(null,"Nothing",1111)
            }
            catch (exception : Exception){
                Resource.error(null,exception.message,1111)
            }
        }
    }
}