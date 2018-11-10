package co.tpcreative.supersafe.common.controller;

public class FilesManager {

    private static FilesManager instance;

    public static FilesManager getInstance() {
        if (instance==null){
            instance = new FilesManager();
        }
        return instance;
    }





}
