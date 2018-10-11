package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;
import com.google.gson.Gson;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.User;

public class SingletonPremiumTimer {

    private CountDownTimer mCountDownTimer;
    long current_milliseconds = 0;
    long end_milliseconds = 0;

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
            Utils.Log(TAG,new Gson().toJson(mUser));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCountDownTimer = new CountDownTimer(end_milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                current_milliseconds = current_milliseconds-1;
                Long serverUptimeSeconds =
                        (millisUntilFinished - current_milliseconds) / 1000;
                String daysLeft = String.format("%d", serverUptimeSeconds / 86400);
                String hoursLeft = String.format("%d", (serverUptimeSeconds % 86400) / 3600);
                String minutesLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) / 60);
                String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                if (ls!=null){
                    ls.onPremiumTimer(daysLeft,hoursLeft,minutesLeft,secondsLeft);
                }
                //Utils.Log(TAG,"day lefts: "+ daysLeft + " hours left: " + hoursLeft +" minutes left: " +minutesLeft + " seconds left: "+ secondsLeft);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish");
            }
        }.start();
    }

    public void onStop(){
        if (mCountDownTimer!=null){
            mCountDownTimer.cancel();
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

}