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
import retrofit2.http.Url;

public interface RootAPI{

    String SIGN_UP = "/api/signup";
    String SIGN_IN = "/api/signin";
    String VERIFY_CODE = "api/verifycode";
    String RESEND_CODE = "api/resendcode";
    String CHECK_USER_CLOUD = "/api/usercloud/check";
    String ADD_USER_CLOUD = "/api/usercloud/add/";
    String CHECK_USER_ID = "/api/user/checkUser";
    String GET_DRIVE_ABOUT = "https://www.googleapis.com/drive/v2/about";
    String CREATE_FOLDẺ= "https://www.googleapis.com/drive/v3/files";


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
    @GET()
    Observable<DriveAbout> onGetDriveAbout(@Header("Authorization") String token, @Url String url);

    @Headers({"Accept: application/json"})
    @POST()
    Observable<DriveAbout> onCrateFolder(@Header("Authorization") String token, @Url String url, @Body DriveApiRequest request);


    @POST()
    @Multipart
    Call<DriveResponse> uploadFileMutil(@Url String url,
                                        @Header("Authorization") String authToken,
                                        @Part MultipartBody.Part metaPart,
                                        @Part MultipartBody.Part dataPart);


    @POST()
    Call<DriveResponse> uploadFileMutil(@Url String url,
                                        @Header("Authorization") String authToken,
                                        @Body RequestBody requestBody);

}
