package co.tpcreative.suppersafe.common.services.download;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by PC on 9/1/2017.
 */

public interface RetrofitInterface {
    
    public static final String DOWNLOAD = "/api/file/download";


    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);

    // Retrofit 2 GET request for rxjava
    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadFileByUrlRx(@Url String fileUrl);
    
    @Streaming
    @POST(DOWNLOAD)
    Observable<Response<ResponseBody>> downloadFile(@Body DownloadFileRequest request);

    @Streaming
    @POST()
    Observable<Response<ResponseBody>> downloadFile(@Url String fileUrl, @Body DownloadFileRequest request);
    
    @GET()
    @Streaming
    Observable<Response<ResponseBody>> downloadFile(@Url String fileUrl);

    @GET(DOWNLOAD)
    @Streaming
    Observable<Response<ResponseBody>> downloadFile();

    @GET()
    @Streaming
    Observable<Response<ResponseBody>> downloadDriveFile(@Url String url,@Header("Authorization") String authToken);

}
