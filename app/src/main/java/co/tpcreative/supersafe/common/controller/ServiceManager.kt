package co.tpcreative.supersafe.common.controller
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.os.Build
import android.os.CancellationSignal
import android.os.IBinder
import android.provider.MediaStore
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.api.requester.ItemService
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseServiceView
import co.tpcreative.supersafe.common.response.DriveResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeService
import co.tpcreative.supersafe.common.api.requester.SyncDataService
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.common.net.MediaType
import com.google.gson.Gson
import com.snatik.storage.Storage
import com.snatik.storage.helpers.OnStorageListener
import com.snatik.storage.helpers.SizeUnit
import id.zelory.compressor.Compressor
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import okhttp3.internal.wait
import java.io.File
import java.util.*
import javax.crypto.Cipher

class ServiceManager : BaseServiceView<Any?> {
    private var myService: SuperSafeService? = null
    private var mContext: Context? = null
    private var subscriptions: Disposable? = null
    private val storage: Storage? = Storage(SuperSafeApplication.getInstance())
    private val mStorage: Storage? = Storage(SuperSafeApplication.getInstance())
    private val mListExport: MutableList<ExportFiles> = ArrayList<ExportFiles>()
    private var mProgress: String? = null

    /*Improved sync data*/
    private val listImport: MutableList<ImportFilesModel> = ArrayList<ImportFilesModel>()
    private var isDownloadData = false
    private var isUploadData = false
    private var isUpdateItemData = false
    private var isUpdateCategoryData = false
    private var isSyncCategory = false
    private var isGetItemList = false
    private var isImportData = false
    private var isExportData = false
    private var isDownloadToExportFiles = false
    private var isDeleteItemData = false
    private var isDeleteCategoryData = false
    private var isHandleLogic = false
    private var isRequestShareIntent = false

