package co.tpcreative.supersafe.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.viewmodel.UserViewModel
import co.tpcreative.supersafe.viewmodel.VerifyViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory() : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)){
            return UserViewModel(UserService(), MicService()) as T
        }
        else if (modelClass.isAssignableFrom(VerifyViewModel::class.java)){
            return VerifyViewModel(UserViewModel(UserService(),MicService())) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}