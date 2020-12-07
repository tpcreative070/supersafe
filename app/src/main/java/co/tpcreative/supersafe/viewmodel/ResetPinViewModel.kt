package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.RequestCodeRequest
import co.tpcreative.supersafe.common.request.TwoFactoryAuthenticationRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.User
import kotlinx.coroutines.Dispatchers

class ResetPinViewModel(private val userViewModel: UserViewModel)  : VerifyViewModel(userViewModel) {

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

    var secretPin  = ""

    fun sendRequestCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.resendCode(getRequestCode())
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error!!){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        mResult.data.data?.requestCode?.code?.let { setCodeRequest(it) }
                        emit(Resource.success(true))
                    }
                }
                else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }catch (e : Exception){
            e.printStackTrace()
            emit(Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null))
        }
        finally {
            isLoading.postValue(false)
        }
    }

    private fun getRequestCode() : RequestCodeRequest {
        return RequestCodeRequest(Utils.getUserId(), Utils.getAccessToken(), SuperSafeApplication.getInstance().getDeviceId())
    }

    private fun setCodeRequest(mCode : String){
        val mUser: User? = Utils.getUserInfo()
        mUser?.code = mCode
        Utils.setUserPreShare(mUser)
    }

    fun verifyTwoFactoryAuthentication() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mRequest = TwoFactoryAuthenticationRequest(secret_pin = secretPin)
            Utils.Log(TAG,mRequest.toJson())
            val mResult =  userViewModel.verifyTwoFactoryAuthenticationCor(mRequest)
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error ==true){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, getErrorMessage(mResult.data?.responseMessage ?:""),null))
                    }else{
                        emit(mResult)
                    }
                }else ->{
                emit(Resource.error(mResult.data?.responseCode?: Utils.CODE_EXCEPTION, getErrorMessage(mResult.data?.responseMessage ?:""),null))
            }
            }
        }catch (e : Exception){
            e.printStackTrace()
            emit(Resource.error(Utils.CODE_EXCEPTION,getErrorMessage(e.message ?:""),null))
        }
        finally {
            isLoading.postValue(false)
        }
    }

    fun getTwoFactoryInfo() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mResult =  userViewModel.getTwoFactoryAuthenticationCor(TwoFactoryAuthenticationRequest())
            when(mResult.status){
                Status.SUCCESS -> {
                    emit(mResult)
                }else ->{
                emit(Resource.error(mResult.data?.responseCode?: Utils.CODE_EXCEPTION, mResult.data?.responseMessage ?:"",null))
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

    private fun getErrorMessage(message : String) : String{
        if (message == "invalid_secret_in_request"){
            return SuperSafeApplication.getInstance().getString(R.string.wrong_secret_pin)
        }
        return getString(R.string.server_error_occurred)
    }


}