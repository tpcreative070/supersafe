package co.tpcreative.supersafe.common.controller;

import android.accounts.Account;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeService;
import co.tpcreative.supersafe.common.services.SuperSafeServiceView;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveTitle;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
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


    public boolean isDownloadData() {
        return isDownloadData;
    }

    public void setDownloadData(boolean downloadData) {
        isDownloadData = downloadData;

    }

    private boolean isDownloadData;
    private boolean isLoadingData;
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

    public void onPickUpNewEmail(Activity context, String account) {
        try {
            Account mAccount = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String value = String.format(SuperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(mAccount, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public interface ServiceManagerSyncDataListener {
        void onCompleted();

        void onError();

        void onCancel();
    }

    /*Response Network*/

    public void onGetDriveAbout() {
        if (myService != null) {
            myService.getDriveAbout();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }



    public void onSyncData() {
        Utils.Log(TAG,"Preparing sync data ###########################");
        if (isDownloadData) {
            Utils.Log(TAG, "List items is downloading...--------------*******************************-----------");
            return;
        }
        if (isUploadData) {
            Utils.Log(TAG, "List items is uploading...----------------*******************************-----------");
            return;
        }
        if (isLoadingData){
            Utils.Log(TAG, "List items is loading...----------------*******************************-----------");
            return;
        }

        if (myService != null) {
            isLoadingData = true;
            myService.onGetListOnlyFilesInApp(new SuperSafeServiceView() {
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG, "Not Found Items :" + message);
                    isLoadingData = false;
                }

                @Override
                public void onSuccessful(String message) {
                    isLoadingData = false;
                    Utils.Log(TAG,"access_token :"+ message);
                }

                @Override
                public void onSuccessful(List<DriveResponse> lists) {
                    isLoadingData = false;
                    final List<Items> items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncUploadDataItems();
                    if (items != null) {
                        if (items.size() > 0) {
                            Utils.Log(TAG,"Preparing uploading...");
                            onUploadDataToStore();
                        } else {
                            Utils.Log(TAG,"Preparing downloading...");
                            onDownloadFilesFromDriveStore();
                        }
                    } else {
                        Utils.Log(TAG,"Preparing downloading...!!!");
                        onDownloadFilesFromDriveStore();
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
            });
        } else {
            Utils.Log(TAG, "My services is null");
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
        if (list == null) {
            Utils.Log(TAG, "No Found data from Cloud!!!");
            return;
        }

        final List<Items>mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;
        for (Items index : list){
            if (!index.originalSync){
                if (!index.thumbnailSync){
                    mList.add(index);
                    totalList+=2;
                }
                else{
                    mList.add(index);
                    totalList+=1;
                }
            }
            else{
                if (!index.thumbnailSync){
                    mList.add(index);
                    totalList+=1;
                }
            }
        }


        if (mList.size()==0) {
            Utils.Log(TAG, "Data items already downloaded from Cloud !!!");
            return;
        }
        else{
            Utils.Log(TAG, "Preparing download "+ totalList+" items from Cloud");
        }


        if (myService != null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser != null) {
                if (mUser.driveConnected) {
                   subscriptions = Observable.fromIterable(mList)
                            .concatMap(i -> Observable.just(i).delay(10000, TimeUnit.MILLISECONDS))
                            .doOnNext(i -> {
                                isDownloadData = true;
                                /*Do something here*/
                                final Items itemObject = i;
                                final Items itemsThumbnail = i;
                                boolean isWorking = true;

                                if (itemObject.localCategories_Id == null) {
                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                                    isWorking = false;
                                }

                                if (myService==null){
                                    isWorking = false;
                                }


                                if (isWorking) {
                                    String path ;
                                    String pathFolder;
                                    if (!itemObject.originalSync) {
                                        Utils.Log(TAG, "Downloading original data !!!");
                                        final DownloadFileRequest request = new DownloadFileRequest();
                                        request.items = itemObject;
                                        request.api_name = String.format(getString(R.string.url_drive_download), itemObject.global_original_id);
                                        request.file_name = itemObject.originalName;
                                        request.Authorization = mUser.access_token;
                                        path = SuperSafeApplication.getInstance().getSuperSafe();
                                        pathFolder = path + itemObject.local_id + "/";
                                        request.path_folder_output = pathFolder;
                                        myService.onDownloadFile(request, new ServiceManager.DownloadServiceListener() {

                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                                Utils.Log(TAG, "onError: "+message);
                                            }

                                            @Override
                                            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                                onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                                Log.d(TAG, "Downloaded id original");
                                                try {
                                                    if (request != null) {
                                                        if (request.items != null) {
                                                            final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(request.items.id);
                                                            if (mItem != null) {
                                                                mItem.statusAction = EnumStatus.DOWNLOAD.ordinal();
                                                                mItem.originalSync = true;
                                                                Log.d(TAG, "Downloaded id original.........................................: global id " + mItem.global_original_id);
                                                                if (mItem.thumbnailSync) {
                                                                    mItem.isSync = true;
                                                                } else {
                                                                    mItem.isSync = false;
                                                                }
                                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                            } else {
                                                                Utils.Log(TAG, "Failed original Save 3");
                                                            }
                                                        }
                                                    } else {
                                                        Utils.Log(TAG, "Failed Save 1");
                                                    }
                                                } catch (Exception e) {
                                                    onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                                    e.printStackTrace();
                                                }
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

                                    if (!itemsThumbnail.thumbnailSync) {
                                        Utils.Log(TAG, "Downloading thumbnail data !!!");
                                        final DownloadFileRequest req = new DownloadFileRequest();
                                        req.items = itemsThumbnail;
                                        req.api_name = String.format(getString(R.string.url_drive_download), itemsThumbnail.global_thumbnail_id);
                                        req.file_name = itemsThumbnail.thumbnailName;
                                        req.Authorization = mUser.access_token;
                                        path = SuperSafeApplication.getInstance().getSuperSafe();
                                        pathFolder = path + itemsThumbnail.local_id + "/";
                                        req.path_folder_output = pathFolder;

                                        myService.onDownloadThumbnailFile(req, new ServiceManager.DownloadServiceListener() {

                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                                Utils.Log(TAG, "onError: "+message);
                                            }

                                            @Override
                                            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                                onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                                Log.d(TAG, "Downloaded id thumbnail");
                                                try {
                                                    if (request != null) {
                                                        if (request.items != null) {
                                                            final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(request.items.id);
                                                            if (mItem != null) {
                                                                mItem.statusAction = EnumStatus.DOWNLOAD.ordinal();
                                                                mItem.thumbnailSync = true;
                                                                Log.d(TAG, "Downloaded id thumbnail.........................................: global id  " + mItem.global_thumbnail_id);
                                                                if (mItem.originalSync) {
                                                                    mItem.isSync = true;
                                                                } else {
                                                                    mItem.isSync = false;
                                                                }
                                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                            } else {
                                                                Utils.Log(TAG, "Failed thumbnail Save 3");
                                                            }
                                                        }
                                                    } else {
                                                        Utils.Log(TAG, "Failed Save 1");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
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

                                } else {
                                    onUpdateSyncDataStatus(mList, EnumStatus.DOWNLOAD);
                                    Utils.Log(TAG, "Not Working");
                                }
                            })
                            .doOnComplete(() -> {
                                //Log.d(TAG, "Completed download^^^^^^^^^^^^^^^^^^^^^666^^^^^^^^^^^^^^^^^^");
                            })
                            .subscribe();
                } else {
                    Utils.Log(TAG, "Drive api not ready");
                }
            } else {
                Utils.Log(TAG, "User not ready");
            }
        } else {
            Utils.Log(TAG, "My services is null");
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
        if (list == null) {
            Utils.Log(TAG, "No Found data from device !!!");
            return;
        }

        final List<Items>mList = new ArrayList<>();
        totalList = 0;
        countSyncData = 0;
        for (Items index : list){
            if (!index.originalSync){
                if (!index.thumbnailSync){
                    mList.add(index);
                    totalList+=2;
                }
                else{
                    mList.add(index);
                    totalList+=1;
                }
            }
            else{
                if (!index.thumbnailSync){
                    mList.add(index);
                    totalList+=1;
                }
            }
        }

        if (mList.size()==0) {
            Utils.Log(TAG, "Data items already uploaded to Cloud !!!");
            return;
        }
        else{
            Utils.Log(TAG, "Preparing upload "+ totalList+" items to Cloud");
        }


        if (myService != null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser != null) {
                if (mUser.driveConnected) {

                    subscriptions = Observable.fromIterable(mList)
                            .concatMap(i -> Observable.just(i).delay(10000, TimeUnit.MILLISECONDS))
                            .doOnNext(i -> {
                                /*Do something here*/
                                isUploadData = true;
                                final Items itemObject = i;
                                boolean isWorking = true;

                                if (itemObject.localCategories_Id == null) {
                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(itemObject);
                                    isWorking = false;
                                }

                                if (myService==null){
                                    isWorking = false;
                                }

                                if (isWorking) {
                                    if (!itemObject.originalSync) {
                                        Utils.Log(TAG, "Uploading original data !!!");
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

                                                //Utils.Log(TAG,"onResponseData global item..."+ new Gson().toJson(response));
                                                //Utils.Log(TAG,"onResponseData local item..."+ new Gson().toJson(items));

                                                try {
                                                    if (response != null) {
                                                        if (response.id != null) {
                                                            String result = Utils.hexToString(response.name);
                                                            DriveTitle contentTitle = new Gson().fromJson(result, DriveTitle.class);
                                                            final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(contentTitle.globalName);
                                                            if (mItem != null) {
                                                                mItem.global_original_id = response.id;
                                                                mItem.fileType = contentTitle.fileType;
                                                                mItem.originalSync = true;
                                                                if (mItem.thumbnailSync) {
                                                                    mItem.isSync = true;
                                                                } else {
                                                                    mItem.isSync = false;
                                                                }
                                                                Log.d(TAG, "Uploaded for original.........................................: global id  " + response.id + " - local id " + mItem.id);
                                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                            } else {
                                                                Utils.Log(TAG, "Failed original Save 3");
                                                            }
                                                        } else {
                                                            Utils.Log(TAG, "Failed Save 2");
                                                        }
                                                    } else {
                                                        Utils.Log(TAG, "Failed Save 1");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                            }

                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                Utils.Log(TAG,"onError: " +message);
                                                onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                            }
                                        });
                                    }

                                    if (!itemObject.thumbnailSync) {
                                        Utils.Log(TAG, "Uploading thumbnail data !!!");
                                        myService.onUploadThumbnailFileInAppFolder(itemObject, new UploadServiceListener() {
                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                                Utils.Log(TAG, "onError: "+ message);
                                            }

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
                                                //Utils.Log(TAG,"onResponseData global item..."+ new Gson().toJson(response));
                                                //Utils.Log(TAG,"onResponseData local item..."+ new Gson().toJson(items));
                                                try {
                                                    if (response != null) {
                                                        if (response.id != null) {
                                                            String result = Utils.hexToString(response.name);
                                                            DriveTitle contentTitle = new Gson().fromJson(result, DriveTitle.class);
                                                            final Items mItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(contentTitle.globalName);
                                                            if (mItem != null) {
                                                                mItem.global_thumbnail_id = response.id;
                                                                mItem.thumbnailSync = true;
                                                                if (mItem.originalSync) {
                                                                    mItem.isSync = true;
                                                                } else {
                                                                    mItem.isSync = false;
                                                                }
                                                                Log.d(TAG, "Uploaded for thumbnail.........................................: global id  " + response.id + " - local id " + mItem.id);
                                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                                                            } else {
                                                                Utils.Log(TAG, "Failed thumbnail Save 3");
                                                            }
                                                        } else {
                                                            Utils.Log(TAG, "Failed Save 2");
                                                        }
                                                    } else {
                                                        Utils.Log(TAG, "Failed Save 1");
                                                    }
                                                } catch (Exception e) {
                                                    Utils.Log(TAG, "Exception");
                                                    e.printStackTrace();
                                                }
                                                onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                            }
                                        });
                                    } else {
                                        onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                        Utils.Log(TAG, "Not Working");
                                    }
                                }
                                else{
                                    onUpdateSyncDataStatus(mList, EnumStatus.UPLOAD);
                                }
                            })
                            .doOnComplete(() -> {
                                //Log.d(TAG, "Completed upload^^^^^^^^^^^^^^^^^^^^^666^^^^^^^^^^^^^^^^^^");
                            })
                            .subscribe();
                } else {
                    Utils.Log(TAG, "Drive api not ready");
                }
            } else {
                Utils.Log(TAG, "User not ready");
            }
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    public void onUpdateSyncDataStatus(final List<Items> list, EnumStatus enumStatus) {
        switch (enumStatus) {
            case UPLOAD:
                countSyncData += 1;
                if (list != null) {
                    if (countSyncData == totalList) {
                        isUploadData = false;
                        Utils.Log(TAG, "Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList);
                        Utils.Log(TAG, "Completed upload sync data.......................^^...........^^.......^^........");
                    } else {
                        Utils.Log(TAG, "Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList);
                    }
                }
                break;
            case DOWNLOAD:
                countSyncData += 1;
                if (list != null) {
                    if (countSyncData == totalList) {
                        isDownloadData = false;
                        Utils.Log(TAG, "Completed download count syn data...................downloaded " + countSyncData+"/"+totalList);
                        Utils.Log(TAG, "Completed download sync data.......................^^...........^^.......^^........");
                    } else {
                        Utils.Log(TAG, "Completed download count syn data...................downloaded " + countSyncData+"/"+totalList);
                    }
                }
                break;
        }

    }


    public void onDismissServices() {
        if (isDownloadData || isUploadData){
          Utils.Log(TAG,"Progress download is :" + isDownloadData);
          Utils.Log(TAG,"Progress upload is :" + isUploadData);
        }
        else{
            onStopService();
            if (myService != null) {
                myService.unbindView();
            }
            if (subscriptions!=null){
                subscriptions.dispose();
            }
        }
        Utils.Log(TAG,"Dismiss Service manager");
    }


    private Observable<Integer> getObservableItems() {
        return Observable.create(subscriber -> {
            for (int i = 0;i<10;i++) {
                subscriber.onNext(i);
            }
            subscriber.onComplete();
        });
    }

    public void getObservable(){
        getObservableItems().
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<Integer>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {

                    }
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Integer pojoObject) {
                        // Show Progress
                    }
                });
    }




    @Override
    public void onError(String message, EnumStatus status) {
        final User mUser = User.getInstance().getUserInfo();
        if (mUser != null) {
            mUser.isInitMainCategoriesProgressing = false;
            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
        }
        Log.d(TAG, "onError response :" + message);
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
        onSyncData();
    }

    @Override
    public void onSuccessful(List<DriveResponse> lists) {

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
