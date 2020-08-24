package co.tpcreative.supersafe.common.api;
import java.util.Map;
import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.request.CategoriesRequest;
import co.tpcreative.supersafe.common.request.ChangeUserIdRequest;
import co.tpcreative.supersafe.common.request.CheckoutRequest;
import co.tpcreative.supersafe.common.request.DriveApiRequest;
import co.tpcreative.supersafe.common.request.OutlookMailRequest;
import co.tpcreative.supersafe.common.request.RequestCodeRequest;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.SignUpRequest;
import co.tpcreative.supersafe.common.request.SyncItemsRequest;
import co.tpcreative.supersafe.common.request.TrackingRequest;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.request.UserRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.response.DataResponse;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.response.RootResponse;
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
    String ADD_EMAIL_TOKEN = "/api/supersafe/outlook/addEmailToken";
    String SIGN_UP = "/api/supersafe/user/signup";
    String SIGN_IN = "/api/supersafe/user/signin";
    String UPDATE = "/api/supersafe/user/changeUserId";
    String REFRESH_USER_TOKEN = "/api/supersafe/user/updateToken";

    String VERIFY_CODE = "api/supersafe/user/verifyCode";
    String RESEND_CODE = "api/supersafe/user/sendCode";
    String CHECK_USER_CLOUD = "/api/supersafe/userCloud/check";
    String ADD_USER_CLOUD = "/api/supersafe/userCloud/add";
    String CHECK_USER_ID = "/api/supersafe/user/checkUser";
    String USER_INFO = "/api/supersafe/user/userInfo";
    String GET_LIST_CATEGORIES_SYNC = "/api/supersafe/category/listCategoriesSync";
    String CATEGORIES_SYNC = "/api/supersafe/category/syncCategories";
    String DELETE_CATEGORIES = "/api/supersafe/category/onDelete";
    String GET_LIST_FILES_SYNC = "/api/supersafe/items/listFilesSync";
    String SYNC_DATA = "/api/supersafe/items/syncData";
    String DELETE_OWN_ITEMS = "/api/supersafe/items/deleteItem";
    String GET_DRIVE_ABOUT = "/drive/v3/about?fields=user,storageQuota,kind";
    String CREATE_FOLDER = "/drive/v3/files";
    String CHECK_IN_APP_FOLDER_EXITING = "/drive/v3/files";
    String GET_LIST_FILE_IN_APP_FOLDER = "/drive/v3/files";
    String GET_FILES_INFO = "/drive/v3/files/{id}";
    String DELETE_CLOUD_ITEM = "/drive/v3/files/{id}";
    String UPLOAD_FILE_TO_GOOGLE_DRIVE = "/upload/drive/v3/files?uploadType=multipart";
    String DOWNLOAD_FILE_FROM_GOOGLE_DRIVE = "/drive/v3/files/{id}?alt=media";
    String CHECKOUT = "/api/supersafe/checkout/transaction";

    String TRACKING = "/api/supersafe/track/tracking";
    String CHECK_VERSION = "/api/track/version";

    @POST(TRACKING)
    Observable<RootResponse> onTracking(@Body TrackingRequest request);

    @POST(CHECK_VERSION)
    Observable<BaseResponse> onCheckVersion();

    @POST(SIGN_UP)
    Observable<RootResponse> onSignUP(@Body SignUpRequest request);

    @POST(SIGN_IN)
    Observable<RootResponse> onSignIn(@Body SignInRequest request);

    @POST(VERIFY_CODE)
    Observable<RootResponse> onVerifyCode(@Body VerifyCodeRequest request);

    @POST(RESEND_CODE)
    Observable<RootResponse> onResendCode(@Body RequestCodeRequest request);

    @POST(CHECK_USER_CLOUD)
    Observable<RootResponse> onCheckUserCloud(@Body UserCloudRequest request);

    @POST(ADD_USER_CLOUD)
    Observable<RootResponse> onAddUserCloud(@Body UserCloudRequest request);

    @POST(UPDATE)
    Observable<RootResponse> onUpdateUser(@Body ChangeUserIdRequest request);

    @POST(REFRESH_USER_TOKEN)
    Observable<RootResponse> onUpdateToken(@Body UserRequest request);

    @POST(CHECKOUT)
    Observable<RootResponse> onCheckout(@Body CheckoutRequest request);

    @POST(CHECK_USER_ID)
    Observable<RootResponse> onCheckUserId(@Body UserRequest request);

    @POST(USER_INFO)
    Observable<RootResponse> onUserInfo(@Body UserRequest request);

    @POST(GET_LIST_FILES_SYNC)
    Observable<RootResponse> onListFilesSync(@Body SyncItemsRequest request);

    @POST(SYNC_DATA)
    Observable<RootResponse> onSyncData(@Body SyncItemsRequest request);

    @POST(DELETE_OWN_ITEMS)
    Observable<RootResponse> onDeleteOwnItems(@Body SyncItemsRequest request);

    @POST(CATEGORIES_SYNC)
    Observable<RootResponse> onCategoriesSync(@Body CategoriesRequest request);

    @POST(GET_LIST_CATEGORIES_SYNC)
    Observable<RootResponse> onListCategoriesSync(@Body CategoriesRequest request);

    @POST(DELETE_CATEGORIES)
    Observable<RootResponse> onDeleteCategories(@Body CategoriesRequest request);

    @Headers({"Accept: application/json"})
    @GET(GET_DRIVE_ABOUT)
    Observable<DriveAbout> onGetDriveAbout(@Header("Authorization") String token);

    @Headers({"Accept: application/json"})
    @POST(SEND_MAIL)
    Call<ResponseBody> onSendMail(@Header("Authorization") String token, @Body EmailToken body);

    @FormUrlEncoded
    @POST()
    Observable<EmailToken> onRefreshEmailToken(@Url String url,@FieldMap Map<String,Object>request);

    @POST(ADD_EMAIL_TOKEN)
    Observable<BaseResponse> onAddEmailToken(@Body OutlookMailRequest request);

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
