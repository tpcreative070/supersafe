package co.tpcreative.supersafe.common.api;
import java.util.Map;
import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.request.DriveApiRequest;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.response.SignInResponse;
import co.tpcreative.supersafe.common.response.SyncCategoriesResponse;
import co.tpcreative.supersafe.common.response.SyncResponse;
import co.tpcreative.supersafe.common.response.UserCloudResponse;
import co.tpcreative.supersafe.common.response.VerifyCodeResponse;
import co.tpcreative.supersafe.model.DriveAbout;
import co.tpcreative.supersafe.model.EmailToken;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RootAPI{
    String ROOT_GOOGLE_DRIVE = "https://www.googleapis.com/";
    String REFRESH_TOKEN = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    String SEND_MAIL = "/v1.0/me/sendMail";
    String ADD_EMAIL_TOKEN = "/api/user/addEmailToken";
    String SIGN_UP = "/api/user/signup";
    String SIGN_IN = "/api/user/signin";
    String UPDATE = "/api/user/update";
    String UPDATE_TOKEN = "/api/user/updateToken";

    String VERIFY_CODE = "api/user/verifycode";
    String RESEND_CODE = "api/user/resendcode";
    String CHECK_USER_CLOUD = "/api/usercloud/check";
    String ADD_USER_CLOUD = "/api/usercloud/add/";
    String CHECK_USER_ID = "/api/user/checkUser";
    String USER_INFO = "/api/user/userInfo";
    String GET_LIST_FILES_SYNC = "/api/items/listFilesSync";
    String GET_LIST_CATEGORIES_SYNC = "/api/category/listCategoriesSync";
    String CATEGORIES_SYNC = "/api/category/syncCategories";
    String DELETE_CATEGORIES = "/api/category/onDelete";
    String SYNC_DATA = "/api/items/syncData";
    String DELETE_OWN_ITEMS = "/api/items/onDelete";
    String GET_DRIVE_ABOUT = "/drive/v3/about?fields=user,storageQuota,kind";
    String CREATE_FOLDER = "/drive/v3/files";
    String CHECK_IN_APP_FOLDER_EXITING = "/drive/v3/files";
    String GET_LIST_FILE_IN_APP_FOLDER = "/drive/v3/files";
    String GET_FILES_INFO = "/drive/v3/files/{id}";
    String DELETE_CLOUD_ITEM = "/drive/v3/files/{id}";
    String UPLOAD_FILE_TO_GOOGLE_DRIVE = "/upload/drive/v3/files?uploadType=multipart";
    String DOWNLOAD_FILE_FROM_GOOGLE_DRIVE = "/drive/v3/files/{id}?alt=media";
    String CHECKOUT = "/api/checkout";

    String AUTHOR = "/api/track/syncDevices";
    String CHECK_VERSION = "/api/track/version";



    @FormUrlEncoded
    @POST(AUTHOR)
    Observable<BaseResponse> onAuthor(@FieldMap Map<String, String> request);


    @POST(CHECK_VERSION)
    Observable<BaseResponse> onCheckVersion();


    @FormUrlEncoded
    @POST(SIGN_UP)
    Observable<SignInResponse> onSignUP(@FieldMap Map<String,String> request);

    @FormUrlEncoded
    @POST(SIGN_IN)
    Observable<SignInResponse> onSignIn(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(VERIFY_CODE)
    Observable<BaseResponse> onVerifyCode(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(RESEND_CODE)
    Observable<VerifyCodeResponse> onResendCode(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(CHECK_USER_CLOUD)
    Observable<UserCloudResponse> onCheckUserCloud(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(ADD_USER_CLOUD)
    Observable<UserCloudResponse> onAddUserCloud(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(UPDATE)
    Observable<SignInResponse> onUpdateUser(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(UPDATE_TOKEN)
    Observable<SignInResponse> onUpdateToken(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(CHECKOUT)
    Observable<BaseResponse> onCheckout(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(CHECK_USER_ID)
    Observable<VerifyCodeResponse> onCheckUserId(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(USER_INFO)
    Observable<BaseResponse> onUserInfo(@FieldMap Map<String,String>request);

    @FormUrlEncoded
    @POST(GET_LIST_FILES_SYNC)
    Observable<SyncResponse> onListFilesSync(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(SYNC_DATA)
    Observable<SyncResponse> onSyncData(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(DELETE_OWN_ITEMS)
    Observable<SyncResponse> onDeleteOwnItems(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(CATEGORIES_SYNC)
    Observable<SyncCategoriesResponse> onCategoriesSync(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(GET_LIST_CATEGORIES_SYNC)
    Observable<SyncCategoriesResponse> onListCategoriesSync(@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(DELETE_CATEGORIES)
    Observable<SyncCategoriesResponse> onDeleteCategories(@FieldMap Map<String,Object>request);


    @Headers({"Accept: application/json"})
    @GET(GET_DRIVE_ABOUT)
    Observable<DriveAbout> onGetDriveAbout(@Header("Authorization") String token);

    @Headers({"Accept: application/json"})
    @POST(SEND_MAIL)
    Call<ResponseBody> onSendMail(@Header("Authorization") String token, @Body EmailToken body);

    @FormUrlEncoded
    @POST()
    Observable<EmailToken> onRefreshEmailToken(@Url String url,@FieldMap Map<String,Object>request);

    @FormUrlEncoded
    @POST(ADD_EMAIL_TOKEN)
    Observable<BaseResponse> onAddEmailToken(@FieldMap Map<String,Object>request);


    @Headers({"Accept: application/json"})
    @POST(CREATE_FOLDER)
    Observable<DriveAbout> onCrateFolder(@Header("Authorization") String token, @Body DriveApiRequest request);


    @Headers({"Accept: application/json"})
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    Observable<DriveAbout> onCheckInAppFolderExisting(@Header("Authorization") String token, @Query("q") String title,@Query("spaces")String value);

    @Headers({"Accept: application/json"})
    @GET(GET_FILES_INFO)
    Observable<DriveAbout> onGetFilesInfo(@Header("Authorization") String token, @Path("id") String id);

    @Headers({"Accept: application/json"})
    @DELETE(DELETE_CLOUD_ITEM)
    Observable<Response<DriveAbout>> onDeleteCloudItem(@Header("Authorization") String token, @Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    Observable<DriveAbout> onGetListFile(@Header("Authorization") String token, @Query("q") String title,@Query("spaces")String value);

    @Headers({"Accept: application/json"})
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    Observable<DriveAbout> onGetListFileInAppFolder(@Header("Authorization") String token,@Query("spaces")String value);


    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    Call<DriveResponse> uploadFileMultipleInAppFolder(
                                        @Header("Authorization") String authToken,
                                        @Part MultipartBody.Part metaPart,
                                        @Part MultipartBody.Part dataPart,
                                        @Query("type") String type);

    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    Observable<Response<ResponseBody>> downloadDriveFile(@Header("Authorization") String authToken,@Path("id") String id);
}
