package co.tpcreative.supersafe.common.controller;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.IBinder;
import android.provider.MediaStore;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;
import com.snatik.storage.helpers.SizeUnit;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseServiceView;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeService;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.ExportFiles;
import co.tpcreative.supersafe.model.ImportFilesModel;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.ResponseRXJava;
import co.tpcreative.supersafe.model.User;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceManager implements BaseServiceView {
    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private SuperSafeService myService;
    private Context mContext;
    private Disposable subscriptions;
    private Storage storage = new Storage(SuperSafeApplication.getInstance());
    private Storage mStorage = new Storage(SuperSafeApplication.getInstance());
    private List<ExportFiles> mListExport = new ArrayList<>();
    private String mProgress;

    /*Improved sync data*/
    private List<ImportFilesModel> listImport = new ArrayList<>();
    private boolean isDownloadData,isUploadData,isUpdateItemData,isUpdateCategoryData,isSyncCategory,isGetItemList, isImportData,isExportData,isDownloadToExportFiles, isDeleteItemData,isDeleteCategoryData,isHandleLogic,isRequestShareIntent;
    /*Using item_id as key for hash map*/
    private Map<String, ItemModel> mMapDeleteItem = new HashMap<>();
    private Map<String, MainCategoryModel> mMapDeleteCategory = new HashMap<>();
    private Map<String, MainCategoryModel> mMapUpdateCategory = new HashMap<>();
    private Map<String, MainCategoryModel> mMapSyncCategory = new HashMap<>();
    private Map<String,ItemModel> mMapDownload = new HashMap<>();
    private Map<String,ItemModel> mMapDownloadToExportFiles = new HashMap<>();
    private Map<String,ItemModel> mMapUpload = new HashMap<>();
    private Map<String,ItemModel> mMapUpdateItem = new HashMap<>();
    private Map<String,ImportFilesModel> mMapImporting = new HashMap<>();
    private List<ItemModel> mDownloadList = new ArrayList<>();
    private int mStart = 20;

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Utils.Log(TAG, "connected");
            myService = ((SuperSafeService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            ServiceManager.getInstance().onGetUserInfo();
            ServiceManager.getInstance().onSyncAuthorDevice();
            ServiceManager.getInstance().onGetDriveAbout();
            Utils.onScanFile(SuperSafeApplication.getInstance(),"scan.log");
        }
        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Utils.Log(TAG, "disconnected");
            myService = null;
        }
    };
    public void onInitConfigurationFile(){
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
    }
    private Cipher mCiphers;
    private boolean isWaitingSendMail;
    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public void setListImport(List<ImportFilesModel> mListImport) {
        if (!isImportData) {
            this.listImport.clear();
            this.listImport.addAll(mListImport);
        }
    }

    /*Preparing sync data*/
    public void onPreparingSyncData(){
        if (Utils.getUserId()==null){
            return;
        }
        Utils.Log(TAG,"onPreparingSyncData...???");
        if (!Utils.isAllowSyncData()){
            Utils.Log(TAG,"onPreparingSyncData is unauthorized " + isDownloadData);
            Utils.onWriteLog(EnumStatus.AUTHOR_SYNC,EnumStatus.AUTHOR_SYNC,"onPreparingSyncData is unauthorized");
            return;
        }
        if (isGetItemList){
            Utils.Log(TAG,"onPreparingSyncData is getting item list. Please wait");
            Utils.onWriteLog(EnumStatus.GET_LIST_FILE,EnumStatus.ERROR,"onPreparingSyncData is getting item list. Please wait");
            return;
        }
        if (isDownloadData){
            Utils.Log(TAG,"onPreparingSyncData is downloading. Please wait");
            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.ERROR,"onPreparingSyncData is downloading. Please wait");
            return;
        }
        if (isUploadData){
            Utils.Log(TAG,"onPreparingSyncData is uploading. Please wait");
            Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.ERROR,"onPreparingSyncData is uploading. Please wait");
            return;
        }
        if (isDeleteItemData){
            Utils.Log(TAG,"onPreparingSyncData is deleting. Please wait");
            Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.ERROR,"onPreparingSyncData is deleting. Please wait");
            return;
        }
        if (isDeleteCategoryData){
            Utils.Log(TAG,"onPreparingSyncData is deleting category. Please wait");
            Utils.onWriteLog(EnumStatus.DELETE_CATEGORIES,EnumStatus.ERROR,"onPreparingSyncData is deleting category. Please wait");
            return;
        }
        if (isImportData){
            Utils.Log(TAG,"onPreparingSyncData is importing. Please wait");
            Utils.onWriteLog(EnumStatus.IMPORTING,EnumStatus.ERROR,"onPreparingSyncData is importing. Please wait");
            return;
        }
        if (isDownloadToExportFiles){
            Utils.Log(TAG,"onPreparingSyncData is downloading files. Please wait");
            Utils.onWriteLog(EnumStatus.DOWNLOADING,EnumStatus.ERROR,"onPreparingSyncData is downloading to export files. Please wait");
            return;
        }
        if (isUpdateItemData){
            Utils.Log(TAG,"onPreparingSyncData is updating item. Please wait");
            Utils.onWriteLog(EnumStatus.UPDATE_ITEM,EnumStatus.ERROR,"onPreparingSyncData is updating item.. Please wait");
            return;
        }
        if (isUpdateCategoryData){
            Utils.Log(TAG,"onPreparingSyncData is updating category. Please wait");
            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.ERROR,"onPreparingSyncData is updating category.. Please wait");
            return;
        }
        if (isHandleLogic){
            Utils.Log(TAG,"onPreparingSyncData is handle logic. Please wait");
            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.ERROR,"onPreparingSyncData is handle logic. Please wait");
            return;
        }

        if (isSyncCategory){
            Utils.Log(TAG,"onPreparingSyncData is sync category. Please wait");
            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC,EnumStatus.ERROR,"onPreparingSyncData is sync category. Please wait");
            return;
        }
        mDownloadList.clear();
        Utils.Log(TAG,"onPreparingSyncData...onGetItemList");
        ServiceManager.getInstance().onGetItemList("0");
    }

    private void onGetItemList(String next){
        if (myService!=null){
            isGetItemList = true;
            /*Stop multiple request*/
            isHandleLogic = true;
            myService.onGetListSync(next, new BaseListener<ItemModel>() {
                @Override
                public void onShowListObjects(List<ItemModel> list) {
                    mDownloadList.addAll(list);
                }
                @Override
                public void onShowObjects(ItemModel object) {

                }
                @Override
                public void onError(String message, EnumStatus status) {
                    isGetItemList = false;
                }
                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    if (status==EnumStatus.LOAD_MORE){
                        isGetItemList = true;
                        ServiceManager.getInstance().onGetItemList(message);
                        Utils.Log(TAG,"Continue load more "+ message);
                    }else if (status == EnumStatus.SYNC_READY){
                        /*Start sync*/
                        isGetItemList = false;
                        SingletonPrivateFragment.getInstance().onUpdateView();
                        onPreparingSyncCategoryData();
                        Utils.Log(TAG,"Start to sync data.......");
                    }
                }
            });
        }
    }

    /*Preparing sync category*/
    public void onPreparingSyncCategoryData(){
        final List<MainCategoryModel> mResult = SQLHelper.requestSyncCategories(false,false);
        if (mResult.size()>0){
            mMapSyncCategory.clear();
            mMapSyncCategory = Utils.mergeListToCategoryHashMap(mResult);
            final MainCategoryModel itemModel = Utils.getArrayOfIndexCategoryHashMap(mMapSyncCategory);
            if (itemModel!=null){
                Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC,EnumStatus.PROGRESS,"Total updating "+mMapSyncCategory.size());
                Utils.Log(TAG,"onPreparingSyncCategoryData ==> total: "+ mMapSyncCategory.size());
                onSyncCategoryData(itemModel);
            }
        }else{
           onPreparingUpdateCategoryData();
        }
    }

    /*Sync category data*/
    public void onSyncCategoryData(MainCategoryModel categoryModel){
        if(myService != null){
            isSyncCategory = true;
            myService.onCategoriesSync(categoryModel, new BaseListener() {
                @Override
                public void onShowListObjects(List list) {

                }

                @Override
                public void onShowObjects(Object object) {

                }

                @Override
                public void onError(String message, EnumStatus status) {
                    isSyncCategory = false;
                }

                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    isSyncCategory = false;
                    if (Utils.deletedIndexOfCategoryHashMap(categoryModel,mMapSyncCategory)){
                        /*Delete local db and folder name*/
                        final MainCategoryModel mUpdatedItem = Utils.getArrayOfIndexCategoryHashMap(mMapSyncCategory);
                        if (mUpdatedItem!=null){
                            onSyncCategoryData(mUpdatedItem);
                            isSyncCategory = true;
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC,EnumStatus.DONE,new Gson().toJson(categoryModel));
                            Utils.Log(TAG,"Next update item..............." + new Gson().toJson(mUpdatedItem));
                        }else{
                            Utils.Log(TAG,"Update completely...............");
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC,EnumStatus.DONE,new Gson().toJson(categoryModel));
                            Utils.onWriteLog(EnumStatus.CATEGORIES_SYNC,EnumStatus.UPDATED_COMPLETED,"Total updating "+mMapSyncCategory.size());
                            onPreparingUpdateCategoryData();
                        }
                    }
                }
            });
        }
    }

    /*Preparing update category*/
    public void onPreparingUpdateCategoryData(){
        final List<MainCategoryModel> mResult = SQLHelper.getRequestUpdateCategoryList();
        if (mResult.size()>0){
            mMapUpdateCategory.clear();
            mMapUpdateCategory = Utils.mergeListToCategoryHashMap(mResult);
            final MainCategoryModel itemModel = Utils.getArrayOfIndexCategoryHashMap(mMapUpdateCategory);
            if (itemModel!=null){
                Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.PROGRESS,"Total updating "+mMapUpdateItem.size());
                Utils.Log(TAG,"onPreparingUpdateCategoryData ==> total: "+ mMapUpdateItem.size());
                onUpdateCategoryData(itemModel);
            }
        }else{
            onPreparingDeleteCategoryData();
        }
    }

    public void onUpdateCategoryData(MainCategoryModel itemModel){
        if(myService != null){
            isUpdateCategoryData = true;
            myService.onCategoriesSync(itemModel, new BaseListener() {
                @Override
                public void onShowListObjects(List list) {

                }

                @Override
                public void onShowObjects(Object object) {

                }

                @Override
                public void onError(String message, EnumStatus status) {
                    isUpdateCategoryData = false;
                }

                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    isUpdateCategoryData = false;
                    if (Utils.deletedIndexOfCategoryHashMap(itemModel,mMapUpdateCategory)){
                        /*Delete local db and folder name*/
                        final MainCategoryModel mUpdatedItem = Utils.getArrayOfIndexCategoryHashMap(mMapUpdateCategory);
                        if (mUpdatedItem!=null){
                            onUpdateCategoryData(mUpdatedItem);
                            isUpdateCategoryData = true;
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.Log(TAG,"Next update item..............." + new Gson().toJson(mUpdatedItem));
                        }else{
                            Utils.Log(TAG,"Update completely...............");
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.onWriteLog(EnumStatus.UPDATE_CATEGORY,EnumStatus.UPDATED_COMPLETED,"Total updating "+mMapUpdateCategory.size());
                            isUpdateCategoryData = false;
                            Utils.onPushEventBus(EnumStatus.UPDATED_COMPLETED);
                            Utils.onPushEventBus(EnumStatus.DONE);
                            onPreparingDeleteCategoryData();
                        }
                    }
                }
            });
        }
    }

    /*Preparing to delete category from system server*/
    private void onPreparingDeleteCategoryData(){
        final List<MainCategoryModel> listRequestDelete = SQLHelper.getDeleteCategoryRequest();
        mMapDeleteCategory.clear();
        mMapDeleteCategory = Utils.mergeListToCategoryHashMap(listRequestDelete);
        Utils.Log(TAG,"onPreparingDeleteCategoryData preparing to delete "+mMapDeleteCategory.size());
        final MainCategoryModel categoryModel = Utils.getArrayOfIndexCategoryHashMap(mMapDeleteCategory);
        if (categoryModel!=null){
            Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.PROGRESS,"Total Delete category "+mMapDeleteCategory.size());
            onDeleteCategoryData(categoryModel);
            Utils.Log(TAG,"onPreparingDeleteCategoryData to delete data " + mMapDeleteCategory.size());
        }else{
           onPreparingDownloadData(mDownloadList);
        }
    }

    /*Delete category from Google drive and server*/
    public void onDeleteCategoryData(final MainCategoryModel mainCategoryModel){
        if (myService==null){
            return;
        }
        isDeleteCategoryData = true;
        myService.onDeleteCategoriesSync(mainCategoryModel, new BaseListener() {
            @Override
            public void onShowListObjects(List list) {

            }
            @Override
            public void onShowObjects(Object object) {

            }

            @Override
            public void onError(String message, EnumStatus status) {
                isDeleteCategoryData = false;
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                if (Utils.deletedIndexOfCategoryHashMap(mainCategoryModel,mMapDeleteCategory)){
                    /*Delete local db and folder name*/
                    SQLHelper.deleteCategory(mainCategoryModel);
                    final MainCategoryModel mDeleteItem = Utils.getArrayOfIndexCategoryHashMap(mMapDeleteCategory);
                    if (mDeleteItem!=null){
                        isDeleteCategoryData = true;
                        Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DONE,new Gson().toJson(mainCategoryModel));
                        onDeleteCategoryData(mDeleteItem);
                    }else{
                        isDeleteCategoryData = false;
                        Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DONE,new Gson().toJson(mainCategoryModel));
                        Utils.Log(TAG,"Deleted completely...............");
                        Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DELETED_CATEGORY_SUCCESSFULLY,"Total uploading "+mMapDeleteCategory.size());
                        onPreparingDownloadData(mDownloadList);
                    }
                }
            }
        });
    }

    /*Preparing to download data from Google drive and system server*/
    private void onPreparingDownloadData(List<ItemModel>globalList){
        List<ItemModel> mListLocal = SQLHelper.getItemListDownload();
        Utils.Log(TAG,"onPreparingDownloadData ==> Local original list "+ new Gson().toJson(mListLocal));
        if (mListLocal!=null){
            Utils.Log(TAG,"onPreparingDownloadData ==> Local list "+ new Gson().toJson(mListLocal));
            if (globalList!=null && mListLocal!=null){
                List<ItemModel> mergeList = Utils.clearListFromDuplicate(globalList,mListLocal);
                Utils.Log(TAG,"onPreparingDownloadData ==> clear duplicated data "+ new Gson().toJson(mergeList));
                if (mergeList!=null){
                    if (mergeList.size()>0){
                        mMapDownload.clear();
                        mMapDownload = Utils.mergeListToHashMap(mergeList);
                        Utils.Log(TAG,"onPreparingDownloadData ==> clear merged data "+ new Gson().toJson(mMapDownload));
                        Utils.Log(TAG,"onPreparingDownloadData ==> merged data "+ new Gson().toJson(mergeList));
                        final ItemModel itemModel = Utils.getArrayOfIndexHashMap(mMapDownload);
                        if (itemModel!=null){
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.PROGRESS,"Total downloading "+mMapDownload.size());
                            Utils.Log(TAG,"Preparing to download "+ new Gson().toJson(itemModel));
                            Utils.Log(TAG,"Preparing to download total  "+ mMapDownload.size());
                            onDownLoadData(itemModel);
                        }
                    }else{
                        /*Preparing upload file to Google drive*/
                        onPreparingUploadData();
                    }
                }
            }
        }
    }

    /*Download file from Google drive*/
    private void onDownLoadData(final ItemModel itemModel){
        if (myService!=null){
            isDownloadData = true;
            mStart = 20;
            Utils.onPushEventBus(EnumStatus.DOWNLOAD);
            myService.onDownloadFile(itemModel,false, new DownloadServiceListener() {
                @Override
                public void onProgressDownload(int percentage) {
                    isDownloadData = true;
                    if (mStart == percentage){
                        Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.PROGRESS,"Progressing "+ mStart);
                        mStart += 20;
                    }
                }
                @Override
                public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                    Utils.Log(TAG,"onDownLoadCompleted ==> onDownLoadCompleted:" + file_name.getAbsolutePath());
                    if (Utils.deletedIndexOfHashMap(itemModel,mMapDownload)){
                        /*Delete local db and folder name*/
                        final ItemModel mDownloadItem = Utils.getArrayOfIndexHashMap(mMapDownload);
                        if (mDownloadItem!=null){
                            onDownLoadData(mDownloadItem);
                            isDownloadData = true;
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.Log(TAG,"Next download item..............." + new Gson().toJson(mDownloadItem));
                        }else{
                            Utils.Log(TAG,"Download completely...............");
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DOWNLOAD_COMPLETED,"Total downloading "+mMapDownload.size());
                            isDownloadData = false;
                            onPreparingUploadData();
                            /*Download done for main tab*/
                            Utils.onPushEventBus(EnumStatus.DONE);
                        }
                    }
                }
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG,"onDownLoadData ==> onError:" + message);
                    Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.ERROR,"onDownLoadData ==> onError "+message);
                    isDownloadData = false;
                    if (status == EnumStatus.NO_SPACE_LEFT){
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT);
                        Utils.onDeleteItemFolder(itemModel.items_id);
                        onPreparingDeleteData();
                        /*Download done for main tab*/
                        Utils.onPushEventBus(EnumStatus.DONE);
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD){
                        if (Utils.deletedIndexOfHashMap(itemModel,mMapDownload)){
                            /*Delete local db and folder name*/
                            final ItemModel mDownloadItem = Utils.getArrayOfIndexHashMap(mMapDownload);
                            if (mDownloadItem!=null){
                                onDownLoadData(mDownloadItem);
                                isDownloadData = true;
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.Log(TAG,"Next download item..............." + new Gson().toJson(mDownloadItem));
                            }else{
                                Utils.Log(TAG,"Download completely...............");
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DOWNLOAD_COMPLETED,"Total downloading "+mMapDownload.size());
                                isDownloadData = false;
                                onPreparingUploadData();
                                /*Download done for main tab*/
                                Utils.onPushEventBus(EnumStatus.DONE);
                            }
                        }
                    }
                }
            });
        }
    }


    /*Preparing upload data*/
    private void onPreparingUploadData(){
        Utils.Log(TAG,"onPreparingUploadData");
        if (!User.getInstance().isCheckAllowUpload()){
            Utils.Log(TAG,"onPreparingUploadData ==> Left 0. Please wait for next month or upgrade to premium version");
            Utils.onPushEventBus(EnumStatus.DONE);
            Utils.onPushEventBus(EnumStatus.REFRESH);
            onPreparingUpdateItemData();
            return;
        }
        /*Preparing upload file to Google drive*/
        final List<ItemModel> mResult = SQLHelper.getItemListUpload();
        final List<ItemModel> listUpload = Utils.getMergedOriginalThumbnailList(true,mResult);
        if (listUpload.size()>0){
            mMapUpload.clear();
            mMapUpload = Utils.mergeListToHashMap(listUpload);
            final ItemModel itemModel = Utils.getArrayOfIndexHashMap(mMapUpload);
            if (itemModel!=null){
                Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.PROGRESS,"Total uploading "+mMapUpload.size());
                Utils.Log(TAG,"onPreparingUploadData ==> total: "+ mMapUpload.size());
                onUploadData(itemModel);
            }
        }else{
            Utils.Log(TAG,"Not found item to upload");
            onPreparingUpdateItemData();
        }
    }

    /*Upload file to Google drive*/
    private void onUploadData(ItemModel itemModel){
        if (myService!=null){
            mStart = 20;
            Utils.onPushEventBus(EnumStatus.UPLOAD);
            myService.onUploadFileInAppFolder(itemModel, new UploadServiceListener() {
                @Override
                public void onProgressUpdate(int percentage) {
                    isUploadData = true;
                    if (mStart == percentage){
                        Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.PROGRESS,"Progressing "+mStart);
                        mStart += 20;
                    }
                }
                @Override
                public void onFinish() {
                }
                @Override
                public void onResponseData(DriveResponse response) {
                    if (response==null){
                        isUploadData = false;
                        return;
                    }
                    ServiceManager.getInstance().onInsertItem(itemModel,response.id, new ServiceManager.ServiceManagerInsertItem() {
                        @Override
                        public void onCancel() {
                            isUploadData = false;
                        }

                        @Override
                        public void onError(String message, EnumStatus status) {
                            isUploadData = false;
                            Utils.onPushEventBus(EnumStatus.DONE);
                        }
                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            if (Utils.deletedIndexOfHashMap(itemModel,mMapUpload)){
                                final ItemModel mUploadItem = Utils.getArrayOfIndexHashMap(mMapUpload);
                                if (mUploadItem!=null){
                                    onUploadData(mUploadItem);
                                    Utils.Log(TAG,"Next upload item..............." + new Gson().toJson(mUploadItem));
                                    isUploadData = true;
                                }else{
                                    isUploadData = false;
                                    onPreparingUpdateItemData();
                                    Utils.Log(TAG,"Upload completely...............");
                                    Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                    Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED);
                                    Utils.onPushEventBus(EnumStatus.DONE);
                                    Utils.onPushEventBus(EnumStatus.REFRESH);
                                    Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.UPLOAD_COMPLETED,"Total uploading "+mMapUpload.size());
                                }
                            }
                        }
                    });
                }
                @Override
                public void onError(String message, EnumStatus status) {
                    isUploadData = false;
                    Utils.onPushEventBus(EnumStatus.DONE);
                    Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.ERROR,"onUploadLoadData ==> onError "+message);
                    if (status == EnumStatus.NO_SPACE_LEFT_CLOUD){
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT_CLOUD);
                        onPreparingDeleteData();
                        Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED);
                    }
                    if (status == EnumStatus.REQUEST_NEXT_UPLOAD){
                        if (Utils.deletedIndexOfHashMap(itemModel,mMapUpload)){
                            final ItemModel mUploadItem = Utils.getArrayOfIndexHashMap(mMapUpload);
                            if (mUploadItem!=null){
                                onUploadData(mUploadItem);
                                Utils.Log(TAG,"Next upload item..............." + new Gson().toJson(mUploadItem));
                                isUploadData = true;
                            }else{
                                isUploadData = false;
                                onPreparingUpdateItemData();
                                Utils.Log(TAG,"Upload completely...............");
                                Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.onPushEventBus(EnumStatus.UPLOAD_COMPLETED);
                                Utils.onPushEventBus(EnumStatus.DONE);
                                Utils.onPushEventBus(EnumStatus.REFRESH);
                                Utils.onWriteLog(EnumStatus.UPLOAD,EnumStatus.UPLOAD_COMPLETED,"Total uploading "+mMapUpload.size());
                            }
                        }
                    }
                }
            });
        }
    }

    /*Preparing update item*/
    public void onPreparingUpdateItemData(){
        final List<ItemModel> mResult = SQLHelper.getRequestUpdateItemList();
        if (mResult.size()>0){
            mMapUpdateItem.clear();
            mMapUpdateItem = Utils.mergeListToHashMap(mResult);
            final ItemModel itemModel = Utils.getArrayOfIndexHashMap(mMapUpdateItem);
            if (itemModel!=null){
                Utils.onWriteLog(EnumStatus.UPDATE,EnumStatus.PROGRESS,"Total updating "+mMapUpdateItem.size());
                Utils.Log(TAG,"onPreparingUpdateItemData ==> total: "+ mMapUpdateItem.size());
                onUpdateItemData(itemModel);
            }
        }else{
            onPreparingDeleteData();
        }
    }

    /*Update item*/
    public void onUpdateItemData(ItemModel itemModel){
        if (myService!=null){
            isUpdateItemData = true;
            myService.onUpdateItems(itemModel, new BaseListener() {
                @Override
                public void onShowListObjects(List list) {

                }

                @Override
                public void onShowObjects(Object object) {

                }

                @Override
                public void onError(String message, EnumStatus status) {
                    isUpdateItemData = false;
                }

                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    isUpdateItemData = false;
                    if (Utils.deletedIndexOfHashMap(itemModel,mMapUpdateItem)){
                        /*Delete local db and folder name*/
                        final ItemModel mUpdatedItem = Utils.getArrayOfIndexHashMap(mMapUpdateItem);
                        if (mUpdatedItem!=null){
                            onUpdateItemData(mUpdatedItem);
                            isUpdateItemData = true;
                            Utils.onWriteLog(EnumStatus.UPDATE,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.Log(TAG,"Next update item..............." + new Gson().toJson(mUpdatedItem));
                        }else{
                            Utils.Log(TAG,"Update completely...............");
                            Utils.onWriteLog(EnumStatus.UPDATE,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.onWriteLog(EnumStatus.UPDATE,EnumStatus.UPDATED_COMPLETED,"Total updating "+mMapUpdateItem.size());
                            isUpdateItemData = false;
                            Utils.onPushEventBus(EnumStatus.UPDATED_COMPLETED);
                            Utils.onPushEventBus(EnumStatus.DONE);
                            onPreparingDeleteData();
                        }
                    }
                }
            });
        }
    }


    /*Preparing to delete item from system server*/
    public void onPreparingDeleteData(){
        final List<ItemModel> mResult = SQLHelper.getDeleteItemRequest();
        /*Merged original and thumbnail*/
        final List<ItemModel> listRequestDelete = Utils.getMergedOriginalThumbnailList(false,mResult);
        mMapDeleteItem.clear();
        mMapDeleteItem = Utils.mergeListToHashMap(listRequestDelete);
        Utils.Log(TAG,"onPreparingDeleteData preparing to delete "+mMapDeleteItem.size());
        final ItemModel itemModel = Utils.getArrayOfIndexHashMap(mMapDeleteItem);
        if (itemModel!=null){
            Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.PROGRESS,"Total Delete category "+mMapDeleteItem.size());
            onDeleteData(itemModel);
            Utils.Log(TAG,"Preparing to delete data " + mMapDeleteItem.size());
        }else{
            Utils.Log(TAG,"Not found item to upload");
            Utils.Log(TAG,"Not found item to delete ");
            Utils.Log(TAG,"Sync items completely======>ready to test");
            isDeleteItemData = false;
            isHandleLogic = false;
            Utils.onPushEventBus(EnumStatus.DONE);
            Utils.checkRequestUploadItemData();
            SingletonPrivateFragment.getInstance().onUpdateView();
        }
    }

    /*Delete item from Google drive and server*/
    public void onDeleteData(final ItemModel itemModel){
        if (myService==null){
            return;
        }
        isDeleteItemData = true;
        /*Request delete item from cloud*/
        myService.onDeleteCloudItems(itemModel, new BaseListener() {
            @Override
            public void onShowListObjects(List list) {

            }
            @Override
            public void onShowObjects(Object object) {

            }
            @Override
            public void onError(String message, EnumStatus status) {

            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                /*Request delete item from system*/
                myService.onDeleteOwnSystem(itemModel, new BaseListener() {
                    @Override
                    public void onShowListObjects(List list) {

                    }

                    @Override
                    public void onShowObjects(Object object) {

                    }

                    @Override
                    public void onError(String message, EnumStatus status) {
                        isDeleteItemData = false;
                    }

                    @Override
                    public void onSuccessful(String message, EnumStatus status) {
                        if (Utils.deletedIndexOfHashMap(itemModel,mMapDeleteItem)){
                            /*Delete local db and folder name*/
                            Utils.onDeleteItemFolder(itemModel.items_id);
                            SQLHelper.deleteItem(itemModel);
                            final ItemModel mDeleteItem = Utils.getArrayOfIndexHashMap(mMapDeleteItem);
                            if (mDeleteItem!=null){
                                isDeleteItemData = true;
                                Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DONE,new Gson().toJson(itemModel));
                                onDeleteData(mDeleteItem);
                            }else{
                                isDeleteItemData = false;
                                Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.Log(TAG,"Deleted completely...............");
                                Utils.onWriteLog(EnumStatus.DELETE,EnumStatus.DELETED_ITEM_SUCCESSFULLY,"Total deleted "+mMapDeleteItem.size());
                                Utils.Log(TAG,"Not found item to upload");
                                Utils.Log(TAG,"Not found item to delete ");
                                Utils.Log(TAG,"Sync items completely======>ready to test");
                                isHandleLogic = false;
                                Utils.onPushEventBus(EnumStatus.DONE);
                                Utils.checkRequestUploadItemData();
                                SingletonPrivateFragment.getInstance().onUpdateView();
                            }
                        }
                    }
                });
            }
        });
    }

    public void onInsertItem(ItemModel itemRequest,String drive_id, ServiceManagerInsertItem ls){
        if (myService!=null){
            myService.onAddItems(itemRequest,drive_id, new ServiceManagerInsertItem() {
                @Override
                public void onCancel() {
                    ls.onCancel();
                }

                @Override
                public void onError(String message, EnumStatus status) {
                    ls.onError(message,status);
                }
                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    ls.onSuccessful(message,status);
                }
            });
        }
    }

    /*Preparing import data*/
    public void onPreparingImportData(){
        if (isImportData){
            return;
        }
        /*Preparing upload file to Google drive*/
        if (listImport.size()>0){
            mMapImporting.clear();
            mMapImporting = Utils.mergeListToHashMapImport(listImport);
            final ImportFilesModel itemModel = Utils.getArrayOfIndexHashMapImport(mMapImporting);
            if (itemModel!=null){
                Utils.Log(TAG,"Preparing to import "+ new Gson().toJson(itemModel));
                Utils.onPushEventBus(EnumStatus.IMPORTING);
                onImportData(itemModel);
            }
        }else{
            Utils.Log(TAG,"Not found item to import");
        }
    }

    /*Import data from gallery*/
    private void onImportData(ImportFilesModel importFiles){
        subscriptions = Observable.create(subscriber -> {
            final MimeTypeFile mMimeTypeFile = importFiles.mimeTypeFile;
            final EnumFormatType enumTypeFile = mMimeTypeFile.formatType;
            final String mPath = importFiles.path;
            final String mMimeType = mMimeTypeFile.mimeType;
            final MainCategoryModel mMainCategories = importFiles.mainCategories;
            final String categories_id = mMainCategories.categories_id;
            final String categories_local_id = mMainCategories.categories_local_id;
            final boolean isFakePin = mMainCategories.isFakePin;
            final String uuId = importFiles.unique_id;
            Bitmap thumbnail = null;
            switch (enumTypeFile) {
                case IMAGE: {
                    Utils.Log(TAG, "Start RXJava Image Progressing");
                    try {
                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;
                        ItemModel itemsPhoto = new ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_" + currentTime, "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, Utils.getSaverSpace(), false, false, 0, false, false, false, EnumStatus.UPLOAD);
                        File file = new Compressor(SuperSafeApplication.getInstance())
                                .setMaxWidth(1032)
                                .setMaxHeight(774)
                                .setQuality(85)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(new File(mPath));
                        Utils.Log(TAG, "start compress");
                        boolean createdThumbnail = storage.createFile(new File(thumbnailPath), file, Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = storage.createFile(new File(originalPath), new File(mPath), Cipher.ENCRYPT_MODE);
                        Utils.Log(TAG, "start end");
                        final ResponseRXJava response = new ResponseRXJava();
                        response.items = itemsPhoto;
                        response.categories = mMainCategories;
                        response.originalPath = mPath;
                        if (createdThumbnail && createdOriginal) {
                            response.isWorking = true;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            response.isWorking = false;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }
                    } catch (Exception e) {
                        Utils.Log(TAG, "Cannot write to " + e);
                        Utils.onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.isWorking = false;
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }
                case VIDEO: {
                    Utils.Log(TAG, "Start RXJava Video Progressing");
                    try {
                        try {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(mPath,
                                    MediaStore.Video.Thumbnails.MINI_KIND);
                            ExifInterface exifInterface = new ExifInterface(mPath);
                            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                            Utils.Log("EXIF", "Exif: " + orientation);
                            Matrix matrix = new Matrix();
                            if (orientation == 6) {
                                matrix.postRotate(90);
                            } else if (orientation == 3) {
                                matrix.postRotate(180);
                            } else if (orientation == 8) {
                                matrix.postRotate(270);
                            }
                            thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true); // rotating bitmap
                        }
                        catch (Exception e){
                            thumbnail  = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().getResources(),
                                    R.drawable.ic_default_video);
                            Utils.Log(TAG, "Cannot write to " + e);
                        }
                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;
                        ItemModel itemsVideo = new ItemModel(mMimeTypeFile.extension, originalPath, thumbnailPath, categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.VIDEO, 0, false, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "thumbnail_" + currentTime, "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD);
                        Utils.Log(TAG,"Call thumbnail");
                        boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);
                        Utils.Log(TAG,"Call original");
                        final ResponseRXJava response = new ResponseRXJava();
                        response.items = itemsVideo;
                        response.categories = mMainCategories;
                        response.originalPath = mPath;
                        if (createdThumbnail && createdOriginal) {
                            response.isWorking = true;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            response.isWorking = false;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }
                    } catch (Exception e) {
                        Utils.Log(TAG, "Cannot write to " + e);
                        Utils.onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.isWorking = false;
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }
                case AUDIO: {
                    Utils.Log(TAG, "Start RXJava Audio Progressing");
                    try {
                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String originalPath = pathContent + currentTime;
                        ItemModel itemsAudio = new ItemModel(mMimeTypeFile.extension, originalPath, "null", categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.AUDIO, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD);
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.items = itemsAudio;
                        response.categories = mMainCategories;
                        response.originalPath = mPath;
                        if (createdOriginal) {
                            response.isWorking = true;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            response.isWorking = false;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }
                    } catch (Exception e) {
                        Utils.Log(TAG, "Cannot write to " + e);
                        Utils.onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.isWorking = false;
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }
                case FILES: {
                    Utils.Log(TAG, "Start RXJava Files Progressing");
                    try {
                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String originalPath = pathContent + currentTime;
                        ItemModel itemsFile = new ItemModel(mMimeTypeFile.extension,originalPath, "null", categories_id, categories_local_id, mMimeType, uuId, EnumFormatType.FILES, 0, true, false, null, null, EnumFileType.NONE, currentTime, mMimeTypeFile.name, "null", "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, false, false, false, 0, false, false, false, EnumStatus.UPLOAD);
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createFile(new File(originalPath), new File(mPath), Cipher.ENCRYPT_MODE);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.items = itemsFile;
                        response.categories = mMainCategories;
                        response.originalPath = mPath;
                        if (createdOriginal) {
                            response.isWorking = true;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            response.isWorking = false;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }
                    } catch (Exception e) {
                        Utils.Log(TAG, "Cannot write to " + e);
                        Utils.onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        final ResponseRXJava response = new ResponseRXJava();
                        response.isWorking = false;
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }
            }
            Utils.Log(TAG, "End up RXJava");
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final ResponseRXJava mResponse = (ResponseRXJava) response;
                    try {
                        if (mResponse.isWorking) {
                            final ItemModel items = mResponse.items;
                            long mb;
                            EnumFormatType enumFormatType = EnumFormatType.values()[items.formatType];
                            switch (enumFormatType) {
                                case AUDIO: {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        items.size = "" + mb;
                                        SQLHelper.insertedItem(items);
                                    }
                                    break;
                                }
                                case FILES: {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        items.size = "" + mb;
                                        SQLHelper.insertedItem(items);
                                    }
                                    break;
                                }
                                default: {
                                    if (storage.isFileExist(items.originalPath) && storage.isFileExist(items.thumbnailPath)) {
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        if (storage.isFileExist(items.thumbnailPath)) {
                                            mb += (long) +storage.getSize(new File(items.thumbnailPath), SizeUnit.B);
                                        }
                                        items.size = "" + mb;
                                        SQLHelper.insertedItem(items);
                                        if (!mResponse.categories.isCustom_Cover) {
                                            if (enumFormatType==EnumFormatType.IMAGE){
                                                final MainCategoryModel main = mResponse.categories;
                                                main.items_id = items.items_id;
                                                SQLHelper.updateCategory(main);
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            Utils.Log(TAG, "Write file successful ");
                        } else {
                            Utils.Log(TAG, "Write file Failed ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mResponse.isWorking) {
                            final ItemModel items = mResponse.items;
                            GalleryCameraMediaManager.getInstance().setProgressing(false);
                            EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM);
                            if (items.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView();
                            } else {
                                SingletonPrivateFragment.getInstance().onUpdateView();
                            }
                            Utils.Log(TAG, "Original path :" + mResponse.originalPath);
                            Storage storage = new Storage(SuperSafeApplication.getInstance());
                            if (!getRequestShareIntent()){
                                Utils.onDeleteFile(mResponse.originalPath);
                            }
                            if (Utils.deletedIndexOfHashMapImport(importFiles,mMapImporting)) {
                                final ImportFilesModel mImportItem = Utils.getArrayOfIndexHashMapImport(mMapImporting);
                                if (mImportItem != null) {
                                    onImportData(mImportItem);
                                    isImportData = true;
                                    Utils.Log(TAG,"Next import data completely");
                                } else {
                                    isImportData = false;
                                    listImport.clear();
                                    Utils.Log(TAG,"Imported data completely");
                                    Utils.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY);
                                    ServiceManager.getInstance().onPreparingSyncData();
                                }
                            }
                        }
                        else{
                            final ImportFilesModel mImportItem = Utils.getArrayOfIndexHashMapImport(mMapImporting);
                            if (mImportItem != null) {
                                onImportData(mImportItem);
                                isImportData = true;
                                Utils.Log(TAG,"Next import data completely");
                            } else {
                                isImportData = false;
                                listImport.clear();
                                Utils.Log(TAG,"Imported data completely");
                                Utils.onPushEventBus(EnumStatus.IMPORTED_COMPLETELY);
                                ServiceManager.getInstance().onPreparingSyncData();
                            }
                        }
                    }
                });
    }


    public void setIsWaitingSendMail(boolean isWaitingSendMail) {
        this.isWaitingSendMail =  isWaitingSendMail;
    }

    public void setProgress(String mProgress) {
        this.mProgress = mProgress;
    }

    public String getProgress() {
        return mProgress;
    }

    public void setmListExport(List<ExportFiles> mListExport) {
        if (!isExportData) {
            this.mListExport.clear();
            this.mListExport.addAll(mListExport);
        }
    }

    public void setExporting(boolean exporting) {
        isExportData = exporting;
    }

    public void setUpdate(boolean update) {
        isUpdateItemData = update;
    }

    public void onPickUpNewEmailNoTitle(Activity context, String account) {
        try {
            Account account1 = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountManager.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, null, null, null, null);
            intent.putExtra("overrideTheme", 1);
            //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpExistingEmail(Activity context, String account) {
        try {
            String value = String.format(SuperSafeApplication.getInstance().getString(R.string.choose_an_account), account);
            Account account1 = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountManager.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context) {
        try {
            if (Utils.getUserId()==null){
                return;
            }
            String value = String.format(SuperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Account account1 = new Account(Utils.getUserId(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent    intent = AccountManager.newChooseAccountIntent(account1, null,
                        new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, value, null, null, null);
                intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private void doBindService() {
        if (myService != null) {
            return;
        }
        Intent intent = null;
        intent = new Intent(mContext, SuperSafeService.class);
        intent.putExtra(TAG, "Message");
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Utils.Log(TAG, "onStartService");
    }

    public void onStartService() {
        if (myService == null) {
            doBindService();
            Utils.Log(TAG,"start services now");
        }
    }

    public void onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection);
            myService = null;
            Utils.Log(TAG,"stop services now");
        }
    }

    public void onSendEmail(){
        if (myService!=null){
            final User mUser = User.getInstance().getUserInfo();
            EmailToken emailToken  = EmailToken.getInstance().convertObject(mUser,EnumStatus.RESET);
            myService.onSendMail(emailToken);
        }
    }

    public SuperSafeService getMyService() {
        return myService;
    }

    private String getString(int res) {
        String value = SuperSafeApplication.getInstance().getString(res);
        return value;
    }

    /*User info*/
    public void onGetUserInfo() {
        Utils.Log(TAG,"onGetUserInfo");
        if (myService != null) {
            myService.onGetUserInfo();
        } else {
            Utils.Log(TAG, "My services is null");
            onStartService();
        }
    }

    /*Update user token*/
    public void onUpdatedUserToken() {
        Utils.onWriteLog("onUpdatedUserToken",EnumStatus.UPDATE_USER_TOKEN);
        if (myService != null) {
            myService.onUpdateUserToken();
        } else {
            ServiceManager.getInstance().onStartService();
            Utils.Log(TAG, "My services is null");
        }
    }

    /*Response Network*/
    public void onGetDriveAbout() {
        if (myService != null) {
            myService.getDriveAbout();
        }
    }

    /*Sync Author Device*/
    public void onSyncAuthorDevice() {
        if (myService != null) {
            myService.onSyncAuthorDevice();
        }
    }

    /*--------------Camera action-----------------*/
    public void onSaveDataOnCamera(final byte[] mData, final MainCategoryModel mainCategories) {
        subscriptions = Observable.create(subscriber -> {
            final MainCategoryModel mMainCategories = mainCategories;
            final String categories_id = mMainCategories.categories_id;
            final String categories_local_id = mMainCategories.categories_local_id;
            final boolean isFakePin = mMainCategories.isFakePin;
            final byte[] data = mData;
            try {
                String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                String currentTime = Utils.getCurrentDateTime();
                String uuId = Utils.getUUId();
                String pathContent = rootPath + uuId + "/";
                storage.createDirectory(pathContent);
                String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                String originalPath = pathContent + currentTime;
                final boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space), false);
                ItemModel items = new ItemModel(getString(R.string.key_jpg), originalPath, thumbnailPath, categories_id, categories_local_id, MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype(), uuId, EnumFormatType.IMAGE, 0, false, false, null, null, EnumFileType.NONE, currentTime, currentTime + getString(R.string.key_jpg),  "thumbnail_" + currentTime, "0", EnumStatusProgress.NONE, false, false, EnumDelete.NONE, isFakePin, isSaver, false, false, 0, false, false, false, EnumStatus.UPLOAD);
                storage.createFileByteDataNoEncrypt(SuperSafeApplication.getInstance(), data, new OnStorageListener() {
                    @Override
                    public void onSuccessful() {
                    }
                    @Override
                    public void onSuccessful(String path) {
                        try {
                            File file = new Compressor(SuperSafeApplication.getInstance())
                                    .setMaxWidth(1032)
                                    .setMaxHeight(774)
                                    .setQuality(85)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .compressToFile(new File(path));
                            boolean createdThumbnail = storage.createFile(new File(thumbnailPath), file, Cipher.ENCRYPT_MODE);
                            boolean createdOriginal = storage.createFile(originalPath, data,Cipher.ENCRYPT_MODE);
                            final ResponseRXJava response = new ResponseRXJava();
                            response.items = items;
                            response.categories = mMainCategories;
                            if (createdThumbnail && createdOriginal) {
                                response.isWorking = true;
                                subscriber.onNext(response);
                                subscriber.onComplete();
                                Utils.Log(TAG, "CreatedFile successful");
                            } else {
                                response.isWorking = false;
                                subscriber.onNext(response);
                                subscriber.onComplete();
                                Utils.Log(TAG, "CreatedFile failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            final ResponseRXJava response = new ResponseRXJava();
                            response.isWorking = false;
                            subscriber.onNext(response);
                            subscriber.onComplete();
                        }
                    }
                    @Override
                    public void onFailed() {
                        final ResponseRXJava response = new ResponseRXJava();
                        response.isWorking = false;
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    }
                    @Override
                    public void onSuccessful(int position) {
                    }
                });
            } catch (Exception e) {
                final ResponseRXJava response = new ResponseRXJava();
                response.isWorking = false;
                subscriber.onNext(response);
                subscriber.onComplete();
                Utils.onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                Utils.Log(TAG, "Cannot write to " + e);
            } finally {
                Utils.Log(TAG, "Finally");
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final ResponseRXJava mResponse = (ResponseRXJava) response;
                    try {
                        if (mResponse.isWorking) {
                            final ItemModel mItem = mResponse.items;
                            long mb;
                            if (storage.isFileExist(mItem.originalPath) && storage.isFileExist(mItem.thumbnailPath)) {
                                mb = (long) +storage.getSize(new File(mItem.originalPath), SizeUnit.B);
                                if (storage.isFileExist(mItem.thumbnailPath)) {
                                    mb += (long) +storage.getSize(new File(mItem.thumbnailPath), SizeUnit.B);
                                }
                                mItem.size = "" + mb;
                                SQLHelper.insertedItem(mItem);
                                if (!mResponse.categories.isCustom_Cover) {
                                    final MainCategoryModel main = mResponse.categories;
                                    main.items_id = mItem.items_id;
                                    SQLHelper.updateCategory(main);
                                    Utils.Log(TAG, "Special main categories " + new Gson().toJson(main));
                                }
                            }
                        }
                        Utils.Log(TAG, "Insert Successful");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mResponse.isWorking) {
                            final ItemModel mItem = mResponse.items;
                            GalleryCameraMediaManager.getInstance().setProgressing(false);
                            EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM);
                            if (mItem.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView();
                            } else {
                                SingletonPrivateFragment.getInstance().onUpdateView();
                                ServiceManager.getInstance().onPreparingSyncData();
                            }
                        }
                    }
                });
    }

    public void onExportingFiles() {
        Utils.Log(TAG, "Export amount files :" + mListExport.size());
        subscriptions = Observable.create(subscriber -> {
            setExporting(true);
            boolean isWorking = false;
            ExportFiles exportFiles = null;
            int position = 0;
            for (int i = 0; i < mListExport.size(); i++) {
                if (!mListExport.get(i).isExport) {
                    exportFiles = mListExport.get(i);
                    isWorking = true;
                    position = i;
                    break;
                }
            }
            if (isWorking) {
                final File mInput = exportFiles.input;
                final File mOutPut = exportFiles.output;
                try {
                    Storage storage = new Storage(SuperSafeApplication.getInstance());
                    storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                    final Cipher mCipher = storage.getCipher(Cipher.DECRYPT_MODE);
                    EnumFormatType formatType = EnumFormatType.values()[exportFiles.formatType];
                    if (formatType == EnumFormatType.VIDEO || formatType == EnumFormatType.AUDIO){
                        storage.createLargeFile(mOutPut, mInput, mCipher, position, new OnStorageListener() {
                            @Override
                            public void onSuccessful() {
                            }
                            @Override
                            public void onFailed() {
                                Utils.onWriteLog("Exporting failed", EnumStatus.EXPORT);
                                Utils.Log(TAG, "Exporting failed");
                            }
                            @Override
                            public void onSuccessful(String path) {
                            }
                            @Override
                            public void onSuccessful(int position) {
                                try {
                                    Utils.Log(TAG, "Exporting large file...............................Successful " +position);
                                    mListExport.get(position).isExport = true;
                                    onExportingFiles();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else{
                        storage.createFile(mOutPut, mInput, Cipher.DECRYPT_MODE, position, new OnStorageListener() {
                            @Override
                            public void onSuccessful() {
                            }
                            @Override
                            public void onFailed() {
                                Utils.onWriteLog("Exporting failed", EnumStatus.EXPORT);
                                Utils.Log(TAG, "Exporting failed");
                            }
                            @Override
                            public void onSuccessful(String path) {
                            }
                            @Override
                            public void onSuccessful(int position) {
                                try {
                                    Utils.Log(TAG, "Exporting file...............................Successful " +position);
                                    mListExport.get(position).isExport = true;
                                    onExportingFiles();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Utils.Log(TAG, "Cannot write to " + e);
                } finally {
                    Utils.Log(TAG, "Finally");
                }
            } else {
                Utils.Log(TAG, "Exporting file............................Done");
                EventBus.getDefault().post(EnumStatus.STOP_PROGRESS);
                setExporting(false);
                mListExport.clear();
                ServiceManager.getInstance().onPreparingSyncData();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {

                });
    }

    @Override
    public void onError(String message, EnumStatus status) {
        Utils.Log(TAG, "onError response :" + message + " - " + status.name());
        if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
            EventBus.getDefault().post(EnumStatus.REQUEST_ACCESS_TOKEN);
            Utils.Log(TAG, "Request token on global");
        }
    }
    @Override
    public Context getContext() {
        return mContext;
    }
    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case SCREEN_OFF: {
                int value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action) {
                    case NONE: {
                        String key  = SuperSafeApplication.getInstance().readKey();
                        if (!"".equals(key)){
                            Utils.onHomePressed();
                        }
                        break;
                    }
                    default: {
                        Utils.Log(TAG, "Nothing to do ???");
                        break;
                    }
                }
                break;
            }
            case GET_DRIVE_ABOUT: {
                break;
            }
            case CONNECTED: {
                EventBus.getDefault().post(EnumStatus.CONNECTED);
                ServiceManager.getInstance().onGetUserInfo();
                PremiumManager.getInstance().onStartInAppPurchase();
                break;
            }
            case DISCONNECTED:{
                onDefaultValue();
                Utils.Log(TAG,"Disconnect");
                break;
            }
            case USER_INFO: {
                Utils.Log(TAG, "Get info successful");
                ServiceManager.getInstance().onPreparingSyncData();
                final User mUser = User.getInstance().getUserInfo();
                if (mUser.isWaitingSendMail){
                    ServiceManager.getInstance().onSendEmail();
                }
                break;
            }
            case UPDATE_USER_TOKEN:{
                ServiceManager.getInstance().onPreparingSyncData();
                break;
            }
        }
    }

    public void onPreparingEnableDownloadData(List<ItemModel>globalList){
        if (isDownloadToExportFiles){
            return;
        }
        List<ItemModel> mergeList = Utils.getMergedOriginalThumbnailList(false,globalList);
        Utils.Log(TAG,"onPreparingEnableDownloadData ==> clear duplicated data "+ new Gson().toJson(mergeList));
        if (mergeList!=null){
            if (mergeList.size()>0){
                mMapDownloadToExportFiles.clear();
                mMapDownloadToExportFiles = Utils.mergeListToHashMap(mergeList);
                Utils.Log(TAG,"onPreparingEnableDownloadData ==> clear merged data "+ new Gson().toJson(mMapDownloadToExportFiles));
                Utils.Log(TAG,"onPreparingEnableDownloadData ==> merged data "+ new Gson().toJson(mergeList));
                final ItemModel itemModel = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles);
                if (itemModel!=null){
                    Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.PROGRESS,"Total downloading "+mMapDownloadToExportFiles.size());
                    Utils.Log(TAG,"onPreparingEnableDownloadData to download "+ new Gson().toJson(itemModel));
                    Utils.Log(TAG,"onPreparingEnableDownloadData to download total  "+ mMapDownloadToExportFiles.size());
                    onDownLoadDataToExportFiles(itemModel);
                }
            }
        }
    }

    /*Download file from Google drive*/
    private void onDownLoadDataToExportFiles(final ItemModel itemModel){
        if (myService!=null){
            isDownloadToExportFiles = true;
            mStart = 20;
            myService.onDownloadFile(itemModel,true, new DownloadServiceListener() {
                @Override
                public void onProgressDownload(int percentage) {
                    isDownloadToExportFiles = true;
                    if (mStart == percentage){
                        Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.PROGRESS,"Progressing "+ mStart);
                        mStart += 20;
                    }
                }
                @Override
                public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                    Utils.Log(TAG,"onDownLoadCompleted ==> onDownLoadCompleted:" + file_name.getAbsolutePath());
                    if (Utils.deletedIndexOfHashMap(itemModel,mMapDownloadToExportFiles)){
                        /*Delete local db and folder name*/
                        final ItemModel mDownloadItem = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles);
                        if (mDownloadItem!=null){
                            onDownLoadDataToExportFiles(mDownloadItem);
                            isDownloadToExportFiles = true;
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.Log(TAG,"Next download item..............." + new Gson().toJson(mDownloadItem));
                        }else{
                            Utils.Log(TAG,"Download completely...............");
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                            Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DOWNLOAD_COMPLETED,"Total downloading "+mMapDownloadToExportFiles.size());
                            isDownloadToExportFiles = false;
                            Utils.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED);
                        }
                    }
                }
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG,"onDownLoadData ==> onError:" + message);
                    Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.ERROR,"onDownLoadData ==> onError "+message);
                    isDownloadToExportFiles = false;
                    if (status == EnumStatus.NO_SPACE_LEFT){
                        Utils.onPushEventBus(EnumStatus.NO_SPACE_LEFT);
                        Utils.onDeleteItemFolder(itemModel.items_id);
                        onPreparingDeleteData();
                    }
                    if (status == EnumStatus.REQUEST_NEXT_DOWNLOAD){
                        if (Utils.deletedIndexOfHashMap(itemModel,mMapDownloadToExportFiles)){
                            /*Delete local db and folder name*/
                            final ItemModel mDownloadItem = Utils.getArrayOfIndexHashMap(mMapDownloadToExportFiles);
                            if (mDownloadItem!=null){
                                onDownLoadDataToExportFiles(mDownloadItem);
                                isDownloadToExportFiles = true;
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.Log(TAG,"Next download item..............." + new Gson().toJson(mDownloadItem));
                            }else{
                                Utils.Log(TAG,"Download completely...............");
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DONE,new Gson().toJson(itemModel));
                                Utils.onWriteLog(EnumStatus.DOWNLOAD,EnumStatus.DOWNLOAD_COMPLETED,"Total downloading "+mMapDownloadToExportFiles.size());
                                isDownloadToExportFiles = false;
                                /*Download completed for export files*/
                                Utils.onPushEventBus(EnumStatus.DOWNLOAD_COMPLETED);
                            }
                        }
                    }
                }
            });
        }
    }



    public void onDefaultValue(){
        isDownloadData = false;
        isUploadData = false;
        isExportData  = false;
        isImportData = false;
        isUpdateItemData = false;
        isUpdateCategoryData = false;
        isDeleteItemData = false;
        isDeleteCategoryData = false;
        isDownloadToExportFiles = false;
        isHandleLogic = false;
        isSyncCategory = false;
        isGetItemList = false;
        isWaitingSendMail = false;
    }

    public void onDismissServices() {
        if (isDownloadData || isUploadData || isDownloadToExportFiles || isExportData || isImportData || isDeleteItemData || isDeleteCategoryData  ||  isWaitingSendMail || isUpdateItemData || isHandleLogic || isSyncCategory || isGetItemList || isUpdateCategoryData) {
            Utils.Log(TAG, "Progress....................!!!!:");
        }
        else {
            onDefaultValue();
            if (myService != null) {
                myService.unbindView();
            }
            if (subscriptions != null) {
                subscriptions.dispose();
            }
            onStopService();
            Utils.Log(TAG, "Dismiss Service manager");
        }
    }

    public boolean getRequestShareIntent() {
        return isRequestShareIntent;
    }

    public void setRequestShareIntent(boolean requestShareIntent) {
        isRequestShareIntent = requestShareIntent;
    }

    public interface ServiceManagerSyncDataListener {
        void onCompleted();
        void onError();
        void onCancel();
    }

    public interface ServiceManagerGalleySyncDataListener {
        void onCompleted(ImportFilesModel importFiles);
        void onFailed(ImportFilesModel importFiles);
    }
    /*Upload Service*/
    public interface UploadServiceListener {
        void onProgressUpdate(int percentage);
        void onFinish();
        void onResponseData(final DriveResponse response);
        void onError(String message, EnumStatus status);
    }
    public interface DownloadServiceListener {
        void onProgressDownload(int percentage);
        void onDownLoadCompleted(File file_name, DownloadFileRequest request);
        void onError(String message, EnumStatus status);
    }

    public interface BaseListener <T> {
        void onShowListObjects(List<T>list);
        void onShowObjects(T object);
        void onError(String message, EnumStatus status);
        void onSuccessful(String message,EnumStatus status);
    }

    public interface ServiceManagerInsertItem {
        void onCancel();
        void onError(String message, EnumStatus status);
        void onSuccessful(String message,EnumStatus status);
    }
}
