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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
                    Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error content "+message+" Status "+status.ordinal(),true);
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
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
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

                                if (!isDownloadData){
                                    SingletonManagerTab.getInstance().onAction(EnumStatus.DOWNLOAD);
                                }

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
                                        path = SuperSafeApplication.getInstance().getSupersafePrivate();
                                        pathFolder = path + itemObject.local_id + "/";
                                        request.path_folder_output = pathFolder;
                                        myService.onDownloadFile(request, new ServiceManager.DownloadServiceListener() {
                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
                                                Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error content "+message+" Status "+EnumStatus.DOWNLOAD,true);
                                                Utils.Log(TAG, "onError: !!! "+message);
                                            }

                                            @Override
                                            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                                onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
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
                                                    onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
                                                    Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error content "+e.getMessage()+" Status "+EnumStatus.DOWNLOAD,true);
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
                                    else{
                                        Utils.Log(TAG, "Original already downloaded");
                                    }

                                    if (!itemsThumbnail.thumbnailSync) {
                                        Utils.Log(TAG, "Downloading thumbnail data !!!");
                                        final DownloadFileRequest req = new DownloadFileRequest();
                                        req.items = itemsThumbnail;
                                        req.api_name = String.format(getString(R.string.url_drive_download), itemsThumbnail.global_thumbnail_id);
                                        req.file_name = itemsThumbnail.thumbnailName;
                                        req.Authorization = mUser.access_token;
                                        path = SuperSafeApplication.getInstance().getSupersafePrivate();
                                        pathFolder = path + itemsThumbnail.local_id + "/";
                                        req.path_folder_output = pathFolder;

                                        myService.onDownloadThumbnailFile(req, new ServiceManager.DownloadServiceListener() {

                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
                                                Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+message+" Status "+EnumStatus.DOWNLOAD,true);
                                                Utils.Log(TAG, "onError: "+message);
                                            }

                                            @Override
                                            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                                onUpdateSyncDataStatus(EnumStatus.DOWNLOAD);
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
                                                    Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.DOWNLOAD,true);
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
                                    Utils.Log(TAG, "Thumbnail already downloaded");
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
            SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
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
                                if (!isUploadData){
                                    SingletonManagerTab.getInstance().onAction(EnumStatus.UPLOAD);
                                }

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
                                                    Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.UPLOAD,true);
                                                }
                                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                            }

                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                Utils.Log(TAG,"onError: " +message);
                                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                                Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+message+" Status "+EnumStatus.UPLOAD,true);
                                            }
                                        });
                                    }
                                    else{
                                        Utils.Log(TAG, "Original already uploaded");
                                    }

                                    if (!itemObject.thumbnailSync) {
                                        Utils.Log(TAG, "Uploading thumbnail data !!!");
                                        myService.onUploadThumbnailFileInAppFolder(itemObject, new UploadServiceListener() {
                                            @Override
                                            public void onError(String message, EnumStatus status) {
                                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                                Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"time " +Utils.getCurrentDateTime()+" Error Content "+message+" Status "+EnumStatus.UPLOAD,true);
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
                                                    Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.UPLOAD,true);
                                                    e.printStackTrace();
                                                }
                                                onUpdateSyncDataStatus(EnumStatus.UPLOAD);
                                            }
                                        });
                                    } else {
                                        Utils.Log(TAG, "Thumbnail already uploaded");
                                    }
                                }
                                else{
                                    onUpdateSyncDataStatus(EnumStatus.UPLOAD);
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


    public void onUpdateSyncDataStatus(EnumStatus enumStatus) {
        switch (enumStatus) {
            case UPLOAD:
                countSyncData += 1;
                    if (countSyncData == totalList) {
                        SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
                        isUploadData = false;
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Message :Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList+" Status "+EnumStatus.DOWNLOAD,true);
                        Utils.Log(TAG, "Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList);
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+"Completed upload sync data.......................^^...........^^.......^^........"+" Status "+EnumStatus.DOWNLOAD,true);
                        Utils.Log(TAG, "Completed upload sync data.......................^^...........^^.......^^........");
                    } else {
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Message :Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList+" Status "+EnumStatus.DOWNLOAD,true);
                        Utils.Log(TAG, "Completed upload count syn data...................uploaded " + countSyncData+"/"+totalList);
                    }
                break;
            case DOWNLOAD:
                countSyncData += 1;
                    if (countSyncData == totalList) {
                        SingletonManagerTab.getInstance().onAction(EnumStatus.DONE);
                        isDownloadData = false;
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+"Completed download count syn data...................downloaded " + countSyncData+"/"+totalList+" Status "+EnumStatus.DOWNLOAD,true);
                        Utils.Log(TAG, "Completed download count syn data...................downloaded " + countSyncData+"/"+totalList);
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+"Completed download sync data.......................^^...........^^.......^^........"+" Status "+EnumStatus.DOWNLOAD,true);
                        Utils.Log(TAG, "Completed download sync data.......................^^...........^^.......^^........");
                    } else {
                        Utils.Log(TAG, "Completed download count syn data...................downloaded " + countSyncData+"/"+totalList);
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+"Completed download count syn data...................downloaded " + countSyncData+"/"+totalList+" Status "+EnumStatus.DOWNLOAD,true);
                    }
                break;
        }
    }


    /*Gallery action*/

    public void onSaveDataOnGallery(final MimeTypeFile mimeTypeFile, final String path, String id){
        if (myService==null){
            Utils.Log(TAG,"Service is null");
            return;
        }
        subscriptions = Observable.create(subscriber -> {
            final MimeTypeFile mMimeTypeFile = mimeTypeFile;
            final EnumFormatType enumTypeFile  = mMimeTypeFile.formatType;
            final String mPath = path;
            final String mMimeType  = mMimeTypeFile.mimeType;
            final String mVideo_id = id;
            final Items items ;
            Utils.Log(TAG,"object "+ new Gson().toJson(mMimeTypeFile));
            Bitmap thumbnail = null;
            switch (enumTypeFile){
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
                        description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                        description.localCategories_Name = MainCategories.getInstance().intent_localCategories_Name;
                        description.local_id = uuId;
                        description.global_original_id = null;
                        description.mimeType = mMimeType;
                        description.thumbnailName = currentTime;
                        description.globalName = uuId;
                        description.formatType = EnumFormatType.IMAGE.ordinal();
                        description.degrees = 0;
                        description.thumbnailSync = false;
                        description.originalSync = false;
                        description.global_thumbnail_id = null;
                        description.fileType = EnumFileType.NONE.ordinal();
                        description.originalName = currentTime;
                        description.title = mMimeTypeFile.name;
                        description.thumbnailName = "thumbnail_" + currentTime;

                        items = new Items(false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.globalName,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.mimeType,
                                description.fileExtension,
                                new Gson().toJson(description),
                                EnumStatus.UPLOAD);

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
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.WRITE_FILE,true);
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
                        description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                        description.localCategories_Name = MainCategories.getInstance().intent_localCategories_Name;
                        description.local_id = uuId;
                        description.global_original_id = null;
                        description.mimeType = mMimeType;
                        description.globalName = uuId;
                        description.formatType = EnumFormatType.VIDEO.ordinal();
                        description.degrees = 0;
                        description.thumbnailSync = false;
                        description.originalSync = false;
                        description.global_thumbnail_id = null;
                        description.fileType = EnumFileType.NONE.ordinal();
                        description.originalName = currentTime;
                        description.title = mMimeTypeFile.name;
                        description.thumbnailName = "thumbnail_" + currentTime;


                        items = new Items(false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.globalName,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.mimeType,
                                description.fileExtension,
                                new Gson().toJson(description),
                                EnumStatus.UPLOAD);


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
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.WRITE_FILE,true);
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
                        description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                        description.localCategories_Name = MainCategories.getInstance().intent_localCategories_Name;
                        description.local_id = uuId;
                        description.mimeType = mMimeType;
                        description.globalName = uuId;
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

                        items = new Items(false,
                                description.originalSync,
                                description.thumbnailSync,
                                description.degrees,
                                description.fileType,
                                description.formatType,
                                description.title,
                                description.originalName,
                                description.thumbnailName,
                                description.globalName,
                                description.originalPath,
                                description.thumbnailPath,
                                description.local_id,
                                description.global_original_id,
                                description.global_thumbnail_id,
                                description.localCategories_Id,
                                description.localCategories_Name,
                                description.mimeType,
                                description.fileExtension,
                                new Gson().toJson(description),
                                EnumStatus.UPLOAD);

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
                        Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.WRITE_FILE,true);
                        subscriber.onNext(null);
                        subscriber.onComplete();
                    } finally {
                        Utils.Log(TAG, "Finally");
                    }
                    break;
                }
            }
            Utils.Log(TAG,"End up RXJava");
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final Items items = (Items) response;
                    if (items!=null){
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
                        Utils.Log(TAG,"Write file successful ");
                    }else{
                        Utils.Log(TAG,"Write file Failed ");
                    }
                    Utils.Log(TAG,new Gson().toJson(items));
                    GalleryCameraMediaManager.getInstance().onUpdatedView();
                    SingletonPrivateFragment.getInstance().onUpdateView();
                });
    }




    /*--------------Camera action-----------------*/

    public void onSaveDataOnCamera(final byte[]mData){

        if (myService==null){
            GalleryCameraMediaManager.getInstance().setProgressing(false);
            Utils.Log(TAG,"Service is null");
        }
        subscriptions = Observable.create(subscriber -> {
            File thumbnail = null;
            final byte[]data = mData;
            final Bitmap mBitmap;
            try {
                mBitmap = Utils.getThumbnailScale(data);
                String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
                String currentTime = Utils.getCurrentDateTime();
                String uuId = Utils.getUUId();
                String pathContent = rootPath + uuId+"/";
                storage.createDirectory(pathContent);
                String thumbnailPath = pathContent+"thumbnail_"+currentTime;
                String originalPath = pathContent+currentTime;

                DriveDescription description = new DriveDescription();
                description.fileExtension = getString(R.string.key_jpg);
                description.originalPath = originalPath;
                description.thumbnailPath = thumbnailPath;
                description.subFolderName = uuId;
                description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                description.localCategories_Name = MainCategories.getInstance().intent_localCategories_Name;
                description.local_id = uuId;
                description.global_original_id = null;
                description.mimeType = MediaType.JPEG.type()+"/"+MediaType.JPEG.subtype();
                description.thumbnailName = currentTime;
                description.globalName = uuId;
                description.formatType = EnumFormatType.IMAGE.ordinal();
                description.degrees = 0;
                description.thumbnailSync = false;
                description.originalSync = false;
                description.global_thumbnail_id = null;
                description.fileType = EnumFileType.NONE.ordinal();
                description.originalName = currentTime;
                description.title = currentTime;
                description.thumbnailName = "thumbnail_"+currentTime;

                Items items = new Items(false,
                        description.originalSync,
                        description.thumbnailSync,
                        description.degrees,
                        description.fileType,
                        description.formatType,
                        description.title,
                        description.originalName,
                        description.thumbnailName ,
                        description.globalName,
                        description.originalPath ,
                        description.thumbnailPath,
                        description.local_id,
                        description.global_original_id,
                        description.global_thumbnail_id,
                        description.localCategories_Id,
                        description.localCategories_Name,
                        description.mimeType,
                        description.fileExtension,
                        new Gson().toJson(description),
                        EnumStatus.UPLOAD);

                boolean createdThumbnail = storage.createFile(thumbnailPath,mBitmap);
                boolean createdOriginal = storage.createFile(originalPath,data);
                if (createdThumbnail && createdOriginal){
                    subscriber.onNext(items);
                    subscriber.onComplete();
                    Utils.Log(TAG,"CreatedFile successful");
                }
                else{
                    subscriber.onNext(null);
                    subscriber.onComplete();
                    Utils.Log(TAG,"CreatedFile failed");
                }

            } catch (Exception e) {
                subscriber.onNext(null);
                subscriber.onComplete();
                Utils.mCreateAndSaveFileOverride("log.txt",SuperSafeApplication.getInstance().getSupersafeLog(),"Time " +Utils.getCurrentDateTime()+" Error Content "+e.getMessage()+" Status "+EnumStatus.WRITE_FILE,true);
                Log.w(TAG, "Cannot write to " + e);
            } finally {
                Utils.Log(TAG,"Finally");
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    try {
                        final Items mItem = (Items) response;
                        if (mItem!=null){
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mItem);
                        }
                        Utils.Log(TAG,"Insert Successful");
                        Utils.Log(TAG,new Gson().toJson(mItem));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    GalleryCameraMediaManager.getInstance().setProgressing(false);
                    SingletonPrivateFragment.getInstance().onUpdateView();
                });

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
