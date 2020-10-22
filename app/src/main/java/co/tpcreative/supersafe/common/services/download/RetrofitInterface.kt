package co.tpcreative.supersafe.common.services.download
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by PC on 9/1/2017.
 */
interface RetrofitInterface {
    @Streaming
    @GET
    open fun downloadFileByUrl(@Url fileUrl: String?): Call<ResponseBody>?

    // Retrofit 2 GET request for rxjava
    @Streaming
    @GET
    open fun downloadFileByUrlRx(@Url fileUrl: String?): Observable<Response<ResponseBody>>?

    @Streaming
    @POST(DOWNLOAD)
    open fun downloadFile(@Body request: DownloadFileRequest?): Observable<Response<ResponseBody>>?

    @Streaming
    @POST
    open fun downloadFile(@Url fileUrl: String?, @Body request: DownloadFileRequest?): Observable<Response<ResponseBody>>?

    @GET
    @Streaming
    open fun downloadFile(@Url fileUrl: String?): Observable<Response<ResponseBody>>?

    @GET(DOWNLOAD)
    @Streaming
    open fun downloadFile(): Observable<Response<ResponseBody?>?>?

    @GET
    @Streaming
    open fun downloadDriveFile(@Url url: String?, @Header("Authorization") authToken: String?): Observable<Response<ResponseBody>>?

    companion object {
        const val DOWNLOAD: String = "/api/file/download"
    }
}