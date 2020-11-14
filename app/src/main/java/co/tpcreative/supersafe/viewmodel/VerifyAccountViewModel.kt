package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.ChangeUserIdRequest
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.UserRequest
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.User
import kotlinx.coroutines.Dispatchers

class VerifyAccountViewModel(private val userViewModel: UserViewModel)  : BaseViewModel(){
    val TAG = VerifyAccountViewModel::class.java.simpleName
    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    var code : String = ""
        set(value) {
            field = value
            validationCode(value)
        }

    var email : String = ""
    set(value) {
        field = value
        validationEmail(value)
    }

    private fun validationCode(mValue : String){
        if (mValue.isEmpty()){
            putError(EnumValidationKey.EDIT_TEXT_CODE, "Request enter code")
        }else if (!Utils.isValid(mValue)){
            putError(EnumValidationKey.EDIT_TEXT_CODE, "Code invalid")
        }else if(mValue.length< 6 || mValue.length>6){
            putError(EnumValidationKey.EDIT_TEXT_CODE, "Must be 6 character")
        }
        else{
            putError(EnumValidationKey.EDIT_TEXT_CODE)
        }
        errorMessages.postValue(errorMessages.value)
        Utils.Log(TAG,"Print ${errorMessages.value?.toJson()} $mValue")
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

    fun verifyCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.verifyCode(getVerifyRequest())
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error!!){
                        putErrorResponse(EnumValidationKey.EDIT_TEXT_CODE,getString(R.string.the_code_not_signed_up))
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        setPremiumData(mResult.data.data)
                        emit(mResult)
                    }
                }
                else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        finally {
            isLoading.postValue(false)
        }
    }

    fun resendCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.resendCode(getRequestCode())
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error!!){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        mResult.data.data?.requestCode?.code?.let { setCodeRequest(it) }
                        val mResultSentEmail = userViewModel.sendEmail(EnumStatus.SIGN_IN)
                        when(mResultSentEmail.status){
                            Status.SUCCESS -> emit(mResultSentEmail)
                            else -> emit(Resource.error(mResultSentEmail.code?: Utils.CODE_EXCEPTION, mResultSentEmail.message ?:"",null))
                        }
                    }
                }
                else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        finally {
            isLoading.postValue(false)
        }
    }

    fun changeEmail() = liveData(Dispatchers.IO) {
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.changedUserId(getRequestChangeEmail())
            when(mResult.status){
                Status.SUCCESS ->{
                    if (mResult.data?.error!!){
                        putErrorResponse(EnumValidationKey.EDIT_TEXT_EMAIL,mResult.data.responseMessage!!)
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        changedUserData(mResult.data.data)
                        emit(mResult)
                    }
                }
                else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }catch (e : Exception){
            Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
        }
        finally {
            isLoading.postValue(false)
        }
    }

    fun sendCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.checkedUserId(UserRequest())
            when(mResult.status){
                Status.SUCCESS ->{
                    if (mResult.data?.error!!){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        val mResultResend = userViewModel.resendCode(getRequestCode())
                        when(mResultResend.status){
                            Status.SUCCESS -> {
                                if (mResultResend.data?.error!!){
                                    emit(Resource.error(mResultResend.data.responseCode?: Utils.CODE_EXCEPTION, mResultResend.data.responseMessage ?:"",null))
                                }else{
                                    mResultResend.data.data?.requestCode?.code?.let { setCodeRequest(it) }
                                    val mResultSentEmail = userViewModel.sendEmail(EnumStatus.SIGN_IN)
                                    when(mResultSentEmail.status){
                                        Status.SUCCESS -> emit(mResultSentEmail)
                                        else -> emit(Resource.error(mResultSentEmail.code?: Utils.CODE_EXCEPTION, mResultSentEmail.message ?:"",null))
                                    }
                                }
                            }
                            else -> emit(Resource.error(mResultResend.code?: Utils.CODE_EXCEPTION, mResultResend.message ?:"",null))
                        }
                    }
                }else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }
        catch (e : Exception){
            e.printStackTrace()
            Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
        }
        finally {
            isLoading.postValue(false)
        }
    }

    private fun getVerifyRequest() : VerifyCodeRequest{
        val mUser = Utils.getUserInfo()
        val request = VerifyCodeRequest()
        request.code = code
        request.user_id = mUser?.email
        request._id = mUser?._id
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        return request
    }

    private fun getRequestCode() : RequestCodeRequest {
        return RequestCodeRequest(Utils.getUserId(), Utils.getAccessToken(), SuperSafeApplication.getInstance().getDeviceId())
    }

    private fun getRequestChangeEmail() : ChangeUserIdRequest{
        return ChangeUserIdRequest(email)
    }

    private fun setCodeRequest(mCode : String){
        val mUser: User? = Utils.getUserInfo()
        mUser?.code = mCode
        Utils.setUserPreShare(mUser)
    }

    private fun setPremiumData(mData : DataResponse?){
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            mUser.verified = true
            if (mData == null) {
                return
            }
            if (mData.premium != null) {
                mUser.premium = mData.premium
            }
            SuperSafeApplication.getInstance().writeUserSecret(mUser)
            Utils.setUserPreShare(mUser)
        }
    }

    private fun changedUserData(mData : DataResponse?){
        val mUser = Utils.getUserInfo()
        if (mData?.author != null) {
            if (mUser != null) {
                mUser.author = mData.author
                mUser.email = email
                mUser.other_email = email
                mUser.change = true
                Utils.setUserPreShare(mUser)
            }
        }
    }
}