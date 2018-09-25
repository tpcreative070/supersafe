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
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.SizeUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeService;
import co.tpcreative.supersafe.common.services.SuperSafeServiceView;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.DriveTitle;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceManager implements SuperSafeServiceView {

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private SuperSafeService myService;
    private Context mContext;
    private boolean isUploadData;
    private Disposable subscriptions;
    private Storage storage = new Storage(SuperSafeApplication.getInstance());
    private Storage mStorage = new Storage(SuperSafeApplication.getInstance());
    private Cipher mCiphers;

    public boolean isDownloadData() {
        return isDownloadData;
    }

    public void setDownloadData(boolean downloadData) {
        isDownloadData = downloadData;
    }

    private boolean isDownloadData;
    private boolean isLoadingData;
    private boolean isDeleteSyncCLoud;
    private boolean isDeleteOwnCloud;
    private int countSyncData = 0;
    private int totalList = 0;

    public boolean isUploadData() {
        return isUploadData;
    }

    public void setUploadData(boolean uploadData) {
        isUploadData = uploadData;
    }

    public int getCountSyncData() {
        return countSyncData;
    }

    public void setCountSyncData(int countSyncData) {
        this.countSyncData = countSyncData;
    }

    public void onPickUpNewEmailNoTitle(Activity context, String account) {
        try {
            Account account1 = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            intent.putExtra("overrideTheme", 1);
            //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT);
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
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
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
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "connected");
            myService = ((SuperSafeService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
            mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        }

        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

    private void doBindService() {
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

    /*Response Network*/

    public void onGetDriveAbout() {
        if (myService != null) {
            myService.getDriveAbout();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    public void onCheckingMissData(String nextPage) {
        Utils.Log(TAG, "Preparing checking miss data ###########################");
        if (myService != null) {
            myService.onCheckingMissData(nextPage, new SuperSafeServiceView() {
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG, "Error :" + message);
                    if (status == EnumStatus.REQUEST_ACCESS_TOKEN) {
                        SingletonManagerTab.getInstance().onRequestAccessToken();
                        Utils.Log(TAG, "Request token on onCheckingMissData");
                    }
                }

                @Override
                public void onSuccessful(String message) {
                    Utils.Log(TAG, "Response :" + message);
                }

                @Override
                public void onSuccessful(List<DriveResponse> lists) {
                    Utils.Log(TAG, "Response !!!:!!!" + new Gson().toJson(lists));
                }

                @Override
                public void onSuccessful(String nextPage, EnumStatus status) {
                    if (status == EnumStatus.LOAD_MORE) {
                        Utils.Log(TAG, "next page on CheckingMissData" + nextPage);
                        onCheckingMissData(nextPage);
                    }
                }

                @Override
                public void onNetworkConnectionChanged(boolean isConnect) {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void startLoading() {

                }

                @Override
                public void stopLoading() {

                }

                @Override
                public void onSuccessfulOnCheck(List<Items> lists) {
                    if (lists.size() > 0) {
                        Utils.Log(TAG, "Found " + lists.size() + " miss");
                        final List<Items> mList = lists;
                        subscriptions = Observable.fromIterable(mList)
                                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                                .doOnNext(i -> {
                                    final Items items = i;
                                    onAddItems(items);
                                })
                                .doOnComplete(() -> {
                                    //Log.d(TAG, "Completed upload^^^^^^^^^^^^^^^^^^^^^666^^^^^^^^^^^^^^^^^^");
                                })
                                .subscribe();
                    } else {
                        Utils.Log(TAG, "No data miss");
                    }
                }
            });
        } else {
            Utils.Log(TAG, "My service is null");
        }
    }


    public void onSyncDataOwnServer(String nextPage) {
        Utils.Log(TAG, "Preparing sync data ###########################");
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

        if (myService != null) {
            isLoadingData = true;
            myService.onGetListSync(nextPage, new SuperSafeServiceView() {
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
                public void onSuccessful(List<DriveResponse> lists) {
                    Utils.Log(TAG, "Response !!!:!!!" + new Gson().toJson(lists));
                }

                @Override
                public void onSuccessful(String nextPage, EnumStatus status) {
                    if (status == EnumStatus.LOAD_MORE) {
                        isLoadingData = false;
                        Utils.Log(TAG, "next page on onSyncDataOwnServer " + nextPage);
                        onSyncDataOwnServer(nextPage);
                    } else if (status == EnumStatus.SYNC_READY) {
                        isLoadingData = false;
                        final List<Items> items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems();

                        final List<Items> mListOwnCloud = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true, true);

                        final List<Items> mListCloud = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true);

//                        if (mListOwnCloud!=null){
//                            onDeleteOnOwnItems();
//                        }
//                        else if (mListCloud!=null){
//                            onDeleteCloud();
//                        }
                        if (items != null) {
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

                @Override
                public void onNetworkConnectionChanged(boolean isConnect) {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void startLoading() {

                }

                @Override
                public void stopLoading() {

                }

                @Override
                public void onSuccessfulOnCheck(List<Items> lists) {

                }
            });
        } else {
            Utils.Log(TAG, "My service is null");
        }
    }

    public void onDeleteCloud() {

        if (myService == null) {
            Utils.Log(TAG, "Service is null on " + EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }

        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true);

        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true);

        if (list == null) {
            Utils.Log(TAG, "No Found data to delete on own items!!!");
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
            onDeleteOnLocal();
            isDeleteSyncCLoud = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            onGetDriveAbout();
            Utils.Log(TAG, "Not Found cloud id to delete");
            return;
        }

        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    final Items mItem = i;
                    myService.onDeleteCloudItems(mItem, mItem.isOriginalGlobalId, new SuperSafeServiceView() {
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
                        public void onSuccessfulOnCheck(List<Items> lists) {

                        }

                        @Override
                        public void onSuccessful(List<DriveResponse> lists) {

                        }

                        @Override
                        public void onNetworkConnectionChanged(boolean isConnect) {

                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void startLoading() {

                        }

                        @Override
                        public void stopLoading() {

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

        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true, true);
        countSyncData = 0;


        isDeleteOwnCloud = true;
        totalList = mList.size();
        if (mList == null) {
            Utils.Log(TAG, "No Found data to delete on own items!!!");
            isDeleteOwnCloud = false;
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            onGetDriveAbout();
            return;
        }

        subscriptions = Observable.fromIterable(mList)
                .concatMap(i -> Observable.just(i).delay(1000, TimeUnit.MILLISECONDS))
                .doOnNext(i -> {
                    final Items mItem = i;
                    myService.onDeleteOwnSystem(mItem, new SuperSafeServiceView() {
                        @Override
                        public void onError(String message, EnumStatus status) {
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);
                            Utils.Log(TAG, message + "--" + status.name());
                        }

                        @Override
                        public void onSuccessful(String message) {

                        }

                        @Override
                        public void onSuccessful(String message, EnumStatus status) {
                            onUpdateSyncDataStatus(EnumStatus.DELETE_SYNC_OWN_DATA);
                            Utils.Log(TAG, message + "--" + status.name());
                        }

                        @Override
                        public void onSuccessfulOnCheck(List<Items> lists) {

                        }

                        @Override
                        public void onSuccessful(List<DriveResponse> lists) {

                        }

                        @Override
                        public void onNetworkConnectionChanged(boolean isConnect) {

                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void startLoading() {

                        }

                        @Override
                        public void stopLoading() {

                        }
                    });
                })
                .doOnComplete(() -> {
                })
                .subscribe();
    }

    public void onDeleteOnLocal() {
        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalAndGlobalListItems(true, true);
        for (Items index : list) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(index);
        }
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

        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems();
        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncDownloadDataItems();

        if (list == null) {
            Utils.Log(TAG, "No Found data from Cloud!!!");
            return;
        }

        final List<Items> mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;

        for (int i = 0; i<list.size();i++) {
            final Items items = list.get(i);
            if (!items.originalSync) {
                final Items item = items;
                item.isOriginalGlobalId = true;
                mList.add(item);
                Utils.Log(TAG, "Adding original :");
            }
        }

        for (int i = 0; i<lists.size();i++) {
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
        SingletonManagerTab.getInstance().onAction(EnumStatus.DOWNLOAD);
        if (mList.size() == 0) {
            Utils.Log(TAG, "Data items already downloaded from Cloud !!!");
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
            isDownloadData = false;
            return;
        } else {
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

                        if (itemObject.localCategories_Id == null) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                            onWriteLog("Delete null at id " + itemObject.id, EnumStatus.DOWNLOAD);
                            Utils.Log(TAG, "localCategories_Id is null at " + itemObject.id);
                            isWorking = false;
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
                                    Utils.Log(TAG, "onError Download: !!! on");
                                }

                                @Override
                                public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                    try {
                                        if (request != null) {
                                            final Items itemsRequest = request.items;
                                            if (itemsRequest != null) {
                                                final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(request.items.items_id);
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
                                                        mItem.isSync = true;
                                                        mItem.statusProgress = EnumStatusProgress.DONE.ordinal();
                                                    } else {
                                                        mItem.isSync = false;
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

        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItems();
        final List<Items> lists = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItems();
        if (list == null) {
            Utils.Log(TAG, "No Found data from device !!!");
            return;
        }

        final List<Items>mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;
        for (int i = 0; i<list.size();i++) {
            final Items items = list.get(i);
            if (!items.originalSync) {
                final Items item = items;
                item.isOriginalGlobalId = true;
                mList.add(item);
            }
        }

        for (int i = 0; i<lists.size();i++) {
            final Items items = lists.get(i);
            if (!items.thumbnailSync) {
                final Items item = items;
                item.isOriginalGlobalId = false;
                mList.add(item);
            }
        }

        totalList = mList.size();
        isUploadData = true;
        SingletonManagerTab.getInstance().onAction(EnumStatus.UPLOAD);
        if (mList.size() == 0) {
            Utils.Log(TAG, "Data items already uploaded to Cloud !!!");
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
            isUploadData = false;
            return;
        } else {
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

                        if (itemObject.localCategories_Id == null) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                            isWorking = false;
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
                                                final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(contentTitle.items_id);
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
                                                        mItem.isSync = true;
                                                        mItem.statusProgress = EnumStatusProgress.DONE.ordinal();
                                                    } else {
                                                        mItem.isSync = false;
                                                    }

                                                    if (mItem.isSync) {
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
        myService.onAddItems(items, new SuperSafeServiceView() {
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + " status " + status.name());
            }

            @Override
            public void onSuccessful(String message) {

            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG, message + " status " + status.name());
            }

            @Override
            public void onSuccessful(List<DriveResponse> lists) {

            }

            @Override
            public void onNetworkConnectionChanged(boolean isConnect) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void startLoading() {

            }

            @Override
            public void stopLoading() {

            }

            @Override
            public void onSuccessfulOnCheck(List<Items> lists) {

            }
        });
    }

    /*Gallery action*/

    public void onSaveDataOnGallery(final MimeTypeFile mimeTypeFile, final String path, String id, MainCategories mainCategories) {
        if (myService == null) {
            Utils.Log(TAG, "Service is null");
            return;
        }
        subscriptions = Observable.create(subscriber -> {

            final MimeTypeFile mMimeTypeFile = mimeTypeFile;
            final EnumFormatType enumTypeFile = mMimeTypeFile.formatType;
            final String mPath = path;
            final String mMimeType = mMimeTypeFile.mimeType;
            final String mVideo_id = id;
            final Items items;
            final MainCategories mMainCategories = mainCategories;
            final String localCategories_Id = mMainCategories.localId;
            final String localCategories_Name = mMainCategories.name;
            final String localCategories_Count = "" + mMainCategories.localCategories_Count;

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
                        description.localCategories_Id = localCategories_Id;
                        description.localCategories_Name = localCategories_Name;
                        description.localCategories_Count = localCategories_Count;
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
                        description.isWaitingSyncDeleteGlobal = false;

                        items = new Items(false,
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
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.localCategories_Count,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.isWaitingSyncDeleteGlobal);

                        Utils.Log(TAG, "start compress");
                        boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                        boolean createdOriginal = storage.createFile(new File(originalPath), new File(mPath), Cipher.ENCRYPT_MODE);
                        Utils.Log(TAG, "start end");


                        if (createdThumbnail && createdOriginal) {
                            subscriber.onNext(items);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            subscriber.onNext(null);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }

                    } catch (Exception e) {
                        Log.w(TAG, "Cannot write to " + e);
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        subscriber.onNext(false);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }

                case VIDEO: {
                    Utils.Log(TAG, "Start RXJava Video Progressing");
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inDither = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        thumbnail = MediaStore.Video.Thumbnails.getThumbnail(SuperSafeApplication.getInstance().getContentResolver(),
                                Long.parseLong(mVideo_id),
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                options);

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
                        description.localCategories_Id = localCategories_Id;
                        description.localCategories_Name = localCategories_Name;
                        description.localCategories_Count = localCategories_Count;
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
                        description.isWaitingSyncDeleteGlobal = false;


                        items = new Items(false,
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
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.localCategories_Count,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.isWaitingSyncDeleteGlobal);


                        boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);

                        if (createdThumbnail && createdOriginal) {
                            subscriber.onNext(items);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            subscriber.onNext(null);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }

                    } catch (Exception e) {
                        Log.w(TAG, "Cannot write to " + e);
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        subscriber.onNext(null);
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
                        description.localCategories_Id = localCategories_Id;
                        description.localCategories_Name = localCategories_Name;
                        description.localCategories_Count = localCategories_Count;
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
                        description.isWaitingSyncDeleteGlobal = false;


                        items = new Items(false,
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
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.localCategories_Count,
                                description.mimeType,
                                description.fileExtension,
                                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                                EnumStatus.UPLOAD,
                                description.size,
                                description.statusProgress,
                                description.isDeleteLocal,
                                description.isDeleteGlobal,
                                description.isWaitingSyncDeleteGlobal);

                        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                        boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);

                        if (createdOriginal) {
                            subscriber.onNext(items);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile successful");
                        } else {
                            subscriber.onNext(null);
                            subscriber.onComplete();
                            Utils.Log(TAG, "CreatedFile failed");
                        }

                    } catch (Exception e) {
                        Log.w(TAG, "Cannot write to " + e);
                        onWriteLog(e.getMessage(), EnumStatus.WRITE_FILE);
                        subscriber.onNext(null);
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
                    final Items items = (Items) response;
                    if (items != null) {
                        long mb;
                        if (storage.isFileExist(items.originalPath)) {
                            final DriveDescription driveDescription = DriveDescription.getInstance().hexToObject(items.description);
                            mb = (long) +storage.getSize(new File(items.originalPath), SizeUnit.B);
                            driveDescription.size = "" + mb;
                            items.size = driveDescription.size;
                            items.description = DriveDescription.getInstance().convertToHex(new Gson().toJson(driveDescription));
                        }
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
                        Utils.Log(TAG, "Write file successful ");
                    } else {
                        Utils.Log(TAG, "Write file Failed ");
                    }
                    // Utils.Log(TAG, new Gson().toJson(items));
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                });
    }


    /*--------------Camera action-----------------*/

    public void onSaveDataOnCamera(final byte[] mData, final MainCategories mainCategories) {

        if (myService == null) {
            GalleryCameraMediaManager.getInstance().setProgressing(false);
            Utils.Log(TAG, "Service is null");
        }
        subscriptions = Observable.create(subscriber -> {

            final MainCategories mMainCategories = mainCategories;
            final String localCategories_Id = mMainCategories.localId;
            final String localCategories_Name = mMainCategories.name;
            final String localCategories_Count = "" + mMainCategories.localCategories_Count;

            File thumbnail = null;
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
                description.localCategories_Id = localCategories_Id;
                description.localCategories_Name = localCategories_Name;
                description.localCategories_Count = localCategories_Count;
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
                description.isWaitingSyncDeleteGlobal = false;

                Items items = new Items(false,
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
                        description.localCategories_Id,
                        description.localCategories_Name,
                        description.localCategories_Count,
                        description.mimeType,
                        description.fileExtension,
                        DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                        EnumStatus.UPLOAD,
                        description.size,
                        description.statusProgress,
                        description.isDeleteLocal,
                        description.isDeleteGlobal,
                        description.isWaitingSyncDeleteGlobal);

                boolean createdThumbnail = storage.createFile(thumbnailPath, mBitmap);
                boolean createdOriginal = storage.createFile(originalPath, data);
                if (createdThumbnail && createdOriginal) {
                    subscriber.onNext(items);
                    subscriber.onComplete();
                    Utils.Log(TAG, "CreatedFile successful");
                } else {
                    subscriber.onNext(null);
                    subscriber.onComplete();
                    Utils.Log(TAG, "CreatedFile failed");
                }

            } catch (Exception e) {
                subscriber.onNext(null);
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
                    try {
                        final Items mItem = (Items) response;
                        if (mItem != null) {
                            long mb;
                            if (storage.isFileExist(mItem.originalPath)) {
                                final DriveDescription driveDescription = DriveDescription.getInstance().hexToObject(mItem.description);
                                mb = (long) +storage.getSize(new File(mItem.originalPath), SizeUnit.B);
                                driveDescription.size = "" + mb;
                                mItem.size = driveDescription.size;
                                mItem.description = DriveDescription.getInstance().convertToHex(new Gson().toJson(driveDescription));
                            }
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mItem);
                        }
                        Utils.Log(TAG, "Insert Successful");
                        // Utils.Log(TAG, new Gson().toJson(mItem));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GalleryCameraMediaManager.getInstance().setProgressing(false);
                    SingletonPrivateFragment.getInstance().onUpdateView();
                    ServiceManager.getInstance().onSyncDataOwnServer("0");
                });

    }

    public void onUpdateSyncDataStatus(EnumStatus enumStatus) {
        switch (enumStatus) {
            case UPLOAD: {
                countSyncData += 1;
                if (countSyncData == totalList) {
                    SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
                    SingletonPrivateFragment.getInstance().onUpdateView();
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
                    onDeleteOnLocal();

                } else {
                    String message = "Completed delete count syn data...................deleted " + countSyncData + "/" + totalList;
                    onWriteLog(message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
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
        Log.d(TAG, "onError response :" + message);
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
    public void onStart() {

    }

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnect) {
        GoogleDriveConnectionManager.getInstance().onNetworkConnectionChanged(isConnect);
        if (isConnect) {
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
    }

    @Override
    public void onSuccessful(List<DriveResponse> lists) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        if (status == EnumStatus.GET_DRIVE_ABOUT) {
            Utils.Log(TAG, "drive about :" + message);
        }
    }

    @Override
    public void onSuccessfulOnCheck(List<Items> lists) {

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

}
