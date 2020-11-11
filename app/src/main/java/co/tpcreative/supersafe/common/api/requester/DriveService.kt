package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RetrofitBuilder
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.UserRequest
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.util.HashMap

class DriveService() {
    val TAG = this::class.java.simpleName

    suspend fun getDriveAbout() : Resource<DriveAbout> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onGetDriveAboutCor(Utils.getDriveAccessToken())
                ResponseHandler.handleSuccess(mResult as DriveAbout)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun getListFileInAppFolderCor(space : String) : Resource<DriveAbout> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onGetListFileInAppFolderCor(Utils.getDriveAccessToken(),space)
                ResponseHandler.handleSuccess(mResult as DriveAbout)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun deleteCloudItemCor(id : String) : Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                ApiHelper.getInstance()?.onDeleteCloudItemCor(Utils.getDriveAccessToken(),id)
                ResponseHandler.handleSuccess("Deleted successfully")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun downloadFile(item : ItemModel) : Resource<String>{
        return withContext(Dispatchers.IO) {
            try {
                val service = RetrofitBuilder.getService(getString(R.string.url_google),listener = mProgressDownloading,EnumTypeServices.GOOGLE_DRIVE)
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

    suspend fun uploadFile(item : ItemModel) : Resource<DriveResponse>{
        return withContext(Dispatchers.IO){
            try {
                val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
                val mContent = onGetContent(items = item)
                val metaPart: MultipartBody.Part = MultipartBody.Part.create(Gson().toJson(mContent).toRequestBody(contentType))
                val mFilePath = onGetFilePath(item = item)
                val dataPart: MultipartBody.Part = MultipartBody.Part.create(ProgressRequestBody(mFilePath,item.mimeType,mProgressUploading))
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

    private val mProgressUploading = object : ProgressRequestBody.UploadCallbacks {
        override fun onProgressUpdate(percentage: Int) {
            Utils.Log(TAG, "Progressing uploaded $percentage%")
        }
        override fun onError() {
            Utils.Log(TAG, "onError")
        }
        override fun onFinish() {
            Utils.Log(TAG, "onFinish")
        }
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