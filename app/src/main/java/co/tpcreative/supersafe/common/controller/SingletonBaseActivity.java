package co.tpcreative.supersafe.common.controller;

import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonBaseActivity {

    private static SingletonBaseActivity instance;
    private SingletonBaseActivityListener listener;

    public static SingletonBaseActivity getInstance(){
        if (instance==null){
            instance = new SingletonBaseActivity();
        }
        return instance;
    }

    public void setListener(SingletonBaseActivityListener listener) {
        this.listener = listener;
    }

    public void onStillScreenLock(EnumStatus status){
        if (listener!=null){
            listener.onStillScreenLock(status);
        }
    }

    public interface SingletonBaseActivityListener{
        void onStillScreenLock(EnumStatus status);
    }

}
