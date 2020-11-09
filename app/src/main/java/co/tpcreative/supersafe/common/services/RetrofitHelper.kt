package co.tpcreative.supersafe.common.services
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.network.BaseDependencies
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper : BaseDependencies() {
    /**
     * The CityService communicates with the json api of the city provider.
     */
    fun getService(url: String?): ApiService? {
        val retrofit: Retrofit? = createRetrofit(url)
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
        httpClient.addInterceptor { chain ->
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

    private fun createOkHttpClientCustom(): OkHttpClient? {
        return provideOkHttpClientDefault()
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