    /*Using item_id as key for hash map*/
    private var mMapDeleteItem: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
    private var mMapDeleteCategory: MutableMap<String, MainCategoryModel> = HashMap<String, MainCategoryModel>()
    private var mMapUpdateCategory: MutableMap<String, MainCategoryModel> = HashMap<String, MainCategoryModel>()
    private var mMapSyncCategory: MutableMap<String, MainCategoryModel> = HashMap<String, MainCategoryModel>()
    private var mMapDownload: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
    private var mMapDownloadToExportFiles: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
    private var mMapUpload: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
    private var mMapUpdateItem: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
    private var mMapImporting: MutableMap<String, ImportFilesModel> = HashMap<String, ImportFilesModel>()
    private val mDownloadList: MutableList<ItemModel> = ArrayList<ItemModel>()
    private var mStart = 20
    private val syncDataService  = SyncDataService()
    private val itemService = ItemService()
    var myConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            Utils.Log(TAG, "connected")
            myService = (binder as SuperSafeService.LocalBinder?)?.getService()
            myService?.bindView(this@ServiceManager)
            storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
            mStorage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
            getInstance()?.onGetUserInfo()
            getInstance()?.onSyncAuthorDevice()
            getInstance()?.onGetDriveAbout()
            Utils.onScanFile(SuperSafeApplication.getInstance(), "scan.log")
        }

        //binder comes from server to communicate with method's of
        override fun onServiceDisconnected(className: ComponentName?) {
            Utils.Log(TAG, "disconnected")
            myService = null
        }
    }

    fun onInitConfigurationFile() {
        storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
        mStorage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    }

    private var mCiphers: Cipher? = null
    private var isWaitingSendMail = false
    fun setListImport(mListImport: MutableList<ImportFilesModel>) {
        if (!isImportData) {
            listImport.clear()
            listImport.addAll(mListImport)
        }
    }

    /*Preparing sync data*/
    fun onPreparingSyncData() {
        if (Utils.getUserId() == null) {
            return
        }
        Utils.Log(TAG, "onPreparingSyncData...???")
        if (Utils.getAccessToken() == null) {
            Utils.Log(TAG, "Need to sign in with Google drive first")
            return
        }
        if (!Utils.isConnectedToGoogleDrive()) {
            Utils.Log(TAG, "Need to connect to Google drive")
            RefreshTokenSingleton.getInstance().onStart(ServiceManager::class.java)
            return
        }
        if (!Utils.isAllowSyncData()) {
            Utils.Log(TAG, "onPreparingSyncData is unauthorized $isDownloadData")
            Utils.onWriteLog(EnumStatus.AUTHOR_SYNC, EnumStatus.AUTHOR_SYNC, "onPreparingSyncData is unauthorized")
            return
        }
        if (isGetItemList) {
            Utils.Log(TAG, "onPreparingSyncData is getting item list. Please wait")
            Utils.onWriteLog(EnumStatus.GET_LIST_FILE, EnumStatus.ERROR, "onPreparingSyncData is getting item list. Please wait")
            return
        }
        if (isDownloadData) {
            Utils.Log(TAG, "onPreparingSyncData is downloading. Please wait")
            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onPreparingSyncData is downloading. Please wait")
            return
        }
        if (isUploadData) {
            Utils.Log(TAG, "onPreparingSyncData is uploading. Please wait")
            Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.ERROR, "onPreparingSyncData is uploading. Please wait")
            return
        }
        if (isDeleteItemData) {
            Utils.Log(TAG, "onPreparingSyncData is deleting. Please wait")
            Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.ERROR, "onPreparingSyncData is deleting. Please wait")
            return
        }
        if (isDeleteCategoryData) {
            Utils.Log(TAG, "onPreparingSyncData is deleting category. Please wait")
            Utils.onWriteLog(EnumStatus.DELETE_CATEGORIES, EnumStatus.ERROR, "onPreparingSyncData is deleting category. Please wait")
            return
        }
        if (isImportData) {
            Utils.Log(TAG, "onPreparingSyncData is importing. Please wait")
            Utils.onWriteLog(EnumStatus.IMPORTING, EnumStatus.ERROR, "onPreparingSyncData is importing. Please wait")
            return
        }
        if (isDownloadToExportFiles) {
            Utils.Log(TAG, "onPreparingSyncData is downloading files. Please wait")
            Utils.onWriteLog(EnumStatus.DOWNLOADING, EnumStatus.ERROR, "onPreparingSyncData is downloading to export files. Please wait")
            return
        }
        if (isUpdateItemData) {
            Utils.Log(TAG, "onPreparingSyncData is updating item. Please wait")
            Utils.onWriteLog(EnumStatus.UPDATE_ITEM, EnumStatus.ERROR, "onPreparingSyncData is updating item.. Please wait")
            return
        }
        if (isUpdateCategoryData) {
            Utils.Log(TAG, "onPreparingSyncData is updating category. Please wait")
            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.ERROR, "onPreparingSyncData is updating category.. Please wait")
            return
        }
        if (isHandleLogic) {
            Utils.Log(TAG, "onPreparingSyncData is handle logic. Please wait")
            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.ERROR, "onPreparingSyncData is handle logic. Please wait")
            return
        }
        if (isSyncCategory) {
            Utils.Log(TAG, "onPreparingSyncData is sync category. Please wait")
            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.ERROR, "onPreparingSyncData is sync category. Please wait")
            return
        }
        mDownloadList.clear()
        Utils.Log(TAG, "onPreparingSyncData...onGetItemList")
        onGetItemList("0")
    }

    fun waitingForResult() {
        /*Get List*/
//        CoroutineScope(Dispatchers.IO).launch {
//            val mResult = async {
//                getItemList()
//            }
//            mResult.await()
//            Utils.Log(TAG,"Completed fetch api...")
//        }

        /*Upload item*/
        CoroutineScope(Dispatchers.IO).launch {
            onUploadFile()
            onDownloadFile()
            Utils.Log(TAG,"Synced completely...")
        }
    }

    private suspend fun getItemList() = withContext(Dispatchers.IO){
        var mNextSpace : String? = "0"
        do {
            val mResult = itemService.onGetListData(SyncItemsRequest(nextPage = mNextSpace))
            when(mResult.status){
                Status.LOADING ->{
                    Utils.Log(TAG,"Loading...")
                }
                Status.SUCCESS -> {
                    Utils.Log(TAG,"Call success ${Gson().toJson(mResult.data)}")
                    mNextSpace = mResult.data?.data?.nextPage
                }
                Status.ERROR -> {
                    mNextSpace = null
                }
            }
        }while (mNextSpace != null)
    }

    private suspend fun onUploadFile() = withContext(Dispatchers.IO){
        val mItem = SQLHelper.getItemId(item_id = "bb3a22f9-8922-400d-8f72-456f58065ef5")
        mItem?.isOriginalGlobalId = true
        val mResult = mItem?.let { syncDataService.onUploadFile(it) }
        when(mResult?.status){
            Status.LOADING ->{
                Utils.Log(TAG,"Loading...")
            }
            Status.SUCCESS -> {
                Utils.Log(TAG,"Call uploaded ${Gson().toJson(mResult.data)}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Upload error ocurred")
            }
        }
    }

    private suspend fun onDownloadFile() = withContext(Dispatchers.IO) {
        val mItem = SQLHelper.getItemId(item_id = "bb3a22f9-8922-400d-8f72-456f58065ef5")
        mItem?.isOriginalGlobalId = true
        mItem?.global_id = "1-Q63TAQqLXiJ3NcqbgO9W3uMEKHRdKA7ivsxeCe8FcD4goWY0Q"
        val mResult = mItem?.let { syncDataService.onDownloadFile(it) }
        when(mResult?.status){
            Status.LOADING ->{
                Utils.Log(TAG,"Loading...")
            }
            Status.SUCCESS -> {
                Utils.Log(TAG,"Call downloaded ${mResult.data}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Download error ocurred")
            }
        }
    }

    private fun onGetItemList(next: String) {
        if (myService != null) {
            isGetItemList = true
            /*Stop multiple request*/isHandleLogic = true
            myService?.onGetListSync(next, object : BaseListener<SyncDataModel> {
                override fun onShowListObjects(list: MutableList<SyncDataModel>) {
                }
                override fun onShowObjects(`object`: SyncDataModel) {
                    `object`.list?.let {
                        mDownloadList.addAll(it)
                    }
                    /*Checking to preparing delete data*/
                    if (`object`.isDone) {
                        onItemDeleteSyncedLocal(mDownloadList)
                    }
                    /*Checking to preparing delete data*/
                    `object`.categoryList?.let {
                        if (`object`.isDone) {
                            onCategoryDeleteSyncedLocal(it)
                        }
                    }
                }

                override fun onError(message: String?, status: EnumStatus) {
                    isGetItemList = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus) {
                    if (status == EnumStatus.LOAD_MORE) {
                        isGetItemList = true
                        message?.let {
                            onGetItemList(it)
                        }
                        Utils.Log(TAG, "Continue load more $message")
                    } else if (status == EnumStatus.SYNC_READY) {
                        /*Start sync*/
                        isGetItemList = false
                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                        onPreparingSyncCategoryData()
                        Utils.Log(TAG, "Start to sync data.......")
                    }
                }
            })
        }
    }

    /*Preparing sync category*/
    fun onPreparingSyncCategoryData() {
        val mResult: MutableList<MainCategoryModel>? = SQLHelper.requestSyncCategories(false, false)
        if (mResult?.size!! > 0) {
            mMapSyncCategory.clear()
            mMapSyncCategory = Utils.mergeListToCategoryHashMap(mResult)
            val itemModel: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapSyncCategory)
            if (itemModel != null) {
                Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.PROGRESS, "Total updating " + mMapSyncCategory.size)
                Utils.Log(TAG, "onPreparingSyncCategoryData ==> total: " + mMapSyncCategory.size)
                onSyncCategoryData(itemModel)
            }
        } else {
            onPreparingUpdateCategoryData()
        }
    }

    /*Sync category data*/
    fun onSyncCategoryData(categoryModel: MainCategoryModel) {
        if (myService != null) {
            isSyncCategory = true
            myService?.onCategoriesSync(categoryModel, object : BaseListener<EmptyModel> {
                override fun onShowListObjects(list: MutableList<EmptyModel>) {}
                override fun onShowObjects(`object`: EmptyModel) {}
                override fun onError(message: String?, status: EnumStatus) {
                    isSyncCategory = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus) {
                    isSyncCategory = false
                    if (Utils.deletedIndexOfCategoryHashMap(categoryModel, mMapSyncCategory)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapSyncCategory)
                        if (mUpdatedItem != null) {
                            onSyncCategoryData(mUpdatedItem)
                            isSyncCategory = true
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.DONE, Gson().toJson(categoryModel))
                            Utils.Log(TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                            Utils.Log(TAG, "Category inserting left ${mMapSyncCategory.size}")
                        } else {
                            Utils.Log(TAG, "Update completely...............")
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.DONE, Gson().toJson(categoryModel))
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapSyncCategory.size)
                            onPreparingUpdateCategoryData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing update category*/
    fun onPreparingUpdateCategoryData() {
        val mResult: MutableList<MainCategoryModel>? = SQLHelper.getRequestUpdateCategoryList()
        if (mResult?.size!! > 0) {
            mMapUpdateCategory.clear()
            mMapUpdateCategory = Utils.mergeListToCategoryHashMap(mResult)
            val itemModel: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapUpdateCategory)
            if (itemModel != null) {
                Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.PROGRESS, "Total updating " + mMapUpdateItem.size)
                Utils.Log(TAG, "onPreparingUpdateCategoryData ==> total: " + mMapUpdateItem.size)
                onUpdateCategoryData(itemModel)
            }
        } else {
            onPreparingDeleteCategoryData()
        }
    }

    fun onUpdateCategoryData(itemModel: MainCategoryModel) {
        if (myService != null) {
            isUpdateCategoryData = true
            myService?.onCategoriesSync(itemModel, object : BaseListener<EmptyModel> {
                override fun onShowListObjects(list: MutableList<EmptyModel>) {}
                override fun onShowObjects(`object`: EmptyModel) {}
                override fun onError(message: String?, status: EnumStatus) {
                    isUpdateCategoryData = false
                }

                override fun onSuccessful(message: String?, status: EnumStatus) {
                    isUpdateCategoryData = false
                    if (Utils.deletedIndexOfCategoryHashMap(itemModel, mMapUpdateCategory)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapUpdateCategory)
                        if (mUpdatedItem != null) {
                            onUpdateCategoryData(mUpdatedItem)
                            isUpdateCategoryData = true
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Log(TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                            Utils.Log(TAG, "Category deleting left ${mMapUpdateCategory.size}")
                        } else {
                            Utils.Log(TAG, "Update completely...............")
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapUpdateCategory.size)
                            isUpdateCategoryData = false
                            Utils.onPushEventBus(EnumStatus.UPDATED_COMPLETED)
                            Utils.onPushEventBus(EnumStatus.DONE)
                            onPreparingDeleteCategoryData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing to delete category from system server*/
    private fun onPreparingDeleteCategoryData() {
        val listRequestDelete: MutableList<MainCategoryModel>? = SQLHelper.getDeleteCategoryRequest()
        mMapDeleteCategory.clear()
        mMapDeleteCategory = Utils.mergeListToCategoryHashMap(listRequestDelete!!)
        Utils.Log(TAG, "onPreparingDeleteCategoryData preparing to delete " + mMapDeleteCategory.size)
        val categoryModel: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapDeleteCategory)
        if (categoryModel != null) {
            Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.PROGRESS, "Total Delete category " + mMapDeleteCategory.size)
            onDeleteCategoryData(categoryModel)
            Utils.Log(TAG, "onPreparingDeleteCategoryData to delete data " + mMapDeleteCategory.size)
        } else {
            onPreparingDownloadData(mDownloadList)
        }
    }

    /*Delete category from Google drive and server*/
    fun onDeleteCategoryData(mainCategoryModel: MainCategoryModel) {
        if (myService == null) {
            return
        }
        isDeleteCategoryData = true
        myService?.onDeleteCategoriesSync(mainCategoryModel, object : BaseListener<EmptyModel> {
            override fun onShowListObjects(list: MutableList<EmptyModel>) {}
            override fun onShowObjects(`object`: EmptyModel) {}
            override fun onError(message: String?, status: EnumStatus) {
                isDeleteCategoryData = false
            }

            override fun onSuccessful(message: String?, status: EnumStatus) {
                if (Utils.deletedIndexOfCategoryHashMap(mainCategoryModel, mMapDeleteCategory)) {
                    /*Delete local db and folder name*/
                    SQLHelper.deleteCategory(mainCategoryModel)
                    val mDeleteItem: MainCategoryModel? = Utils.getArrayOfIndexCategoryHashMap(mMapDeleteCategory)
                    if (mDeleteItem != null) {
                        isDeleteCategoryData = true
                        Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(mainCategoryModel))
                        onDeleteCategoryData(mDeleteItem)
                        Utils.Log(TAG, "Category deleting left ${mMapDeleteCategory.size}")
                    } else {
                        isDeleteCategoryData = false
                        Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(mainCategoryModel))
                        Utils.Log(TAG, "Deleted completely...............")
                        Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DELETED_CATEGORY_SUCCESSFULLY, "Total uploading " + mMapDeleteCategory.size)
                        onPreparingDownloadData(mDownloadList)
                    }
                }
            }
        })
    }

    /*Preparing to download data from Google drive and system server*/
    private fun onPreparingDownloadData(globalList: MutableList<ItemModel>?) {
        val mListLocal: MutableList<ItemModel>? = SQLHelper.getItemListDownload()
        Utils.Log(TAG, "onPreparingDownloadData ==> Local original list " + Gson().toJson(mListLocal))
        if (mListLocal != null) {
            Utils.Log(TAG, "onPreparingDownloadData ==> Local list " + Gson().toJson(mListLocal))
            if (globalList != null) {
                val mergeList: MutableList<ItemModel>? = Utils.clearListFromDuplicate(globalList, mListLocal)
                Utils.Log(TAG, "onPreparingDownloadData ==> clear duplicated data " + Gson().toJson(mergeList))
                if (mergeList != null) {
                    if (mergeList.size > 0) {
                        mMapDownload.clear()
                        mMapDownload = Utils.mergeListToHashMap(mergeList)
                        Utils.Log(TAG, "onPreparingDownloadData ==> clear merged data " + Gson().toJson(mMapDownload))
                        Utils.Log(TAG, "onPreparingDownloadData ==> merged data " + Gson().toJson(mergeList))
                        val itemModel: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownload)
                        if (itemModel != null) {
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Total downloading " + mMapDownload.size)
                            Utils.Log(TAG, "Preparing to download " + Gson().toJson(itemModel))
                            Utils.Log(TAG, "Preparing to download total  " + mMapDownload.size)
                            onDownLoadData(itemModel)
                        }
                    } else {
                        /*Preparing upload file to Google drive*/
                        onPreparingUploadData()
                    }
                }
            }
        }
    }

    /*Download file from Google drive*/
    private fun onDownLoadData(itemModel: ItemModel) {
        if (myService != null) {
            isDownloadData = true
            mStart = 20
            Utils.onPushEventBus(EnumStatus.DOWNLOAD)
            myService?.onDownloadFile(itemModel, false, object : DownloadServiceListener {
                override fun onProgressDownload(percentage: Int) {
                    isDownloadData = true
                    if (mStart == percentage) {
                        Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest) {
                    Utils.Log(TAG, "onDownLoadCompleted ==> onDownLoadCompleted:" + file_name?.getAbsolutePath())
                    if (Utils.deletedIndexOfHashMap(itemModel, mMapDownload)) {
                        /*Delete local db and folder name*/
                        val mDownloadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownload)
                        if (mDownloadItem != null) {
                            onDownLoadData(mDownloadItem)
                            isDownloadData = true
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Log(TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                            Utils.Log(TAG, "Item downloading left ${mMapDownload.size}")
                        } else {
                            Utils.Log(TAG, "Download completely...............")
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownload.size)
                            isDownloadData = false
                            onPreparingUploadData()
                            /*Download done for main tab*/Utils.onPushEventBus(EnumStatus.DONE)
                        }
                    }
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    Utils.Log(TAG, "onDownLoadData ==> onError:$message")
                    Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onDownLoadData ==> onError $message")
                    isDownloadData = false
                    if (status == EnumStatus.NO_SPACE_LEFT) {
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT)
                        Utils.onDeleteItemFolder(itemModel.items_id)
                        onPreparingDeleteData()
                        /*Download done for main tab*/Utils.onPushEventBus(EnumStatus.DONE)
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD) {
                        if (Utils.deletedIndexOfHashMap(itemModel, mMapDownload)) {
                            /*Delete local db and folder name*/
                            val mDownloadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownload)
                            if (mDownloadItem != null) {
                                onDownLoadData(mDownloadItem)
                                isDownloadData = true
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Log(TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                            } else {
                                Utils.Log(TAG, "Download completely...............")
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownload.size)
                                isDownloadData = false
                                onPreparingUploadData()
                                /*Download done for main tab*/Utils.onPushEventBus(EnumStatus.DONE)
                            }
                        }
                    }
                }
            })
        }
    }

    /*Preparing upload data*/
    private fun onPreparingUploadData() {
        Utils.Log(TAG, "onPreparingUploadData")
        if (!Utils.isCheckAllowUpload()) {
            Utils.Log(TAG, "onPreparingUploadData ==> Left 0. Please wait for next month or upgrade to premium version")
            Utils.onPushEventBus(EnumStatus.DONE)
            Utils.onPushEventBus(EnumStatus.REFRESH)
            onPreparingUpdateItemData()
            return
        }
        /*Preparing upload file to Google drive*/
        val mResult: MutableList<ItemModel>? = SQLHelper.getItemListUpload()
        val listUpload: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(true, mResult!!)
        if (listUpload!!.size > 0) {
            mMapUpload.clear()
            mMapUpload = Utils.mergeListToHashMap(listUpload)
            val itemModel: ItemModel? = Utils.getArrayOfIndexHashMap(mMapUpload)
            if (itemModel != null) {
                Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.PROGRESS, "Total uploading " + mMapUpload.size)
                Utils.Log(TAG, "onPreparingUploadData ==> total: " + mMapUpload.size)
                onUploadData(itemModel)
            }
        } else {
            Utils.Log(TAG, "Not found item to upload")
            onPreparingUpdateItemData()
        }
    }

    /*Upload file to Google drive*/
    private fun onUploadData(itemModel: ItemModel) {
        if (myService != null) {
            mStart = 20
            Utils.onPushEventBus(EnumStatus.UPLOAD)
            myService?.onUploadFileInAppFolder(itemModel, object : UploadServiceListener {
                override fun onProgressUpdate(percentage: Int) {
                    isUploadData = true
                    if (mStart == percentage) {
                        Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onFinish() {}
                override fun onResponseData(response: DriveResponse) {
                    response.id?.let {
                        onInsertItem(itemModel, it, object : ServiceManagerInsertItem {
                            override fun onCancel() {
                                isUploadData = false
                            }

                            override fun onError(message: String?, status: EnumStatus?) {
                                isUploadData = false
                                Utils.onPushEventBus(EnumStatus.DONE)
                            }

                            override fun onSuccessful(message: String?, status: EnumStatus?) {
                                if (Utils.deletedIndexOfHashMap(itemModel, mMapUpload)) {
                                    val mUploadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapUpload)
                                    if (mUploadItem != null) {
                                        onUploadData(mUploadItem)
                                        Utils.Log(TAG, "Next upload item..............." + Gson().toJson(mUploadItem))
                                        Utils.Log(TAG, "Item uploading left ${mMapUpload.size}")
                                        isUploadData = true
                                    } else {
                                        isUploadData = false
                                        onPreparingUpdateItemData()
                                        Utils.Log(TAG, "Upload completely...............")
                                        Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                        Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                                        Utils.onPushEventBus(EnumStatus.DONE)
                                        Utils.onPushEventBus(EnumStatus.REFRESH)
                                        Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.UPLOAD_COMPLETED, "Total uploading " + mMapUpload.size)
                                    }
                                }
                            }
                        })
                    }
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    isUploadData = false
                    Utils.onPushEventBus(EnumStatus.DONE)
                    Utils.Log(TAG, "" + message)
                    Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.ERROR, "onUploadLoadData ==> onError $message")
                    if (status == EnumStatus.NO_SPACE_LEFT_CLOUD) {
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT_CLOUD)
                        onPreparingDeleteData()
                        Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                    }
                    if (status == EnumStatus.REQUEST_NEXT_UPLOAD) {
                        if (Utils.deletedIndexOfHashMap(itemModel, mMapUpload)) {
                            val mUploadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapUpload)
                            if (mUploadItem != null) {
                                onUploadData(mUploadItem)
                                Utils.Log(TAG, "Next upload item..............." + Gson().toJson(mUploadItem))
                                isUploadData = true
                            } else {
                                isUploadData = false
                                onPreparingUpdateItemData()
                                Utils.Log(TAG, "Upload completely...............")
                                Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED)
                                Utils.onPushEventBus(EnumStatus.DONE)
                                Utils.onPushEventBus(EnumStatus.REFRESH)
                                Utils.onWriteLog(EnumStatus.UPLOAD, EnumStatus.UPLOAD_COMPLETED, "Total uploading " + mMapUpload.size)
                            }
                        }
                    }
                }
            })
        }
    }

    /*Preparing update item*/
    fun onPreparingUpdateItemData() {
        val mResult: MutableList<ItemModel>? = SQLHelper.getRequestUpdateItemList()
        if (mResult!!.size > 0) {
            mMapUpdateItem.clear()
            mMapUpdateItem = Utils.mergeListToHashMap(mResult)
            val itemModel: ItemModel? = Utils.getArrayOfIndexHashMap(mMapUpdateItem)
            if (itemModel != null) {
                Utils.onWriteLog(EnumStatus.UPDATE, EnumStatus.PROGRESS, "Total updating " + mMapUpdateItem.size)
                Utils.Log(TAG, "onPreparingUpdateItemData ==> total: " + mMapUpdateItem.size)
                onUpdateItemData(itemModel)
            }
        } else {
            onPreparingDeleteData()
        }
    }

    /*Update item*/
    fun onUpdateItemData(itemModel: ItemModel) {
        if (myService != null) {
            isUpdateItemData = true
            myService?.onUpdateItems(itemModel, object : BaseListener<EmptyModel> {
                override fun onShowListObjects(list: MutableList<EmptyModel>) {}
                override fun onShowObjects(`object`: EmptyModel) {}
                override fun onError(message: String?, status: EnumStatus) {
                    isUpdateItemData = false
                }
                override fun onSuccessful(message: String?, status: EnumStatus) {
                    isUpdateItemData = false
                    if (Utils.deletedIndexOfHashMap(itemModel, mMapUpdateItem)) {
                        /*Delete local db and folder name*/
                        val mUpdatedItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapUpdateItem)
                        if (mUpdatedItem != null) {
                            onUpdateItemData(mUpdatedItem)
                            isUpdateItemData = true
                            Utils.onWriteLog(EnumStatus.UPDATE, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Log(TAG, "Next update item..............." + Gson().toJson(mUpdatedItem))
                            Utils.Log(TAG, "Item updating left ${mMapUpdateItem.size}")
                        } else {
                            Utils.Log(TAG, "Update completely...............")
                            Utils.onWriteLog(EnumStatus.UPDATE, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.onWriteLog(EnumStatus.UPDATE, EnumStatus.UPDATED_COMPLETED, "Total updating " + mMapUpdateItem.size)
                            isUpdateItemData = false
                            Utils.onPushEventBus(EnumStatus.UPDATED_COMPLETED)
                            Utils.onPushEventBus(EnumStatus.DONE)
                            onPreparingDeleteData()
                        }
                    }
                }
            })
        }
    }

    /*Preparing to delete item from system server*/
    fun onPreparingDeleteData() {
        val mResult: MutableList<ItemModel>? = SQLHelper.getDeleteItemRequest()
        /*Merged original and thumbnail*/
        val listRequestDelete: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(false, mResult!!)
        mMapDeleteItem.clear()
        mMapDeleteItem = Utils.mergeListToHashMap(listRequestDelete!!)
        Utils.Log(TAG, "onPreparingDeleteData preparing to delete " + mMapDeleteItem.size)
        val itemModel: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDeleteItem)
        if (itemModel != null) {
            Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.PROGRESS, "Total Delete category " + mMapDeleteItem.size)
            onDeleteData(itemModel)
            Utils.Log(TAG, "Preparing to delete data " + mMapDeleteItem.size)
        } else {
            Utils.Log(TAG, "Not found item to upload")
            Utils.Log(TAG, "Not found item to delete ")
            Utils.Log(TAG, "Sync items completely======>ready to test")
            isDeleteItemData = false
            isHandleLogic = false
            Utils.onPushEventBus(EnumStatus.DONE)
            Utils.checkRequestUploadItemData()
            SingletonPrivateFragment.Companion.getInstance()?.onUpdateView()
        }
    }

    /*Delete item from Google drive and server*/
    fun onDeleteData(itemModel: ItemModel) {
        if (myService == null) {
            return
        }
        isDeleteItemData = true
        /*Request delete item from cloud*/
        myService?.onDeleteCloudItems(itemModel, object : BaseListener<EmptyModel> {
            override fun onShowListObjects(list: MutableList<EmptyModel>) {}
            override fun onShowObjects(`object`: EmptyModel) {}
            override fun onError(message: String?, status: EnumStatus) {
                isDeleteItemData = false
            }
            override fun onSuccessful(message: String?, status: EnumStatus) {
                /*Request delete item from system*/
                myService?.onDeleteOwnSystem(itemModel, object : BaseListener<EmptyModel> {
                    override fun onShowListObjects(list: MutableList<EmptyModel>) {}
                    override fun onShowObjects(`object`: EmptyModel) {}
                    override fun onError(message: String?, status: EnumStatus) {
                        isDeleteItemData = false
                    }
                    override fun onSuccessful(message: String?, status: EnumStatus) {
                        if (Utils.deletedIndexOfHashMap(itemModel, mMapDeleteItem)) {
                            /*Delete local db and folder name*/
                            Utils.onDeleteItemFolder(itemModel.items_id)
                            SQLHelper.deleteItem(itemModel)
                            val mDeleteItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDeleteItem)
                            if (mDeleteItem != null) {
                                isDeleteItemData = true
                                Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(itemModel))
                                onDeleteData(mDeleteItem)
                                Utils.Log(TAG, "Item deleting left ${mMapDeleteItem.size}")
                            } else {
                                isDeleteItemData = false
                                Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Log(TAG, "Deleted completely...............")
                                Utils.onWriteLog(EnumStatus.DELETE, EnumStatus.DELETED_ITEM_SUCCESSFULLY, "Total deleted " + mMapDeleteItem.size)
                                Utils.Log(TAG, "Not found item to upload")
                                Utils.Log(TAG, "Not found item to delete ")
                                Utils.Log(TAG, "Sync items completely======>ready to test")
                                isHandleLogic = false
                                Utils.onPushEventBus(EnumStatus.DONE)
                                Utils.checkRequestUploadItemData()
                                SingletonPrivateFragment.getInstance()?.onUpdateView()
                            }
                        }
                    }
                })
            }
        })
    }

    fun onInsertItem(itemRequest: ItemModel, drive_id: String, ls: ServiceManagerInsertItem) {
        if (myService != null) {
            myService?.onAddItems(itemRequest, drive_id, object : ServiceManagerInsertItem {
                override fun onCancel() {
                    ls.onCancel()
                }
                override fun onError(message: String?, status: EnumStatus?) {
                    ls.onError(message, status)
                    Utils.Log(TAG, "" + message)
                }
                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    ls.onSuccessful(message, status)
                }
            })
        }
    }

    /*Preparing import data*/
    fun onPreparingImportData() {
        if (isImportData) {
            return
        }
        /*Preparing upload file to Google drive*/if (listImport.size > 0) {
            mMapImporting.clear()
            mMapImporting = Utils.mergeListToHashMapImport(listImport)!!
            val itemModel: ImportFilesModel? = Utils.getArrayOfIndexHashMapImport(mMapImporting)
            if (itemModel != null) {
                Utils.Log(TAG, "Preparing to import " + Gson().toJson(itemModel))
                Utils.onPushEventBus(EnumStatus.IMPORTING)
                onImportData(itemModel)
            }
        } else {
            Utils.Log(TAG, "Not found item to import")
        }
    }

    /*Import data from gallery*/
    private fun onImportData(importFiles: ImportFilesModel) {
        subscriptions = Observable.create<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val mMimeTypeFile: MimeTypeFile = importFiles.mimeTypeFile!!
            val enumTypeFile = mMimeTypeFile.formatType
            val mPath: String = importFiles.path!!
            val mMimeType = mMimeTypeFile.mimeType
            val mMainCategories: MainCategoryModel = importFiles.mainCategories!!
            val mCategoriesId: String = mMainCategories.categories_id!!
            val mCategoriesLocalId: String = mMainCategories.categories_local_id!!
            val isFakePin: Boolean = mMainCategories.isFakePin
            val uuId: String = importFiles.unique_id!!
            var thumbnail: Bitmap? = null
            when (enumTypeFile) {
                EnumFormatType.IMAGE -> {
                    Utils.Log(TAG, "Start RXJava Image Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                        val currentTime: String = Utils.getCurrentDateTime() as String
                        val pathContent = "$rootPath$uuId/"
                        storage?.createDirectory(pathContent)
                        val thumbnailPath = pathContent + "thumbnail_" + currentTime
                        val originalPath = pathContent + currentTime
                        val itemsPhoto = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, Utils.getSaverSpace(), false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        val file: File = Compressor(SuperSafeApplication.getInstance())
                                .setMaxWidth(1032)
                                .setMaxHeight(774)
                                .setQuality(85)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(File(mPath))
                        Utils.Log(TAG, "start compress")
                        val createdThumbnail = storage?.createFile(File(thumbnailPath), file, Cipher.ENCRYPT_MODE)
                        val createdOriginal = storage?.createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                        Utils.Log(TAG, "start end")
                        val response = ResponseRXJava()
                        response.items = itemsPhoto
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdThumbnail!! && createdOriginal!!) {
                            response.isWorking = true
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Log(TAG, "Cannot write to $e")
                        Utils.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber?.onNext(response)
                        subscriber?.onComplete()
                    } finally {
                        Utils.Log(TAG, "Finally")
                    }
                }
                EnumFormatType.VIDEO -> {
                    Utils.Log(TAG, "Start RXJava Video Progressing")
                    try {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val mSize = Size(600, 600)
                                val ca = CancellationSignal()
                                thumbnail = ThumbnailUtils.createVideoThumbnail(File(mPath), mSize, ca)
                            } else {
                                thumbnail = ThumbnailUtils.createVideoThumbnail(mPath,
                                        MediaStore.Video.Thumbnails.MINI_KIND)
                            }
                            val exifInterface = ExifInterface(mPath)
                            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                            Utils.Log("EXIF", "Exif: $orientation")
                            val matrix = Matrix()
                            if (orientation == 6) {
                                matrix.postRotate(90f)
                            } else if (orientation == 3) {
                                matrix.postRotate(180f)
                            } else if (orientation == 8) {
                                matrix.postRotate(270f)
                            }
                            thumbnail = Bitmap.createBitmap(thumbnail!!, 0, 0, thumbnail.width, thumbnail.height, matrix, true) // rotating bitmap
                        } catch (e: Exception) {
                            thumbnail = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().resources,
                                    R.drawable.ic_default_video)
                            Utils.Log(TAG, "Cannot write to $e")
                        }
                        val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                        val currentTime: String = Utils.getCurrentDateTime() as String
                        val pathContent = "$rootPath$uuId/"
                        storage?.createDirectory(pathContent)
                        val thumbnailPath = pathContent + "thumbnail_" + currentTime
                        val originalPath = pathContent + currentTime
                        val itemsVideo = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.VIDEO, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        Utils.Log(TAG, "Call thumbnail")
                        val createdThumbnail: Boolean = storage?.createFile(thumbnailPath, thumbnail)!!
                        mCiphers = mStorage?.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage?.createLargeFile(File(originalPath), File(mPath), mCiphers)
                        Utils.Log(TAG, "Call original")
                        val response = ResponseRXJava()
                        response.items = itemsVideo
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdThumbnail && createdOriginal!!) {
                            response.isWorking = true
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Log(TAG, "Cannot write to $e")
                        Utils.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber?.onNext(response)
                        subscriber?.onComplete()
                    } finally {
                        Utils.Log(TAG, "Finally")
                    }
                }
                EnumFormatType.AUDIO -> {
                    Utils.Log(TAG, "Start RXJava Audio Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                        val currentTime: String = Utils.getCurrentDateTime() as String
                        val pathContent = "$rootPath$uuId/"
                        storage?.createDirectory(pathContent)
                        val originalPath = pathContent + currentTime
                        val itemsAudio = ItemModel(mMimeTypeFile.extension, originalPath, "null", mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.AUDIO, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        mCiphers = mStorage?.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage?.createLargeFile(File(originalPath), File(mPath), mCiphers)
                        val response = ResponseRXJava()
                        response.items = itemsAudio
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdOriginal!!) {
                            response.isWorking = true
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Log(TAG, "Cannot write to $e")
                        Utils.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber?.onNext(response)
                        subscriber?.onComplete()
                    } finally {
                        Utils.Log(TAG, "Finally")
                    }
                }
                EnumFormatType.FILES -> {
                    Utils.Log(TAG, "Start RXJava Files Progressing")
                    try {
                        val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                        val currentTime: String = Utils.getCurrentDateTime() as String
                        val pathContent = "$rootPath$uuId/"
                        storage?.createDirectory(pathContent)
                        val originalPath = pathContent + currentTime
                        val itemsFile = ItemModel(mMimeTypeFile.extension, originalPath, "null", mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.FILES, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                        mCiphers = mStorage?.getCipher(Cipher.ENCRYPT_MODE)
                        val createdOriginal = mStorage?.createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                        val response = ResponseRXJava()
                        response.items = itemsFile
                        response.categories = mMainCategories
                        response.originalPath = mPath
                        if (createdOriginal!!) {
                            response.isWorking = true
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile successful")
                        } else {
                            response.isWorking = false
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                            Utils.Log(TAG, "CreatedFile failed")
                        }
                    } catch (e: Exception) {
                        Utils.Log(TAG, "Cannot write to $e")
                        Utils.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber?.onNext(response)
                        subscriber?.onComplete()
                    } finally {
                        Utils.Log(TAG, "Finally")
                    }
                }
            }
            Utils.Log(TAG, "End up RXJava")
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mResponse: ResponseRXJava? = response as ResponseRXJava?
                    try {
                        if (mResponse?.isWorking!!) {
                            mResponse.items?.let { items ->
                                var mb: Long
                                when (val enumFormatType = EnumFormatType.values()[items.formatType]) {
                                    EnumFormatType.AUDIO -> {
                                        if (storage?.isFileExist(items.getOriginal())!!) {
                                            mb = +storage.getSize(File(items.getOriginal()), SizeUnit.B).toLong()
                                            items.size = "" + mb
                                            SQLHelper.insertedItem(items)
                                        }
                                    }
                                    EnumFormatType.FILES -> {
                                        if (storage?.isFileExist(items.getOriginal())!!) {
                                            mb = +storage.getSize(File(items.getOriginal()), SizeUnit.B).toLong()
                                            items.size = "" + mb
                                            SQLHelper.insertedItem(items)
                                        }
                                    }
                                    else -> {
                                        if (storage?.isFileExist(items.getOriginal())!! && storage.isFileExist(items.getThumbnail())) {
                                            mb = +storage.getSize(File(items.getOriginal()), SizeUnit.B).toLong()
                                            if (storage.isFileExist(items.getThumbnail())) {
                                                mb += +storage.getSize(File(items.getThumbnail()), SizeUnit.B).toLong()
                                            }
                                            items.size = "" + mb
                                            SQLHelper.insertedItem(items)
                                            if (!mResponse.categories?.isCustom_Cover!!) {
                                                if (enumFormatType == EnumFormatType.IMAGE) {
                                                    val main: MainCategoryModel = mResponse.categories as MainCategoryModel
                                                    main.items_id = items.items_id
                                                    SQLHelper.updateCategory(main)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Utils.Log(TAG, "Write file successful ")
                        } else {
                            Utils.Log(TAG, "Write file Failed ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (mResponse?.isWorking!!) {
                            val items: ItemModel = mResponse.items as ItemModel
                            GalleryCameraMediaManager.getInstance()?.setProgressing(false)
                            Utils.onPushEventBus(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                            if (items.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView()
                            } else {
                                SingletonPrivateFragment.getInstance()?.onUpdateView()
                            }
                            Utils.Log(TAG, "Original path :" + mResponse.originalPath)
                            if (!getRequestShareIntent()) {
                                Utils.onDeleteFile(mResponse.originalPath)
                            }
                            if (Utils.deletedIndexOfHashMapImport(importFiles, mMapImporting)) {
                                val mImportItem: ImportFilesModel? = Utils.getArrayOfIndexHashMapImport(mMapImporting)
                                if (mImportItem != null) {
                                    onImportData(mImportItem)
                                    isImportData = true
                                    Utils.Log(TAG, "Next import data completely")
                                } else {
                                    isImportData = false
                                    listImport.clear()
                                    Utils.Log(TAG, "Imported data completely")
                                    Utils.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY)
                                    getInstance()?.onPreparingSyncData()
                                }
                            }
                        } else {
                            val mImportItem: ImportFilesModel? = Utils.getArrayOfIndexHashMapImport(mMapImporting)
                            if (mImportItem != null) {
                                onImportData(mImportItem)
                                isImportData = true
                                Utils.Log(TAG, "Next import data completely")
                            } else {
                                isImportData = false
                                listImport.clear()
                                Utils.Log(TAG, "Imported data completely")
                                Utils.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY)
                                onPreparingSyncData()
                            }
                        }
                    }
                }
    }

    fun setIsWaitingSendMail(isWaitingSendMail: Boolean) {
        this.isWaitingSendMail = isWaitingSendMail
    }

    fun setProgress(mProgress: String?) {
        this.mProgress = mProgress
    }

    fun getProgress(): String? {
        return mProgress
    }

    fun setListExport(mListExport: MutableList<ExportFiles>) {
        if (!isExportData) {
            this.mListExport.clear()
            this.mListExport.addAll(mListExport)
        }
    }

    private fun setExporting(exporting: Boolean) {
        isExportData = exporting
    }

    fun setUpdate(update: Boolean) {
        isUpdateItemData = update
    }

    fun onPickUpNewEmailNoTitle(context: Activity, account: String?) {
        try {
            val account1 = Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), null, null, null, null)
            intent.putExtra("overrideTheme", 1)
            //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun onPickUpExistingEmail(context: Activity, account: String?) {
        try {
            val value = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.choose_an_account), account)
            val account1 = Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), value, null, null, null)
            intent.putExtra("overrideTheme", 1)
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun onPickUpNewEmail(context: Activity?) {
        try {
            if (Utils.getUserId() == null) {
                return
            }
            val value = String.format(SuperSafeApplication.getInstance().getString(R.string.choose_an_new_account))
            val account1 = Account(Utils.getUserId(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent: Intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), value, null, null, null)
            intent.putExtra("overrideTheme", 1)
            context?.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun setContext(mContext: Context?) {
        this.mContext = mContext
    }

    private fun doBindService() {
        if (myService != null) {
            return
        }
        var intent: Intent? = null
        intent = Intent(mContext, SuperSafeService::class.java)
        intent.putExtra(TAG, "Message")
        myConnection?.let { mContext?.bindService(intent, it, Context.BIND_AUTO_CREATE) }
        Utils.Log(TAG, "onStartService")
    }

    fun onStartService() {
        if (myService == null) {
            doBindService()
            Utils.Log(TAG, "start services now")
        }
    }

    private fun onStopService() {
        if (myService != null) {
            myConnection?.let { mContext?.unbindService(it) }
            myService = null
            Utils.Log(TAG, "stop services now")
        }
    }

    fun onSendEmail() {
        if (myService != null) {
            val mUser: User? = Utils.getUserInfo()
            val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.RESET) }
            if (emailToken != null) {
                myService?.onSendMail(emailToken)
            }
        }
    }

    fun getMyService(): SuperSafeService? {
        return myService
    }

    private fun getString(res: Int): String? {
        return SuperSafeApplication.getInstance().getString(res)
    }

    /*User info*/
    fun onGetUserInfo() {
        Utils.Log(TAG, "onGetUserInfo")
        if (myService != null) {
            myService?.onGetUserInfo()
        } else {
            Utils.Log(TAG, "My services is null")
            onStartService()
        }
    }

    /*Update user token*/
    fun onUpdatedUserToken() {
        Utils.onWriteLog("onUpdatedUserToken", EnumStatus.UPDATE_USER_TOKEN)
        if (myService != null) {
            myService?.onUpdateUserToken()
        } else {
            getInstance()?.onStartService()
            Utils.Log(TAG, "My services is null")
        }
    }

    /*Response Network*/
    fun onGetDriveAbout() {
        if (myService != null) {
            myService?.getDriveAbout()
        }
    }

    /*Sync Author Device*/
    fun onSyncAuthorDevice() {
        if (myService != null) {
            myService?.onSyncAuthorDevice()
        }
    }

    /*--------------Camera action-----------------*/
    fun onSaveDataOnCamera(mData: ByteArray?, mainCategories: MainCategoryModel?) {
        subscriptions = Observable.create<Any?> { subscriber: ObservableEmitter<Any?>? ->
            val mMainCategories: MainCategoryModel? = mainCategories
            val mCategoriesId: String = mMainCategories?.categories_id as String
            val mCategoriesLocalId: String = mMainCategories.categories_local_id as String
            val isFakePin: Boolean = mMainCategories.isFakePin
            try {
                val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                val currentTime: String = Utils.getCurrentDateTime() as String
                val uuId: String = Utils.getUUId() as String
                val pathContent = "$rootPath$uuId/"
                storage?.createDirectory(pathContent)
                val thumbnailPath = pathContent + "thumbnail_" + currentTime
                val originalPath = pathContent + currentTime
                val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
                val items = ItemModel(getString(R.string.key_jpg), originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype(), uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, currentTime + getString(R.string.key_jpg), "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, isSaver, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                storage?.createFileByteDataNoEncrypt(SuperSafeApplication.getInstance(), mData, object : OnStorageListener {
                    override fun onSuccessful() {}
                    override fun onSuccessful(path: String?) {
                        try {
                            val file: File = Compressor(SuperSafeApplication.getInstance())
                                    .setMaxWidth(1032)
                                    .setMaxHeight(774)
                                    .setQuality(85)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .compressToFile(File(path))
                            val createdThumbnail = storage.createFile(File(thumbnailPath), file, Cipher.ENCRYPT_MODE)
                            val createdOriginal = storage.createFile(originalPath, mData, Cipher.ENCRYPT_MODE)
                            val response = ResponseRXJava()
                            response.items = items
                            response.categories = mMainCategories
                            if (createdThumbnail && createdOriginal) {
                                response.isWorking = true
                                subscriber?.onNext(response)
                                subscriber?.onComplete()
                                Utils.Log(TAG, "CreatedFile successful")
                            } else {
                                response.isWorking = false
                                subscriber?.onNext(response)
                                subscriber?.onComplete()
                                Utils.Log(TAG, "CreatedFile failed")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val response = ResponseRXJava()
                            response.isWorking = false
                            subscriber?.onNext(response)
                            subscriber?.onComplete()
                        }
                    }

                    override fun onFailed() {
                        val response = ResponseRXJava()
                        response.isWorking = false
                        subscriber?.onNext(response)
                        subscriber?.onComplete()
                    }

                    override fun onSuccessful(position: Int) {}
                })
            } catch (e: Exception) {
                val response = ResponseRXJava()
                response.isWorking = false
                subscriber?.onNext(response)
                subscriber?.onComplete()
                Utils.onWriteLog(e.message, EnumStatus.WRITE_FILE)
                Utils.Log(TAG, "Cannot write to $e")
            } finally {
                Utils.Log(TAG, "Finally")
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mResponse: ResponseRXJava? = response as ResponseRXJava?
                    try {
                        if (mResponse?.isWorking!!) {
                            val mItem: ItemModel = mResponse.items as ItemModel
                            var mb: Long
                            if (storage?.isFileExist(mItem.getOriginal())!! && storage.isFileExist(mItem.getThumbnail())) {
                                mb = +storage.getSize(File(mItem.getOriginal()), SizeUnit.B).toLong()
                                if (storage.isFileExist(mItem.getThumbnail())) {
                                    mb += +storage.getSize(File(mItem.getThumbnail()), SizeUnit.B).toLong()
                                }
                                mItem.size = "" + mb
                                SQLHelper.insertedItem(mItem)
                                if (!mResponse.categories?.isCustom_Cover!!) {
                                    val main: MainCategoryModel = mResponse.categories as MainCategoryModel
                                    main.items_id = mItem.items_id
                                    SQLHelper.updateCategory(main)
                                    Utils.Log(TAG, "Special main categories " + Gson().toJson(main))
                                }
                            }
                        }
                        Utils.Log(TAG, "Insert Successful")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (mResponse?.isWorking!!) {
                            val mItem: ItemModel = mResponse.items as ItemModel
                            GalleryCameraMediaManager.getInstance()?.setProgressing(false)
                            Utils.onPushEventBus(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                            if (mItem.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView()
                            } else {
                                SingletonPrivateFragment.getInstance()?.onUpdateView()
                            }
                        }
                    }
                }
    }

    fun onExportingFiles() {
        Utils.Log(TAG, "Export amount files :" + mListExport.size)
        subscriptions = Observable.create<Any?> {
            setExporting(true)
            var isWorking = false
            var exportFiles: ExportFiles? = null
            var position = 0
            for (i in mListExport.indices) {
                if (!mListExport.get(i).isExport) {
                    exportFiles = mListExport.get(i)
                    isWorking = true
                    position = i
                    break
                }
            }
            if (isWorking) {
                val mInput: File = exportFiles?.input as File
                val mOutPut: File = exportFiles.output as File
                try {
                    val storage = Storage(SuperSafeApplication.getInstance())
                    storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
                    val mCipher = storage.getCipher(Cipher.DECRYPT_MODE)
                    val formatType = EnumFormatType.values()[exportFiles.formatType]
                    if (formatType == EnumFormatType.VIDEO || formatType == EnumFormatType.AUDIO) {
                        storage.createLargeFile(mOutPut, mInput, mCipher, position, object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                Utils.onWriteLog("Exporting failed", EnumStatus.EXPORT)
                                Utils.Log(TAG, "Exporting failed")
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {
                                try {
                                    Utils.Log(TAG, "Exporting large file...............................Successful $position")
                                    mListExport.get(position).isExport = true
                                    onExportingFiles()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    } else {
                        storage.createFile(mOutPut, mInput, Cipher.DECRYPT_MODE, position, object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                Utils.onWriteLog("Exporting failed", EnumStatus.EXPORT)
                                Utils.Log(TAG, "Exporting failed")
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {
                                try {
                                    Utils.Log(TAG, "Exporting file...............................Successful $position")
                                    mListExport[position].isExport = true
                                    onExportingFiles()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                } catch (e: Exception) {
                    Utils.Log(TAG, "Cannot write to $e")
                } finally {
                    Utils.Log(TAG, "Finally")
                }
            } else {
                Utils.Log(TAG, "Exporting file............................Done")
                Utils.onPushEventBus(EnumStatus.STOP_PROGRESS)
                setExporting(false)
                mListExport.clear()
                onPreparingSyncData()
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? -> }
    }

    override fun onError(message: String?, status: EnumStatus) {
        Utils.Log(TAG, "onError response :" + message + " - " + status.name)
        if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
            Utils.onPushEventBus(EnumStatus.REQUEST_ACCESS_TOKEN)
            Utils.Log(TAG, "Request token on global")
        }
    }

    override fun getContext(): Context? {
        return mContext
    }


    override fun onSuccessful(message: String?, status: EnumStatus) {
        when (status) {
            EnumStatus.SCREEN_OFF -> {
                val value: Int = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
                when (EnumPinAction.values()[value]) {
                    EnumPinAction.NONE -> {
                        val key: String = SuperSafeApplication.getInstance().readKey() as String
                        if ("" != key) {
                            Utils.onHomePressed()
                        }
                    }
                    else -> {
                        Utils.Log(TAG, "Nothing to do ???")
                    }
                }
            }
            EnumStatus.GET_DRIVE_ABOUT -> {
            }
            EnumStatus.CONNECTED -> {
                Utils.onPushEventBus(EnumStatus.CONNECTED)
                onGetUserInfo()
            }
            EnumStatus.DISCONNECTED -> {
                onDefaultValue()
                Utils.Log(TAG, "Disconnect")
            }
            EnumStatus.USER_INFO -> {
                Utils.Log(TAG, "Get info successful")
                onPreparingSyncData()
                val mUser: User? = Utils.getUserInfo()
                mUser?.let {
                    if (it.isWaitingSendMail) {
                        getInstance()?.onSendEmail()
                    }
                }
            }
            EnumStatus.UPDATE_USER_TOKEN -> {
                onPreparingSyncData()
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    fun onPreparingEnableDownloadData(globalList: MutableList<ItemModel>?) {
        if (isDownloadToExportFiles) {
            return
        }
        val mergeList: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(false, globalList!!)
        Utils.Log(TAG, "onPreparingEnableDownloadData ==> clear duplicated data " + Gson().toJson(mergeList))
        if (mergeList != null) {
            if (mergeList.size > 0) {
                mMapDownloadToExportFiles.clear()
                mMapDownloadToExportFiles = Utils.mergeListToHashMap(mergeList)
                Utils.Log(TAG, "onPreparingEnableDownloadData ==> clear merged data " + Gson().toJson(mMapDownloadToExportFiles))
                Utils.Log(TAG, "onPreparingEnableDownloadData ==> merged data " + Gson().toJson(mergeList))
                val itemModel: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                if (itemModel != null) {
                    Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Total downloading " + mMapDownloadToExportFiles.size)
                    Utils.Log(TAG, "onPreparingEnableDownloadData to download " + Gson().toJson(itemModel))
                    Utils.Log(TAG, "onPreparingEnableDownloadData to download total  " + mMapDownloadToExportFiles.size)
                    onDownLoadDataToExportFiles(itemModel)
                }
            }
        }
    }

    /*Download file from Google drive*/
    private fun onDownLoadDataToExportFiles(itemModel: ItemModel) {
        if (myService != null) {
            isDownloadToExportFiles = true
            mStart = 20
            myService?.onDownloadFile(itemModel, true, object : DownloadServiceListener {
                override fun onProgressDownload(percentage: Int) {
                    isDownloadToExportFiles = true
                    if (mStart == percentage) {
                        Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.PROGRESS, "Progressing $mStart")
                        mStart += 20
                    }
                }

                override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest) {
                    Utils.Log(TAG, "onDownLoadCompleted ==> onDownLoadCompleted:" + file_name?.getAbsolutePath())
                    if (Utils.deletedIndexOfHashMap(itemModel, mMapDownloadToExportFiles)) {
                        /*Delete local db and folder name*/
                        val mDownloadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                        if (mDownloadItem != null) {
                            onDownLoadDataToExportFiles(mDownloadItem)
                            isDownloadToExportFiles = true
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.Log(TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                        } else {
                            Utils.Log(TAG, "Download completely...............")
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                            Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownloadToExportFiles.size)
                            isDownloadToExportFiles = false
                            Utils.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED)
                        }
                    }
                }

                override fun onError(message: String?, status: EnumStatus?) {
                    Utils.Log(TAG, "onDownLoadData ==> onError:$message")
                    Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.ERROR, "onDownLoadData ==> onError $message")
                    isDownloadToExportFiles = false
                    if (status == EnumStatus.NO_SPACE_LEFT) {
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT)
                        Utils.onDeleteItemFolder(itemModel.items_id)
                        onPreparingDeleteData()
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD) {
                        if (Utils.deletedIndexOfHashMap(itemModel, mMapDownloadToExportFiles)) {
                            /*Delete local db and folder name*/
                            val mDownloadItem: ItemModel? = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles)
                            if (mDownloadItem != null) {
                                onDownLoadDataToExportFiles(mDownloadItem)
                                isDownloadToExportFiles = true
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.Log(TAG, "Next download item..............." + Gson().toJson(mDownloadItem))
                            } else {
                                Utils.Log(TAG, "Download completely...............")
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DONE, Gson().toJson(itemModel))
                                Utils.onWriteLog(EnumStatus.DOWNLOAD, EnumStatus.DOWNLOAD_COMPLETED, "Total downloading " + mMapDownloadToExportFiles.size)
                                isDownloadToExportFiles = false
                                /*Download completed for export files*/Utils.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED)
                            }
                        }
                    }
                }
            })
        }
    }

    /*Request category delete synced local*/
    fun onCategoryDeleteSyncedLocal(mList: List<MainCategoryModel>) {
        val mResultList = Utils.checkCategoryDeleteSyncedLocal(mList)
        mResultList.let {
            for (index in it) {
                SQLHelper.deleteCategory(index)
            }
            Utils.Log(TAG, Gson().toJson(mList))
            Utils.Log(TAG, "Total need to delete categories synced local.... " + it.size)
        }
    }

    /*Request item delete synced local*/
    fun onItemDeleteSyncedLocal(mList: List<ItemModel>) {
        val mResultList = Utils.checkItemDeleteSyncedLocal(mList)
        mResultList.let {
            for (index in it) {
                SQLHelper.deleteItem(index)
                index.items_id?.let { it1 ->
                    Utils.deleteFolderOfItemId(it1)
                }
            }
            Utils.Log(TAG, "Total need to delete items synced local.... " + it.size)
        }
    }

    fun onDefaultValue() {
        isDownloadData = false
        isUploadData = false
        isExportData = false
        isImportData = false
        isUpdateItemData = false
        isUpdateCategoryData = false
        isDeleteItemData = false
        isDeleteCategoryData = false
        isDownloadToExportFiles = false
        isHandleLogic = false
        isSyncCategory = false
        isGetItemList = false
        isWaitingSendMail = false
    }

    fun onDismissServices() {
        if (isDownloadData || isUploadData || isDownloadToExportFiles || isExportData || isImportData || isDeleteItemData || isDeleteCategoryData || isWaitingSendMail || isUpdateItemData || isHandleLogic || isSyncCategory || isGetItemList || isUpdateCategoryData) {
            Utils.Log(TAG, "Progress....................!!!!:")
        } else {
            onDefaultValue()
            if (myService != null) {
                myService?.unbindView()
            }
            if (subscriptions != null) {
                subscriptions?.dispose()
            }
            onStopService()
            Utils.Log(TAG, "Dismiss Service manager")
        }
    }

    fun getRequestShareIntent(): Boolean {
        return isRequestShareIntent
    }

    fun setRequestShareIntent(requestShareIntent: Boolean) {
        isRequestShareIntent = requestShareIntent
    }

    interface ServiceManagerSyncDataListener {
        fun onCompleted()
        fun onError()
        fun onCancel()
    }

    /*Upload Service*/
    interface UploadServiceListener {
        fun onProgressUpdate(percentage: Int)
        fun onFinish()
        fun onResponseData(response: DriveResponse)
        fun onError(message: String?, status: EnumStatus?)
    }

    interface DownloadServiceListener {
        fun onProgressDownload(percentage: Int)
        fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest)
        fun onError(message: String?, status: EnumStatus?)
    }

    interface BaseListener<T> {
        fun onShowListObjects(list: MutableList<T>)
        fun onShowObjects(`object`: T)
        fun onError(message: String?, status: EnumStatus)
        fun onSuccessful(message: String?, status: EnumStatus)
    }

    interface ServiceManagerInsertItem {
        fun onCancel()
        fun onError(message: String?, status: EnumStatus?)
        fun onSuccessful(message: String?, status: EnumStatus?)
    }

    companion object {
        private val TAG = ServiceManager::class.java.simpleName
        private var instance: ServiceManager? = null
        fun getInstance(): ServiceManager? {
            if (instance == null) {
                instance = ServiceManager()
            }
            return instance
        }
    }
}