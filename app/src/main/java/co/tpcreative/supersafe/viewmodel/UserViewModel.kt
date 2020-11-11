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
import co.tpcreative.supersafe.common.request.SignUpRequest
import co.tpcreative.supersafe.common.request.UserRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.Authorization
import co.tpcreative.supersafe.model.EnumResponseCode
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import com.snatik.storage.security.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    fun signUp() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResultSignUp = service.signUp(getSignUpRequest())
            when(mResultSignUp.status){
                Status.SUCCESS -> {
                    if (mResultSignUp.data?.error!!){
                        errorResponseMessage.postValue(mResultSignUp.data.responseMessage)
                        emit(Resource.error(mResultSignUp.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignUp.data.responseMessage ?:"",null))
                    }else{
                        val mData: DataResponse? = mResultSignUp.data.data
                        Utils.setUserPreShare(mData?.user)
                        ServiceManager.getInstance()?.onInitConfigurationFile()
                        emit(mResultSignUp)
                    }
                }
                else -> emit(Resource.error(mResultSignUp.code?:Utils.CODE_EXCEPTION, mResultSignUp.message ?:"",null))
            }
        }catch (e : Exception){
            e.printStackTrace()
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null))
        }
        finally {
            isLoading.postValue(false)
        }
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

    suspend fun updatedUserToken() : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                val mResult = UserRequest()
                val mResultUpdated = service.updateToken(mResult)
                when(mResultUpdated.status){
                    Status.SUCCESS -> {
                        val mData = mResultUpdated.data?.data
                        val mResultDeleteOldToken = service.deleteOldAccessTokenCor(mResult)
                        when(mResultDeleteOldToken.status){
                            Status.SUCCESS ->{
                                setUpdatedTokenValue(mData)
                                mResultUpdated
                            }
                            else ->{
                                Resource.error(mResultDeleteOldToken.code?:Utils.CODE_EXCEPTION,mResultDeleteOldToken.message ?: "",null)
                            }
                        }
                    }
                    else ->{
                        if (isRequestSignIn(mResultUpdated.code ?:Utils.CODE_EXCEPTION)){
                            val mResultSignIn = service.signIn(getSignInRequest(Utils.getUserId()))
                            when(mResultSignIn.status){
                                Status.SUCCESS ->{
                                    if (mResultSignIn.data?.error!!){
                                        errorResponseMessage.postValue(getString(R.string.signed_in_failed))
                                        Resource.error(mResultSignIn.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignIn.data.responseMessage ?:"",null)
                                    }else{
                                        setUpdatedTakenValueAfterSignedIn(mResultSignIn.data.data)
                                        mResultSignIn
                                    }
                                }
                                else ->{
                                    Resource.error(mResultSignIn.code?:Utils.CODE_EXCEPTION,mResultSignIn.message ?: "",null)
                                }
                            }
                        }else{
                            Resource.error(mResultUpdated.code?:Utils.CODE_EXCEPTION,mResultUpdated.message ?: "",null)
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
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

    private fun getSignInRequest(mEmail : String? =null) : SignInRequest{
        val email: String = mEmail ?: email.toLowerCase(Locale.ROOT).trim { it <= ' ' }
        val request = SignInRequest()
        request.user_id = email
        request.password = SecurityUtil.key_password_default_encrypted
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        return request
    }

    private fun getSignUpRequest() : SignUpRequest {
        val request = SignUpRequest()
        request.user_id = email
        request.name = getString(R.string.free)
        request.password = SecurityUtil.key_password_default_encrypted
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        return request
    }

    private fun setUpdatedTokenValue(mData : DataResponse?){
        val mUser: User? = Utils.getUserInfo()
        mData?.let {
            if (it.user?.author != null) {
                val mAuthorGlobal: Authorization? = it.user?.author
                val mAuthor = mUser?.author
                mAuthor?.refresh_token = mAuthorGlobal?.refresh_token
                mAuthor?.session_token = mAuthorGlobal?.session_token
                mUser?.author = mAuthor
                Utils.setUserPreShare(mUser)
            }
        }
    }

    private fun setUpdatedTakenValueAfterSignedIn(mData : DataResponse?){
        val user: User? = Utils.getUserInfo()
        mData?.user?.let {
            user?.author = it.author
            Utils.setUserPreShare(user)
        }
    }

    private fun isRequestSignIn(mCode : Int)  : Boolean {
        if (mCode==EnumResponseCode.INVALID_AUTHENTICATION.code || mCode == EnumResponseCode.BAD_REQUEST.code || mCode == EnumResponseCode.FORBIDDEN.code){
            return true
        }
        return false
    }
}