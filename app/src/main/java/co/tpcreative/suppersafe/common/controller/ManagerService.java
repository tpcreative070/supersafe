package co.tpcreative.suppersafe.common.controller;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import co.tpcreative.suppersafe.common.services.SupperSafeService;

public class ManagerService {

    private static ManagerService instance;
    private SupperSafeService myService;
    private Context mContext;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;


    public DriveClient getDriveClient() {
        return mDriveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    public void setDriveClient(DriveClient mDriveClient) {
        this.mDriveClient = mDriveClient;
    }

    public void setDriveResourceClient(DriveResourceClient mDriveResourceClient) {
        this.mDriveResourceClient = mDriveResourceClient;
    }

    private final String TAG = ManagerService.class.getSimpleName();

    public static ManagerService getInstance(){
        if (instance==null){
            instance = new ManagerService();
        }
        return instance;
    }

    public void setContext(Context mContext){
        this.mContext = mContext;
    }

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG,"connected");
            myService = ((SupperSafeService.LocalBinder) binder).getService();
        }
        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"disconnected");
            myService = null;
        }
    };

    private void doBindService() {
        Intent intent = null;
        intent = new Intent(mContext, SupperSafeService.class);
        intent.putExtra(TAG, "Message");
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"onStartService");
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

}
