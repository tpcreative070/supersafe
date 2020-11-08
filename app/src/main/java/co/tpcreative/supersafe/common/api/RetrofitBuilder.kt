package co.tpcreative.supersafe.common.api
import co.tpcreative.supersafe.common.network.BaseDependencies
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder : BaseDependencies() {
    private val BASE_URL = SuperSafeApplication.getInstance().getUrl()!!
    private fun createOkHttpClientCustom(): OkHttpClient {
        return provideOkHttpClientDefault()
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClientCustom()) // <-- Hook the client right here
                .build() //Doesn't require the adapter
    }
    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}