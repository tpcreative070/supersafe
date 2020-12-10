package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.extension.putUserPreShare
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.ChangeUserIdRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import kotlinx.coroutines.Dispatchers

class VerifyAccountViewModel(private val userViewModel: UserViewModel)  : VerifyViewModel(userViewModel){
    override val TAG = VerifyAccountViewModel::class.java.simpleName
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

    var email : String = Utils.getUserId() ?:""
    set(value) {
        field = value
        validationEmail(value)
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

    private fun getRequestChangeEmail() : ChangeUserIdRequest{
        return ChangeUserIdRequest(email)
    }
    private fun changedUserData(mData : DataResponse?){
        val mUser = Utils.getUserInfo()
        if (mData?.author != null) {
            if (mUser != null) {
                mUser.author = mData.author
                mUser.email = email
                mUser.other_email = email
                mUser.change = true
                Utils.putUserPreShare(mUser)
            }
        }
    }
}