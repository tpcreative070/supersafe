package co.tpcreative.supersafe.common.networkimport

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseDependencies {
    protected fun provideOkHttpClientDefault(): OkHttpClient {
        val timeout = getTimeOut()
        return OkHttpClient.Builder()
                .readTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .writeTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .connectTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .addInterceptor(
                        Interceptor { chain ->
                            val request = chain.request()
                            val builder = request.newBuilder()
                            val headers = getHeaders()
                            if (headers != null && headers.size > 0) {
                                for ((key, value) in headers) {
                                    Timber.d("%s : %s", key, value)
                                    builder.addHeader(key, value)
                                }
                            }
                            chain.proceed(builder.build())
                        }).build()
    }

    protected open fun getHeaders(): HashMap<String?, String?>? {
        return null
    }

    protected open fun getTimeOut(): Int {
        return DEFAULT_TIMEOUT
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 1
    }
}