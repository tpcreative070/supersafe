package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;
import com.google.gson.Gson;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.User;

public class SingletonPremiumTimer {

    private CountDownTimer mCountDownTimer;
    long current_milliseconds = 0;
    long end_milliseconds = 0;
    private String daysLeft ;
    private String hoursLeft ;
    private String minutesLeft ;
    private String secondsLeft ;

    private SingletonPremiumTimerListener ls;

    private static final String TAG = SingletonPremiumTimer.class.getSimpleName();


    private static SingletonPremiumTimer instance;

    public static SingletonPremiumTimer getInstance(){
        if (instance==null){
            instance = new SingletonPremiumTimer();
        }
        return instance;
    }

    public void setListener(SingletonPremiumTimerListener ls){
        this.ls = ls;
    }

    public void onStartTimer(){
        Utils.Log(TAG,"onStartTimer");
        if (mCountDownTimer!=null){
            Utils.Log(TAG,"Running............");
            return;
        }
        Utils.Log(TAG,"Start");
        try {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser==null){
                Utils.Log(TAG,"User is null");
                return;
            }

            if (mUser.premium==null){
                Utils.Log(TAG,"Premium is null");
                return;
            }
            current_milliseconds =  mUser.premium.current_milliseconds ;
            end_milliseconds = mUser.premium.past_milliseconds;

            if (current_milliseconds>= end_milliseconds){
                mUser.premium.status = false;
                PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(mUser));
                return;
            }

            Utils.Log(TAG,"Device milliseconds :"+ mUser.premium.device_milliseconds +" current milliseconds "+ mUser.premium.current_milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCountDownTimer = new CountDownTimer(end_milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                current_milliseconds = current_milliseconds-1;
                Long serverUptimeSeconds =
                        (millisUntilFinished - current_milliseconds) / 1000;
                daysLeft = String.format("%d", serverUptimeSeconds / 86400);
                hoursLeft = String.format("%d", (serverUptimeSeconds % 86400) / 3600);
                minutesLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) / 60);
                secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                if (ls!=null){
                    ls.onPremiumTimer(daysLeft,hoursLeft,minutesLeft,secondsLeft);
                }

                if (daysLeft.equals("0") && hoursLeft.equals("0") && minutesLeft.equals("0") && secondsLeft.equals("0")) {
                    final User user = User.getInstance().getUserInfo();
                    if (user!=null){
                        if (user.premium!=null){
                            user.premium.status = false;
                            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(user));
                            onStop();
                            Utils.Log(TAG,"Limited upload now");
                        }
                    }
                }
                Utils.Log(TAG,"day lefts: "+ daysLeft + " hours left: " + hoursLeft +" minutes left: " +minutesLeft + " seconds left: "+ secondsLeft);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :"+ end_milliseconds +" - "+current_milliseconds);
            }
        }.start();
    }

    public void onStop(){
        if (mCountDownTimer!=null){
            mCountDownTimer.cancel();
            mCountDownTimer.onFinish();
            mCountDownTimer = null;
            final User user = User.getInstance().getUserInfo();
            if (user!=null){
                if (user.premium!=null){
                    user.premium.device_milliseconds = System.currentTimeMillis();
                    PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(user));
                }
            }
        }
    }

    public void onPremiumTimer(String days,String hours,String minutes,String seconds){
        if (ls!=null){
            ls.onPremiumTimer(days,hours,minutes,seconds);
        }
    }

    public interface SingletonPremiumTimerListener{
        void onPremiumTimer(String days,String hours,String minutes,String seconds);
    }

    public String getDaysLeft() {
        return daysLeft;
    }

}
