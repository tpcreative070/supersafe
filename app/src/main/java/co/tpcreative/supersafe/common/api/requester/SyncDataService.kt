package co.tpcreative.supersafe.common.api.requester
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.helper.ApiHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.ResponseHandler
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.DriveEvent
import co.tpcreative.supersafe.model.EnumFileType
import co.tpcreative.supersafe.model.ItemModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.HashMap

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

    suspend fun onUploadFile(item : ItemModel) : Resource<DriveResponse>?{
        return withContext(Dispatchers.IO){
            val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
            val mContent = onGetContent(items = item)
            val metaPart: MultipartBody.Part = MultipartBody.Part.create(Gson().toJson(mContent).toRequestBody(contentType))
            onGetFilePath(item = item)?.let { mFilePath ->
                val dataPart: MultipartBody.Part = MultipartBody.Part.create(onProgressingUploading(mFilePath,item.mimeType))
                try {
                    val mResult  = ApiHelper.getInstance()?.uploadFileMultipleInAppFolderCor(Utils.getDriveAccessToken(), metaPart, dataPart, item.mimeType)
                    ResponseHandler.handleSuccess(mResult!!)
                }catch (exception : Exception){
                    Utils.Log(TAG,"Running here")
                    ResponseHandler.handleException(exception)
                }
            }
        }
    }

    private fun onProgressingUploading(mFile : File,mContentType : String?) : ProgressRequestBody{
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