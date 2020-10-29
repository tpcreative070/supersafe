/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.supersafe.common.services.upload
import android.util.Log
import co.tpcreative.supersafe.common.api.request.UploadingFileRequest
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.http.HttpEntity
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.io.File
import java.io.IOException

/**
 * @author PC
 */
class UploadService {
    private var totalSize: Long = 0
    private var listener: UploadServiceListener? = null
    private var url: String? = null
    fun getSubscriptions(): Disposable? {
        return subscriptions
    }

    fun setSubscriptions(subscriptions: Disposable?) {
        this.subscriptions = subscriptions
    }

    private var subscriptions: Disposable? = null
    fun setListener(listener: UploadServiceListener?, url: String?) {
        this.listener = listener
        this.url = url
    }

    fun postSpecificFile(upload: UploadingFileRequest?, key: String?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val client: HttpClient = DefaultHttpClient()
            val post = HttpPost(url)
            val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            var responseString: String? = null
            try {
                val var4: MutableIterator<MutableMap.MutableEntry<String?, String?>>? = upload?.mapHeader?.entries?.iterator()
                while (var4!!.hasNext()) {
                    val entry: MutableMap.MutableEntry<*, *>? = var4.next() as MutableMap.MutableEntry<*, *>?
                    post.addHeader(entry?.key as String?, entry?.value as String?)
                }
                for (index in upload.list!!) {
                    val sourceFile = File(index?.getAbsolutePath())
                    builder.addPart(key, FileBody(sourceFile))
                }
                for (entry in upload.mapBody?.entries!!) {
                    builder.addTextBody(entry.key, entry.value)
                    Log.d(TAG, "adding text body : " + entry.key)
                }
                val yourEntity: HttpEntity = builder.build()
                val myEntity = ProgressiveEntity(yourEntity, object : ProgressiveEntity.ProgressListener {
                    override fun transferred(num: Long) {
                        val percent = (num / totalSize.toFloat() * 100).toInt()
                        listener?.onProgressing(percent, totalSize)
                    }

                    override fun transferSpeed(speed: Double) {
                        listener?.onSpeed(speed)
                    }
                })
                totalSize = myEntity.contentLength
                post.setEntity(myEntity)
                val response = client.execute(post)
                val r_entity: HttpEntity? = response.entity
                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    EntityUtils.toString(r_entity)
                } else {
                    "Error occurred! Http Status Code: $statusCode"
                }
            } catch (e: ClientProtocolException) {
                responseString = e.toString()
            } catch (e: IOException) {
                responseString = e.toString()
            }
            subscriber?.onNext(responseString as String)
            subscriber?.onComplete()
        }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe { response: Any? -> listener?.onUploadCompleted(response as String?, upload) }
    }

    fun postUploadFileMulti(upload: UploadingFileRequest?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val client: HttpClient = DefaultHttpClient()
            val post = HttpPost(url)
            val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            var responseString: String? = null
            try {
                val var4: MutableIterator<MutableMap.MutableEntry<String?, String?>>? = upload?.mapHeader?.entries?.iterator()
                while (var4!!.hasNext()) {
                    val entry: MutableMap.MutableEntry<*, *>? = var4?.next() as MutableMap.MutableEntry<*, *>?
                    post.addHeader(entry?.key as String?, entry?.value as String?)
                }
                for (index in upload.list!!) {
                    val sourceFile = File(index?.getAbsolutePath())
                    //builder.addPart("file", new FileBody(sourceFile));
                    val cbFile: ContentBody = FileBody(sourceFile, ContentType.create("image/jpeg"), sourceFile.name)
                    builder.addPart("data", cbFile)
                }
                for (entry in upload.mapBody?.entries!!) {
                    builder.addTextBody(entry.key, entry.value)
                    Log.d(TAG, "adding text body : " + entry.key)
                }
                val yourEntity: HttpEntity = builder.build()
                val myEntity = ProgressiveEntity(yourEntity, object : ProgressiveEntity.ProgressListener {
                    override fun transferred(num: Long) {
                        val percent = (num / totalSize.toFloat() * 100).toInt()
                        listener?.onProgressing(percent, totalSize)
                    }

                    override fun transferSpeed(speed: Double) {
                        listener?.onSpeed(speed)
                    }
                })
                totalSize = myEntity.contentLength
                post.setEntity(myEntity)
                val response = client.execute(post)
                val r_entity: HttpEntity? = response.entity
                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    EntityUtils.toString(r_entity)
                } else {
                    "Error occurred! Http Status Code: $statusCode"
                }
            } catch (e: ClientProtocolException) {
                responseString = e.toString()
            } catch (e: IOException) {
                responseString = e.toString()
            }
            subscriber?.onNext(responseString as String)
            subscriber?.onComplete()
        }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe { response: Any? -> listener?.onUploadCompleted(response as String?, upload) }
    }

    interface UploadServiceListener {
        fun onUploadCompleted(response: String?, request: UploadingFileRequest?)
        fun onProgressing(percent: Int, total: Long)
        fun onSpeed(speed: Double)
    }

    companion object {
        val TAG = UploadService::class.java.simpleName
    }
}