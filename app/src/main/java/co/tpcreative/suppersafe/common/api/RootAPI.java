package co.tpcreative.suppersafe.common.api;
import java.util.Map;

import co.tpcreative.suppersafe.common.api.response.BaseResponse;
import co.tpcreative.suppersafe.common.response.SignInResponse;
import co.tpcreative.suppersafe.common.response.VerifyCodeResponse;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RootAPI{

    String SIGN_UP = "/api/signup";
    String SIGN_IN = "/api/signin";
    String VERIFY_CODE = "api/verifycode";
    String RESEND_CODE = "api/resendcode";


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


}
