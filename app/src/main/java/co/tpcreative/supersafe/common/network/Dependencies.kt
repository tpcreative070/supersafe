package co.tpcreative.supersafe.common.network
import android.content.Context
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
    private var dependenciesListener: DependenciesListener<*>? = null
    private var mTimeOut = 1
    fun dependenciesListener(dependenciesListener: DependenciesListener<*>) {
        sInstance?.dependenciesListener = dependenciesListener
    }

    fun setTimeOutByMinute(minute: Int) {
        if (minute > 0) {
            mTimeOut = minute
        }
    }

    fun setRootURL(url: String?,context: Context) {
        this.URL = url
        this.context = context
    }

    fun changeApiBaseUrl(newApiBaseUrl: String?) {
        URL = newApiBaseUrl
        init()
    }

    fun init() {
    }

    fun provideRestApi(okHttpClient: OkHttpClient, tClass: Class<T>): T {
        val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        retrofitInstance = Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofitInstance!!.build()!!.create(tClass)
    }

    protected override fun getHeaders(): HashMap<String?, String?>? {
        val hashMap = HashMap<String?, String?>()
        var oauthToken: String? = null
        if (dependenciesListener != null) {
            if (dependenciesListener!!.onCustomHeader() != null) {
                hashMap.putAll(dependenciesListener!!.onCustomHeader())
            }
            oauthToken = dependenciesListener!!.onAuthorToken()
        }
        if (oauthToken != null) {
            hashMap["Authorization"] = oauthToken
        }
        return hashMap
    }

    protected override fun getTimeOut(): Int {
        return mTimeOut
    }

    interface DependenciesListener<T> {
        fun onObject(): Class<T>
        fun onAuthorToken(): String
        fun onCustomHeader(): HashMap<String, String>
    }

    companion object {
        private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val TIMEOUT_MAXIMUM = 30
        lateinit var serverAPI: Any
        var sInstance: Dependencies<*>?= null

        fun getInstance(context : Context,url : String): Dependencies<*> {
            if (sInstance == null) {
                if (sInstance == null) {
                    sInstance = Dependencies<Any>()
                }
            }
            this.sInstance?.URL = url
            this.sInstance?.context = context
            return sInstance!!
        }

        val TAG = Dependencies::class.java.simpleName
    }
}