package co.tpcreative.suppersafe.common.api;
import java.util.Map;
import co.tpcreative.suppersafe.common.BaseResponse;
import co.tpcreative.suppersafe.common.response.SignInResponse;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RootAPI{

    String SIGN_UP = "/api/signup";
    String SIGN_IN = "/api/signin";

    @FormUrlEncoded
    @POST(SIGN_UP)
    Observable<SignInResponse> onSignUP(@FieldMap Map<String,String> request);

    @FormUrlEncoded
    @POST(SIGN_IN)
    Observable<SignInResponse> onSignIn(@FieldMap Map<String,String>request);

}
