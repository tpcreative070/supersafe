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
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.services.SupperSafeService;
import co.tpcreative.suppersafe.common.services.SupperSafeServiceView;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.EnumStatus;
import co.tpcreative.suppersafe.model.Items;
import co.tpcreative.suppersafe.model.MainCategories;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.model.room.InstanceGenerator;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;
import io.reactivex.Observable;


public class ServiceManager implements SupperSafeServiceView{

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private SupperSafeService myService;
    private Context mContext;
    private boolean isSyncData;
    private int countSyncData  = 0;

    public boolean isSyncData() {
        return isSyncData;
    }

    public void setSyncData(boolean syncData) {
        isSyncData = syncData;
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




    public void onGetListFileInApp(){
        if (myService!=null){
            myService.onGetListFolderInApp();
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

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

    public void onGetListFilesInAppFolder(){
        if (myService!=null){
            myService.onGetListOnlyFilesInApp();
        }
        else{
            Utils.Log(TAG,"My services is null");
        }
    }

    public void onSyncDataToDriveStore(){

        if (isSyncData){
            Utils.Log(TAG,"List items is sync");
            return;
        }

        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            Utils.Log(TAG,"Check network connection");
            return;
        }

        final List<Items> mList = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListSyncDataItems();
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

                                isSyncData = true;

                                boolean isWorking =false ;

                                if (itemObject.localCategories_Id==null){
                                    InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onDelete(itemObject);
                                    Utils.Log(TAG,"Local Id is null");
                                }

                                final String value = checkGlobalId(itemObject.localCategories_Id);

                                if (itemObject.global_id!=null){
                                   isWorking = true;
                                }
                                else{
                                    if (value!=null){
                                        itemObject.globalCategories_Id = value;
                                        isWorking = true;
                                    }
                                    else{
                                        isWorking = false;
                                        onUpdateSyncDataStatus(mList);
                                        Utils.Log(TAG,"Global Id is null");
                                    }
                                }

                                if (itemObject.isSync){
                                    isWorking = false;
                                    onUpdateSyncDataStatus(mList);
                                }

                                Utils.Log(TAG,"Sync Data !!!");

                                if (isWorking){
                                    myService.onUploadFileInAppFolder(itemObject,new UploadServiceListener() {
                                        @Override
                                        public void onError() {
                                            onUpdateSyncDataStatus(mList);
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
                                        public void onResponseData(DriveResponse response, Items items) {
                                            onUpdateSyncDataStatus(mList);
                                            Utils.Log(TAG,"onResponseData global item..."+ new Gson().toJson(response));
                                            Utils.Log(TAG,"onResponseData local item..."+ new Gson().toJson(items));
                                            if (response!=null){
                                                if (response.id!=null){
                                                    final Items mItem = items;
                                                    mItem.isSync = true;
                                                    mItem.global_id = response.id;
                                                    InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onUpdate(mItem);
                                                }
                                            }
                                            }
                                        @Override
                                        public void onFailure() {
                                            onUpdateSyncDataStatus(mList);
                                            Utils.Log(TAG,"onFailure");
                                        }
                                    });
                                }
                                else{
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

    public void onUpdateSyncDataStatus(final List<Items>list){
        countSyncData+=1;
        if (list!=null){
            if (countSyncData==list.size()){
                isSyncData = false;
                Utils.Log(TAG,"Completed finish sync data.......................");
            }
            else{
                Utils.Log(TAG,"Completed finish count syn data..................." + countSyncData);
            }
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


    public String checkGlobalId(final String localId){
        try{
            final List<MainCategories> list = MainCategories.getInstance().getMainCategoriesList();
            if (list==null){
                return null;
            }

            if (list.size()==0){
                return null;
            }

            if (localId==null){
                Log.d(TAG,"local Id is null");
                return null;
            }

            for (MainCategories index : list){
                if (localId.equals(index.getLocalId())){
                    return index.getGlobalId();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*Upload Service*/
    public interface UploadServiceListener{
        void onError();
        void onProgressUpdate(int percentage);
        void onFinish();
        void onResponseData(final DriveResponse response,final Items items);
        void onFailure();
    }

}
