package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
class UnlockAllAlbumViewModel(private val userViewModel : UserViewModel) : VerifyViewModel(userViewModel) {
    override val TAG = this::class.java.simpleName
    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    override var code : String = ""
        set(value) {
            field = value
            validationCode(value)
        }
}