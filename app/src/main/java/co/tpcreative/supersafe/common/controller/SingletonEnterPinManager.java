package co.tpcreative.supersafe.common.controller;

public class SingletonEnterPinManager {

    private static SingletonEnterPinManager instance;


    public static SingletonEnterPinManager getInstance(){
        if (instance==null){
            instance = new SingletonEnterPinManager();
        }
        return instance;
    }






}
