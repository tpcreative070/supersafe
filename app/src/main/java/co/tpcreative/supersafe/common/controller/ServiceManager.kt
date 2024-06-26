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
import co.tpcreative.supersafe.common.api.requester.*
import co.tpcreative.supersafe.common.extension.*
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseServiceView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeService
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.util.SizeUnit
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.*
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.common.net.MediaType
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import javax.crypto.Cipher

class ServiceManager() : BaseServiceView<Any?> {
    private var myService: SuperSafeService? = null
    private var mContext: Context? = null
    /*Improved sync data*/
    private var isRequestShareIntent = false
    private var isRequestingSyncCor = false
    private var isRequestingUpdateUserToken = false
    var actionProgressing : EnumStatus = EnumStatus.DONE

    /*Using item_id as key for hash map*/
    private val userViewModel = UserViewModel(UserService(), MicService())
    private val itemViewModel = ItemViewModel(ItemService())
    private val categoryViewModel = CategoryViewModel(CategoryService())
    private val driveViewModel = DriveViewModel(DriveService(), ItemService())
    private var emailOutlookViewModel = EmailOutlookViewModel(MicService())
    private var myConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            Utils.Log(TAG, "connected")
            myService = (binder as SuperSafeService.LocalBinder?)?.getService()
            myService?.bindView(this@ServiceManager)
            initService()
            Utils.onScanFile(SuperSafeApplication.getInstance(), "scan.log")
        }

        //binder comes from server to communicate with method's of
        override fun onServiceDisconnected(className: ComponentName?) {
            Utils.Log(TAG, "disconnected")
            myService = null
        }
    }

    fun cleanUp(){
        instance = null
    }
    private var isWaitingSendMail = false

    /*Preparing sync data*/
    fun onPreparingSyncData() {
        if (Utils.getUserId() == null) {
            return
        }
        if (Utils.getAccessToken() == null) {
            Utils.Log(TAG, "Need to sign in with Google drive first")
            return
        }
        if (!Utils.isConnectedToGoogleDrive()) {
            Utils.Log(TAG, "Need to connect to Google drive")
            RefreshTokenSingleton.getInstance().onStart(ServiceManager::class.java)
            return
        }
        if (!Utils.isCheckingAllowDriveConnect()) {
            Utils.Log(TAG, "onPreparingSyncData is unauthorized")
            Utils.onWriteLog(EnumStatus.AUTHOR_SYNC, EnumStatus.AUTHOR_SYNC, "onPreparingSyncData is unauthorized")
            return
        }
        if (Utils.getScreenStatus() == EnumPinAction.SCREEN_LOCK.ordinal || Utils.getScreenStatus() == EnumPinAction.SPLASH_SCREEN.ordinal){
            Utils.Log(TAG, "Waiting until unlock to call this func.....")
            return
        }
        if (isRequestingSyncCor){
            Utils.onPushEventBus(actionProgressing)
            Utils.Log(TAG,"==========================================Sync data is loading===============================================")
            return
        }
        Utils.Log(TAG,"Preparing sync data")
        onTrackingSync()
    }

    fun isRequestingUpdatedUserToken() : Boolean{
        return isRequestingUpdateUserToken
    }

    fun initService() = CoroutineScope(Dispatchers.IO).launch {
        if(Utils.getUserId()?.isNotEmpty()==true){
            getUserInfo()
            getTracking()
        }
    }

    private suspend fun getUserInfo() = withContext(Dispatchers.IO){
        val mResult = userViewModel.getUserInfo()
        when(mResult.status){
            Status.SUCCESS -> {
                onPreparingSyncData()
                val mUser: User? = Utils.getUserInfo()
                mUser?.let {
                    if (it.isWaitingSendMail) {
                        getInstance()?.onSendEmail()
                    }
                }
                Utils.Log(TAG,"Fetch user info completed")
            }
            else -> Utils.Log(TAG,"Fetch user info issue ${mResult.message}")
        }
    }

    suspend fun getDriveAbout() : Resource<Boolean> = withContext(Dispatchers.IO){
        try {
            val mResult = driveViewModel.getDriveAbout()
            when(mResult.status){
                Status.SUCCESS -> {
                    Utils.Log(TAG,"Fetch drive about completed")
                    mResult
                }
                else ->{
                    Utils.Log(TAG,"Fetch drive about issue ${mResult.message}")
                    Resource.error(mResult.code ?: Utils.CODE_EXCEPTION,mResult.message ?: "",null)
                }
            }
        }catch (e : Exception){
            Resource.error(Utils.CODE_EXCEPTION,e.message ?:"",null)
        }
    }

    suspend fun getInAppList() : Resource<Boolean> = withContext(Dispatchers.IO){
        try {
            val mResult = driveViewModel.geInAppList()
            when(mResult.status){
                Status.SUCCESS -> {
                    Utils.Log(TAG,"Fetch drive list completed")
                    mResult
                }
                else ->{
                    Utils.Log(TAG,"Fetch drive about issue ${mResult.message}")
                    Resource.error(mResult.code ?: Utils.CODE_EXCEPTION,mResult.message ?: "",null)
                }
            }
        }catch (e : Exception){
            Resource.error(Utils.CODE_EXCEPTION,e.message ?:"",null)
        }
    }


    private suspend fun getTracking() = withContext(Dispatchers.IO){
        val mResult = userViewModel.getTracking()
        when(mResult.status){
            Status.SUCCESS -> Utils.Log(TAG,"Fetch drive tracking completed")
            else -> Utils.Log(TAG,"Fetch tracking issue ${mResult.message}")
        }
    }

    private fun onTrackingSync() = CoroutineScope(Dispatchers.IO).launch {
        isRequestingSyncCor = true
        val mResult = itemViewModel.trackingSync()
        when(mResult.status){
            Status.SUCCESS -> {
                if (Utils.isRequestSyncData() || Utils.isRequestUpload()) {
                    onPreparingSyncDataCor()
                }else{
                    if (Utils.isConnectedToGoogleDrive()){
                        Utils.onPushEventBus(EnumStatus.DONE)
                    }
                    isRequestingSyncCor = false
                }
                Utils.Log(TAG, mResult.data?.data?.trackingSync?.toJson())
            }
            else ->{
                Utils.Log(TAG,mResult.message)
                isRequestingSyncCor = false
            }
        }
    }

    private fun onPreparingSyncDataCor() = CoroutineScope(Dispatchers.IO).launch {
        val mResultItemList = itemViewModel.getItemList()
        when(mResultItemList.status){
            Status.SUCCESS -> {
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                val mResultSyncCategory = categoryViewModel.syncCategoryData()
                when(mResultSyncCategory.status){
                    Status.SUCCESS -> {
                        val mResultUpdatedCategory = categoryViewModel.updateCategoryData()
                        when(mResultUpdatedCategory.status){
                            Status.SUCCESS ->{
                                val mResultDeletedCategory = categoryViewModel.dateCategoryData()
                                when(mResultDeletedCategory.status){
                                    Status.SUCCESS -> {
                                        Utils.onPushEventBus(EnumStatus.DOWNLOAD)
                                        actionProgressing = EnumStatus.DOWNLOAD
                                        val mResultDownloadedFiles = driveViewModel.downLoadData(false,mResultItemList.data)
                                        when(mResultDownloadedFiles.status){
                                            Status.SUCCESS -> {
                                                Utils.onPushEventBus(EnumStatus.UPLOAD)
                                                actionProgressing = EnumStatus.UPLOAD
                                                val mResultUploadedFiles = driveViewModel.uploadData()
                                                when(mResultUploadedFiles.status){
                                                    Status.SUCCESS -> {
                                                        Utils.onPushEventBus(EnumStatus.DONE)
                                                        val mResultUpdatedItem = itemViewModel.updateItemToSystem()
                                                        when(mResultUpdatedItem.status){
                                                            Status.SUCCESS -> {
                                                                actionProgressing = EnumStatus.DONE
                                                                Utils.onPushEventBus(EnumStatus.DONE)
                                                                val mResultDeletedItem = driveViewModel.deleteItemFromCloud()
                                                                when(mResultDeletedItem.status){
                                                                    Status.SUCCESS -> {
                                                                        Utils.onPushEventBus(EnumStatus.DONE)
                                                                        Utils.Log(TAG,"Sync completed")
                                                                    }
                                                                    else -> Utils.Log(TAG,mResultDeletedItem.message)
                                                                }
                                                            }
                                                            else -> Utils.Log(TAG,mResultUpdatedItem.message)
                                                        }
                                                    }
                                                    else -> Utils.Log(TAG,mResultUploadedFiles.message)
                                                }
                                                Utils.onPushEventBus(EnumStatus.DONE)
                                            }
                                            else -> Utils.Log(TAG,mResultDownloadedFiles.message)
                                        }
                                        Utils.onPushEventBus(EnumStatus.DONE)
                                    }
                                    else -> Utils.Log(TAG,mResultDeletedCategory.message)
                                }
                            }
                            else -> Utils.Log(TAG,mResultUpdatedCategory.message)
                        }
                    }
                    else -> Utils.Log(TAG,mResultSyncCategory.message)
                }
            }
            else -> Utils.Log(TAG,mResultItemList.message)
        }
        Utils.Log(TAG,"Sync completely done")
        isRequestingSyncCor = false
        Utils.setRequestSyncData(false)
        actionProgressing = EnumStatus.DONE
        Utils.onPushEventBus(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
        SingletonPrivateFragment.getInstance()?.onUpdateView()
        if (SuperSafeApplication.isRunningBackground == true){
            onDismissServices()
        }
    }

    fun updatedDriveAccessToken() = CoroutineScope(Dispatchers.IO).launch {
        RefreshTokenSingleton.getInstance().onStart(ServiceManager::class.java)
    }

    fun updatedUserToken() = CoroutineScope(Dispatchers.IO).launch{
        isRequestingUpdateUserToken = true
        val mResult = userViewModel.updatedUserToken()
        when(mResult.status){
            Status.SUCCESS ->{
                Utils.Log(TAG,"Success ${mResult.data?.toJson()}")
                onPreparingSyncData()
            }
            else ->{
                Utils.Log(TAG,"Error ${mResult.message}")
            }
        }
        isRequestingUpdateUserToken = false
    }

    /*Preparing sync category*/
    fun onPreparingSyncCategoryData() = CoroutineScope(Dispatchers.IO).launch {
        val mResult = categoryViewModel.syncCategoryData()
        when(mResult.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Category synced completely")
                val mUpdatedCategory = categoryViewModel.updateCategoryData()
                when (mUpdatedCategory.status){
                    Status.SUCCESS -> {
                        val mDeletedCategory = categoryViewModel.dateCategoryData()
                        when(mDeletedCategory.status){
                            Status.SUCCESS -> Utils.Log(TAG,"Category updated completely")
                            else -> Utils.Log(TAG,mDeletedCategory.message)
                        }
                    }
                    else -> Utils.Log(TAG,mResult.message)
                }
            }
            else -> Utils.Log(TAG,mResult.message)
        }
    }


    /*Import data from gallery*/
    suspend fun onImportData(mData : MutableList<ImportFilesModel>) : Resource<Boolean>{
       return withContext(Dispatchers.IO){
           try {
               for (index in mData) {
                   val mMimeTypeFile: MimeTypeFile = index.mimeTypeFile!!
                   val enumTypeFile = mMimeTypeFile.formatType
                   val mPath: String = index.path!!
                   val mMimeType = mMimeTypeFile.mimeType
                   val mMainCategories: MainCategoryModel = index.mainCategories!!
                   val mCategoriesId: String = mMainCategories.categories_id!!
                   val mCategoriesLocalId: String = mMainCategories.categories_local_id!!
                   val isFakePin: Boolean = mMainCategories.isFakePin
                   val uuId: String = index.unique_id!!
                   var thumbnail: Bitmap?
                   when (enumTypeFile) {
                       EnumFormatType.IMAGE -> {
                           try {
                               val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                               val currentTime: String = Utils.getCurrentDateTime() as String
                               val pathContent = "$rootPath$uuId/"
                               pathContent.createDirectory()
                               val thumbnailPath = pathContent + "thumbnail_" + currentTime
                               val originalPath = pathContent + currentTime
                               val itemsPhoto = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, Utils.isSaverSpace(), false, false, 0, false, false, false, EnumStatus.UPLOAD)
                               val file = getThumbnail(mPath)
                               Utils.Log(TAG, "start compress")
                               val createdThumbnail = file.createFile(File(thumbnailPath), file, Cipher.ENCRYPT_MODE)
                               val createdOriginal = file.createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                               if (createdThumbnail && createdOriginal) {
                                   handleResponseImport(itemsPhoto,mMainCategories,mPath)
                                   Utils.Log(TAG, "CreatedFile successful")
                               } else {
                                   Utils.Log(TAG, "CreatedFile failed")
                               }
                           } catch (e: Exception) {
                               Utils.Log(TAG, "Cannot write to $e")
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
                                   val orientation = ExifInterface(mPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
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
                               pathContent.createDirectory()
                               val thumbnailPath = pathContent + "thumbnail_" + currentTime
                               val originalPath = pathContent + currentTime
                               val itemsVideo = ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.VIDEO, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                               Utils.Log(TAG, "Call thumbnail")
                               val createdThumbnail: Boolean = thumbnail?.toByteArray()?.createFile(thumbnailPath)!!
                               val createdOriginal = File(originalPath).createCipherFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                               Utils.Log(TAG, "Call original")
                               if (createdThumbnail && createdOriginal) {
                                   handleResponseImport(itemsVideo,mMainCategories,mPath)
                                   Utils.Log(TAG, "CreatedFile successful")
                               } else {
                                   Utils.Log(TAG, "CreatedFile failed")
                               }
                           } catch (e: Exception) {
                               Utils.Log(TAG, "Cannot write to $e")
                           } finally {
                               Utils.Log(TAG, "Finally")
                           }
                       }
                       EnumFormatType.AUDIO -> {
                           try {
                               val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                               val currentTime: String = Utils.getCurrentDateTime() as String
                               val pathContent = "$rootPath$uuId/"
                               pathContent.createDirectory()
                               val originalPath = pathContent + currentTime
                               val itemsAudio = ItemModel(mMimeTypeFile.extension, originalPath, "null", mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.AUDIO, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                               val createdOriginal = File(originalPath).createCipherFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                               if (createdOriginal) {
                                   handleResponseImport(itemsAudio,mMainCategories,mPath)
                                   Utils.Log(TAG, "CreatedFile successful")
                               } else {
                                   Utils.Log(TAG, "CreatedFile failed")
                               }
                           } catch (e: Exception) {
                               Utils.Log(TAG, "Cannot write to $e")
                           } finally {
                               Utils.Log(TAG, "Finally")
                           }
                       }
                       EnumFormatType.FILES -> {
                           try {
                               val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
                               val currentTime: String = Utils.getCurrentDateTime() as String
                               val pathContent = "$rootPath$uuId/"
                               pathContent.createDirectory()
                               val originalPath = pathContent + currentTime
                               val itemsFile = ItemModel(mMimeTypeFile.extension, originalPath, "null", mCategoriesId, mCategoriesLocalId, mMimeType, uuId, EnumFormatType.FILES, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD)
                               val createdOriginal = File(originalPath).createFile(File(originalPath), File(mPath), Cipher.ENCRYPT_MODE)
                               if (createdOriginal) {
                                   Utils.Log(TAG, "CreatedFile successful")
                                   handleResponseImport(itemsFile,mMainCategories,mPath)
                               } else {
                                   Utils.Log(TAG, "CreatedFile failed")
                               }
                           } catch (e: Exception) {
                               Utils.Log(TAG, "Cannot write to $e")
                           } finally {
                               Utils.Log(TAG, "Finally")
                           }
                       }
                   }
               }
               Utils.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY)
               Resource.success(true)
           }
           catch (e : Exception) {
               e.printStackTrace()
               Resource.error(Utils.CODE_EXCEPTION, e.message ?: "", null)
           }
       }
    }

    private fun handleResponseImport(mData: ItemModel,categories : MainCategoryModel,originalPath : String) {
        try {
            mData.let { items ->
                var mb: Long
                when (val enumFormatType = EnumFormatType.values()[items.formatType]) {
                    EnumFormatType.AUDIO -> {
                        if (items.getOriginal().isFileExist()) {
                            mb = +File(items.getOriginal()).getSize(SizeUnit.B).toLong()
                            items.size = "" + mb
                            SQLHelper.insertedItem(items)
                        }
                    }
                    EnumFormatType.FILES -> {
                        if (items.getOriginal().isFileExist()) {
                            mb = +File(items.getOriginal()).getSize(SizeUnit.B).toLong()
                            items.size = "" + mb
                            SQLHelper.insertedItem(items)
                        }
                    }
                    else -> {
                        if (items.getOriginal().isFileExist() && items.getThumbnail().isFileExist()) {
                            mb = +File(items.getOriginal()).getSize(SizeUnit.B).toLong()
                            if (items.getThumbnail().isFileExist()) {
                                mb += +File(items.getThumbnail()).getSize( SizeUnit.B).toLong()
                            }
                            items.size = "" + mb
                            SQLHelper.insertedItem(items)
                            if (!categories.isCustom_Cover) {
                                if (enumFormatType == EnumFormatType.IMAGE) {
                                    categories.items_id = items.items_id
                                    SQLHelper.updateCategory(categories)
                                }
                            }
                        }
                    }
                }
                Utils.onPushEventBus(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                if (items.isFakePin) {
                    SingletonFakePinComponent.getInstance().onUpdateView()
                } else {
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                }
                /*Share intent is not deleted*/
                if (!getRequestShareIntent()) {
                    Utils.onDeleteFile(originalPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    fun setIsWaitingSendMail(isWaitingSendMail: Boolean) {
        this.isWaitingSendMail = isWaitingSendMail
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
        }else{
            Utils.Log(TAG,"Service already started")
        }
    }

    fun onStopService() {
        if (myService != null) {
            myConnection?.let { mContext?.unbindService(it) }
            myService = null
            Utils.Log(TAG, "stop services now")
        }
    }

    fun onSendEmail() = CoroutineScope(Dispatchers.IO).launch {
        emailOutlookViewModel.sendEmail(EnumStatus.RESET)
    }

    fun getMyService(): SuperSafeService? {
        return myService
    }

    private fun getString(res: Int): String {
        return SuperSafeApplication.getInstance().getString(res)
    }

    /*User info*/
    private fun onGetUserInfo() = CoroutineScope(Dispatchers.IO).launch {
        Utils.Log(TAG, "onGetUserInfo")
        val mResult = userViewModel.getUserInfo()
        when(mResult.status){
            Status.SUCCESS -> Utils.Log(TAG,"Get User info completed")
            else -> Utils.Log(TAG,mResult.message)
        }
    }

    /*--------------Camera action-----------------*/
    fun onSaveDataOnCamera(mData: ByteArray?, mainCategories: MainCategoryModel?) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val mCategoriesId: String = mainCategories?.categories_id as String
            val mCategoriesLocalId: String = mainCategories.categories_local_id as String
            val isFakePin: Boolean = mainCategories.isFakePin
            val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
            val currentTime: String = Utils.getCurrentDateTime() as String
            val uuId: String = Utils.getUUId() as String
            val pathContent = "$rootPath$uuId/"
            pathContent.createDirectory()
            val thumbnailPath = pathContent + "thumbnail_" + currentTime
            val originalPath = pathContent + currentTime
            val isSaver: Boolean = Utils.isSaverSpace()
            val items = ItemModel(getString(R.string.key_jpg), originalPath, thumbnailPath, mCategoriesId, mCategoriesLocalId, MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype(), uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, currentTime + getString(R.string.key_jpg), "thumbnail_$currentTime", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, isSaver, false, false, 0, false, false, false, EnumStatus.UPLOAD)
            val mFileOriginal = mData?.createFileByteDataNoEncrypt(SuperSafeApplication.getInstance())
            val mFileThumbnail = getThumbnail(mFileOriginal?.absolutePath!!)
            val createdThumbnail = mFileThumbnail.createFile(File(thumbnailPath), mFileThumbnail, Cipher.ENCRYPT_MODE)
            val createdOriginal = mFileOriginal.createFile(File(originalPath), mFileOriginal, Cipher.ENCRYPT_MODE)
            if (createdThumbnail && createdOriginal) {
                Utils.Log(TAG, "CreatedFile successful")
            } else {
                Utils.Log(TAG, "CreatedFile failed")
            }
            var mb: Long
            if (items.getOriginal().isFileExist() && items.getThumbnail().isFileExist()) {
                mb = +File(items.getOriginal()).getSize(SizeUnit.B).toLong()
                mb += +File(items.getThumbnail()).getSize( SizeUnit.B).toLong()
                items.size = "" + mb
                SQLHelper.insertedItem(items)
                Utils.Log(TAG,"Saved file to local db")
                if (!mainCategories.isCustom_Cover) {
                    mainCategories.items_id = items.items_id
                    SQLHelper.updateCategory(mainCategories)
                }
            }
            GalleryCameraMediaManager.getInstance()?.setProgressing(false)
            if (items.isFakePin) {
                SingletonFakePinComponent.getInstance().onUpdateView()
            } else {
                SingletonPrivateFragment.getInstance()?.onUpdateView()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        catch (e : IOException){
            e.printStackTrace()
        }
    }

    suspend fun getThumbnail(path : String) : File  = withContext(Dispatchers.IO) {
       return@withContext Compressor.compress(SuperSafeApplication.getInstance(), File(path)) {
            resolution(1032, 774)
           quality(85)
           format(Bitmap.CompressFormat.JPEG)
        }
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
                when (EnumPinAction.values()[Utils.getScreenStatus()]) {
                    EnumPinAction.NONE -> {
                        val key: String? = SuperSafeApplication.getInstance().readKey()
                        if (key?.isNotEmpty() == true) {
                            Utils.putScreenStatus(EnumPinAction.SCREEN_LOCK.ordinal)
                        }
                    }
                    else -> {
                        Utils.Log(TAG, "Nothing to do ???")
                    }
                }
            }
            EnumStatus.CONNECTED -> {
                Utils.setRequestSyncData(true)
                Utils.onPushEventBus(EnumStatus.CONNECTED)
                if (!Utils.getUserId().isNullOrEmpty()){
                    onPreparingSyncData()
                }
            }
            EnumStatus.DISCONNECTED -> {
                onDefaultValue()
                Utils.Log(TAG, "Disconnect")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    suspend fun downloadFilesToExporting(globalList: MutableList<ItemModel>?) : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                val mResult = driveViewModel.downLoadData(true,globalList)
                Utils.Log(TAG,"Start download")
                when(mResult.status){
                    Status.SUCCESS -> {
                        Utils.Log(TAG,"Completed download file")
                        Resource.success(true)
                    }
                    else ->{
                        Utils.Log(TAG,mResult.message)
                        Resource.error(mResult.code ?: Utils.CODE_EXCEPTION,mResult.message ?:"",null)
                    }
                }
            }catch (e : Exception){
                Resource.error(Utils.CODE_EXCEPTION,e.message ?:"", null)
            }
        }
    }

    suspend fun exportingItems(mData : MutableList<ItemModel>,isSharingFiles : Boolean) : Resource<MutableList<File>>{
        return withContext(Dispatchers.IO){
            try {
                val mResponseList = mutableListOf<File>()
                for (index in mData){
                    val mInput  = Utils.geInputExportFiles(index)
                    val mOutPut: File = Utils.geOutputExportFiles(index,isSharingFiles)!!
                    try {
                        when (EnumFormatType.values()[index.formatType]){
                            EnumFormatType.IMAGE -> {
                                mOutPut.createFile(mOutPut,mInput,Cipher.DECRYPT_MODE)
                            }
                            EnumFormatType.FILES -> {
                                mOutPut.createFile(mOutPut,mInput,Cipher.DECRYPT_MODE)
                            }
                            else -> {
                                /*Exporting type is video and audio, Because configuration to play media is cipher object*/
                                mOutPut.createCipherFile(mOutPut,mInput,Cipher.DECRYPT_MODE)
                            }
                        }
                        mResponseList.add(mOutPut)
                        Utils.Log(TAG,"Exported completely")
                    } catch (e: Exception) {
                        Utils.Log(TAG, "Cannot write to $e")
                    } finally {
                        Utils.Log(TAG, "Finally")
                    }
                }
                Resource.success(mResponseList)
            }
            catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION,e.message ?:"",null)
            }
        }
    }

    private fun onDefaultValue() {
        isWaitingSendMail = false
        isRequestingUpdateUserToken = false
        isRequestingSyncCor = false
    }

    fun onDismissServices() {
        if (isWaitingSendMail || isRequestingUpdateUserToken || isRequestingSyncCor) {
            Utils.Log(TAG, "Progress....................!!!!:")
        } else {
            onDefaultValue()
            if (myService != null) {
                myService?.unbindView()
            }
            onStopService()
            Utils.Log(TAG, "Dismiss Service manager")
        }
    }

    private fun getRequestShareIntent(): Boolean {
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

    init {
       mContext = SuperSafeApplication.getInstance()
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