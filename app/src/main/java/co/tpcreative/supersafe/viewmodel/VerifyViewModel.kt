package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.User
import kotlinx.coroutines.Dispatchers

class VerifyViewModel(private val userViewModel: UserViewModel) : BaseViewModel(){
    val TAG = this::class.java.simpleName
    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    var code : String = ""
        set(value) {
            field = value
            validationCode(value)
        }

    private fun validationCode(mValue : String){
        if (mValue.isEmpty()){
            putError(EnumValidationKey.EDIT_CODE, "Request enter code")
        }else if (!Utils.isValid(mValue)){
            putError(EnumValidationKey.EDIT_CODE, "Email invalid")
        }else if(mValue.length< 6 || mValue.length>6){
            putError(EnumValidationKey.EDIT_CODE, "Must be 6 character")
        }
        else{
            putError(EnumValidationKey.EDIT_CODE)
        }
        errorMessages.postValue(errorMessages.value)
        Utils.Log(TAG,"Print ${errorMessages.value?.toJson()} $mValue")
    }

    fun verifyCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.verifyCode(getVerifyRequest())
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error!!){
                        putErrorResponse(EnumValidationKey.EDIT_CODE,getString(R.string.the_code_not_signed_up))
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
                    if (!mResult.data?.error!!){
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
}