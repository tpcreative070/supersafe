package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.RetrofitBuilder
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.RetrofitHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.DriveEvent
import co.tpcreative.supersafe.model.EnumFileType
import co.tpcreative.supersafe.model.ItemModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.internal.wait
import okio.BufferedSink
import okio.buffer
import okio.sink
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SyncDataService(val apiService: ApiService? = null) {
    val TAG = this::class.java.simpleName
    suspend fun onGetListData(request : SyncItemsRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onListFilesSyncCor(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun onDownloadFile(item : ItemModel) : Resource<String>{
        return withContext(Dispatchers.IO) {
            try {
                val service = RetrofitBuilder.getService(getString(R.string.url_google),listener = mProgressDownloading)
                val mResult = ApiHelper.getInstance()?.downloadDriveFileCor(Utils.getDriveAccessToken(),item.global_id,service!!)
                onSaveFileToDisk(mResult!!,onGetContentOfDownload(item)!!)
                ResponseHandler.handleSuccess("Download successful")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    private fun onSaveFileToDisk(response: ResponseBody, request: DownloadFileRequest) {
          try {
              File(request.path_folder_output).mkdirs()
              val destinationFile = File(request.path_folder_output, request.file_name)
              if (!destinationFile.exists()) {
                  destinationFile.createNewFile()
                  Utils.Log(TAG, "created file")
              }
              val bufferedSink: BufferedSink = destinationFile.sink().buffer()
              response.source().let { bufferedSink.writeAll(it) }
              bufferedSink.close()
              Utils.Log(TAG,"Saved completely ${response.contentLength()}")
          } catch (e: IOException) {
              val destinationFile = File(request.path_folder_output, request.file_name)
              if (destinationFile.isFile && destinationFile.exists()) {
                  destinationFile.delete()
              }
              e.printStackTrace()
          }
    }

    suspend fun onUploadFile(item : ItemModel) : Resource<DriveResponse>?{
        return withContext(Dispatchers.IO){
            try {
                val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
                val mContent = onGetContent(items = item)
                val metaPart: MultipartBody.Part = MultipartBody.Part.create(Gson().toJson(mContent).toRequestBody(contentType))
                val mFilePath = onGetFilePath(item = item)
                val dataPart: MultipartBody.Part = MultipartBody.Part.create(onProgressingUploading(mFilePath,item.mimeType))
                val mResult  = ApiHelper.getInstance()?.uploadFileMultipleInAppFolderCor(Utils.getDriveAccessToken(), metaPart, dataPart, item.mimeType)
                ResponseHandler.handleSuccess(mResult!!)
            }catch (exception : Exception){
                Utils.Log(TAG,"Running here")
                ResponseHandler.handleException(exception)
            }
        }
    }

    private val mProgressDownloading  = object : ProgressResponseBody.ProgressResponseBodyListener{
        override fun onAttachmentDownloadedError(message: String?) {
        }
        override fun onAttachmentDownloadUpdate(percent: Int) {
            Utils.Log(TAG,"Downloading...$percent%")
        }
        override fun onAttachmentElapsedTime(elapsed: Long) {
        }
        override fun onAttachmentAllTimeForDownloading(all: Long) {
        }
        override fun onAttachmentRemainingTime(all: Long) {
        }
        override fun onAttachmentSpeedPerSecond(all: Double) {
        }
        override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {
        }
        override fun onAttachmentDownloadedSuccess() {
            Utils.Log(TAG,"Download completed")
        }
    }

    private fun onProgressingUploading(mFile : File?,mContentType : String?) : ProgressRequestBody{
        return ProgressRequestBody(mFile,mContentType, object : ProgressRequestBody.UploadCallbacks {
                override fun onProgressUpdate(percentage: Int) {
                    Utils.Log(TAG, "Progressing uploaded $percentage%")
                }
                override fun onError() {
                    Utils.Log(TAG, "onError")
                }
                override fun onFinish() {
                    Utils.Log(TAG, "onFinish")
                }
        })
    }

    private fun onGetContent(items : ItemModel) : HashMap<String?,Any?>?{
        val mContent = HashMap<String?, Any?>()
        val mContentEvent = DriveEvent()
        if (items.isOriginalGlobalId) {
            mContentEvent.fileType = EnumFileType.ORIGINAL.ordinal
        } else {
            mContentEvent.fileType = EnumFileType.THUMBNAIL.ordinal
        }
        if (!Utils.isNotEmptyOrNull(items.categories_id)) {
            return null
        }
        mContentEvent.items_id = items.items_id
        val hex: String? = DriveEvent.getInstance()?.convertToHex(Gson().toJson(mContentEvent))
        mContent[getString(R.string.key_name)] = hex
        val list: MutableList<String?> = ArrayList()
        list.add(getString(R.string.key_appDataFolder))
        mContent[getString(R.string.key_parents)] = list
        return mContent
    }

    private fun onGetContentOfDownload(item : ItemModel) : DownloadFileRequest? {
        val request = DownloadFileRequest()
        var id: String? = ""
        if (item.isOriginalGlobalId) {
            id = item.global_id
            request.file_name = item.originalName
        } else {
            id = item.global_id
            request.file_name = item.thumbnailName
        }
        request.items = item
        request.id = id
        if (!Utils.isNotEmptyOrNull(id)) {
            return null
        }
        item.setOriginal(Utils.getOriginalPath(item.originalName, item.items_id))
        request.path_folder_output = Utils.createDestinationDownloadItem(item.items_id)
        return request
    }

    private fun getString(res : Int) : String{
        return SuperSafeApplication.getInstance().applicationContext.getString(res)
    }

    private fun onGetFilePath(item : ItemModel) : File?{
        val file: File? = if (item.isOriginalGlobalId) {
            File(item.getOriginal())
        } else {
            File(item.getThumbnail())
        }
        if (!SuperSafeApplication.getInstance().getStorage()!!.isFileExist(file?.absolutePath)) {
            SQLHelper.deleteItem(item)
            return null
        }
        return file
    }
}