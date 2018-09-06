package co.tpcreative.suppersafe.common.controller;
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.services.SupperSafeService;
import co.tpcreative.suppersafe.common.services.SupperSafeServiceView;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.DriveDescription;
import co.tpcreative.suppersafe.model.EnumStatus;
import co.tpcreative.suppersafe.model.Items;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.model.room.InstanceGenerator;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;
import io.reactivex.Observable;


public class ServiceManager implements SupperSafeServiceView{

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private SupperSafeService myService;
    private Context mContext;
    private boolean isUploadData;

    public boolean isDownloadData() {
        return isDownloadData;
    }

    public void setDownloadData(boolean downloadData) {
        isDownloadData = downloadData;
    }

    private boolean isDownloadData;
    private int countSyncData  = 0;

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
            myService = ((SupperSafeService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
        }
        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

    private void doBindService() {
        Intent intent = null;
        intent = new Intent(mContext, SupperSafeService.class);
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

    public SupperSafeService getMyService() {
        return myService;
    }

    protected void showMessage(String message) {
        Toast.makeText(SupperSafeApplication.getInstance(), message, Toast.LENGTH_LONG).show();
    }

    private String getString(int res){
        String value = SupperSafeApplication.getInstance().getString(res);
        return value;
    }

    public void onPickUpNewEmailNoTitle(Activity context,String account){
        try {
            Account account1 = new Account(account,GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
            intent.putExtra("overrideTheme", 1);
          //  intent.putExtra("selectedAccount",account);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpExistingEmail(Activity context,String account){
        try {
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_account),account);
            Account account1 = new Account(account,GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountPicker.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context){
        try {
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onPickUpNewEmail(Activity context,String account){
        try {
            Account mAccount = new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE );
            String value = String.format(SupperSafeApplication.getInstance().getString(R.string.choose_an_new_account));
            Intent intent = AccountPicker.newChooseAccountIntent(mAccount, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, VerifyAccountActivity.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onAddPermission(final Activity activity, ServiceManagerAskPermissionListener ls, Collection<String> listPermission) {
        Dexter.withActivity(activity)
                .withPermissions(listPermission)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            ls.onGrantedPermission();
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                            ls.onError();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                            ls.onError();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    public interface ServiceManagerAskPermissionListener{
        void onGrantedPermission();
        void onError();
    }

    public interface ServiceManagerSyncDataListener {
        void onCompleted();
        void onError();
        void onCancel();
    }

    /*Response Network*/

    public void onGetDriveAbout(){
        if (myService!=null){
            myService.getDriveAbout();
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }


    /*


    public void onGetListFileInApp(){
        if (myService!=null){
            myService.onGetListFolderInApp();
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    */

    /*

    public void onInitMainCategories(){

        final User user = User.getInstance().getUserInfo();

        if (user!=null){

            if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
                Utils.Log(TAG,"Check network connection");
                return;
            }

            if (myService==null){
                Utils.Log(TAG,"My services is null");
                return;
            }

            if (!user.driveConnected){
                return;
            }

            if (user.isInitMainCategoriesProgressing){
                Utils.Log(TAG,"Main categories is progressing");
                return;
            }
            else{
              user.isInitMainCategoriesProgressing = true;
              PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
            }
        }
        else{
            Utils.Log(TAG,"User is null");
            return;
        }

        Utils.Log(TAG,"onInitMainCategories");
        List<MainCategories> mList = MainCategories.getInstance().getMainCategoriesList();
        if (mList==null){
            mList = MainCategories.getInstance().getList();
        }
        if (mList!=null){
            final int total = mList.size()-1;
            for (int i = 0 ; i<mList.size();i++){
                if (total==i){
                    myService.onCheckInAppFolderExisting(mList.get(i).getName(),true);
                }
                else{
                    myService.onCheckInAppFolderExisting(mList.get(i).getName(),false);
                }
            }
        }
    }

    */

    /*

    public void onRefreshData(){
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.driveConnected){
                if (!mUser.isRefresh){
                    onInitMainCategories();
                }
            }
        }
    }

    */

    /*

    public void onUploadFilesToInAppFolder(final File file,String folderId,final String mimeType){
        if (myService!=null){
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                if (mUser.driveConnected && folderId!=null){
                    myService.onUploadFileInAppFolder(file,folderId,mimeType);
                }
                else{
                    onInitMainCategories();
                    Utils.Log(TAG,"Drive api not ready");
                }
            }
            else{
                Utils.Log(TAG,"User not ready");
            }
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    */

    public void onGetListFilesInAppFolder(){
        if (myService!=null){
            myService.onGetListOnlyFilesInApp();
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    public void onSyncData(){
        if (isDownloadData){
            Utils.Log(TAG,"List items is downloading...");
            return;
        }
        if (isUploadData){
            Utils.Log(TAG,"List items is uploading...");
            return;
        }

        if (myService!=null){
            myService.onGetListOnlyFilesInApp(new SupperSafeServiceView() {
                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG,"Get file on error :"+message);
                }

                @Override
                public void onSuccessful(String message) {

                }

                @Override
                public void onSuccessful(List<DriveResponse> lists) {
                    final List<Items> items = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListSyncUploadDataItems();
                    if (items!=null){
                        if (items.size()>0){
                            onUploadDataToStore();
                        }
                        else {
                            onDownloadFilesFromDriveStore();
                        }
                    }
                    else{
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
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }


    public void onDownloadFilesFromDriveStore(){

        if (isDownloadData){
            Utils.Log(TAG,"List items is sync");
            return;
        }

        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            Utils.Log(TAG,"Check network connection");
            return;
        }

        final List<Items> mList = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListSyncDownloadDataItems();
        if (mList==null){
            Utils.Log(TAG,"List items is null");
            return;
        }

        countSyncData = 0;

        if (myService!=null){
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                if (mUser.driveConnected){
                    Observable.fromIterable(mList)
                            .concatMap(i-> Observable.just(i).delay(10000, TimeUnit.MILLISECONDS))
                            .doOnNext(i->{

                                /*Do something here*/
                                final  Items itemObject  = i;

                                isUploadData = true;
                                boolean isWorking = true ;

                                if (itemObject.localCategories_Id==null){
                                    InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onDelete(itemObject);
                                    isWorking = false;
                                }

                                if (itemObject.isSync){
                                    isWorking = false;
                                }

                                Utils.Log(TAG,"Sync Data downloading");
                                final DownloadFileRequest request = new DownloadFileRequest();


                                request.items = itemObject;
                                request.api_name = String.format(getString(R.string.url_drive_download),itemObject.global_id);
                                request.file_name = itemObject.name;
                                request.Authorization = mUser.access_token;
                                String path = SupperSafeApplication.getInstance().getSupperSafe();
                                String pathFolder = path + itemObject.local_id+"/";
                                request.path_folder_output = pathFolder;

                                if (isWorking){
                                    myService.onDownloadFile(request,new ServiceManager.DownloadServiceListener() {

                                        @Override
                                        public void onError(String message) {
                                            onUpdateSyncDataStatus(mList,EnumStatus.DOWNLOAD);
                                            Utils.Log(TAG,"onError");
                                        }

                                        @Override
                                        public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                                            onUpdateSyncDataStatus(mList,EnumStatus.DOWNLOAD);
                                            try {
                                                if (request!=null){
                                                   if (request.items!=null){
                                                       final Items mItem = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getItemId(request.items.id);
                                                       if (mItem!=null){
                                                           mItem.isSync = true;
                                                           mItem.statusAction = EnumStatus.DOWNLOAD.ordinal();
                                                           Log.d(TAG,"Donwload id.........................................:  " + mItem.id);
                                                           InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onUpdate(mItem);
                                                       }
                                                       else{
                                                           Utils.Log(TAG,"Failed Save 3");
                                                       }
                                                   }
                                                }
                                                else{
                                                    Utils.Log(TAG,"Failed Save 1");
                                                }
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onProgressDownload(int percentage) {

                                        }

                                        @Override
                                        public void onSaved() {

                                        }

                                        @Override
                                        public void onFailure() {
                                            onUpdateSyncDataStatus(mList,EnumStatus.DOWNLOAD);
                                            Utils.Log(TAG,"onFailure");
                                        }
                                    });
                                }
                                else{
                                    onUpdateSyncDataStatus(mList,EnumStatus.DOWNLOAD);
                                    Utils.Log(TAG,"Not Working");
                                }
                            })
                            .doOnComplete(() -> {
                                Log.d(TAG,"Completed");
                            })
                            .subscribe();
                }
                else{
                    Utils.Log(TAG,"Drive api not ready");
                }
            }
            else{
                Utils.Log(TAG,"User not ready");
            }
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    public void onUploadDataToStore(){

        if (isUploadData){
            Utils.Log(TAG,"List items is sync");
            return;
        }

        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            Utils.Log(TAG,"Check network connection");
            return;
        }

        final List<Items> mList = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListSyncUploadDataItems();
        if (mList==null){
            Utils.Log(TAG,"List items is null");
            return;
        }

        countSyncData = 0;

        if (myService!=null){
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                if (mUser.driveConnected){
                    Observable.fromIterable(mList)
                            .concatMap(i-> Observable.just(i).delay(10000, TimeUnit.MILLISECONDS))
                            .doOnNext(i->{
                                Utils.Log(TAG,"Sync Data");
                                /*Do something here*/
                                final  Items itemObject  = i;

                                isUploadData = true;
                                boolean isWorking = true ;

                                if (itemObject.localCategories_Id==null){
                                    InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onDelete(itemObject);
                                    isWorking = false;
                                }

                                if (itemObject.isSync){
                                    isWorking = false;
                                }

                                Utils.Log(TAG,"Sync Data !!!");

                                if (isWorking){
                                    myService.onUploadFileInAppFolder(itemObject,new UploadServiceListener() {
                                        @Override
                                        public void onError() {
                                            onUpdateSyncDataStatus(mList,EnumStatus.UPLOAD);
                                            Utils.Log(TAG,"onError");
                                        }
                                        @Override
                                        public void onProgressUpdate(int percentage) {
                                            //Utils.Log(TAG,"onProgressUpdate "+ percentage +"%");
                                        }

                                        @Override
                                        public void onFinish() {
                                            Utils.Log(TAG,"onFinish");
                                        }

                                        @Override
                                        public void onResponseData(DriveResponse response) {
                                            //Utils.Log(TAG,"onResponseData global item..."+ new Gson().toJson(response));
                                            //Utils.Log(TAG,"onResponseData local item..."+ new Gson().toJson(items));
                                            try {
                                                if (response!=null){
                                                    if (response.id!=null){

                                                        final Items mItem = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getItemId(response.name);
                                                        if (mItem!=null){
                                                            mItem.isSync = true;
                                                            mItem.global_id = response.id;
                                                            Log.d(TAG,"Upload id.........................................:  "+ response.id +" - " + mItem.id);
                                                            InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onUpdate(mItem);
                                                        }
                                                        else{
                                                            Utils.Log(TAG,"Failed Save 3");
                                                        }
                                                    }
                                                    else{
                                                        Utils.Log(TAG,"Failed Save 2");
                                                    }
                                                }
                                                else{
                                                    Utils.Log(TAG,"Failed Save 1");
                                                }
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            onUpdateSyncDataStatus(mList,EnumStatus.UPLOAD);
                                        }
                                        @Override
                                        public void onFailure() {
                                            onUpdateSyncDataStatus(mList,EnumStatus.UPLOAD);
                                            Utils.Log(TAG,"onFailure");
                                        }
                                    });
                                }
                                else{
                                    onUpdateSyncDataStatus(mList,EnumStatus.UPLOAD);
                                    Utils.Log(TAG,"Not Working");
                                }
                            })
                            .doOnComplete(() -> {
                                   Log.d(TAG,"Completed");
                                   })
                            .subscribe();
                }
                else{
                    Utils.Log(TAG,"Drive api not ready");
                }
            }
            else{
                Utils.Log(TAG,"User not ready");
            }
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    public void onUpdateSyncDataStatus(final List<Items>list,EnumStatus enumStatus){

        switch (enumStatus){
            case UPLOAD:
                countSyncData+=1;
                if (list!=null){
                    if (countSyncData==list.size()){
                        isUploadData = false;
                        Utils.Log(TAG,"Completed upload sync data.......................");
                    }
                    else{
                        Utils.Log(TAG,"Completed upload count syn data..................." + countSyncData);
                    }
                }
                break;
            case DOWNLOAD:
                countSyncData+=1;
                if (list!=null){
                    if (countSyncData==list.size()){
                        isDownloadData = false;
                        Utils.Log(TAG,"Completed download sync data.......................");
                    }
                    else{
                        Utils.Log(TAG,"Completed download count syn data..................." + countSyncData);
                    }
                }
                break;
        }

    }


    public void onDismissRXJava(){
        if (myService!=null){
            myService.unbindView();
        }
    }

    @Override
    public void onError(String message, EnumStatus status) {
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            mUser.isInitMainCategoriesProgressing = false;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
        }
        Log.d(TAG,"onError response :" +message);
    }

    @Override
    public void onSuccessful(String message) {
        Log.d(TAG,"onSuccessful Response  :" +message);
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
    }

    @Override
    public void onSuccessful(List<DriveResponse> lists) {

    }

    /*Upload Service*/
    public interface UploadServiceListener{
        void onError();
        void onProgressUpdate(int percentage);
        void onFinish();
        void onResponseData(final DriveResponse response);
        void onFailure();
    }

    public interface DownloadServiceListener{
        void onError(String message);
        void onProgressDownload(int percentage);
        void onSaved();
        void onDownLoadCompleted(File file_name, DownloadFileRequest request);
        void onFailure();
    }

}
