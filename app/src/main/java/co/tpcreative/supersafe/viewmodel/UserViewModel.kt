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
import co.tpcreative.supersafe.common.request.MicRequest
import co.tpcreative.supersafe.common.request.OutlookMailRequest
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmailToken
import co.tpcreative.supersafe.model.EnumResponseCode
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.snatik.storage.security.SecurityUtil
import kotlinx.coroutines.Dispatchers
import java.util.*

enum class EnumValidationKey {
    EDIT_TEXT_EMAIL
}
class UserViewModel(private val service: UserService, private val micService: MicService) : ViewModel(){
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
                Status.LOADING ->{}
                Status.SUCCESS ->{
                    if (mResultSignIn.data?.error!!){
                        errorResponseMessage.postValue(getString(R.string.signed_in_failed))
                        emit(Resource.error(mResultSignIn.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignIn.data.responseMessage ?:"",null))
                    }else{
                        val mData: DataResponse? = mResultSignIn.data.data
                        Utils.Log(TAG,mData?.user?.email_token?.toJson())
                        Utils.setUserPreShare(mData?.user)
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                        val mMicRequest = getEmailContent()
                        val mResultSentEmail = micService.sendMail(mMicRequest)
                        when(mResultSentEmail.status){
                            Status.LOADING ->{}
                            Status.SUCCESS -> {
                                emit(mResultSignIn)
                            }
                            Status.ERROR ->{
                                if (EnumResponseCode.INVALID_AUTHENTICATION.code==mResultSentEmail.code){
                                    val mRequestEmailToken = getRefreshContent(mMicRequest.data)
                                    val mResultRefreshToken = micService.refreshEmailToken(mRequestEmailToken)
                                    when(mResultRefreshToken.status){
                                        Status.LOADING ->{}
                                        Status.SUCCESS ->{
                                            Utils.setEmailToken(mResultRefreshToken.data)
                                            val mResultAddedMailToken = micService.addEmailToken(getAddedEmailToken())
                                            when(mResultAddedMailToken.status){
                                                Status.LOADING -> {}
                                                Status.SUCCESS ->{
                                                    val mSentEmail = micService.sendMail(getEmailContent())
                                                    when(mSentEmail.status){
                                                        Status.LOADING ->{}
                                                        Status.SUCCESS ->{
                                                            emit(mResultSignIn)
                                                        }
                                                        Status.ERROR ->{
                                                            emit(Resource.error(mSentEmail.code?:Utils.CODE_EXCEPTION, mSentEmail.message ?:"",null))
                                                        }
                                                    }
                                                }
                                                Status.ERROR ->{
                                                    emit(Resource.error(mResultAddedMailToken.code?:Utils.CODE_EXCEPTION, mResultAddedMailToken.message ?:"",null))
                                                }
                                            }
                                        }
                                        Status.ERROR -> {
                                            emit(Resource.error(mResultRefreshToken.code?:Utils.CODE_EXCEPTION, mResultRefreshToken.message ?:"",null))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->{
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

    private fun getEmailContent() : MicRequest{
        val mUser: User? = Utils.getUserInfo()
        val mEmailToken =  mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.SIGN_IN) }
        return MicRequest(Utils.getMicAccessToken()!!,mEmailToken!!)
    }

    private fun getRefreshContent(request:EmailToken?) : MutableMap<String?,Any?>{
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[getString(R.string.key_client_id)] = request?.client_id
        hash[getString(R.string.key_redirect_uri)] = request?.redirect_uri
        hash[getString(R.string.key_grant_type)] = request?.grant_type
        hash[getString(R.string.key_refresh_token)] = request?.refresh_token
        return hash
    }

    private fun getAddedEmailToken() : OutlookMailRequest {
        val mUser = Utils.getUserInfo()
        return OutlookMailRequest(mUser?.email_token?.refresh_token, mUser?.email_token?.access_token)
    }
}