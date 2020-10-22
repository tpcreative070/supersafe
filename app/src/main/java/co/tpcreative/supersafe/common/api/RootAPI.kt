package co.tpcreative.supersafe.common.api
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.request.*
import co.tpcreative.supersafe.common.requestimport.*
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.model.DriveAbout
import co.tpcreative.supersafe.model.EmailToken
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface RootAPI {
    @POST(TRACKING)
    open fun onTracking(@Body request: TrackingRequest): Observable<RootResponse>?

    @POST(CHECK_VERSION)
    open fun onCheckVersion(): Observable<BaseResponse>?

    @POST(SIGN_UP)
    open fun onSignUP(@Body request: SignUpRequest): Observable<RootResponse>?

    @POST(SIGN_IN)
    open fun onSignIn(@Body request: SignInRequest): Observable<RootResponse>?

    @POST(VERIFY_CODE)
    open fun onVerifyCode(@Body request: VerifyCodeRequest): Observable<RootResponse>?

    @POST(RESEND_CODE)
    open fun onResendCode(@Body request: RequestCodeRequest): Observable<RootResponse>?

    @POST(CHECK_USER_CLOUD)
    open fun onCheckUserCloud(@Body request: UserCloudRequest): Observable<RootResponse>?

    @POST(ADD_USER_CLOUD)
    open fun onAddUserCloud(@Body request: UserCloudRequest?): Observable<RootResponse>?

    @POST(UPDATE)
    open fun onUpdateUser(@Body request: ChangeUserIdRequest): Observable<RootResponse>?

    @POST(REFRESH_USER_TOKEN)
    open fun onUpdateToken(@Body request: UserRequest): Observable<RootResponse>?

    @POST(DELETE_OLD_ACCESS_TOKEN)
    open fun onDeleteOldAccessToken(@Body request: UserRequest): Observable<RootResponse>?

    @POST(CHECKOUT)
    open fun onCheckout(@Body request: CheckoutRequest): Observable<RootResponse>?

    @POST(CHECK_USER_ID)
    open fun onCheckUserId(@Body request: UserRequest): Observable<RootResponse>?

    @POST(USER_INFO)
    open fun onUserInfo(@Body request: UserRequest): Observable<RootResponse>?

    @POST(GET_LIST_FILES_SYNC)
    open fun onListFilesSync(@Body request: SyncItemsRequest): Observable<RootResponse>?

    @POST(SYNC_DATA)
    open fun onSyncData(@Body request: SyncItemsRequest): Observable<RootResponse>?

    @POST(DELETE_OWN_ITEMS)
    open fun onDeleteOwnItems(@Body request: SyncItemsRequest): Observable<RootResponse>?

    @POST(CATEGORIES_SYNC)
    open fun onCategoriesSync(@Body request: CategoriesRequest?): Observable<RootResponse>?

    @POST(GET_LIST_CATEGORIES_SYNC)
    open fun onListCategoriesSync(@Body request: CategoriesRequest?): Observable<RootResponse>?

    @POST(DELETE_CATEGORIES)
    open fun onDeleteCategories(@Body request: CategoriesRequest?): Observable<RootResponse>?

    @Headers("Accept: application/json")
    @GET(GET_DRIVE_ABOUT)
    open fun onGetDriveAbout(@Header("Authorization") token: String?): Observable<DriveAbout>?

    @Headers("Accept: application/json")
    @POST(SEND_MAIL)
    open fun onSendMail(@Header("Authorization") token: String?, @Body body: EmailToken): Call<ResponseBody>?

    @FormUrlEncoded
    @POST
    open fun onRefreshEmailToken(@Url url: String?, @FieldMap request: MutableMap<String, Any?>): Observable<EmailToken>?

    @POST(ADD_EMAIL_TOKEN)
    open fun onAddEmailToken(@Body request: OutlookMailRequest?): Observable<BaseResponse>?

    @Headers("Accept: application/json")
    @POST(CREATE_FOLDER)
    open fun onCrateFolder(@Header("Authorization") token: String?, @Body request: DriveApiRequest): Observable<DriveAbout>?

    @Headers("Accept: application/json")
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    open fun onCheckInAppFolderExisting(@Header("Authorization") token: String?, @Query("q") title: String?, @Query("spaces") value: String?): Observable<DriveAbout>?

    @Headers("Accept: application/json")
    @GET(GET_FILES_INFO)
    open fun onGetFilesInfo(@Header("Authorization") token: String?, @Path("id") id: String?): Observable<DriveAbout>?

    @Headers("Accept: application/json")
    @DELETE(DELETE_CLOUD_ITEM)
    open fun onDeleteCloudItem(@Header("Authorization") token: String?, @Path("id") id: String?): Observable<Response<DriveAbout>>?

    @Headers("Accept: application/json")
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    open fun onGetListFile(@Header("Authorization") token: String?, @Query("q") title: String?, @Query("spaces") value: String?): Observable<DriveAbout>?

    @Headers("Accept: application/json")
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    open fun onGetListFileInAppFolder(@Header("Authorization") token: String?, @Query("spaces") value: String?): Observable<DriveAbout>?

    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    open fun uploadFileMultipleInAppFolder(
            @Header("Authorization") authToken: String?,
            @Part metaPart: MultipartBody.Part?,
            @Part dataPart: MultipartBody.Part?,
            @Query("type") type: String?): Call<DriveResponse>?

    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    open fun downloadDriveFile(@Header("Authorization") authToken: String?, @Path("id") id: String?): Observable<Response<ResponseBody>>?

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
        const val CHECK_VERSION: String = "/api/track/version"
    }
}