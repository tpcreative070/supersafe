package co.tpcreative.suppersafe.common.api;
import com.google.gson.JsonObject;

import java.util.Map;

import co.tpcreative.suppersafe.common.api.response.BaseResponse;
import co.tpcreative.suppersafe.common.request.DriveApiRequest;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.response.SignInResponse;
import co.tpcreative.suppersafe.common.response.UserCloudResponse;
import co.tpcreative.suppersafe.common.response.VerifyCodeResponse;
import co.tpcreative.suppersafe.model.DriveAbout;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
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
import retrofit2.http.Url;

public interface RootAPI{

    String SIGN_UP = "/api/signup";
    String SIGN_IN = "/api/signin";
    String VERIFY_CODE = "api/verifycode";
    String RESEND_CODE = "api/resendcode";
    String CHECK_USER_CLOUD = "/api/usercloud/check";
    String ADD_USER_CLOUD = "/api/usercloud/add/";
    String CHECK_USER_ID = "/api/user/checkUser";
    String GET_DRIVE_ABOUT = "/drive/v2/about";
    String CREATE_FOLDER = "/drive/v3/files";
    String CHECK_IN_APP_FOLDER_EXITING = "/drive/v3/files";
    String GET_LIST_FILE_IN_APP_FOLDER = "/drive/v3/files";





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
    @POST(CHECK_USER_ID)
    Observable<VerifyCodeResponse> onCheckUserId(@FieldMap Map<String,String>request);

    @Headers({"Accept: application/json"})
    @GET(GET_DRIVE_ABOUT)
    Observable<DriveAbout> onGetDriveAbout(@Header("Authorization") String token);

    @Headers({"Accept: application/json"})
    @POST(CREATE_FOLDER)
    Observable<DriveAbout> onCrateFolder(@Header("Authorization") String token, @Body DriveApiRequest request);


    @Headers({"Accept: application/json"})
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    Observable<DriveAbout> onCheckInAppFolderExisting(@Header("Authorization") String token, @Query("q") String title,@Query("spaces")String value);


    @Headers({"Accept: application/json"})
    @GET(CHECK_IN_APP_FOLDER_EXITING)
    Observable<DriveAbout> onGetListFile(@Header("Authorization") String token, @Query("q") String title,@Query("spaces")String value);

    @Headers({"Accept: application/json"})
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    Observable<DriveAbout> onGetListFileInAppFolder(@Header("Authorization") String token, @Query("q") String title,@Query("spaces")String value);


    @POST()
    @Multipart
    Call<DriveResponse> uploadFileMutil(@Url String url,
                                        @Header("Authorization") String authToken,
                                        @Part MultipartBody.Part metaPart,
                                        @Part MultipartBody.Part dataPart,
                                        @Query("type") String videoType);

    @POST()
    @Multipart
    Call<DriveResponse> uploadFileMultipleInAppFolder(@Url String url,
                                        @Header("Authorization") String authToken,
                                        @Part MultipartBody.Part metaPart,
                                        @Part MultipartBody.Part dataPart,
                                        @Query("type") String type);

    @POST()
    Call<DriveResponse> uploadFileMutil(@Url String url,
                                        @Header("Authorization") String authToken,
                                        @Body RequestBody requestBody);
}
