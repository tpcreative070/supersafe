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
import android.widget.Toast;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;
import com.snatik.storage.helpers.SizeUnit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.DriveTitle;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.Premium;
import co.tpcreative.supersafe.model.ResponseRXJava;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
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
    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "connected");
            myService = ((SuperSafeService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            ServiceManager.getInstance().onCheckingMissData();
            ServiceManager.getInstance().onGetUserInfo();
        }

        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

    private Cipher mCiphers;
    private boolean isDownloadData;
    private boolean isLoadingData;
    private boolean isDeleteSyncCLoud;
    private boolean isDeleteOwnCloud;
    private boolean isGetListCategories;
    private boolean isCategoriesSync;
    private boolean isDeleteAlbum;
    private int countSyncData = 0;
    private int totalList = 0;

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }


    public boolean isDeleteAlbum() {
        return isDeleteAlbum;
    }

    public void setDeleteAlbum(boolean deleteAlbum) {
        isDeleteAlbum = deleteAlbum;
    }

    public boolean isCategoriesSync() {
        return isCategoriesSync;
    }

    public void setCategoriesSync(boolean categoriesSync) {
        isCategoriesSync = categoriesSync;
    }

    public boolean isGetListCategories() {
        return isGetListCategories;
    }

    public void setGetListCategories(boolean getListCategories) {
        isGetListCategories = getListCategories;
    }

    public boolean isDownloadData() {
        return isDownloadData;
    }

    public void setDownloadData(boolean downloadData) {
        isDownloadData = downloadData;
    }

    public boolean isDeleteSyncCLoud() {
        return isDeleteSyncCLoud;
    }

    public void setDeleteSyncCLoud(boolean deleteSyncCLoud) {
        isDeleteSyncCLoud = deleteSyncCLoud;
    }

    public boolean isDeleteOwnCloud() {
        return isDeleteOwnCloud;
    }

    public void setDeleteOwnCloud(boolean deleteOwnCloud) {
        isDeleteOwnCloud = deleteOwnCloud;
    }

    public boolean isUploadData() {
        return isUploadData;
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
       if (myService!=null){
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

    public SuperSafeService getMyService() {
        return myService;
    }

    protected void showMessage(String message) {
        Toast.makeText(SuperSafeApplication.getInstance(), message, Toast.LENGTH_LONG).show();
    }

    private String getString(int res) {
        String value = SuperSafeApplication.getInstance().getString(res);
        return value;
    }


    /*User info*/
    public void onGetUserInfo() {
        if (myService != null) {
            myService.onGetUserInfo();
        } else {
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

        myService.onGetListCategoriesSync(new BaseView() {
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + "--" + status.name());
                isGetListCategories = false;
            }

            @Override
            public void onSuccessful(String message) {

            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG, message + "--" + status.name());
                SingletonPrivateFragment.getInstance().onUpdateView();
                isGetListCategories = false;
                getObservable();
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
                return null;
            }

            @Override
            public Activity getActivity() {
                return null;
            }
        });
    }


    private Observable<MainCategories> getObservableItems(List<MainCategories> categories) {
        return Observable.create(subscriber -> {
            for (MainCategories index : categories) {
                myService.onCategoriesSync(index, new BaseView() {
                    @Override
                    public void onError(String message, EnumStatus status) {
                        Utils.Log(TAG, message + "--" + status.name());
                        subscriber.onNext(index);
                        subscriber.onComplete();
                    }

                    @Override
                    public void onSuccessful(String message) {

                    }

                    @Override
                    public void onSuccessful(String message, EnumStatus status) {
                        Utils.Log(TAG, message + "--" + status.name());
                        subscriber.onNext(index);
                        subscriber.onComplete();
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
                        return null;
                    }

                    @Override
                    public Activity getActivity() {
                        return null;
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
                        Utils.Log(TAG, "complete");
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
        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true, false, false);
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
        if (isGetListCategories) {
            Utils.Log(TAG, "Getting list categories...----------------*******************************-----------");
            return;
        }

        final boolean isPauseCloudSync = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync),false);
        if (isPauseCloudSync){
            Utils.Log(TAG, "Pause Cloud Sync is Enabled...----------------*******************************-----------");
            return;
        }


        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.syncData!=null){
                final Premium mPremium = mUser.premium;
                if (mPremium!=null){
                    if (mUser.syncData.left<=0 && !mPremium.status){
                        Utils.Log(TAG, mUser.syncData.message+"-------------*******************************-----------");
                        return;
                    }
                }
            }
        }


        if (myService != null) {
            isLoadingData = true;
            myService.onGetListSync(nextPage, new BaseView() {
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
                    return null;
                }

                @Override
                public Activity getActivity() {
                    return null;
                }

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
                public void onSuccessful(String message) {
                    Utils.Log(TAG, "Response :" + message);
                }

                @Override
                public void onSuccessful(String nextPage, EnumStatus status) {
                    if (status == EnumStatus.LOAD_MORE) {
                        isLoadingData = false;
                        Utils.Log(TAG, "next page on onSyncDataOwnServer " + nextPage);
                        onSyncDataOwnServer(nextPage);
                    } else if (status == EnumStatus.SYNC_READY) {

                        isLoadingData = false;
                        final List<Items> items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems(false);

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
                        } else if (items != null) {
                            if (items.size() > 0) {
                                Utils.Log(TAG, "Preparing downloading...");
                                onDownloadFilesFromDriveStore();
                            } else {
                                Utils.Log(TAG, "Preparing uploading...");
                                onUploadDataToStore();
                            }
                        } else {
                            Utils.Log(TAG, "Preparing uploading...");
                            onUploadDataToStore();
                        }

                    }
                }
            });
        } else {
            Utils.Log(TAG, "My service is null");
        }
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
                    myService.onDeleteOwnSystem(mItem, new BaseView() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "--" + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);
                        }

                        @Override
                        public void onSuccessful(String message) {

                        }

                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + " -- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);

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
                            return null;
                        }

                        @Override
                        public Activity getActivity() {
                            return null;
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
                    myService.onDeleteCategoriesSync(main, new BaseView() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "--" + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_CATEGORIES);
                        }

                        @Override
                        public void onSuccessful(String message) {

                        }

                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + " -- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_CATEGORIES);
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
                            return null;
                        }

                        @Override
                        public Activity getActivity() {
                            return null;
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
            if (index.global_thumbnail_id != null && formatTypeFile != EnumFormatType.AUDIO) {
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
                    myService.onDeleteCloudItems(mItem, mItem.isOriginalGlobalId, new BaseView() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_CLOUD_DATA);
                        }

                        @Override
                        public void onSuccessful(String message) {

                        }

                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            Utils.Log(TAG, message + "- " + status.name());
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_CLOUD_DATA);
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
                            return null;
                        }

                        @Override
                        public Activity getActivity() {
                            return null;
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
            Utils.Log(TAG, "Data items already downloaded from Cloud !!!");
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
            isDownloadData = false;
            return;
        } else {
            SingletonManagerTab.getInstance().onAction(EnumStatus.DOWNLOAD);
            String message = "Preparing download " + totalList + " items from Cloud";
            Utils.Log(TAG, message);
            onWriteLog(message, EnumStatus.DOWNLOAD);
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
                            onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
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
                            onWriteLog("global_original_id is null ", EnumStatus.DOWNLOAD);
                            onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
                            //Utils.Log(TAG,"Drive description on original " + new Gson().toJson(new DriveDescription().hexToObject(itemObject.description)));
                            isWorking = false;
                        }

                        if (itemObject.global_thumbnail_id == null & formatTypeFile != EnumFormatType.AUDIO) {
                            Utils.Log(TAG, "global_thumbnail_id is null at " + itemObject.id + " format type :" + formatTypeFile.name() + "---name global :" + itemObject.items_id);
                            onWriteLog("global_thumbnail_id is null ", EnumStatus.DOWNLOAD);
                            onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
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

                            myService.onDownloadFile(itemObject, new ServiceManager.DownloadServiceListener() {
                                @Override
                                public void onError(String message, EnumStatus status) {
                                    onWriteLog(message, EnumStatus.DOWNLOAD);
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
                                                        Log.d(TAG, "Downloaded id thumbnail.........................................: global id  " + mItem.global_original_id + " - local id " + mItem.id);
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
                                                    final MainCategories categories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(mItem.categories_id,false);
                                                    if (categories!=null){
                                                        if (!categories.isCustom_Cover){
                                                            categories.item = new Gson().toJson(mItem);
                                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(categories);
                                                        }
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
                                        onWriteLog(e.getMessage(), EnumStatus.DOWNLOAD);
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
                    onWriteLog("Drive api not ready", EnumStatus.DOWNLOAD);
                    SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
                }
            } else {
                isDownloadData = false;
                Utils.Log(TAG, "User not ready");
                onWriteLog("User not ready", EnumStatus.DOWNLOAD);
                SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
            }
        } else {
            isDownloadData = false;
            Utils.Log(TAG, "My services is null");
            onWriteLog("My services is null", EnumStatus.DOWNLOAD);
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
            onWriteLog(message, EnumStatus.UPLOAD);
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
                                                DriveTitle contentTitle = DriveTitle.getInstance().hexToObject(response.name);
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
                                                            default: {
                                                                onAddItems(mItem);
                                                            }
                                                        }
                                                    }
                                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
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
                                        onWriteLog(e.getMessage(), EnumStatus.UPLOAD);
                                    } finally {
                                        onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                    }
                                }

                                @Override
                                public void onError(String message, EnumStatus status) {
                                    Utils.Log(TAG, "onError: " + message);
                                    onWriteLog(message, EnumStatus.UPLOAD);
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
                    onWriteLog("Drive api not ready", EnumStatus.UPLOAD);
                    SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
                }
            } else {
                isUploadData = false;
                Utils.Log(TAG, "User not ready");
                onWriteLog("User not ready", EnumStatus.UPLOAD);
                SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
            }
        } else {
            isUploadData = false;
            Utils.Log(TAG, "My services is null");
            onWriteLog("My services is null", EnumStatus.UPLOAD);
            SingletonManagerTab.getInstance().onAction(EnumStatus.SYNC_ERROR);
        }
    }


    public void onAddItems(final Items items) {
        if (myService == null) {
            Utils.Log(TAG, "My service is null");
            return;
        }
        Utils.Log(TAG, "Preparing insert  to own Server");
        myService.onAddItems(items, new BaseView() {
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + " status " + status.name());
            }

            @Override
            public void onSuccessful(String message) {

            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                items.isSyncOwnServer = true;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                Utils.Log(TAG, message + " status " + status.name());
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
                return null;
            }

            @Override
            public Activity getActivity() {
                return null;
            }

        });
    }

    /*Gallery action*/

    public void onSaveDataOnGallery(final MimeTypeFile mimeTypeFile, final List<Integer>mListFiles ,final String path, MainCategories mainCategories) {
        subscriptions = Observable.create(subscriber -> {
            final MimeTypeFile mMimeTypeFile = mimeTypeFile;
            final EnumFormatType enumTypeFile = mMimeTypeFile.formatType;
            final String mPath = path;
            final String mMimeType = mMimeTypeFile.mimeType;
            final Items items;
            final MainCategories mMainCategories = mainCategories;
            final String categories_id = mMainCategories.categories_id;
            final String categories_local_id = mMainCategories.categories_local_id;
            final boolean isFakePin = mMainCategories.isFakePin;

            Utils.Log(TAG, "object " + new Gson().toJson(mMimeTypeFile));
            Bitmap thumbnail = null;
            switch (enumTypeFile) {
                case IMAGE: {
                    Utils.Log(TAG, "Start RXJava Image Progressing");
                    try {

                        final int THUMBSIZE_HEIGHT = 600;
                        final int THUMBSIZE_WIDTH = 400;

                        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                        bmpFactoryOptions.inJustDecodeBounds = true;
                        Bitmap bitmap = BitmapFactory.decodeFile(mPath, bmpFactoryOptions);
                        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) THUMBSIZE_HEIGHT);
                        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) THUMBSIZE_WIDTH);
                        if (heightRatio > 1 || widthRatio > 1) {
                            if (heightRatio > widthRatio) {
                                bmpFactoryOptions.inSampleSize = heightRatio;
                            } else {
                                bmpFactoryOptions.inSampleSize = widthRatio;
                            }
                        }
                        bmpFactoryOptions.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(mPath, bmpFactoryOptions);

                        thumbnail = ThumbnailUtils.extractThumbnail(bitmap,
                                THUMBSIZE_HEIGHT,
                                THUMBSIZE_WIDTH);
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



                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;


                        DriveDescription description = new DriveDescription();
                        description.fileExtension = mMimeTypeFile.extension;

                        description.originalPath = originalPath;
                        description.thumbnailPath = thumbnailPath;
                        description.subFolderName = uuId;
                        description.categories_local_id = categories_local_id;
                        description.categories_id = categories_id;
                        description.local_id = uuId;
                        description.global_original_id = null;
                        description.mimeType = mMimeType;
                        description.thumbnailName = currentTime;
                        description.items_id = uuId;
                        description.formatType = EnumFormatType.IMAGE.ordinal();
                        description.degrees = 0;
                        description.thumbnailSync = false;
                        description.originalSync = false;
                        description.global_thumbnail_id = null;
                        description.fileType = EnumFileType.NONE.ordinal();
                        description.originalName = currentTime;
                        description.title = mMimeTypeFile.name;
                        description.thumbnailName = "thumbnail_" + currentTime;
                        description.size = "0";
                        description.statusProgress = EnumStatusProgress.NONE.ordinal();
                        description.isDeleteLocal = false;
                        description.isDeleteGlobal = false;
                        description.deleteAction = EnumDelete.NONE.ordinal();
                        description.isFakePin = isFakePin;


                        items = new Items(false,
                                false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.items_id,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.categories_id,
                                description.categories_local_id,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.deleteAction,
                                description.isFakePin);

                        Utils.Log(TAG, "start compress");
                        boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                        boolean createdOriginal = storage.createFile(new File(originalPath), new File(mPath), Cipher.ENCRYPT_MODE);
                        Utils.Log(TAG, "start end");



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
                        Log.w(TAG, "Cannot write to " + e);
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
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


                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                        String originalPath = pathContent + currentTime;


                        DriveDescription description = new DriveDescription();
                        description.fileExtension = mMimeTypeFile.extension;
                        description.originalPath = originalPath;
                        description.thumbnailPath = thumbnailPath;
                        description.subFolderName = uuId;
                        description.categories_id = categories_id;
                        description.categories_local_id = categories_local_id;
                        description.local_id = uuId;
                        description.global_original_id = null;
                        description.mimeType = mMimeType;
                        description.items_id = uuId;
                        description.formatType = EnumFormatType.VIDEO.ordinal();
                        description.degrees = 0;
                        description.thumbnailSync = false;
                        description.originalSync = false;
                        description.global_thumbnail_id = null;
                        description.fileType = EnumFileType.NONE.ordinal();
                        description.originalName = currentTime;
                        description.title = mMimeTypeFile.name;
                        description.thumbnailName = "thumbnail_" + currentTime;
                        description.size = "0";
                        description.statusProgress = EnumStatusProgress.NONE.ordinal();
                        description.isDeleteLocal = false;
                        description.isDeleteGlobal = false;
                        description.deleteAction = EnumDelete.NONE.ordinal();
                        description.isFakePin = isFakePin;


                        items = new Items(false,
                                false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.items_id,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.categories_id,
                                description.categories_local_id,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.deleteAction,
                                description.isFakePin);


                        boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);


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
                        Log.w(TAG, "Cannot write to " + e);
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
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
                    Utils.Log(TAG, "Start RXJava Video Progressing");
                    try {
                        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                        String currentTime = Utils.getCurrentDateTime();
                        String uuId = Utils.getUUId();
                        String pathContent = rootPath + uuId + "/";
                        storage.createDirectory(pathContent);
                        String originalPath = pathContent + currentTime;

                        DriveDescription description = new DriveDescription();
                        description.fileExtension = mMimeTypeFile.extension;
                        description.originalPath = originalPath;
                        description.thumbnailPath = null;
                        description.subFolderName = uuId;
                        description.categories_id = categories_id;
                        description.categories_local_id = categories_local_id;
                        description.local_id = uuId;
                        description.mimeType = mMimeType;
                        description.items_id = uuId;
                        description.formatType = EnumFormatType.AUDIO.ordinal();
                        description.degrees = 0;
                        description.thumbnailSync = true;
                        description.originalSync = false;
                        description.global_original_id = null;
                        description.global_thumbnail_id = null;
                        description.fileType = EnumFileType.NONE.ordinal();
                        description.originalName = currentTime;
                        description.title = mMimeTypeFile.name;
                        description.thumbnailName = null;
                        description.size = "0";
                        description.statusProgress = EnumStatusProgress.NONE.ordinal();
                        description.isDeleteLocal = false;
                        description.isDeleteGlobal = false;
                        description.deleteAction = EnumDelete.NONE.ordinal();
                        description.isFakePin = isFakePin;


                        items = new Items(false,
                                false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.items_id,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.categories_id,
                                description.categories_local_id,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.deleteAction,
                                description.isFakePin);

                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);

                        final ResponseRXJava response = new ResponseRXJava();
                        response.items = items;
                        response.categories = mMainCategories;

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
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
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
                                        final DriveDescription driveDescription = DriveDescription.getInstance().hexToObject(items.description);
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        driveDescription.size = "" + mb;
                                        items.size = driveDescription.size;
                                        items.description = DriveDescription.getInstance().convertToHex(new Gson().toJson(driveDescription));
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);

                                        if (!mResponse.categories.isCustom_Cover){
                                            final MainCategories main = mResponse.categories;
                                            main.item = new Gson().toJson(items);
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
                                        }
                                    }
                                    break;
                                }
                                default: {
                                    if (storage.isFileExist(items.originalPath) && storage.isFileExist(items.thumbnailPath)) {
                                        final DriveDescription driveDescription = DriveDescription.getInstance().hexToObject(items.description);
                                        mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                                        if (storage.isFileExist(items.thumbnailPath)) {
                                            mb += (long) +storage.getSize(new File(items.thumbnailPath), SizeUnit.B);
                                        }
                                        driveDescription.size = "" + mb;
                                        items.size = driveDescription.size;
                                        items.description = DriveDescription.getInstance().convertToHex(new Gson().toJson(driveDescription));
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);

                                        if (!mResponse.categories.isCustom_Cover){
                                            final MainCategories main = mResponse.categories;
                                            main.item = new Gson().toJson(items);
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
                        if (mResponse.isWorking){
                            final Items items = mResponse.items;
                            GalleryCameraMediaManager.getInstance().setProgressing(false);
                            GalleryCameraMediaManager.getInstance().onUpdatedView();
                            if (items.isFakePin) {
                                SingletonFakePinComponent.getInstance().onUpdateView();
                            } else {
                                SingletonPrivateFragment.getInstance().onUpdateView();
                                ServiceManager.getInstance().onSyncDataOwnServer("0");
                            }
                        }
                        if (mListFiles.size()>0){
                            mListFiles.remove(0);
                        }
                        GalleryCameraMediaManager.getInstance().onStopProgress();
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

            final byte[] data = mData;
            final Bitmap mBitmap;
            try {
                mBitmap = Utils.getThumbnailScale(data);
                String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                String currentTime = Utils.getCurrentDateTime();
                String uuId = Utils.getUUId();
                String pathContent = rootPath + uuId + "/";
                storage.createDirectory(pathContent);
                String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                String originalPath = pathContent + currentTime;

                DriveDescription description = new DriveDescription();
                description.fileExtension = getString(R.string.key_jpg);
                description.originalPath = originalPath;
                description.thumbnailPath = thumbnailPath;
                description.subFolderName = uuId;
                description.categories_id = categories_id;
                description.categories_local_id = categories_local_id;
                description.local_id = uuId;
                description.global_original_id = null;
                description.mimeType = MediaType.JPEG.type() + "/" + MediaType.JPEG.subtype();
                description.thumbnailName = currentTime;
                description.items_id = uuId;
                description.formatType = EnumFormatType.IMAGE.ordinal();
                description.degrees = 0;
                description.thumbnailSync = false;
                description.originalSync = false;
                description.global_thumbnail_id = null;
                description.fileType = EnumFileType.NONE.ordinal();
                description.originalName = currentTime;
                description.title = currentTime;
                description.thumbnailName = "thumbnail_" + currentTime;
                description.size = "0";
                description.statusProgress = EnumStatusProgress.NONE.ordinal();
                description.isDeleteLocal = false;
                description.isDeleteGlobal = false;
                description.deleteAction = EnumDelete.NONE.ordinal();
                description.isFakePin = isFakePin;


                Items items = new Items(false,
                        false,
                        description.originalSync,
                        description.thumbnailSync,
                        description.degrees,
                        description.fileType,
                        description.formatType,
                        description.title,
                        description.originalName,
                        description.thumbnailName,
                        description.items_id,
                        description.originalPath,
                        description.thumbnailPath,
                        description.local_id,
                        description.global_original_id,
                        description.global_thumbnail_id,
                        description.categories_id = categories_id,
                        description.categories_local_id,
                        description.mimeType,
                        description.fileExtension,
                        DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                        EnumStatus.UPLOAD,
                        description.size,
                        description.statusProgress,
                        description.isDeleteLocal,
                        description.isDeleteGlobal,
                        description.deleteAction,
                        description.isFakePin);

                boolean createdThumbnail = storage.createFile(thumbnailPath, mBitmap);
                boolean createdOriginal = storage.createFile(originalPath, data);


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
                final ResponseRXJava response = new ResponseRXJava();
                response.isWorking = false;
                subscriber.onNext(response);
                subscriber.onComplete();
                onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
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
                                final DriveDescription driveDescription = DriveDescription.getInstance().hexToObject(mItem.description);
                                mb = (long) +storage.getSize(new File(mItem.originalPath), SizeUnit.B);
                                if (storage.isFileExist(mItem.thumbnailPath)) {
                                    mb += (long) +storage.getSize(new File(mItem.thumbnailPath), SizeUnit.B);
                                }
                                driveDescription.size = "" + mb;
                                mItem.size = driveDescription.size;
                                mItem.description = DriveDescription.getInstance().convertToHex(new Gson().toJson(driveDescription));
                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mItem);

                                if (!mResponse.categories.isCustom_Cover){
                                    final MainCategories main = mResponse.categories;
                                    main.item = new Gson().toJson(mItem);
                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
                                    Utils.Log(TAG,"Special main categories "+ new Gson().toJson(main));
                                }

                            }
                        }
                        Utils.Log(TAG, "Insert Successful");
                        // Utils.Log(TAG, new Gson().toJson(mItem));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mResponse.isWorking){
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

    public void onExportFiles(final File input, final File output, final List<Integer>mListExport) {
        subscriptions = Observable.create(subscriber -> {
            final File mInput = input;
            final File mOutPut = output;
            try {
                storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                final Cipher mCipher = storage.getCipher(Cipher.DECRYPT_MODE);
                storage.createLargeFile(mOutPut,mInput,mCipher, new OnStorageListener() {
                    @Override
                    public void onSuccessful() {
                        Utils.Log(TAG,"Exporting successful");
                        subscriber.onNext(true);
                        subscriber.onComplete();
                    }
                    @Override
                    public void onFailed() {
                        subscriber.onNext(false);
                        subscriber.onComplete();
                        Utils.Log(TAG,"Exporting failed");
                    }
                });
            } catch (Exception e) {
                subscriber.onNext(false);
                subscriber.onComplete();
                Log.w(TAG, "Cannot write to " + e);
            } finally {
                Utils.Log(TAG, "Finally");
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    if (mListExport.size()>0){
                        mListExport.remove(0);
                    }
                    GalleryCameraMediaManager.getInstance().onStopProgress();
                });
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
                    onWriteLog(message, EnumStatus.UPLOAD);
                    onWriteLog(messageDone, EnumStatus.UPLOAD);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);
                    Utils.Log(TAG, "Request syn data on upload.........");
                    onWriteLog("Request syn data on upload", EnumStatus.UPLOAD);
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();

                } else {
                    String message = "Completed upload count syn data...................uploaded " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.UPLOAD);
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
                    onWriteLog(message, EnumStatus.DOWNLOAD);
                    onWriteLog(messageDone, EnumStatus.DOWNLOAD);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    onWriteLog("Request syn data on download", EnumStatus.DOWNLOAD);
                    Utils.Log(TAG, "Request syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();

                } else {
                    String message = "Completed download count syn data...................downloaded " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.DOWNLOAD);
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
                    onWriteLog(message, EnumStatus.DELETE_SYNC_OWN_DATA);
                    onWriteLog(messageDone, EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    onWriteLog("Request own syn data on download", EnumStatus.DELETE_SYNC_OWN_DATA);
                    Utils.Log(TAG, "Request own syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();

                } else {
                    String message = "Completed delete count syn data...................deleted " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.DELETE_SYNC_OWN_DATA);
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
                    onWriteLog(message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    onWriteLog(messageDone, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    onWriteLog("Request cloud syn data on download", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    Utils.Log(TAG, "Request cloud syn data on download.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    onGetDriveAbout();
                    //onDeleteOnLocal();
                } else {
                    String message = "Completed delete count syn data...................deleted " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
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
                    onWriteLog(message, EnumStatus.DELETE_CATEGORIES);
                    onWriteLog(messageDone, EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, message);
                    Utils.Log(TAG, messageDone);

                    onWriteLog("Request cloud syn data on album", EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, "Request cloud syn data on album.........");
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    onGetDriveAbout();
                    //onDeleteOnLocal();
                } else {
                    String message = "Completed delete count album...................deleted " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.DELETE_CATEGORIES);
                    Utils.Log(TAG, message);
                }
                break;
            }
        }
    }

    public void onWriteLog(String message, EnumStatus status) {
        Utils.mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSupersafeLog(), "----Time----" + Utils.getCurrentDateTimeFormat() + " ----Status---- :" + status.name() + " ----Content--- :" + message, true);
    }

    public void onDismissServices() {
        if (isDownloadData || isUploadData) {
            Utils.Log(TAG, "Progress download is :" + isDownloadData);
            Utils.Log(TAG, "Progress upload is :" + isUploadData);
        } else {
            onStopService();
            SingletonPremiumTimer.getInstance().onStop();
            if (myService != null) {
                myService.unbindView();
            }
            if (subscriptions != null) {
                subscriptions.dispose();
            }
        }
        Utils.Log(TAG, "Dismiss Service manager");
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
        return null;
    }

    @Override
    public Activity getActivity() {
        return null;
    }


    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case SCREEN_OFF: {
                int  value = PrefsController.getInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action){
                    case NONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_PRESS_HOME.ordinal());
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing to do");
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
                ServiceManager.getInstance().onSyncDataOwnServer("0");
                ServiceManager.getInstance().onCheckingMissData();
                ServiceManager.getInstance().onGetUserInfo();
                PremiumManager.getInstance().onStartInAppPurchase();
                break;
            }
            case USER_INFO: {

                final boolean isPremiumComplimentary  = User.getInstance().isPremiumComplimentary();
                if (!isPremiumComplimentary){
                    return;
                }

                SingletonPremiumTimer.getInstance().onStop();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SingletonPremiumTimer.getInstance().onStartTimer();
                    }
                }, 5000);
                Utils.Log(TAG, "Get info successful");
                break;
            }
        }
    }

    public interface ServiceManagerSyncDataListener {
        void onCompleted();

        void onError();

        void onCancel();
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
