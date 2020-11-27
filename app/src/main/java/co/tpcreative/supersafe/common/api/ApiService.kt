package co.tpcreative.supersafe.common.api
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.model.DriveAbout
import co.tpcreative.supersafe.model.EmailToken
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST(TRACKING)
    suspend fun onTrackingCor(@Body request: TrackingRequest): RootResponse?
    @POST(TRACKING_SYNC)
    suspend fun onTrackingSyncCor(@Body request: TrackingSyncRequest): RootResponse?

    @POST(SIGN_UP)
    suspend fun onSignUPCor(@Body request: SignUpRequest): RootResponse?

    @POST(SIGN_IN)
    suspend fun onSignInCor(@Body request: SignInRequest): RootResponse?

    @POST(VERIFY_CODE)
    suspend fun onVerifyCodeCor(@Body request: VerifyCodeRequest): RootResponse?

    @POST(RESEND_CODE)
    suspend fun onResendCodeCor(@Body request: RequestCodeRequest): RootResponse?

    @POST(CHECK_USER_CLOUD)
    suspend fun onCheckUserCloudCor(@Body request: UserCloudRequest): RootResponse?

    @POST(ADD_USER_CLOUD)
    suspend fun onAddUserCloudCor(@Body request: UserCloudRequest?): RootResponse?

    @POST(UPDATE)
    suspend fun onUpdateUserCor(@Body request: ChangeUserIdRequest): RootResponse?

    @POST(REFRESH_USER_TOKEN)
    suspend fun onUpdateTokenCor(@Body request: UserRequest): RootResponse?

    @POST(DELETE_OLD_ACCESS_TOKEN)
    suspend fun onDeleteOldAccessTokenCor(@Body request: UserRequest) : RootResponse?

    @POST(CHECKOUT)
    suspend fun onCheckoutCor(@Body request: CheckoutRequest): RootResponse?

    @POST(CHECK_USER_ID)
    suspend fun onCheckUserIdCor(@Body request: UserRequest): RootResponse?

    @POST(USER_INFO)
    suspend fun onUserInfoCor(@Body request: UserRequest): RootResponse?

    @POST(GET_LIST_FILES_SYNC)
    suspend fun onListFilesSyncCor(@Body request: SyncItemsRequest) : RootResponse?

    @POST(SYNC_DATA)
    suspend fun onSyncDataCor(@Body request: SyncItemsRequest): RootResponse?

    @POST(DELETE_OWN_ITEMS)
    suspend fun onDeleteOwnItemsCor(@Body request: SyncItemsRequest): RootResponse?

    @POST(CATEGORIES_SYNC)
    suspend fun onCategoriesSyncCor(@Body request: CategoriesRequest?): RootResponse?

    @POST(DELETE_CATEGORIES)
    suspend fun onDeleteCategoriesCor(@Body request: CategoriesRequest?): RootResponse?


    @Headers("Accept: application/json")
    @GET(GET_DRIVE_ABOUT)
    suspend fun onGetDriveAboutCor(@Header("Authorization") token: String?): DriveAbout?


    @Headers("Accept: application/json")
    @POST(SEND_MAIL)
    suspend fun onSendMailCor(@Header("Authorization") token: String?, @Body body: EmailToken): ResponseBody?

    @FormUrlEncoded
    @POST
    suspend fun onRefreshEmailTokenCor(@Url url: String?, @FieldMap request: MutableMap<String?, Any?>): EmailToken?

    @POST(ADD_EMAIL_TOKEN)
    suspend fun onAddEmailTokenCor(@Body request: OutlookMailRequest?): BaseResponse?


    @Headers("Accept: application/json")
    @DELETE(DELETE_CLOUD_ITEM)
    suspend fun onDeleteCloudItemCor(@Header("Authorization") token: String?, @Path("id") id: String?): Response<DriveAbout>?



    @Headers("Accept: application/json")
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    suspend fun onGetListFileInAppFolderCor(@Header("Authorization") token: String?, @Query("spaces") value: String?): DriveAbout?

    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    fun uploadFileMultipleInAppFolder(
            @Header("Authorization") authToken: String?,
            @Part metaPart: MultipartBody.Part?,
            @Part dataPart: MultipartBody.Part?,
            @Query("type") type: String?): Call<DriveResponse>?

    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    suspend fun uploadFileMultipleInAppFolderCor(
            @Header("Authorization") authToken: String?,
            @Part metaPart: MultipartBody.Part?,
            @Part dataPart: MultipartBody.Part?,
            @Query("type") type: String?): DriveResponse?


    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    suspend fun downloadDriveFileCor(@Header("Authorization") authToken: String?, @Path("id") id: String?): ResponseBody?

    companion object {
        const val ROOT_GOOGLE_DRIVE: String = "https://www.googleapis.com/"
        const val REFRESH_TOKEN: String = "https://login.microsoftonline.com/common/oauth2/v2.0/token"
        const val SEND_MAIL: String = "/v1.0/me/sendMail"
        const val ADD_EMAIL_TOKEN: String = "/api/supersafe/outlook/addEmailToken"
        const val SIGN_UP: String = "/api/supersafe/user/signup"
        const val SIGN_IN: String = "/api/supersafe/user/signin"
        const val UPDATE: String = "/api/supersafe/user/changeUserId"
        const val REFRESH_USER_TOKEN: String = "/api/supersafe/user/updateToken"
        const val DELETE_OLD_ACCESS_TOKEN: String = "/api/supersafe/user/deleteOldAccessToken"
        const val VERIFY_CODE: String = "api/supersafe/user/verifyCode"
        const val RESEND_CODE: String = "api/supersafe/user/sendCode"
        const val CHECK_USER_CLOUD: String = "/api/supersafe/userCloud/check"
        const val ADD_USER_CLOUD: String = "/api/supersafe/userCloud/add"
        const val CHECK_USER_ID: String = "/api/supersafe/user/checkUser"
        const val USER_INFO: String = "/api/supersafe/user/userInfo"
        const val GET_LIST_CATEGORIES_SYNC: String = "/api/supersafe/category/listCategoriesSync"
        const val CATEGORIES_SYNC: String = "/api/supersafe/category/syncCategories"
        const val DELETE_CATEGORIES: String = "/api/supersafe/category/onDelete"
        const val GET_LIST_FILES_SYNC: String = "/api/supersafe/items/listFilesSync"
        const val SYNC_DATA: String = "/api/supersafe/items/syncData"
        const val DELETE_OWN_ITEMS: String = "/api/supersafe/items/deleteItem"
        const val GET_DRIVE_ABOUT: String = "/drive/v3/about?fields=user,storageQuota,kind"
        const val CREATE_FOLDER: String = "/drive/v3/files"
        const val CHECK_IN_APP_FOLDER_EXITING: String = "/drive/v3/files"
        const val GET_LIST_FILE_IN_APP_FOLDER: String = "/drive/v3/files"
        const val GET_FILES_INFO: String = "/drive/v3/files/{id}"
        const val DELETE_CLOUD_ITEM: String = "/drive/v3/files/{id}"
        const val UPLOAD_FILE_TO_GOOGLE_DRIVE: String = "/upload/drive/v3/files?uploadType=multipart"
        const val DOWNLOAD_FILE_FROM_GOOGLE_DRIVE: String = "/drive/v3/files/{id}?alt=media"
        const val CHECKOUT: String = "/api/supersafe/checkout/transaction"
        const val TRACKING: String = "/api/supersafe/track/tracking"
        const val TRACKING_SYNC: String = "/api/supersafe/items/trackingSync"
        const val CHECK_VERSION: String = "/api/track/version"
    }
}