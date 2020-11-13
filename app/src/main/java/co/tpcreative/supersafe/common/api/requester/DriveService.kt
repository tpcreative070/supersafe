package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RetrofitBuilder
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.response.DriveResponse
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

    suspend fun downloadFile(item : ItemModel,request : DownloadFileRequest,listener : ProgressResponseBody.ProgressResponseBodyListener) : Resource<String>{
        return withContext(Dispatchers.IO) {
            try {
                val service = RetrofitBuilder.getService(getString(R.string.url_google),listener = listener,EnumTypeServices.GOOGLE_DRIVE)
                val mResult = ApiHelper.getInstance()?.downloadDriveFileCor(Utils.getDriveAccessToken(),item.global_id,service!!)
                onSaveFileToDisk(mResult!!,request)
                ResponseHandler.handleSuccess("Download successful")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun uploadFile(item : ItemModel,mContent :  MutableMap<String?,Any?>?,listener: ProgressRequestBody.UploadCallbacks,mFilePath: File?) : Resource<DriveResponse>{
        return withContext(Dispatchers.IO){
            try {
                val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
                //val mContent = onGetContent(items = item)
                val metaPart: MultipartBody.Part = MultipartBody.Part.create(Gson().toJson(mContent).toRequestBody(contentType))
                //val mFilePath = onGetFilePath(item = item)
                val dataPart: MultipartBody.Part = MultipartBody.Part.create(ProgressRequestBody(mFilePath,item.mimeType,listener))
                val mResult  = ApiHelper.getInstance()?.uploadFileMultipleInAppFolderCor(Utils.getDriveAccessToken(), metaPart, dataPart, item.mimeType)
                ResponseHandler.handleSuccess(mResult!!)
            }catch (exception : Exception){
                Utils.Log(TAG,"Running here")
                ResponseHandler.handleException(exception)
            }
        }
    }

    private fun onSaveFileToDisk(response: ResponseBody, request: DownloadFileRequest) {
        try {
            File(request.path_folder_output!!).mkdirs()
            val destinationFile = File(request.path_folder_output, request.file_name!!)
            if (!destinationFile.exists()) {
                destinationFile.createNewFile()
                Utils.Log(TAG, "created file")
            }
            val bufferedSink: BufferedSink = destinationFile.sink().buffer()
            response.source().let { bufferedSink.writeAll(it) }
            bufferedSink.close()
            Utils.Log(TAG,"Saved completely ${response.contentLength()}")
        } catch (e: IOException) {
            val destinationFile = File(request.path_folder_output, request.file_name!!)
            if (destinationFile.isFile && destinationFile.exists()) {
                destinationFile.delete()
            }
            e.printStackTrace()
        }
    }

    private fun getString(res : Int) : String{
        return SuperSafeApplication.getInstance().applicationContext.getString(res)
    }
}