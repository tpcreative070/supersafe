package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class UserViewModel(private val service: UserService, micService: MicService) : BaseViewModel<ItemModel>(){
    private val emailViewModel = EmailOutlookViewModel(micService)
    val TAG = this::class.java.simpleName

    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

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
                        emailResponseError(mResultSignUp.data.responseMessage!!)
                        emit(Resource.error(mResultSignUp.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignUp.data.responseMessage ?:"",null))
                    }else{
                        /*Clean up cache before enter up app*/
                        //Utils.clearAppDataAndReCreateData()
                        Utils.Log(TAG,"clearAppDataAndReCreateData")
                        val mData: DataResponse? = mResultSignUp.data.data
                        Utils.setUserPreShare(mData?.user)
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
                        emailResponseError(getString(R.string.signed_in_failed))
                        emit(Resource.error(mResultSignIn.data.responseCode ?: Utils.CODE_EXCEPTION, mResultSignIn.data.responseMessage ?:"",null))
                    }else{
                        /*Clean up cache before enter up app*/
                        //Utils.clearAppDataAndReCreateData()
                        Utils.Log(TAG,"clearAppDataAndReCreateData")
                        val mData: DataResponse? = mResultSignIn.data.data
                        Utils.setUserPreShare(mData?.user)
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
                                setUpdatedTokenValue(mData)
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
                                        emailResponseError(getString(R.string.signed_in_failed))
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

    suspend fun getUserInfo() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult = service.userInfo(UserRequest())
                when (mResult.status) {
                    Status.SUCCESS -> {
                        Utils.Log(TAG, mResult.data?.responseMessage)
                        if (!(mResult.data?.error)!!) {
                            mResult.data.data?.let { updatedUserInfo(it) }
                            Resource.success(true)
                        }else{
                            Resource.error(mResult.code ?:Utils.CODE_EXCEPTION ,mResult.message ?: "",null)
                        }
                    }
                    else ->{
                        Utils.Log(TAG, mResult.message)
                        Resource.error(mResult.code ?:Utils.CODE_EXCEPTION ,mResult.message ?: "",null)
                    }
                }
                Resource.success(false)
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun getTracking() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult = service.tracking(TrackingRequest(Utils.getUserId(), SuperSafeApplication.getInstance().getDeviceId()))
                when (mResult.status) {
                    Status.SUCCESS -> {
                        Utils.Log(TAG, mResult.data?.responseMessage)
                        Resource.success(true)
                    }
                    else ->{
                        Utils.Log(TAG, mResult.message)
                        Resource.error(mResult.code ?:Utils.CODE_EXCEPTION ,mResult.message ?: "",null)
                    }
                }
                Resource.success(false)
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun verifyCode(request : VerifyCodeRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO){
            try {
               service.verifyCode(request)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun resendCode(request: RequestCodeRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO){
            try {
                service.resendCode(request)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun sendEmail(status: EnumStatus) : Resource<String> {
        return withContext(Dispatchers.IO){
            try {
                emailViewModel.sendEmail(status)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun changedUserId(request: ChangeUserIdRequest) : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                service.updateUser(request)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun checkedUserId(request : UserRequest? = UserRequest()) : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                service.checkUserId(request!!)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun addUserCloud(request: UserCloudRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO){
            try {
                service.addUserCloud(request)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun checkingUserCloud(request: UserCloudRequest) : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                service.checkingUserCloud(request)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    suspend fun checkoutItems(request: CheckoutRequest) : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                service.checkout(request)
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
            }
        }
    }

    private fun validationEmail(mValue : String){
        if (mValue.isEmpty()){
            putError(EnumValidationKey.EDIT_TEXT_EMAIL, "Request enter email")
        }else if (!Utils.isValidEmail(mValue)){
            putError(EnumValidationKey.EDIT_TEXT_EMAIL, "Email invalid")
        }
        else{
            putError(EnumValidationKey.EDIT_TEXT_EMAIL)
        }
    }

    private fun emailResponseError(mValue : String){
        putErrorResponse(EnumValidationKey.EDIT_TEXT_EMAIL,mValue)
        Utils.Log(TAG,"Print ${errorResponseMessage.value?.toJson()} $mValue")
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

    private fun updatedUserInfo(mData : DataResponse){
        val mUser = Utils.getUserInfo()
        if (mData.premium != null && mData.email_token != null) {
            mUser?.premium = mData.premium
            mUser?.email_token = mData.email_token
            Utils.setUserPreShare(mUser)
        }
    }
}