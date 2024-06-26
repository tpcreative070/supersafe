package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EmailToken
import okhttp3.MultipartBody

class ApiHelper() {

    /*This is area for drive*/
    suspend fun onGetDriveAboutCor(authToken: String?) =  getDriveApi()?.onGetDriveAboutCor(authToken)
    suspend fun uploadFileMultipleInAppFolderCor(authToken: String?,
                        metaPart: MultipartBody.Part?,
                        dataPart: MultipartBody.Part?,type: String?)  =  getDriveApi()?.uploadFileMultipleInAppFolderCor(authToken,metaPart,dataPart,type)
    suspend fun downloadDriveFileCor(authToken: String?,id: String?, service : ApiService) =  service.downloadDriveFileCor(authToken,id)
    suspend fun onDeleteCloudItemCor(token: String?, id: String?) = getDriveApi()?.onDeleteCloudItemCor(token,id)
    suspend fun onGetListFileInAppFolderCor(token: String?, space: String?) = getDriveApi()?.onGetListFileInAppFolderCor(token,space)


    /*This is area for mic*/
    suspend fun onSendMailCor(token: String?,body: EmailToken) = getMicApi()?.onSendMailCor(token,body)
    suspend fun onRefreshEmailTokenCor(url: String?,request: MutableMap<String?, Any?>) = getMicApi()?.onRefreshEmailTokenCor(url,request)
    suspend fun onAddEmailTokenCor(request: OutlookMailRequest?) = getApiCor()?.onAddEmailTokenCor(request)


    /*This is area for item*/
    suspend fun onListFilesSyncCor(data : SyncItemsRequest) =   getApiCor()?.onListFilesSyncCor(data)
    suspend fun onSyncDataCor(data : SyncItemsRequest) =   getApiCor()?.onSyncDataCor(data)
    suspend fun onDeleteOwnItemsCor(data : SyncItemsRequest) =   getApiCor()?.onDeleteOwnItemsCor(data)
    suspend fun trackingSyncCor(request: TrackingSyncRequest) = getApiCor()?.onTrackingSyncCor(request)


    /*This is area for user*/
    suspend fun signUpCor(request: SignUpRequest) = getApiCor()?.onSignUPCor(request)
    suspend fun signInCor(request: SignInRequest) = getApiCor()?.onSignInCor(request)
    suspend fun onVerifyCodeCor(request: VerifyCodeRequest) = getApiCor()?.onVerifyCodeCor(request)
    suspend fun onResendCodeCor(request: RequestCodeRequest) = getApiCor()?.onResendCodeCor(request)
    suspend fun onCheckUserCloudCor(request: UserCloudRequest) = getApiCor()?.onCheckUserCloudCor(request)
    suspend fun onAddUserCloudCor(request: UserCloudRequest) = getApiCor()?.onAddUserCloudCor(request)
    suspend fun onUpdateUserCor(request: ChangeUserIdRequest) = getApiCor()?.onUpdateUserCor(request)

    suspend fun onAddTwoFactorAuthenticationCor(request: TwoFactorAuthenticationRequest) = getApiCor()?.onAddTwoFactorAuthenticationCor(request)
    suspend fun onChangeTwoFactorAuthenticationCor(request: TwoFactorAuthenticationRequest) = getApiCor()?.onChangeTwoFactorAuthenticationCor(request)
    suspend fun onVerifyTwoFactorAuthenticationCor(request: TwoFactorAuthenticationRequest) = getApiCor()?.onVerifyTwoFactorAuthenticationCor(request)
    suspend fun onGetTwoFactorAuthenticationCor(request: TwoFactorAuthenticationRequest) = getApiCor()?.onGetTwoFactorAuthenticationCor(request)

    suspend fun onDeleteOldAccessTokenCor(request: UserRequest) = getApiCor()?.onDeleteOldAccessTokenCor(request)
    suspend fun updateTokenCor(request: UserRequest) = getApiCor()?.onUpdateTokenCor(request)
    suspend fun trackingCor(request: TrackingRequest) = getApiCor()?.onTrackingCor(request)
    suspend fun onCheckoutCor(request: CheckoutRequest) = getApiCor()?.onCheckoutCor(request)
    suspend fun onCheckUserIdCor(request: UserRequest) = getApiCor()?.onCheckUserIdCor(request)
    suspend fun onUserInfoCor(request: UserRequest) = getApiCor()?.onUserInfoCor(request)

    /*This is area for category*/
    suspend fun onCategoriesSyncCor(request:CategoriesRequest) = getApiCor()?.onCategoriesSyncCor(request)
    suspend fun onDeleteCategoriesCor(request:CategoriesRequest) = getApiCor()?.onDeleteCategoriesCor(request)



    companion object {
        private var mInstance : ApiHelper? = null
        fun getInstance() : ApiHelper? {
            if (mInstance==null){
                mInstance = ApiHelper()
            }
            return mInstance
        }
    }

    private fun getDriveApi() : ApiService? {
        return SuperSafeApplication.serverDriveApiCor
    }

    private fun getMicApi() : ApiService? {
        return SuperSafeApplication.serverMicCor
    }

    private fun getApiCor() : ApiService? {
        return SuperSafeApplication.serverApiCor
    }
}