package co.tpcreative.supersafe.common.api
import co.tpcreative.supersafe.common.network.BaseDependencies
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import co.tpcreative.supersafe.model.EnumTypeServices
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder : BaseDependencies() {
    private val BASE_URL = SuperSafeApplication.getInstance().getUrl()!!
    private fun createOkHttpClientCustom(): OkHttpClient {
        return provideOkHttpClientDefault()
    }

    private fun getOkHttpDownloadClientBuilder(progressListener: ProgressResponseBody.ProgressResponseBodyListener?): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES).addInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()
                    val var4: MutableIterator<*>? = HashMap<String,Any>().entries.iterator()
                    while (var4!!.hasNext()) {
                        val entry: MutableMap.MutableEntry<*, *>? = var4.next() as MutableMap.MutableEntry<*,*>?
                        if (entry != null) {
                            builder.addHeader(entry.key as String, entry.value as String)
                        }
                    }
                    if (progressListener == null) return@Interceptor chain.proceed(builder.build())
                    val originalResponse = chain.proceed(builder.build())
                    originalResponse.newBuilder()
                            .body(ProgressResponseBody(originalResponse.body, progressListener))
                            .build()
                }).build()
    }

    /**
     * This custom client will append the "username=demo" query after every request.
     */
    private fun createOkHttpClientForEmailOutlook(): OkHttpClient {
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.addInterceptor Interceptor@{ chain ->
            val original = chain.request()
            val originalHttpUrl: HttpUrl? = original.url
            val url: HttpUrl? = originalHttpUrl?.newBuilder()
                    ?.build()
            // Request customization: add request headers
            val requestBuilder: Request.Builder = original.newBuilder()
                    .url(url!!)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        return httpClient.build()
    }


    private fun getRetrofit(url : String? = null,listener: ProgressResponseBody.ProgressResponseBodyListener? = null, typeService : EnumTypeServices): Retrofit {
        val mClient: OkHttpClient = when(typeService){
            EnumTypeServices.SYSTEM ->{
                createOkHttpClientCustom()
            }
            EnumTypeServices.GOOGLE_DRIVE ->{
                getOkHttpDownloadClientBuilder(listener)
            }
            EnumTypeServices.EMAIL_OUTLOOK ->{
                createOkHttpClientForEmailOutlook()
            }
        }
        val mURL = url ?: BASE_URL
        return Retrofit.Builder()
                .baseUrl(mURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(mClient) // <-- Hook the client right here
                .build() //Doesn't require the adapter
    }

    fun getService(url : String? = null,listener : ProgressResponseBody.ProgressResponseBodyListener? = null,typeService : EnumTypeServices): ApiService? {
        val retrofit: Retrofit? = getRetrofit(url,listener,typeService)
        return retrofit?.create(ApiService::class.java)
    }
}