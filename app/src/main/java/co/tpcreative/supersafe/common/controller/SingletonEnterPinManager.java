package co.tpcreative.supersafe.common.controller;

public class SingletonEnterPinManager {

    private static SingletonEnterPinManager instance;

    private boolean isEnterPinWorking;

    public static SingletonEnterPinManager getInstance(){
        if (instance==null){
            instance = new SingletonEnterPinManager();
        }
        return instance;
    }

    public void setEnterPinWorking(boolean enterPinWorking) {
        isEnterPinWorking = enterPinWorking;
    }

    public boolean isEnterPinWorking() {
        return isEnterPinWorking;
    }


}
