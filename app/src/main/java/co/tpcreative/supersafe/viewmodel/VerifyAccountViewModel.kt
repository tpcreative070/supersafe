package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import kotlinx.coroutines.Dispatchers

class VerifyAccountViewModel(val userViewModel: UserViewModel)  : ViewModel(){
    val TAG = VerifyAccountViewModel::class.java.simpleName
    val errorMessages : MutableLiveData<MutableMap<String, String>> by lazy {
        MutableLiveData<MutableMap<String,String>>()
    }

    val isLoading : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val errorResponseMessage  : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    var code : String = ""
        set(value) {
            field = value
            validationCode(value)
        }

    private fun validationCode(mValue : String){
        if (mValue.isEmpty()){
            errorMessages.value?.set(EnumValidationKey.EDIT_CODE.name, "Request enter email")
        }else if (!Utils.isValidEmail(mValue)){
            errorMessages.value?.set(EnumValidationKey.EDIT_CODE.name, "Email invalid")
        }
        else{
            errorMessages.value?.remove(EnumValidationKey.EDIT_CODE.name)
        }
        errorMessages.postValue(errorMessages.value)
        Utils.Log(TAG,"Print ${errorMessages.value?.toJson()} $mValue")
    }

    init {
        errorMessages.value = mutableMapOf(EnumValidationKey.EDIT_TEXT_EMAIL.name to "")
        isLoading.value = false
        errorResponseMessage.value = ""
    }
    fun verifyCode() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.verifyCode(VerifyCodeRequest())
            when(mResult.status){
                Status.SUCCESS -> emit(mResult)
                else -> emit(Resource.error(mResult.code?: Utils.CODE_EXCEPTION, mResult.message ?:"",null))
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}