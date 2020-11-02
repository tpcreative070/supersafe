package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.requester.SyncDataService
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Result
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

class SyncItemViewModel(private val apiService: ApiService) : ViewModel(){
    suspend fun getItemList() = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            val mResult = apiService.onListFilesSyncT<RootResponse>(request = SyncItemsRequest("","",""))
            when(mResult){
                is Result.Ok -> {
                    emit(Resource.success(data = mResult.value))
                }
                is Result.Error -> {
                    emit(Resource.error(data=null,message = mResult.exception.message()))
                }
                is Result.Exception -> {
                    emit(Resource.error(data=null,message = mResult.exception.message))
                }
                else -> Utils.Log("","Nothing")
            }
            emit(Resource.success(data=mResult))
        }catch (exception: Exception){
            emit(Resource.error(data=null,message = exception.message?:"Error occured"))
        }
    }
}