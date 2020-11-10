package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import com.snatik.storage.security.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

enum class EnumValidationKey {
    EDIT_TEXT_EMAIL
}
class UserViewModel(private val service: UserService) : ViewModel(){
    val TAG = this::class.java.simpleName
    val errorMessages : MutableLiveData<MutableMap<String,String>> by lazy {
        MutableLiveData<MutableMap<String,String>>()
    }

    val isLoading : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val errorResponseMessage  : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    init {
        errorMessages.value = mutableMapOf(EnumValidationKey.EDIT_TEXT_EMAIL.name to "")
        isLoading.value = false
        errorResponseMessage.value = ""
    }

    var email : String = ""
        set(value) {
            field = value
            validationEmail(value)
        }

    fun signIn() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = service.signIn(getSignInRequest())
            when(mResult.status){
                Status.LOADING ->{
                }
                Status.SUCCESS ->{
                    val mData = mResult.data
                    if (mData?.error!!){
                        errorResponseMessage.postValue(getString(R.string.signed_in_failed))
                        return@liveData
                    }
                }
                Status.ERROR ->{

                }
            }
            emit(mResult)
        }catch (e : Exception){
            e.printStackTrace()
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null))
        }
        finally {
            isLoading.postValue(false)
        }
    }

    private fun validationEmail(mValue : String){
        if (mValue.isEmpty()){
            errorMessages.value?.set(EnumValidationKey.EDIT_TEXT_EMAIL.name, "Request enter email")
        }else if (!Utils.isValidEmail(mValue)){
            errorMessages.value?.set(EnumValidationKey.EDIT_TEXT_EMAIL.name, "Email invalid")
        }
        else{
            errorMessages.value?.remove(EnumValidationKey.EDIT_TEXT_EMAIL.name)
        }
        errorMessages.postValue(errorMessages.value)
        Utils.Log(TAG,"Print ${errorMessages.value?.toJson()} $mValue")
    }

    private fun getSignInRequest() : SignInRequest{
        val email: String = email.toLowerCase(Locale.ROOT).trim { it <= ' ' }
        val request = SignInRequest()
        request.user_id = email
        request.password = SecurityUtil.key_password_default_encrypted
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        return request
    }
}