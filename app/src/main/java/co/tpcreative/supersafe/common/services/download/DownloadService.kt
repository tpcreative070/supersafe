package co.tpcreative.supersafe.common.services.download
import android.util.Log
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.Okio
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by PC on 9/1/2017.
 */
class DownloadService : ProgressResponseBody.ProgressResponseBodyListener {
    private var listener: DownLoadServiceListener? = null
    private var rootAPI: ApiService? = null
    private var header: MutableMap<String, String>? = HashMap()
    fun onProgressingDownload(downLoadServiceListener: DownLoadServiceListener?) {
        listener = downLoadServiceListener
    }

    override fun onAttachmentDownloadUpdate(percent: Int) {
        listener?.onProgressingDownloading(percent)
    }

    override fun onAttachmentElapsedTime(elapsed: Long) {
        listener?.onAttachmentElapsedTime(elapsed)
    }

    override fun onAttachmentAllTimeForDownloading(all: Long) {
        listener?.onAttachmentAllTimeForDownloading(all)
    }

    override fun onAttachmentRemainingTime(all: Long) {
        listener?.onAttachmentRemainingTime(all)
    }

    override fun onAttachmentSpeedPerSecond(all: Double) {
        listener?.onAttachmentSpeedPerSecond(all)
    }

    override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {
        listener?.onAttachmentTotalDownload(totalByte, totalByteDownloaded)
    }

    override fun onAttachmentDownloadedError(message: String?) {
        listener?.onDownLoadError("Error occurred downloading from body : $message")
    }

    override fun onAttachmentDownloadedSuccess() {}

    @Synchronized
    fun downloadFileFromGoogleDrive(request: DownloadFileRequest?) {
        rootAPI?.downloadDriveFile(request?.Authorization, request?.id)
                ?.flatMap(processResponse(request))
                ?.subscribeOn(Schedulers.computation())
                ?.observeOn(Schedulers.computation())
                ?.subscribe(handleResult(request))
    }

    @Synchronized
    private fun processResponse(request: DownloadFileRequest?): Function<Response<ResponseBody>?, Observable<File>>? {
        return object : Function<Response<ResponseBody>?, Observable<File>> {
            @Throws(Exception::class)
            override fun apply(responseBodyResponse: Response<ResponseBody>): Observable<File> {
                if (responseBodyResponse == null) {
                    Log.d(DownloadService.Companion.TAG, "response Body is null")
                }
                if (responseBodyResponse != null && listener != null) {
                    listener?.onCodeResponse(responseBodyResponse.code(), request)
                }
                return saveToDisk(responseBodyResponse, request)
            }
        }
    }

    @Synchronized
    private fun saveToDisk(response: Response<ResponseBody>?, request: DownloadFileRequest?): Observable<File> {
        return Observable.create(object : ObservableOnSubscribe<File> {
            @Throws(Exception::class)
            override fun subscribe(subscriber: ObservableEmitter<File>) {
                try {
                    File(request?.path_folder_output).mkdirs()
                    val destinationFile = File(request?.path_folder_output, request?.file_name)
                    if (!destinationFile.exists()) {
                        destinationFile.createNewFile()
                        Log.d(DownloadService.Companion.TAG, "created file")
                    }
                    val bufferedSink: BufferedSink = Okio.buffer(Okio.sink(destinationFile))
                    bufferedSink.writeAll(response?.body()?.source())
                    if (listener != null) {
                        listener?.onSavedCompleted()
                    }
                    bufferedSink.close()
                    subscriber.onNext(destinationFile)
                    subscriber.onComplete()
                } catch (e: IOException) {
                    e.printStackTrace()
                    if (listener != null) {
                        val destinationFile = File(request?.path_folder_output, request?.file_name)
                        if (destinationFile.isFile && destinationFile.exists()) {
                            destinationFile.delete()
                        }
                        val response = HashMap<String?, Any?>()
                        response["message"] = "Downloading occurred error on save file: " + e.message
                        response["request"] = Gson().toJson(request)
                        listener?.onErrorSave(Gson().toJson(response))
                    }
                    subscriber.onError(e)
                }
            }
        })
    }

    private fun handleResult(mFileName: DownloadFileRequest?): Observer<File> {
        return object : Observer<File> {
            var file_name: File? = null
            override fun onSubscribe(d: Disposable) {}
            override fun onComplete() {
                Log.d(DownloadService.Companion.TAG, "Download completed")
            }
            override fun onError(e: Throwable) {
                e.printStackTrace()
                val destinationFile = File(mFileName?.path_folder_output, mFileName?.file_name)
                if (destinationFile.isFile && destinationFile.exists()) {
                    destinationFile.delete()
                }
                val response = HashMap<String?, Any?>()
                response["message"] = "Downloading occurred error on save file: " + e.message
                response["request"] = Gson().toJson(mFileName)
                listener?.onDownLoadError(Gson().toJson(response))
            }

            override fun onNext(file: File) {
                file_name = file
                listener?.onDownLoadCompleted(file, mFileName)
                Log.d(DownloadService.Companion.TAG, "File onNext to " + file.getAbsolutePath())
            }
        }
    }

    @Synchronized
    private fun <T> createService(serviceClass: Class<T>?, baseUrl: String): T? {
        val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpDownloadClientBuilder(this))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
        return retrofit.create(serviceClass)
    }

    @Synchronized
    private fun getOkHttpDownloadClientBuilder(progressListener: ProgressResponseBody.ProgressResponseBodyListener?): OkHttpClient? {
        if (listener != null) {
            if (listener?.onHeader() != null) {
                header = listener?.onHeader()
            }
        }
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES).addInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()
                    val var4: MutableIterator<*>? = header?.entries?.iterator()
                    while (var4!!.hasNext()) {
                        val entry: MutableMap.MutableEntry<*, *>? = var4.next() as MutableMap.MutableEntry<*,*>?
                        if (entry != null) {
                            builder.addHeader(entry.key as String, entry.value as String)
                        }
                    }
                    if (progressListener == null) return@Interceptor chain.proceed(builder.build())
                    val originalResponse = chain.proceed(builder.build())
                    originalResponse.newBuilder()
                            .body(ProgressResponseBody(originalResponse.body(), progressListener))
                            .build()
                }).build()
    }

    interface DownLoadServiceListener {
         fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?)
         fun onDownLoadError(error: String?)
         fun onProgressingDownloading(percent: Int)
         fun onAttachmentElapsedTime(elapsed: Long)
         fun onAttachmentAllTimeForDownloading(all: Long)
         fun onAttachmentRemainingTime(all: Long)
         fun onAttachmentSpeedPerSecond(all: Double)
         fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long)
         fun onSavedCompleted()
         fun onErrorSave(name: String?)
         fun onCodeResponse(code: Int, request: DownloadFileRequest?)
         fun onHeader(): MutableMap<String, String>?
    }

    companion object {
        val TAG = DownloadService::class.java.simpleName
    }

    init {
        if (rootAPI == null) {
            rootAPI = ApiService::class.java?.let { createService<ApiService>(it, ApiService.Companion.ROOT_GOOGLE_DRIVE) }
        }
    }
}