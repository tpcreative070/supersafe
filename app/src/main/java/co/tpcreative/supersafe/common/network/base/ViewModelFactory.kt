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
        else if (modelClass.isAssignableFrom(ShareFilesViewModel::class.java)){
            return ShareFilesViewModel() as T
        }
        else if (modelClass.isAssignableFrom(PhotoSlideShowViewModel::class.java)){
            return PhotoSlideShowViewModel() as T
        }
        else if (modelClass.isAssignableFrom(CloudManagerViewModel::class.java)){
            return CloudManagerViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ThemeSettingsViewModel::class.java)){
            return ThemeSettingsViewModel() as T
        }
        else if (modelClass.isAssignableFrom(AlbumSettingsViewModel::class.java)){
            return AlbumSettingsViewModel() as T
        }
        else if (modelClass.isAssignableFrom(AlbumCoverViewModel::class.java)){
            return AlbumCoverViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ResetPinViewModel::class.java)){
            return ResetPinViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if (modelClass.isAssignableFrom(PrivateViewModel::class.java)){
            return PrivateViewModel() as T
        }
        else if (modelClass.isAssignableFrom(PremiumViewModel::class.java)){
            return PremiumViewModel(UserViewModel(UserService(),MicService())) as T
        }
        else if (modelClass.isAssignableFrom(MeViewModel::class.java)){
            return MeViewModel() as T
        }
        else if (modelClass.isAssignableFrom(PlayerViewModel::class.java)){
            return PlayerViewModel() as T
        }
        else if (modelClass.isAssignableFrom(FakePinComponentViewModel::class.java)){
            return FakePinComponentViewModel() as T
        }
        else if (modelClass.isAssignableFrom(MoveAlbumViewModel::class.java)){
            return MoveAlbumViewModel() as T
        }
        else if (modelClass.isAssignableFrom(HelpAndSupportViewModel::class.java)){
            return HelpAndSupportViewModel(EmailOutlookViewModel(MicService())) as T
        }
        else if (modelClass.isAssignableFrom(AccountManagerViewModel::class.java)){
            return AccountManagerViewModel() as T
        }
        else if (modelClass.isAssignableFrom(BreakInAlertsViewModel::class.java)){
            return BreakInAlertsViewModel() as T
        }
        else if (modelClass.isAssignableFrom(LockScreenViewModel::class.java)){
            return LockScreenViewModel() as T
        }
        else if (modelClass.isAssignableFrom(TwoFactorAuthenticationViewModel::class.java)){
            return TwoFactorAuthenticationViewModel(UserViewModel(UserService(),MicService())) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}