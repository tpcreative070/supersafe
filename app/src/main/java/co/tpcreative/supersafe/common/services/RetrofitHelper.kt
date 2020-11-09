package co.tpcreative.supersafe.common.services
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.network.BaseDependencies
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitHelper : BaseDependencies() {
    /**
     * The CityService communicates with the json api of the city provider.
     */

    fun getService(url: String? , listener : ProgressResponseBody.ProgressResponseBodyListener? = null): ApiService? {
        val retrofit: Retrofit? = createRetrofit(url,listener)
        return retrofit?.create(ApiService::class.java)
    }

    fun getTPCreativeService(url: String?): ApiService? {
        val retrofit: Retrofit? = createRetrofitCustom(url)
        return retrofit?.create(ApiService::class.java)
    }

    /**
     * This custom client will append the "username=demo" query after every request.
     */
    private fun createOkHttpClient(): OkHttpClient? {
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

    private fun createOkHttpClientCustom(): OkHttpClient? {
        return provideOkHttpClientDefault()
    }

    /**
     * Creates a pre configured Retrofit instance
     */
    private fun createRetrofit(url: String?,listener: ProgressResponseBody.ProgressResponseBodyListener? = null): Retrofit? {
        val mClient = if (listener!=null) getOkHttpDownloadClientBuilder(listener) else createOkHttpClient()
        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
                .client(mClient)
                .build()
    }

    /**
     * Creates a pre configured Retrofit instance
     */
    private fun createRetrofitCustom(url: String?): Retrofit? {
        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
                .client(createOkHttpClientCustom())
                .build()
    }
}