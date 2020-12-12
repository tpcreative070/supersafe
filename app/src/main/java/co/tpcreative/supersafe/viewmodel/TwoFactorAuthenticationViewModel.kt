package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.TwoFactorAuthenticationRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.ItemModel
import kotlinx.coroutines.Dispatchers
enum class EnumTwoFactoryAuthentication {
    DISABLE,
    ENABLE,
    CHANGE,
    REQUEST_CHANGE,
    GENERATE,
    REQUEST_GENERATE,
    NONE
}
class TwoFactorAuthenticationViewModel(private val userViewModel: UserViewModel) : BaseViewModel<ItemModel>() {
    val TAG = this::class.java.simpleName
    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    var newSecretPin : String?=null
        set(value) {
            field = value
            validationCode(value ?:"")
        }

    var secretPin  = ""
    var isEnabled = false

     private fun validationCode(mValue : String){
        if (mValue.isEmpty()){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "Request enter SECRET PIN")
        }else if (!Utils.isValid(mValue)){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "Code invalid")
        }else if(mValue.length< 6 || mValue.length>6){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "Must be 6 digit numbers")
        }
        else if (newSecretPin==secretPin){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "New secret pin must differ from old secret pin")
        }
        else if (newSecretPin == Utils.getPinFromSharedPreferences()){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "New secret pin must differ from REAL PIN")
        }
        else if (newSecretPin == Utils.getFakePinFromSharedPreferences()){
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN, "New secret pin must differ from FAKE PIN")
        }
        else{
            putError(EnumValidationKey.EDIT_TEXT_SECRET_PIN)
        }
        Utils.Log(TAG,"Print ${errorMessages.value?.toJson()} $mValue")
    }


    fun getTwoFactoryInfo() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mResult =  userViewModel.getTwoFactoryAuthenticationCor(TwoFactorAuthenticationRequest())
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

    fun switchStatusTwoFactoryInfo() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mResult =  userViewModel.changeTwoFactoryAuthenticationCor(TwoFactorAuthenticationRequest(isEnabled = isEnabled,secret_pin = secretPin))
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error ==true){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        emit(mResult)
                    }
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

    fun verifyTwoFactoryAuthentication() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mRequest = TwoFactorAuthenticationRequest(secret_pin = secretPin ,isEnabled = isEnabled)
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

    private fun getErrorMessage(message : String) : String{
        if (message == "invalid_secret_in_request"){
            return SuperSafeApplication.getInstance().getString(R.string.wrong_secret_pin)
        }
        return getString(R.string.server_error_occurred)
    }

    fun changeTwoFactoryAuthentication() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mRequest = TwoFactorAuthenticationRequest(secret_pin = secretPin ,new_secret_pin  = newSecretPin,isEnabled = isEnabled)
            Utils.Log(TAG,mRequest.toJson())
            val mResult =  userViewModel.changeTwoFactoryAuthenticationCor(mRequest)
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error ==true){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        emit(mResult)
                    }
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

    fun addTwoFactoryAuthentication() = liveData(Dispatchers.Main){
        try {
            isLoading.postValue(true)
            val mRequest = TwoFactorAuthenticationRequest(secret_pin  = newSecretPin,isEnabled = isEnabled)
            Utils.Log(TAG,mRequest.toJson())
            val mResult =  userViewModel.addTwoFactoryAuthenticationCor(mRequest)
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error ==true){
                        emit(Resource.error(mResult.data.responseCode?: Utils.CODE_EXCEPTION, mResult.data.responseMessage ?:"",null))
                    }else{
                        emit(mResult)
                    }
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
}