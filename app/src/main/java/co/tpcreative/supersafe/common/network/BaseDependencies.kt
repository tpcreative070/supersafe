package co.tpcreative.supersafe.common.network
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


open class BaseDependencies {
    var mInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG){
            this.level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    protected fun provideOkHttpClientDefault(): OkHttpClient {
        val timeout = getTimeOut()
        return OkHttpClient.Builder()
                .readTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .writeTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .connectTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()
                    val headers = getHeaders()
                    headers.let {
                        for ((key, value) in it) {
                            Timber.d("%s : %s", key, value)
                            builder.addHeader(key, value)
                        }
                    }
                    chain.proceed(builder.build())
                }.addInterceptor(mInterceptor).build()
    }

    private fun getHeaders(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json"
        hashMap["Authorization"] = onAuthorToken()
        return hashMap
    }

    private fun onAuthorToken(): String {
        var authorization = ""
        try {
            val user: User? = Utils.getUserInfo()
            if (user != null) {
                authorization = ""
                user.author?.session_token?.let {
                    authorization = it
                }
                Utils.onWriteLog(authorization, EnumStatus.REQUEST_ACCESS_TOKEN)
            }
            return authorization
        } catch (e: Exception) {
        }
        return SecurityUtil.DEFAULT_TOKEN
    }

    protected open fun getTimeOut(): Int {
        return DEFAULT_TIMEOUT
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 1
    }
}