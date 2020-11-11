package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import com.snatik.storage.security.SecurityUtil
import kotlinx.coroutines.Dispatchers
import java.util.*

enum class EnumValidationKey {
    EDIT_TEXT_EMAIL
}
class UserViewModel(private val service: UserService, micService: MicService) : ViewModel(){
    private val emailViewModel = EmailOutlookViewModel(micService)
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
            val mResultSignIn = service.signIn(getSignInRequest())
            when(mResultSignIn.status){
                Status.SUCCESS ->{
                    if (mResultSignIn.data?.error!!){
                        errorResponseMessage.postValue(getString(R.string.signed_in_failed))
                        emit(Resource.error(mResultSignIn.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignIn.data.responseMessage ?:"",null))
                    }else{
                        val mData: DataResponse? = mResultSignIn.data.data
                        Utils.Log(TAG,mData?.user?.email_token?.toJson())
                        Utils.setUserPreShare(mData?.user)
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                        val mResultSentEmail = emailViewModel.sendEmail(EnumStatus.SIGN_IN)
                        when(mResultSentEmail.status){
                            Status.SUCCESS ->{
                                emit(mResultSignIn)
                            }
                            else -> emit(Resource.error(mResultSentEmail.code?:Utils.CODE_EXCEPTION, mResultSentEmail.message ?:"",null))
                        }
                    }
                }
                else  ->{
                    emit(Resource.error(mResultSignIn.code?:Utils.CODE_EXCEPTION, mResultSignIn.message ?:"",null))
                }
            }
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