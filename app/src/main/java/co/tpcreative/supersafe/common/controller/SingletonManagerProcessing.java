package co.tpcreative.supersafe.common.controller;
import android.app.Activity;
import android.app.AlertDialog;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import dmax.dialog.SpotsDialog;

public class SingletonManagerProcessing {

    private static final String TAG = SingletonManagerProcessing.class.getSimpleName();
    private static SingletonManagerProcessing instance;
    private AlertDialog dialog;

    public static SingletonManagerProcessing getInstance(){
        if (instance==null){
            instance = new SingletonManagerProcessing();
        }
        return instance;
    }


    public void onStartProgressing(Activity activity){
        try{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog==null){
                        dialog = new SpotsDialog.Builder()
                                .setContext(activity)
                                .setMessage(SuperSafeApplication.getInstance().getString(R.string.progressing))
                                .setCancelable(true)
                                .build();
                    }
                    if (!dialog.isShowing()){
                        dialog.show();
                        Utils.Log(TAG,"Showing dialog...");
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStopProgressing(Activity activity){
        Utils.Log(TAG,"onStopProgressing");
        try{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog!=null){
                        dialog.dismiss();
                        dialog = null;
                    }
                }
            });
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }




}