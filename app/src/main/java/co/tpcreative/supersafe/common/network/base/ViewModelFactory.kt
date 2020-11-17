package co.tpcreative.supersafe.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.viewmodel.*
import java.lang.IllegalArgumentException

class ViewModelFactory() : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)){
            return UserViewModel(UserService(), MicService()) as T
        }
        else if (modelClass.isAssignableFrom(VerifyViewModel::class.java)){
            return VerifyViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if (modelClass.isAssignableFrom(VerifyAccountViewModel::class.java)){
            return VerifyAccountViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if(modelClass.isAssignableFrom(CheckSystemViewModel::class.java)){
            return CheckSystemViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if(modelClass.isAssignableFrom(EnableCloudViewModel::class.java)){
            return EnableCloudViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if(modelClass.isAssignableFrom(UnlockAllAlbumViewModel::class.java)){
            return UnlockAllAlbumViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if (modelClass.isAssignableFrom(TrashViewModel::class.java)){
            return TrashViewModel() as T
        }
        else if (modelClass.isAssignableFrom(AlbumDetailViewModel::class.java)){
            return AlbumDetailViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}