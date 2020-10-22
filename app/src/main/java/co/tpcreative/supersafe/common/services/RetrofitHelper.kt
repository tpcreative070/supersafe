package co.tpcreative.supersafe.common.services
import co.tpcreative.supersafe.common.api.RootAPI
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper {
    /**
     * The CityService communicates with the json api of the city provider.
     */
    fun getCityService(url: String?): RootAPI? {
        val retrofit: Retrofit? = createRetrofit(url)
        return retrofit?.create(RootAPI::class.java)
    }

    /**
     * This custom client will append the "username=demo" query after every request.
     */
    private fun createOkHttpClient(): OkHttpClient? {
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.addInterceptor(Interceptor { chain ->
            val original = chain.request()
            val originalHttpUrl: HttpUrl? = original.url()
            val url: HttpUrl? = originalHttpUrl?.newBuilder()
                    ?.build()
            // Request customization: add request headers
            val requestBuilder: Request.Builder = original.newBuilder()
                    .url(url)
            val request = requestBuilder.build()
            chain.proceed(request)
        })
        return httpClient.build()
    }

    /**
     * Creates a pre configured Retrofit instance
     */
    private fun createRetrofit(url: String?): Retrofit? {
        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
                .client(createOkHttpClient())
                .build()
    }
}