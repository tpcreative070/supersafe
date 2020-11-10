package co.tpcreative.supersafe.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.requester.SyncDataService
import co.tpcreative.supersafe.common.helper.ApiHelper
import java.lang.IllegalArgumentException

class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SyncDataService::class.java)){
            return SyncDataService(apiHelper)as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}