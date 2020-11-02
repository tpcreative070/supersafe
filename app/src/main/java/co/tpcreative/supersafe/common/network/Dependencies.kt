package co.tpcreative.supersafe.common.network
import android.content.Context
import co.tpcreative.supersafe.common.api.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier
import java.util.*


class Dependencies<T> private constructor() : BaseDependencies() {
    private var retrofitInstance: Retrofit.Builder? = null
    private var context: Context? = null
    private var URL: String? = null
    private var dependenciesListener: DependenciesListener<Any>? = null
    private var mTimeOut = 1
    fun dependenciesListener(dependenciesListener: DependenciesListener<Any>?) {
        sInstance?.dependenciesListener = dependenciesListener
    }

    fun setTimeOutByMinute(minute: Int) {
        if (minute > 0) {
            mTimeOut = minute
        }
    }

    fun setRootURL(url: String?, context: Context) {
        this.URL = url
        this.context = context
    }

    fun changeApiBaseUrl(newApiBaseUrl: String?) {
        URL = newApiBaseUrl
        init()
    }

    fun init() {
        if (serverAPI == null) {
            val okHttpClient = provideOkHttpClientDefault()
            serverAPI = sInstance?.provideRestApi(okHttpClient)
        }
    }

    private fun provideRestApi(okHttpClient: OkHttpClient): ApiService {
        val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        retrofitInstance = Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofitInstance!!.build().create(ApiService::class.java)
    }

    override fun getTimeOut(): Int {
        return mTimeOut
    }

    interface DependenciesListener<T>{
        fun onObject(): Class<T>
    }

    companion object {
        var serverAPI: ApiService? = null
        var sInstance: Dependencies<Any>?= null
        fun getInstance(context: Context, url: String): Dependencies<*> {
            if (sInstance == null) {
                sInstance = Dependencies<Any>()
            }
            this.sInstance?.URL = url
            this.sInstance?.context = context
            return sInstance!!
        }
        val TAG = Dependencies::class.java.simpleName
    }
}