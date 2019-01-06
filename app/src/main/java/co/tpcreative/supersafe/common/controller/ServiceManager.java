package co.tpcreative.supersafe.common.controller;
import android.accounts.Account;
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
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;
import com.snatik.storage.helpers.SizeUnit;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeService;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveEvent;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.ExportFiles;
import co.tpcreative.supersafe.model.ImportFiles;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.ResponseRXJava;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.fakepin.FakePinComponentActivity;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceManager implements BaseView {

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private SuperSafeService myService;
    private Context mContext;
    private boolean isUploadData;
    private Disposable subscriptions;
    private Storage storage = new Storage(SuperSafeApplication.getInstance());
    private Storage mStorage = new Storage(SuperSafeApplication.getInstance());
    private List<ImportFiles> mListImport = new ArrayList<>();
    private List<ExportFiles> mListExport = new ArrayList<>();
    private List<Items> mListDownLoadFiles = new ArrayList<>();

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "connected");
            myService = ((SuperSafeService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            ServiceManager.getInstance().onGetUserInfo();
            ServiceManager.getInstance().onSyncCheckVersion();
            ServiceManager.getInstance().onSyncAuthorDevice();
            ServiceManager.getInstance().onGetDriveAbout();
            ServiceManager.getInstance().onCheckingMissData();
        }

        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

    public void onInitConfigurationFile(){
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
    }

    private Cipher mCiphers;
    private boolean isDownloadData;
    private boolean isLoadingData;
    private boolean isDeleteSyncCLoud;
    private boolean isDeleteOwnCloud;
    private boolean isGetListCategories;
    private boolean isCategoriesSync;
    private boolean isDeleteAlbum;
    private boolean isImporting;
    private boolean isExporting;
    private boolean isDownloadingFiles;
    private boolean isWaitingSendMail;
    private boolean isUpdate;
    private int countSyncData = 0;
    private int totalList = 0;

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public boolean isImporting() {
        return isImporting;
    }

    public void setImporting(boolean importing) {
        isImporting = importing;
    }

    public boolean isWaitingSendMail() {
        return isWaitingSendMail;
    }

    public void setIsWaitingSendMail(boolean isWaitingSendMail) {
        this.isWaitingSendMail =  isWaitingSendMail;
    }

    public void setmListImport(List<ImportFiles> mListImport) {
        if (!isImporting) {
            this.mListImport.clear();
            this.mListImport.addAll(mListImport);
        }
    }

    public void setListDownloadFile(List<Items> downloadFile) {
        Utils.Log(TAG, "Download file " + isDownloadingFiles);
        if (!isDownloadingFiles) {
            this.mListDownLoadFiles.clear();
            this.mListDownLoadFiles.addAll(downloadFile);
        }
    }

    public void setDownloadingFiles(boolean downloadingFiles) {
        isDownloadingFiles = downloadingFiles;
    }

    public void setmListExport(List<ExportFiles> mListExport) {
        if (!isExporting) {
            this.mListExport.clear();
            this.mListExport.addAll(mListExport);
        }
    }

    public void setExporting(boolean exporting) {
        isExporting = exporting;
    }

    public boolean isDeleteAlbum() {
        return isDeleteAlbum;
    }

    public void setDeleteAlbum(boolean deleteAlbum) {
        isDeleteAlbum = deleteAlbum;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public void setCategoriesSync(boolean categoriesSync) {
        isCategoriesSync = categoriesSync;
    }

    public void setGetListCategories(boolean getListCategories) {
        isGetListCategories = getListCategories;
    }

    public void setDownloadData(boolean downloadData) {
        isDownloadData = downloadData;
    }

    public void setDeleteSyncCLoud(boolean deleteSyncCLoud) {
        isDeleteSyncCLoud = deleteSyncCLoud;
    }


    public void setDeleteOwnCloud(boolean deleteOwnCloud) {
        isDeleteOwnCloud = deleteOwnCloud;
    }

    public void setUploadData(boolean uploadData) {
        isUploadData = uploadData;
    }

    public void onPickUpNewEmailNoTitle(Activity context, String account) {
        try {
            Account account1 = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
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
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context) {
        try {
            String value = String.format(SuperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, value, null, null, null);
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
        Log.d(TAG, "onStartService");
    }

    public void onStartService() {
        if (myService == null) {
            doBindService();
        }
    }

    public void onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection);
            myService = null;
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
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    /*Sync Author Device*/

    public void onSyncAuthorDevice() {
        if (myService != null) {
            myService.onSyncAuthorDevice();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    /*Check Version App*/
    public void onSyncCheckVersion() {
        if (myService != null) {
            myService.onCheckVersion();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    public void onGetListCategoriesSync() {
        if (myService == null) {
            Utils.Log(TAG, "My services on categories sync is null");
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }
        if (isCategoriesSync) {
            Utils.Log(TAG, "List categories is sync...!!!--------------*******************************-----------");
            return;
        }
        if (isGetListCategories) {
            Utils.Log(TAG, "Setting list categories is sync...--------------*******************************-----------");
            return;
        }
        isGetListCategories = true;
        myService.onGetListCategoriesSync(new ServiceManagerShortListener() {
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + "--" + status.name());
                isGetListCategories = false;
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG, message + "--" + status.name());
                SingletonPrivateFragment.getInstance().onUpdateView();
                isGetListCategories = false;
                getObservable();
            }
        });
    }

    private Observable<MainCategories> getObservableItems(List<MainCategories> categories) {
        return Observable.create(subscriber -> {
            for (MainCategories index : categories) {
                myService.onCategoriesSync(index, new ServiceManagerShortListener() {
                    @Override
                    public void onError(String message, EnumStatus status) {
                        Utils.Log(TAG, message + "--" + status.name());
                        subscriber.onNext(index);
                        subscriber.onComplete();
                    }
                    @Override
                    public void onSuccessful(String message, EnumStatus status) {
                        Utils.Log(TAG, message + "--" + status.name());
                        subscriber.onNext(index);
                        subscriber.onComplete();
                    }
                });
            }
        });
    }

    public void getObservable() {
        if (myService == null) {
            Utils.Log(TAG, "My services on categories sync is null");
            return;
        }
        final List<MainCategories> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).loadListItemCategoriesSync(false, false);
        if (mList == null) {
            Utils.Log(TAG, "Categories already sync");
            return;
        }
        if (isCategoriesSync) {
            Utils.Log(TAG, "List categories is sync...--------------*******************************-----------");
            return;
        }
        isCategoriesSync = true;
        if (mList.size() == 0) {
            Utils.Log(TAG, "Categories already sync");
            isCategoriesSync = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            SingletonPrivateFragment.getInstance().onUpdateView();
            return;
        }
        getObservableItems(mList).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<MainCategories>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onComplete() {
                        Utils.Log(TAG, "complete sync categories");
                        isCategoriesSync = false;
                        getObservable();
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onNext(MainCategories pojoObject) {
                        // Show Progress
                        Utils.Log(TAG, "next");
                    }
                });
    }


    public void onCheckingMissData() {
        Utils.Log(TAG, "Preparing checking miss data ###########################");
        if (myService == null) {
            return;
        }
        final List<Items> checkNull = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItemsByNull(false);
        if (checkNull != null && checkNull.size() > 0) {
            for (int i = 0; i < checkNull.size(); i++) {
                if (checkNull.get(i).categories_id == null || checkNull.get(i).categories_id.equals("null")) {
                    final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(checkNull.get(i).categories_local_id, false);
                    if (main != null) {
                        checkNull.get(i).categories_id = main.categories_id;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(checkNull.get(i));
                        Utils.Log(TAG, "Update categories id...................^^^???");
                    }
                }
            }
        }

        List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true, false, false);
        if (mList == null) {
            Utils.Log(TAG, "Not Found Miss Data");
            return;
        }
        if (mList.size() == 0) {
            Utils.Log(TAG, "----------------Not Found Miss Data---------------");
            return;
        }

        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    Utils.Log(TAG, ".................Working on onCheckingMissData..............");
                    onAddItems(i);
                })
                .doOnComplete(() -> {
                })
                .subscribe();
    }


    public void onSyncDataOwnServer(String nextPage) {
        Utils.Log(TAG, "Preparing sync data ###########################");
        if (isCategoriesSync) {
            Utils.Log(TAG, "List categories is sync...--------------*******************************-----------");
            return;
        }
        if (isDownloadData) {
            SingletonManagerTab.getInstance().onAction(EnumStatus.DOWNLOAD);
            Utils.Log(TAG, "List items is downloading...--------------*******************************-----------");
            return;
        }
        if (isUploadData) {
            SingletonManagerTab.getInstance().onAction(EnumStatus.UPLOAD);
            Utils.Log(TAG, "List items is uploading...----------------*******************************-----------");
            return;
        }
        if (isLoadingData) {
            Utils.Log(TAG, "List items is loading...----------------*******************************-----------");
            return;
        }

        if (isDeleteOwnCloud) {
            Utils.Log(TAG, "List sync own items is deleting...----------------*******************************-----------");
            return;
        }

        if (isDeleteSyncCLoud) {
            Utils.Log(TAG, "List sync cloud items is deleting...----------------*******************************-----------");
            return;
        }

        if (isDeleteAlbum) {
            Utils.Log(TAG, "List categories is deleting...----------------*******************************-----------");
            return;
        }

        final List<Items> mUpdateListItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLoadListItemUpdate(true,true, true,false);
        if (isUpdate) {
            Utils.Log(TAG, "List categories is updating...----------------*******************************-----------");
            return;
        }
        else{
            if (mUpdateListItem!=null && mUpdateListItem.size()>0){
                onUpdateOnOwnItems();
            }
        }

        if (isGetListCategories) {
            Utils.Log(TAG, "Getting list categories...----------------*******************************-----------");
            return;
        }

        final boolean isPauseCloudSync = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync), false);
        if (isPauseCloudSync) {
            Utils.Log(TAG, "Pause Cloud Sync is Enabled...----------------*******************************-----------");
            return;
        }

        final User mUser = User.getInstance().getUserInfo();
        if (mUser == null) {
            return;
        }

        if (mUser.premium == null) {
            Utils.Log(TAG, "Premium is null..----------------*******************************-----------");
            return;
        }

        if (myService != null) {
            isLoadingData = true;
            myService.onGetListSync(nextPage, new ServiceManagerShortListener() {
                @Override
                public void onError(String message, EnumStatus status) {
                    if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
                        SingletonManagerTab.getInstance().onRequestAccessToken();
                        Utils.Log(TAG, "Request token on onSyncDataOwnServer");
                    }
                    Utils.Log(TAG, "Error :" + message);
                    isLoadingData = false;
                }
                @Override
                public void onSuccessful(String nextPage, EnumStatus status) {
                    if (status == EnumStatus.LOAD_MORE) {
                        isLoadingData = false;
                        Utils.Log(TAG, "next page on onSyncDataOwnServer " + nextPage);
                        onSyncDataOwnServer(nextPage);
                    }
                    else if (status == EnumStatus.RELOAD){
                        isLoadingData = false;
                        onSyncDataOwnServer("0");
                    }
                    else if (status == EnumStatus.SYNC_READY) {
                        isLoadingData = false;
                        final List<Items> itemsDownload = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems(false);

                        final List<Items> mListOwnCloud = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true, EnumDelete.DELETE_WAITING.ordinal(), false);

                        final List<Items> mListCloud = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true, false);

                        final List<Items> mPreviousList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true, false);

                        final List<MainCategories> mainCategories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).loadListItemCategoriesSync(false, false);

                        final List<MainCategories> mPreviousMainCategories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).loadListItemCategoriesSync(true, false);

                        final List<MainCategories> deleteAlbum = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(true, false);

                        boolean isDeleteAlbum = true;
                        if (deleteAlbum != null && deleteAlbum.size() > 0) {
                            Utils.Log(TAG, "new main categories " + new Gson().toJson(deleteAlbum));
                            for (MainCategories index : deleteAlbum) {
                                final List<Items> mItems = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(index.categories_local_id, false);
                                if (mItems != null && mItems.size() > 0) {
                                    isDeleteAlbum = false;
                                }
                            }
                        } else {
                            Utils.Log(TAG, "new main categories  not found");
                            isDeleteAlbum = false;
                        }

                        boolean isPreviousDelete = false;
                        if (mPreviousList != null && mPreviousList.size() > 0) {
                            if (myService.getHashMapGlobal() != null) {
                                for (Items index : mPreviousList) {
                                    String value = myService.getHashMapGlobal().get(index.items_id);
                                    if (value == null) {
                                        isPreviousDelete = true;
                                    }
                                }
                            }
                        }

                        boolean isPreviousAlbumDelete = false;
                        if (mPreviousMainCategories != null && mPreviousMainCategories.size() > 0) {
                            if (myService.getHashMapGlobalCategories() != null && myService.getHashMapGlobalCategories().size() > 0) {
                                for (MainCategories index : mPreviousMainCategories) {
                                    String value = myService.getHashMapGlobalCategories().get(index.categories_id);
                                    if (value == null) {
                                        isPreviousAlbumDelete = true;
                                        Utils.Log(TAG, "Delete previous album......");
                                    }
                                }
                            }
                        }


                        if (mainCategories != null && mainCategories.size() > 0) {
                            getObservable();
                            Utils.Log(TAG, "Preparing categories sync on own cloud...");
                        } else if (mListOwnCloud != null && mListOwnCloud.size() > 0) {
                            Utils.Log(TAG, "Preparing deleting on own cloud...");
                            onDeleteOnOwnItems();
                        } else if (mListCloud != null && mListCloud.size() > 0) {
                            Utils.Log(TAG, "Preparing deleting on cloud...");
                            onDeleteCloud();
                        } else if (isDeleteAlbum) {
                            Utils.Log(TAG, "Preparing deleting on global album...");
                            onDeleteAlbum();
                        } else if (isPreviousDelete) {
                            Utils.Log(TAG, "Preparing deleting on previous...");
                            myService.onDeletePreviousSync(new DeleteServiceListener() {
                                @Override
                                public void onDone() {
                                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                                }
                            });
                        } else if (isPreviousAlbumDelete) {
                            Utils.Log(TAG, "Preparing deleting on previous album on local...");
                            myService.onDeletePreviousCategoriesSync(new DeleteServiceListener() {
                                @Override
                                public void onDone() {
                                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                                }
                            });
                        }
                        else if (itemsDownload != null) {
                            if (itemsDownload.size() > 0) {
                                Utils.Log(TAG, "Preparing downloading..."+new Gson().toJson(itemsDownload));
                                onDownloadFilesFromDriveStore();
                            } else {
                                Utils.Log(TAG, "Preparing uploading...");
                                if (User.getInstance().isCheckAllowUpload()) {
                                    onUploadDataToStore();
                                } else {
                                    Utils.Log(TAG, "Limit uploaded now..----------------*******************************-----------");
                                }
                            }
                        } else {
                            Utils.Log(TAG, "Preparing uploading...");
                            if (User.getInstance().isCheckAllowUpload()) {
                                onUploadDataToStore();
                            } else {
                                Utils.Log(TAG, "Limit uploaded now..----------------*******************************-----------");
                            }
                        }
                    }
                }
            });
        } else {
            Utils.Log(TAG, "My service is null");
        }
    }


    public void onUpdateOnOwnItems() {
        if (myService == null) {
            Utils.Log(TAG, "Service is null on " + EnumStatus.UPDATE);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }

        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLoadListItemUpdate(true,true, true,false);

        if (mList == null) {
            Utils.Log(TAG, "No Found data to delete on own items!!!");
            return;
        }

        countSyncData = 0;
        isUpdate = true;
        totalList = mList.size();
        if (mList.size() == 0) {
            Utils.Log(TAG, "Not Found own data id to update");
            isUpdate = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            return;
        }
        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    Utils.Log(TAG, "Starting Updating items on own cloud.......");
                    final Items mItem = i;
                    isUpdate = true;
                    myService.onUpdateItems(mItem, new ServiceManagerShortListener() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, "Result updated "+ message + "--" + status.name());
                            onUpdateSyncDataStatus(EnumStatus.UPDATE);
                        }
                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG,"Result updated "+ message + " -- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.UPDATE);
                        }
                    });
                })
                .doOnComplete(() -> {
                })
                .subscribe();
    }


    public void onDeleteOnOwnItems() {
        if (myService == null) {
            Utils.Log(TAG, "Service is null on " + EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }

        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }

        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true, EnumDelete.DELETE_WAITING.ordinal(), false);

        if (mList == null) {
            Utils.Log(TAG, "No Found data to delete on own items!!!");
            return;
        }
        countSyncData = 0;
        isDeleteOwnCloud = true;
        totalList = mList.size();
        if (mList.size() == 0) {
            Utils.Log(TAG, "Not Found own data id to delete");
            isDeleteOwnCloud = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            return;
        }
        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    Utils.Log(TAG, "Starting deleting items on own cloud.......");
                    final Items mItem = i;
                    isDeleteOwnCloud = true;
                    myService.onDeleteOwnSystem(mItem, new ServiceManagerShortListener() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "--" + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);
                        }
                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + " -- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);

                        }
                    });
                })
                .doOnComplete(() -> {
                })
                .subscribe();
    }

    /*Delete album*/

    public void onDeleteAlbum() {
        if (myService == null) {
            Utils.Log(TAG, "Service is null on " + EnumStatus.DELETE_CATEGORIES);
            return;
        }

        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }

        final List<MainCategories> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(true, false);

        if (mList == null) {
            Utils.Log(TAG, "No Found data to delete on own items!!!");
            return;
        }

        countSyncData = 0;
        isDeleteAlbum = true;
        totalList = mList.size();
        if (mList.size() == 0) {
            Utils.Log(TAG, "Not Found own data id to delete");
            isDeleteAlbum = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            return;
        }

        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    Utils.Log(TAG, "Starting deleting items on own cloud.......");
                    final MainCategories main = i;
                    isDeleteAlbum = true;
                    myService.onDeleteCategoriesSync(main, new ServiceManagerShortListener() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "--" + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_CATEGORIES);
                        }
                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + " -- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_CATEGORIES);
                        }
                    });
                })
                .doOnComplete(() -> {
                })
                .subscribe();
    }


    public void onDeleteCloud() {
        if (myService == null) {
            Utils.Log(TAG, "Service is null on " + EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }
        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true, false);

        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true, false);

        if (list == null) {
            Utils.Log(TAG, "No Found data to delete on cloud items!!!");
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }
        final List<Items> mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;
        for (Items index : list) {
            if (index.global_original_id != null) {
                final Items item = index;
                item.isOriginalGlobalId = true;
                mList.add(item);
            }
        }
        for (Items index : lists) {
            EnumFormatType formatTypeFile = EnumFormatType.values()[index.formatType];
            if (index.global_thumbnail_id != null && (formatTypeFile != EnumFormatType.AUDIO && formatTypeFile != EnumFormatType.FILES)) {
                final Items item = index;
                item.isOriginalGlobalId = false;
                mList.add(item);
            }
        }
        totalList = mList.size();
        isDeleteSyncCLoud = true;
        if (mList.size() == 0) {
            //onDeleteOnLocal();
            isDeleteSyncCLoud = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            Utils.Log(TAG, "Not Found cloud id to delete");
            return;
        }
        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    isDeleteSyncCLoud = true;
                    final Items mItem = i;
                    Utils.Log(TAG, "Starting deleting items on cloud.......");
                    myService.onDeleteCloudItems(mItem, mItem.isOriginalGlobalId, new ServiceManagerShortListener() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_CLOUD_DATA);
                        }
                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_CLOUD_DATA);
                        }
                    });

                })
                .doOnComplete(() -> {
                })
                .subscribe();

    }

    public void onDownloadFilesFromDriveStore() {
        if (isDownloadData) {
            Utils.Log(TAG, "Downloading sync item from cloud !!!");
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }
        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems(false);
        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems(false);
        if (list == null) {
            Utils.Log(TAG, "No Found data from Cloud!!!");
            return;
        }
        final List<Items> mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;

        for (int i = 0; i < list.size(); i++) {
            final Items items = list.get(i);
            if (!items.originalSync) {
                final Items item = items;
                item.isOriginalGlobalId = true;
                mList.add(item);
                Utils.Log(TAG, "Adding original :");
            }
        }

        for (int i = 0; i < lists.size(); i++) {
            final Items items = lists.get(i);
            if (!items.thumbnailSync) {
                final Items item = items;
                item.isOriginalGlobalId = false;
                mList.add(item);
                Utils.Log(TAG, "Adding thumbnail :");
            }
        }

        totalList = mList.size();
        isDownloadData = true;
        if (mList.size() == 0) {
            Utils.Log(TAG, "Data items already downloaded from Cloud !!!!");
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
            isDownloadData = false;
            return;
        } else {
            SingletonManagerTab.getInstance().onAction(EnumStatus.DOWNLOAD);
            String message = "Preparing download " + totalList + " items from Cloud";
            Utils.Log(TAG, message);
            Utils.onWriteLog(message, EnumStatus.DOWNLOAD);
        }

        if (myService != null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser != null) {
                if (mUser.driveConnected) {
                    for (Items index : mList) {
                        isDownloadData = true;
                        /*Do something here*/
                        final Items itemObject = index;
                        boolean isWorking = true;

                        if (itemObject.categories_local_id == null) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                            Utils.onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
                            Utils.Log(TAG, "categories_id is null at " + itemObject.id);
                            isWorking = false;
                        } else {
                            isWorking = false;
                            final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(itemObject.categories_local_id, false);
                            if (main != null) {
                                if (main.categories_id != null) {
                                    isWorking = true;
                                } else {
                                    isWorking = false;
                                }
                            }
                        }

                        EnumFormatType formatTypeFile = EnumFormatType.values()[itemObject.formatType];
                        if (itemObject.global_original_id == null) {
                            Utils.Log(TAG, "global_original_id is null at " + " format type :" + formatTypeFile.name() + "---name global :" + itemObject.items_id);
                            Utils.onWriteLog("global_original_id is null ", EnumStatus.DOWNLOAD);
                            Utils.onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
                            //Utils.Log(TAG,"Drive description on original " + new Gson().toJson(new DriveDescription().hexToObject(itemObject.description)));
                            isWorking = false;
                        }
                        if (itemObject.global_thumbnail_id == null & (formatTypeFile != EnumFormatType.AUDIO && formatTypeFile != EnumFormatType.FILES)) {
                            Utils.Log(TAG, "global_thumbnail_id is null at " + itemObject.id + " format type :" + formatTypeFile.name() + "---name global :" + itemObject.items_id);
                            Utils.onWriteLog("global_thumbnail_id is null ", EnumStatus.DOWNLOAD);
                            Utils.onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
                            isWorking = false;
                        }
                        if (myService == null) {
                            isWorking = false;
                        }
                        if (isWorking) {
                            itemObject.statusProgress = EnumStatusProgress.PROGRESSING.ordinal();
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(itemObject);
                            if (itemObject.isOriginalGlobalId) {
                                Utils.Log(TAG, "Preparing downloading original file");
                            } else {
                                Utils.Log(TAG, "Preparing downloading thumbnail file");
                            }
                            myService.onDownloadFile(itemObject, null, new ServiceManager.DownloadServiceListener() {
                                @Override
                                public void onError(String message, EnumStatus status) {
                                    Utils.onWriteLog(message, EnumStatus.DOWNLOAD);
                                    onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
                                    Utils.Log(TAG, "onError Download: !!! on - " + message);
                                }
                                @Override
                                public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                    try {
                                        if (request != null) {
                                            final Items itemsRequest = request.items;
                                            if (itemsRequest != null) {
                                                final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(request.items.items_id, false);
                                                if (mItem != null) {
                                                    mItem.statusAction = EnumStatus.DOWNLOAD.ordinal();
                                                    if (itemsRequest.isOriginalGlobalId) {
                                                        mItem.originalSync = true;
                                                        Log.d(TAG, "Downloaded id original.........................................: global id  " + mItem.global_original_id + " - local id " + mItem.id);
                                                    } else {
                                                        Log.d(TAG, "Downloaded id thumbnail.........................................: global id  " + mItem.global_thumbnail_id + " - local id " + mItem.id);
                                                        mItem.thumbnailSync = true;
                                                    }

                                                    if (mItem.thumbnailSync && mItem.originalSync) {
                                                        mItem.isSyncCloud = true;
                                                        mItem.isSyncOwnServer = true;
                                                        mItem.statusProgress = EnumStatusProgress.DONE.ordinal();
                                                    } else {
                                                        mItem.isSyncCloud = false;
                                                    }

                                                    /*Custom cover*/
                                                    final MainCategories categories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(mItem.categories_id, false);
                                                    if (categories != null) {
                                                        if (!categories.isCustom_Cover) {
                                                            EnumFormatType formatType = EnumFormatType.values()[mItem.formatType];
                                                            switch (formatType){
                                                                case IMAGE:{
                                                                    categories.items_id = mItem.items_id;
                                                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(categories);
                                                                    break;
                                                                }
                                                            }
                                                            Utils.Log(TAG, "Update main categories for custom cover");
                                                        }
                                                    } else {
                                                        Utils.Log(TAG, "Can not find main categories for custom cover: " + mItem.categories_id);
                                                    }
                                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                } else {
                                                    Utils.Log(TAG, "Failed Save 3");
                                                }
                                            }
                                        } else {
                                            Utils.Log(TAG, "Failed Save 1");
                                        }
                                    } catch (Exception e) {
                                        Utils.onWriteLog(e.getMessage(), EnumStatus.DOWNLOAD);
                                        e.printStackTrace();
                                    }
                                    onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
                                }

                                @Override
                                public void onProgressDownload(int percentage) {
                                    isDownloadData = true;
                                }

                                @Override
                                public void onSaved() {
                                    Utils.Log(TAG, "onSaved");
                                }
                            });
                        }
                    }
                } else {
                    isDownloadData = false;
                    Utils.Log(TAG, "Drive api not ready");
                    Utils.onWriteLog("Drive api not ready", EnumStatus.DOWNLOAD);
                    SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
                }
            } else {
                isDownloadData = false;
                Utils.Log(TAG, "User not ready");
                Utils.onWriteLog("User not ready", EnumStatus.DOWNLOAD);
                SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
            }
        } else {
            isDownloadData = false;
            Utils.Log(TAG, "My services is null");
            Utils.onWriteLog("My services is null", EnumStatus.DOWNLOAD);
            SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
        }
    }

    public void onUploadDataToStore() {

        if (isUploadData) {
            Utils.Log(TAG, "Uploading data item to cloud !!!");
            return;
        }

        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "Check network connection");
            return;
        }

        final List<Items> checkNull = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItemsByNull(false);
        if (checkNull != null && checkNull.size() > 0) {
            for (int i = 0; i < checkNull.size(); i++) {
                if (checkNull.get(i).categories_id == null || checkNull.get(i).categories_id.equals("null")) {
                    final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(checkNull.get(i).categories_local_id, false);
                    if (main != null) {
                        checkNull.get(i).categories_id = main.categories_id;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(checkNull.get(i));

                        Utils.Log(TAG, "Update categories id...................^^^???");
                    }
                }
            }
        }

        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItems(false);
        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItems(false);

        if (list == null) {
            Utils.Log(TAG, "No Found data from device !!!");
            return;
        }

        final List<Items> mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;
        for (int i = 0; i < list.size(); i++) {
            final Items items = list.get(i);
            if (!items.originalSync) {
                final Items item = items;
                item.isOriginalGlobalId = true;
                mList.add(item);
            }
        }

        for (int i = 0; i < lists.size(); i++) {
            final Items items = lists.get(i);
            if (!items.thumbnailSync) {
                final Items item = items;
                item.isOriginalGlobalId = false;
                mList.add(item);
            }
        }

        totalList = mList.size();
        isUploadData = true;
        if (mList.size() == 0) {
            SingletonPrivateFragment.getInstance().onUpdateView();
            Utils.Log(TAG, "Data items already uploaded to Cloud !!!");
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
            isUploadData = false;
            return;
        } else {
            SingletonManagerTab.getInstance().onAction(EnumStatus.UPLOAD);
            String message = "Preparing upload " + totalList + " items to Cloud";
            Utils.Log(TAG, message);
            Utils.onWriteLog(message, EnumStatus.UPLOAD);
        }

        if (myService != null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser != null) {
                if (mUser.driveConnected) {
                    for (Items index : mList) {
                        /*Do something here*/
                        isUploadData = true;
                        final Items itemObject = index;
                        boolean isWorking = true;

                        if (itemObject.categories_local_id == null) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                            isWorking = false;
                        } else {
                            isWorking = false;
                            final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(itemObject.categories_local_id, false);
                            if (main != null) {
                                if (main.categories_id == null) {
                                    isWorking = false;
                                } else {
                                    isWorking = true;
                                }
                            }
                        }

                        if (myService == null) {
                            isWorking = false;
                        }
                        if (isWorking) {

                            if (itemObject.isOriginalGlobalId) {
                                Utils.Log(TAG, "Uploading original data !!! " + itemObject.items_id);
                            } else {
                                Utils.Log(TAG, "Uploading thumbnail data !!! " + itemObject.items_id);
                            }

                            itemObject.statusProgress = EnumStatusProgress.PROGRESSING.ordinal();
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(itemObject);

                            myService.onUploadFileInAppFolder(itemObject, new UploadServiceListener() {
                                @Override
                                public void onProgressUpdate(int percentage) {
                                    //Utils.Log(TAG,"onProgressUpdate "+ percentage +"%");
                                    isUploadData = true;
                                }

                                @Override
                                public void onFinish() {
                                    Utils.Log(TAG, "onFinish");
                                }

                                @Override
                                public void onResponseData(DriveResponse response) {
                                    try {
                                        if (response != null) {
                                            if (response.id != null) {
                                                DriveEvent contentTitle = DriveEvent.getInstance().hexToObject(response.name);
                                                final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(contentTitle.items_id, false);
                                                if (mItem != null && contentTitle != null) {
                                                    EnumFileType type = EnumFileType.values()[contentTitle.fileType];
                                                    if (type == EnumFileType.ORIGINAL) {
                                                        mItem.originalSync = true;
                                                        mItem.global_original_id = response.id;
                                                        Log.d(TAG, "Uploaded for original.........................................: global id  " + response.id + " - local id " + mItem.id);
                                                    } else {
                                                        mItem.thumbnailSync = true;
                                                        mItem.global_thumbnail_id = response.id;
                                                        Log.d(TAG, "Uploaded for thumbnail.........................................: global id  " + response.id + " - local id " + mItem.id);
                                                    }
                                                    mItem.fileType = contentTitle.fileType;
                                                    if (mItem.thumbnailSync && mItem.originalSync) {
                                                        mItem.isSyncCloud = true;
                                                        mItem.statusProgress = EnumStatusProgress.DONE.ordinal();
                                                    } else {
                                                        mItem.isSyncCloud = false;
                                                    }
                                                    if (mItem.isSyncCloud) {
                                                        EnumFormatType formatTypeFile = EnumFormatType.values()[mItem.formatType];
                                                        switch (formatTypeFile) {
                                                            case AUDIO: {
                                                                mItem.global_thumbnail_id = "null";
                                                                onAddItems(mItem);
                                                                break;
                                                            }
                                                            case FILES: {
                                                                mItem.global_thumbnail_id = "null";
                                                                onAddItems(mItem);
                                                                break;
                                                            }
                                                            default: {
                                                                onAddItems(mItem);
                                                            }
                                                        }
                                                    }
                                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                    /*Saver Files*/
                                                    boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space), false);
                                                    if (isSaver) {
                                                        EnumFormatType formatType = EnumFormatType.values()[mItem.formatType];
                                                        switch (formatType) {
                                                            case IMAGE: {
                                                                storage.deleteFile(mItem.originalPath);
                                                                break;
                                                            }
                                                        }
                                                    }

                                                } else {
                                                    Utils.Log(TAG, "Failed Save 3");
                                                }
                                            } else {
                                                Utils.Log(TAG, "Failed Save 2");
                                            }
                                        } else {
                                            Utils.Log(TAG, "Failed Save 1");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Utils.onWriteLog(e.getMessage(), EnumStatus.UPLOAD);
                                    } finally {
                                        onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                    }
                                }
                                @Override
                                public void onError(String message, EnumStatus status) {
                                    Utils.Log(TAG, "onError: " + message);
                                    Utils.onWriteLog(message, EnumStatus.UPLOAD);
                                    onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                }
                            });
                        } else {
                            if (!itemObject.originalSync && !itemObject.thumbnailSync) {
                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                Utils.Log(TAG, "Exception upload....................... 2");
                            } else {
                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                Utils.Log(TAG, "Exception upload....................... 1");
                            }
                        }
                    }
                } else {
                    isUploadData = false;
                    Utils.Log(TAG, "Drive api not ready");
                    Utils.onWriteLog("Drive api not ready", EnumStatus.UPLOAD);
                    SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
                }
            } else {
                isUploadData = false;
                Utils.Log(TAG, "User not ready");
                Utils.onWriteLog("User not ready", EnumStatus.UPLOAD);
                SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
            }
        } else {
            isUploadData = false;
            Utils.Log(TAG, "My services is null");
            Utils.onWriteLog("My services is null", EnumStatus.UPLOAD);
            SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
        }
    }

    public void onAddItems(final Items items) {
        if (myService == null) {
            Utils.Log(TAG, "My service is null");
            return;
        }
        Utils.Log(TAG, "Preparing insert  to own Server");
        myService.onAddItems(items, new ServiceManagerShortListener() {
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + " status " + status.name());
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                items.isSyncOwnServer = true;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                Utils.Log(TAG, message + " status " + status.name());
            }
        });
    }

    /*Gallery action*/

    public void onSaveDataOnGallery(final ImportFiles importFiles, ServiceManagerGalleySyncDataListener listener) {
        subscriptions = Observable.create(subscriber -> {
            final MimeTypeFile mMimeTypeFile = importFiles.mimeTypeFile;
            final EnumFormatType enumTypeFile = mMimeTypeFile.formatType;
            final String mPath = importFiles.path;
            final String mMimeType = mMimeTypeFile.mimeType;
            final Items items;
            final MainCategories mMainCategories = importFiles.mainCategories;
            final String categories_id = mMainCategories.categories_id;
            final String categories_local_id = mMainCategories.categories_local_id;
            final boolean isFakePin = mMainCategories.isFakePin;
            Utils.Log(TAG, "object " + new Gson().toJson(mMimeTypeFile));
            Bitmap thumbnail = null;
            switch (enumTypeFile) {
                case IMAGE: {
                    Utils.Log(TAG, "Start RXJava Image Progressing");
                    try {

                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;


                        Items itemsPhoto = new Items();
                        itemsPhoto.fileExtension = mMimeTypeFile.extension;

                        itemsPhoto.originalPath = originalPath;
                        itemsPhoto.thumbnailPath = thumbnailPath;
                        itemsPhoto.categories_local_id = categories_local_id;
                        itemsPhoto.categories_id = categories_id;
                        itemsPhoto.global_original_id = null;
                        itemsPhoto.mimeType = mMimeType;
                        itemsPhoto.thumbnailName = currentTime;
                        itemsPhoto.items_id = uuId;
                        itemsPhoto.formatType = EnumFormatType.IMAGE.ordinal();
                        itemsPhoto.degrees = 0;
                        itemsPhoto.thumbnailSync = false;
                        itemsPhoto.originalSync = false;
                        itemsPhoto.global_thumbnail_id = null;
                        itemsPhoto.fileType = EnumFileType.NONE.ordinal();
                        itemsPhoto.originalName = currentTime;
                        itemsPhoto.title = mMimeTypeFile.name;
                        itemsPhoto.thumbnailName = "thumbnail_" + currentTime;
                        itemsPhoto.size = "0";
                        itemsPhoto.statusProgress = EnumStatusProgress.NONE.ordinal();
                        itemsPhoto.isDeleteLocal = false;
                        itemsPhoto.isDeleteGlobal = false;
                        itemsPhoto.deleteAction = EnumDelete.NONE.ordinal();
                        itemsPhoto.isFakePin = isFakePin;

                        final boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space), false);
                        itemsPhoto.isSaver = isSaver;

                        itemsPhoto.isExport = false;
                        itemsPhoto.isWaitingForExporting = false;
                        itemsPhoto.custom_items = 0;

                        itemsPhoto.isSyncCloud = false;
                        itemsPhoto.isSyncOwnServer = false;
                        itemsPhoto.isUpdate = false;
                        itemsPhoto.statusAction = EnumStatus.UPLOAD.ordinal();

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
                        Log.w(TAG, "Cannot write to " + e);
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
                            Log.d("EXIF", "Exif: " + orientation);
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
                            Log.w(TAG, "Cannot write to " + e);
                        }

                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;


                        Items itemsVideo = new Items();
                        itemsVideo.fileExtension = mMimeTypeFile.extension;
                        itemsVideo.originalPath = originalPath;
                        itemsVideo.thumbnailPath = thumbnailPath;
                        itemsVideo.categories_id = categories_id;
                        itemsVideo.categories_local_id = categories_local_id;
                        itemsVideo.global_original_id = null;
                        itemsVideo.mimeType = mMimeType;
                        itemsVideo.items_id = uuId;
                        itemsVideo.formatType = EnumFormatType.VIDEO.ordinal();
                        itemsVideo.degrees = 0;
                        itemsVideo.thumbnailSync = false;
                        itemsVideo.originalSync = false;
                        itemsVideo.global_thumbnail_id = null;
                        itemsVideo.fileType = EnumFileType.NONE.ordinal();
                        itemsVideo.originalName = currentTime;
                        itemsVideo.title = mMimeTypeFile.name;
                        itemsVideo.thumbnailName = "thumbnail_" + currentTime;
                        itemsVideo.size = "0";
                        itemsVideo.statusProgress = EnumStatusProgress.NONE.ordinal();
                        itemsVideo.isDeleteLocal = false;
                        itemsVideo.isDeleteGlobal = false;
                        itemsVideo.deleteAction = EnumDelete.NONE.ordinal();
                        itemsVideo.isFakePin = isFakePin;
                        itemsVideo.isSaver = false;
                        itemsVideo.isExport = false;
                        itemsVideo.isWaitingForExporting = false;
                        itemsVideo.custom_items = 0;

                        itemsVideo.isSyncCloud = false;
                        itemsVideo.isSyncOwnServer = false;
                        itemsVideo.isUpdate = false;
                        itemsVideo.statusAction = EnumStatus.UPLOAD.ordinal();


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
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String originalPath = pathContent + currentTime;

                        Items itemsAudio = new Items();
                        itemsAudio.fileExtension = mMimeTypeFile.extension;
                        itemsAudio.originalPath = originalPath;
                        itemsAudio.thumbnailPath = "null";
                        itemsAudio.categories_id = categories_id;
                        itemsAudio.categories_local_id = categories_local_id;
                        itemsAudio.mimeType = mMimeType;
                        itemsAudio.items_id = uuId;
                        itemsAudio.formatType = EnumFormatType.AUDIO.ordinal();
                        itemsAudio.degrees = 0;
                        itemsAudio.thumbnailSync = true;
                        itemsAudio.originalSync = false;
                        itemsAudio.global_original_id = null;
                        itemsAudio.global_thumbnail_id = null;
                        itemsAudio.fileType = EnumFileType.NONE.ordinal();
                        itemsAudio.originalName = currentTime;
                        itemsAudio.title = mMimeTypeFile.name;
                        itemsAudio.thumbnailName = "null";
                        itemsAudio.size = "0";
                        itemsAudio.statusProgress = EnumStatusProgress.NONE.ordinal();
                        itemsAudio.isDeleteLocal = false;
                        itemsAudio.isDeleteGlobal = false;
                        itemsAudio.deleteAction = EnumDelete.NONE.ordinal();
                        itemsAudio.isFakePin = isFakePin;
                        itemsAudio.isSaver = false;
                        itemsAudio.isExport = false;
                        itemsAudio.isWaitingForExporting = false;
                        itemsAudio.custom_items = 0;


                        itemsAudio.isSyncCloud = false;
                        itemsAudio.isSyncOwnServer = false;
                        itemsAudio.isUpdate = false;
                        itemsAudio.statusAction = EnumStatus.UPLOAD.ordinal();



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
                        Log.w(TAG, "Cannot write to " + e);
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
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String originalPath = pathContent + currentTime;

                        Items itemsFile = new Items();
                        itemsFile.fileExtension = mMimeTypeFile.extension;
                        itemsFile.originalPath = originalPath;
                        itemsFile.thumbnailPath = "null";
                        itemsFile.categories_id = categories_id;
                        itemsFile.categories_local_id = categories_local_id;
                        itemsFile.mimeType = mMimeType;
                        itemsFile.items_id = uuId;
                        itemsFile.formatType = EnumFormatType.FILES.ordinal();
                        itemsFile.degrees = 0;
                        itemsFile.thumbnailSync = true;
                        itemsFile.originalSync = false;
                        itemsFile.global_original_id = null;
                        itemsFile.global_thumbnail_id = null;
                        itemsFile.fileType = EnumFileType.NONE.ordinal();
                        itemsFile.originalName = currentTime;
                        itemsFile.title = mMimeTypeFile.name;
                        itemsFile.thumbnailName = "null";
                        itemsFile.size = "0";
                        itemsFile.statusProgress = EnumStatusProgress.NONE.ordinal();
                        itemsFile.isDeleteLocal = false;
                        itemsFile.isDeleteGlobal = false;
                        itemsFile.deleteAction = EnumDelete.NONE.ordinal();
                        itemsFile.isFakePin = isFakePin;
                        itemsFile.isSaver = false;
                        itemsFile.isExport = false;
                        itemsFile.isWaitingForExporting = false;
                        itemsFile.custom_items = 0;

                        itemsFile.isSyncCloud = false;
                        itemsFile.isSyncOwnServer = false;
                        itemsFile.isUpdate = false;
                        itemsFile.statusAction = EnumStatus.UPLOAD.ordinal();

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
                            final Items items = mResponse.items;
                            long mb;
                            EnumFormatType enumFormatType = EnumFormatType.values()[items.formatType];
                            switch (enumFormatType) {
                                case AUDIO: {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        items.size = "" + mb;
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
                                    }
                                    break;
                                }
                                case FILES: {
                                    if (storage.isFileExist(items.originalPath)) {
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        items.size = "" + mb;
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
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
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);

                                        if (!mResponse.categories.isCustom_Cover) {
                                            final MainCategories main = mResponse.categories;
                                            main.items_id = items.items_id;
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
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
                            final Items items = mResponse.items;
                            GalleryCameraMediaManager.getInstance().setProgressing(false);
                            GalleryCameraMediaManager.getInstance().onUpdatedView();
                            if (items.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView();
                            } else {
                                SingletonPrivateFragment.getInstance().onUpdateView();
                                ServiceManager.getInstance().onSyncDataOwnServer("0");
                            }
                            Utils.Log(TAG, "Original path :" + mResponse.originalPath);
                            Storage storage = new Storage(SuperSafeApplication.getInstance());
                            storage.deleteFile(mResponse.originalPath);
                            listener.onCompleted(importFiles);
                        }
                        else{
                            listener.onFailed(importFiles);
                        }
                    }
                });
    }

    /*--------------Camera action-----------------*/

    public void onSaveDataOnCamera(final byte[] mData, final MainCategories mainCategories) {
        subscriptions = Observable.create(subscriber -> {

            final MainCategories mMainCategories = mainCategories;
            final String categories_id = mMainCategories.categories_id;
            final String categories_local_id = mMainCategories.categories_local_id;
            final boolean isFakePin = mMainCategories.isFakePin;

            Utils.Log(TAG,"categories_local_id " +categories_local_id );

            final byte[] data = mData;
            try {
                String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                String currentTime = Utils.getCurrentDateTime();
                String uuId = Utils.getUUId();
                String pathContent = rootPath + uuId + "/";
                storage.createDirectory(pathContent);
                String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                String originalPath = pathContent + currentTime;

                Items items = new Items();
                items.fileExtension = getString(R.string.key_jpg);
                items.originalPath = originalPath;
                items.thumbnailPath = thumbnailPath;
                items.categories_id = categories_id;
                items.categories_local_id = categories_local_id;
                items.global_original_id = null;
                items.mimeType = MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype();
                items.thumbnailName = currentTime;
                items.items_id = uuId;
                items.formatType = EnumFormatType.IMAGE.ordinal();
                items.degrees = 0;
                items.thumbnailSync = false;
                items.originalSync = false;
                items.global_thumbnail_id = null;
                items.fileType = EnumFileType.NONE.ordinal();
                items.originalName = currentTime;
                items.title = currentTime + getString(R.string.key_jpg);;
                items.thumbnailName = "thumbnail_" + currentTime;
                items.size = "0";
                items.statusProgress = EnumStatusProgress.NONE.ordinal();
                items.isDeleteLocal = false;
                items.isDeleteGlobal = false;
                items.deleteAction = EnumDelete.NONE.ordinal();
                items.isFakePin = isFakePin;
                items.isExport = false;
                items.isWaitingForExporting = false;
                items.custom_items = 0;
                items.isSyncCloud = false;
                items.isSyncOwnServer = false;
                items.isUpdate = false;
                items.statusAction = EnumStatus.UPLOAD.ordinal();

                final boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space), false);
                items.isSaver = isSaver;

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
                Log.w(TAG, "Cannot write to " + e);
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
                            final Items mItem = mResponse.items;
                            long mb;
                            if (storage.isFileExist(mItem.originalPath) && storage.isFileExist(mItem.thumbnailPath)) {
                                mb = (long) +storage.getSize(new File(mItem.originalPath), SizeUnit.B);
                                if (storage.isFileExist(mItem.thumbnailPath)) {
                                    mb += (long) +storage.getSize(new File(mItem.thumbnailPath), SizeUnit.B);
                                }
                                mItem.size = "" + mb;
                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mItem);
                                if (!mResponse.categories.isCustom_Cover) {
                                    final MainCategories main = mResponse.categories;
                                    main.items_id = mItem.items_id;
                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
                                    Utils.Log(TAG, "Special main categories " + new Gson().toJson(main));
                                }

                            }
                        }
                        Utils.Log(TAG, "Insert Successful");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mResponse.isWorking) {
                            final Items mItem = mResponse.items;
                            GalleryCameraMediaManager.getInstance().setProgressing(false);
                            GalleryCameraMediaManager.getInstance().onUpdatedView();
                            if (mItem.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView();
                            } else {
                                SingletonPrivateFragment.getInstance().onUpdateView();
                                ServiceManager.getInstance().onSyncDataOwnServer("0");
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
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName("Exporting error")
                                            .putContentType("Content error "+e.getMessage())
                                            .putContentId("Exporting "+System.currentTimeMillis()));
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
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName("Exporting error")
                                            .putContentType("Content error "+e.getMessage())
                                            .putContentId("Exporting "+System.currentTimeMillis()));
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Cannot write to " + e);
                } finally {
                    Utils.Log(TAG, "Finally");
                }

            } else {
                Utils.Log(TAG, "Exporting file............................Done");
                GalleryCameraMediaManager.getInstance().onStopProgress();
                setExporting(false);
                mListExport.clear();
            }

        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {

                });
    }

    public void onImportingFiles() {
        Utils.Log(TAG, "Import amount files :" + mListImport.size());
        subscriptions = Observable.create(subscriber -> {
            setImporting(true);
            boolean isWorking = false;
            ImportFiles importFiles = null;
            for (int i = 0; i < mListImport.size(); i++) {
                if (!mListImport.get(i).isImport) {
                    importFiles = mListImport.get(i);
                    importFiles.position = i;
                    isWorking = true;
                    break;
                }
            }
            if (isWorking) {
                ServiceManager.getInstance().onSaveDataOnGallery(importFiles, new ServiceManager.ServiceManagerGalleySyncDataListener() {
                    @Override
                    public void onCompleted(ImportFiles importFiles) {
                        try {
                            mListImport.get(importFiles.position).isImport = true;
                            Utils.Log(TAG, "Importing file............................Successful "+importFiles.position);
                            onImportingFiles();
                        }
                        catch (Exception e){
                            Answers.getInstance().logContentView(new ContentViewEvent()
                                    .putContentName("Importing error")
                                    .putContentType("Content error "+e.getMessage())
                                    .putContentId("Importing "+System.currentTimeMillis()));
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailed(ImportFiles importFiles) {
                        try {
                            mListImport.get(importFiles.position).isImport = true;
                            onImportingFiles();
                            Utils.Log(TAG, "Importing file............................Failed "+importFiles.position);
                        }
                        catch (Exception e){
                            Answers.getInstance().logContentView(new ContentViewEvent()
                                    .putContentName("Importing error")
                                    .putContentType("Content error "+e.getMessage())
                                    .putContentId("Importing "+System.currentTimeMillis()));
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Utils.Log(TAG, "Importing file............................Done");
                GalleryCameraMediaManager.getInstance().onStopProgress();
                setImporting(false);
                mListImport.clear();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {

                });
    }

    /*Download file*/

    private Observable<Items> getObservableItems(Items items, int position) {
        return Observable.create(subscriber -> {
            ServiceManager.getInstance().getMyService().onDownloadFile(items, null, new ServiceManager.DownloadServiceListener() {
                @Override
                public void onProgressDownload(int percentage) {
                    Utils.Log(TAG, "Percentage " + percentage);
                }
                @Override
                public void onSaved() {

                }
                @Override
                public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                    mListDownLoadFiles.get(position).isSaver = false;
                    items.isSaver = false;
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                    subscriber.onNext(items);
                    subscriber.onComplete();
                }
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG, message + "--" + status.name());
                    subscriber.onNext(items);
                    subscriber.onComplete();
                }
            });
        });
    }

    public void getObservableDownload(boolean isExporting) {
        Utils.Log(TAG, "Preparing download.....");
        Utils.Log(TAG, "Download amount files :" + mListDownLoadFiles.size());
        setDownloadingFiles(true);
        int position = 0;
        Items items = null;
        boolean isWorking = false;
        for (int i = 0; i < mListDownLoadFiles.size(); i++) {
            final Items index = mListDownLoadFiles.get(i);
            if (index.isSaver && index.isChecked) {
                isWorking = true;
                position = i;
                items = index;
                break;
            }
        }
        if (isWorking) {
            final User user = User.getInstance().getUserInfo();
            if (user != null) {
                if (!user.driveConnected) {
                    Utils.Log(TAG, " Drive disconnected");
                    return;
                }
            }
            items.isOriginalGlobalId = true;
            final int next = position;
            getObservableItems(items, position).
                    subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).
                    subscribe(new Observer<Items>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                        @Override
                        public void onComplete() {
                            Utils.Log(TAG, "Downloading completed............."+next);
                            getObservableDownload(isExporting);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (isExporting){
                                GalleryCameraMediaManager.getInstance().onCompletedDownload(EnumStatus.ERROR);
                            }
                            else{
                                EventBus.getDefault().post(EnumStatus.DownLoadDone);
                            }
                        }
                        @Override
                        public void onNext(Items object) {
                            // Show Progress
                            Utils.Log(TAG, "next");
                        }

                    });
        } else {
            if (isExporting){
                GalleryCameraMediaManager.getInstance().onCompletedDownload(EnumStatus.DONE);
            }
            else{
                EventBus.getDefault().post(EnumStatus.DownLoadDone);
            }
            setDownloadingFiles(false);
            mListDownLoadFiles.clear();
        }
    }

    public void onUpdateSyncDataStatus(EnumStatus enumStatus) {
        switch (enumStatus) {
            case UPLOAD: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    isUploadData = false;
                    String message = "Completed upload count syn data...................uploaded " + countSyncData + "/" + totalList;
                    String messageDone = "Completed upload sync data.......................^^...........^^.......^^........";
                    Utils.onWriteLog(message, EnumStatus.UPLOAD);
                    Utils.onWriteLog(messageDone, EnumStatus.UPLOAD);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);
                    Utils.Log(TAG, "Request syn data on upload.........");
                    Utils.onWriteLog("Request syn data on upload", EnumStatus.UPLOAD);
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();
                } else {
                    String message = "Completed upload count syn data...................uploaded " + countSyncData + "/" + totalList;
                    Utils.onWriteLog(message, EnumStatus.UPLOAD);
                    Utils.Log(TAG, message);
                }
                break;
            }
            case DOWNLOAD: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    isDownloadData = false;
                    String message = "Completed download count syn data...................downloaded " + countSyncData + "/" + totalList;
                    String messageDone = "Completed downloaded sync data.......................^^...........^^.......^^........";
                    Utils.onWriteLog(message, EnumStatus.DOWNLOAD);
                    Utils.onWriteLog(messageDone, EnumStatus.DOWNLOAD);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    Utils.onWriteLog("Request syn data on download", EnumStatus.DOWNLOAD);
                    Utils.Log(TAG, "Request syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();
                } else {
                    String message = "Completed download count syn data...................downloaded " + countSyncData + "/" + totalList;
                    Utils.onWriteLog(message, EnumStatus.DOWNLOAD);
                    Utils.Log(TAG, message);
                }
                break;
            }
            case DELETE_SYNC_OWN_DATA: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    isDeleteOwnCloud = false;
                    String message = "Completed delete own cloud count syn data...................deleted " + countSyncData + "/" + totalList;
                    String messageDone = "Completed delete own sync data.......................^^...........^^.......^^........";
                    Utils.onWriteLog(message, EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.onWriteLog(messageDone, EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);
                    Utils.onWriteLog("Request own syn data on download", EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.Log(TAG, "Request own syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();
                } else {
                    String message = "Completed delete count syn data...................deleted " + countSyncData + "/" + totalList;
                    Utils.onWriteLog(message, EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.Log(TAG, message);
                }
                break;
            }
            case DELETE_SYNC_CLOUD_DATA: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    isDeleteSyncCLoud = false;
                    String message = "Completed delete cloud count syn data...................deleted " + countSyncData + "/" + totalList;
                    String messageDone = "Completed delete cloud sync data.......................^^...........^^.......^^........";
                    Utils.onWriteLog(message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.onWriteLog(messageDone, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    Utils.onWriteLog("Request cloud syn data on download", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.Log(TAG, "Request cloud syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();
                    //onDeleteOnLocal();
                } else {
                    String message = "Completed delete count syn data...................deleted " + countSyncData + "/" + totalList;
                    Utils.onWriteLog(message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.Log(TAG, message);
                }
                break;
            }
            case DELETE_CATEGORIES: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    isDeleteAlbum = false;
                    String message = "Completed delete album...................deleted " + countSyncData + "/" + totalList;
                    String messageDone = "Completed delete album data.......................^^...........^^.......^^........";
                    Utils.onWriteLog(message, EnumStatus.DELETE_CATEGORIES);
                    Utils.onWriteLog(messageDone, EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    Utils.onWriteLog("Request cloud syn data on album", EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, "Request cloud syn data on album.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    onGetDriveAbout();
                    //onDeleteOnLocal();
                } else {
                    String message = "Completed delete count album...................deleted " + countSyncData + "/" + totalList;
                    Utils.onWriteLog(message, EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, message);
                }
                break;
            }
            case UPDATE:{
                countSyncData += 1;
                if (countSyncData == totalList) {
                    isUpdate = false;
                    String message = "Completed update own...................items " + countSyncData + "/" + totalList;
                    Utils.Log(TAG,message);
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                } else {
                    String message = "Completed delete count item...................updated " + countSyncData + "/" + totalList;
                    Utils.Log(TAG, message);
                }
                break;
            }
        }
    }



    public void onDismissServices() {
        if (isDownloadData || isUploadData || isDownloadingFiles || isExporting || isImporting || isDeleteOwnCloud || isDeleteSyncCLoud || isDeleteAlbum || isGetListCategories || isCategoriesSync|| isWaitingSendMail || isUpdate) {
            Utils.Log(TAG, "Progress isDownloadData is :" + isDownloadData);
            Utils.Log(TAG, "Progress isUploadData is :" + isUploadData);
            Utils.Log(TAG, "Progress isDownloadingFiles is :" + isDownloadingFiles);
            Utils.Log(TAG, "Progress isExporting is :" + isExporting);
            Utils.Log(TAG, "Progress isImporting is :" + isImporting);
            Utils.Log(TAG, "Progress isDeleteOwnCloud is :" + isDeleteOwnCloud);
            Utils.Log(TAG, "Progress isDeleteSyncCLoud is :" + isDeleteSyncCLoud);
            Utils.Log(TAG, "Progress isDeleteAlbum is :" + isDeleteAlbum);
            Utils.Log(TAG, "Progress isGetListCategories is :" + isGetListCategories);
            Utils.Log(TAG, "Progress isCategoriesSync is :" + isCategoriesSync);
            Utils.Log(TAG, "Progress isWaitingSendMail is :" + isWaitingSendMail);
            Utils.Log(TAG, "Progress isUpdate is :" + isUpdate);
        }
        else {
            SingletonPremiumTimer.getInstance().onStop();
            ServiceManager.getInstance().setDownloadData(false);
            ServiceManager.getInstance().setUploadData(false);
            ServiceManager.getInstance().setDownloadingFiles(false);
            ServiceManager.getInstance().setExporting(false);
            ServiceManager.getInstance().setImporting(false);
            ServiceManager.getInstance().setDeleteOwnCloud(false);
            ServiceManager.getInstance().setDeleteSyncCLoud(false);
            ServiceManager.getInstance().setDeleteAlbum(false);
            ServiceManager.getInstance().setGetListCategories(false);
            ServiceManager.getInstance().setCategoriesSync(false);
            ServiceManager.getInstance().setIsWaitingSendMail(false);
            ServiceManager.getInstance().setUpdate(false);
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

    @Override
    public void onError(String message, EnumStatus status) {
        Log.d(TAG, "onError response :" + message + " - " + status.name());
        if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
            SingletonManagerTab.getInstance().onRequestAccessToken();
            Utils.Log(TAG, "Request token on global");
        }
    }

    @Override
    public void onSuccessful(String message) {
        Log.d(TAG, "onSuccessful Response  :" + message);
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public Activity getActivity() {
        return null;
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

                            if (!MainTabActivity.isVisit && !FakePinComponentActivity.isVisit){
                                return;
                            }

                            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
                            Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);
                            EnterPinActivity.isVisible = true;
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
                Utils.Log(TAG, "drive about :" + message);
                break;
            }
            case CONNECTED: {
                GoogleDriveConnectionManager.getInstance().onNetworkConnectionChanged(true);
                ServiceManager.getInstance().onGetUserInfo();
                PremiumManager.getInstance().onStartInAppPurchase();
                break;
            }
            case DISCONNECTED:{
                ServiceManager.getInstance().setDownloadData(false);
                ServiceManager.getInstance().setUploadData(false);
                ServiceManager.getInstance().setDownloadingFiles(false);
                ServiceManager.getInstance().setExporting(false);
                ServiceManager.getInstance().setImporting(false);
                ServiceManager.getInstance().setDeleteOwnCloud(false);
                ServiceManager.getInstance().setDeleteSyncCLoud(false);
                ServiceManager.getInstance().setDeleteAlbum(false);
                ServiceManager.getInstance().setGetListCategories(false);
                ServiceManager.getInstance().setCategoriesSync(false);
                ServiceManager.getInstance().setIsWaitingSendMail(false);
                ServiceManager.getInstance().setUpdate(false);
                Utils.Log(TAG,"Disconnect");
                break;
            }
            case USER_INFO: {
                final boolean isExpired = User.getInstance().isPremiumExpired();
                if (isExpired) {
                    if (!User.getInstance().isCheckAllowUpload()){
                        return;
                    }

                }

                SingletonPremiumTimer.getInstance().onStop();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"onStartTimer");
                        SingletonPremiumTimer.getInstance().onStartTimer();
                    }
                }, 5000);
                Utils.Log(TAG, "Get info successful");
                ServiceManager.getInstance().onSyncDataOwnServer("0");
                ServiceManager.getInstance().onCheckingMissData();
                final User mUser = User.getInstance().getUserInfo();
                if (mUser.isWaitingSendMail){
                    ServiceManager.getInstance().onSendEmail();
                }
                break;
            }
            case UPDATE_USER_TOKEN:{
                onSyncDataOwnServer("0");
                break;
            }
        }
    }

    public interface ServiceManagerSyncDataListener {
        void onCompleted();
        void onError();
        void onCancel();
    }

    public interface ServiceManagerShortListener{
        void onError(String message, EnumStatus status);
        void onSuccessful(String message,EnumStatus status);
    }

    public interface ServiceManagerGalleySyncDataListener {
        void onCompleted(ImportFiles importFiles);
        void onFailed(ImportFiles importFiles);
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
        void onSaved();
        void onDownLoadCompleted(File file_name, DownloadFileRequest request);
        void onError(String message, EnumStatus status);
    }
    public interface DeleteServiceListener {
        void onDone();
    }

}
