package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService (){
    val TAG = this::class.java.simpleName
    suspend fun signUp(request : SignUpRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.signUpCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun signIn(request : SignInRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.signInCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                Utils.Log(TAG,request.toJson())
                throwable.printStackTrace()
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun verifyCode(request: VerifyCodeRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onVerifyCodeCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun resendCode(request: RequestCodeRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onResendCodeCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun checkingUserCloud(request: UserCloudRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onCheckUserCloudCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun addUserCloud(request: UserCloudRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onAddUserCloudCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun updateUser(request: ChangeUserIdRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onUpdateUserCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun updateToken(request : UserRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.updateTokenCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun deleteOldAccessTokenCor(request : UserRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onDeleteOldAccessTokenCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun checkout(request : CheckoutRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onCheckoutCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun checkUserId(request : UserRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onCheckUserIdCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun userInfo(request : UserRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onUserInfoCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun tracking(request : TrackingRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.trackingCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }
}