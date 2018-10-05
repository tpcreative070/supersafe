package co.tpcreative.supersafe.common.controller;

import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonBaseApiActivity {

    private static SingletonBaseApiActivity instance;
    private SingletonBaseApiActivityListener listener;

    public static SingletonBaseApiActivity getInstance(){
        if (instance==null){
            instance = new SingletonBaseApiActivity();
        }
        return instance;
    }

    public void setListener(SingletonBaseApiActivityListener listener) {
        this.listener = listener;
    }

    public void onStillScreenLock(EnumStatus status){
        if (listener!=null){
            listener.onStillScreenLock(status);
        }
    }

    public interface SingletonBaseApiActivityListener{
        void onStillScreenLock(EnumStatus status);
    }

}
