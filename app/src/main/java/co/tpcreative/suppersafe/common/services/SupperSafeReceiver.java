package co.tpcreative.suppersafe.common.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class SupperSafeReceiver extends BroadcastReceiver {

    public static final String TAG = SupperSafeReceiver.class.getSimpleName();
    public static ConnectivityReceiverListener connectivityReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive");
        String action=intent.getAction();
        /*In the case status of network changed and then updating status for app(Connected/Disconnect)*/
        if (action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null
                    && activeNetwork.isConnected();
            if (connectivityReceiverListener != null) {
                connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
            }
        }
    }

    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) SupperSafeApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }


}

