package co.tpcreative.supersafe.common.api
import co.tpcreative.supersafe.common.network.BaseDependencies
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    private fun getRetrofit(url : String? = null,listener: ProgressResponseBody.ProgressResponseBodyListener? = null): Retrofit {
        val mClient = if (listener!=null) getOkHttpDownloadClientBuilder(listener) else createOkHttpClientCustom()
        val mURL = url ?: BASE_URL
        return Retrofit.Builder()
                .baseUrl(mURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(mClient) // <-- Hook the client right here
                .build() //Doesn't require the adapter
    }

    fun getService(url : String? = null,listener : ProgressResponseBody.ProgressResponseBodyListener? = null): ApiService? {
        val retrofit: Retrofit? = getRetrofit(url,listener)
        return retrofit?.create(ApiService::class.java)
    }
}