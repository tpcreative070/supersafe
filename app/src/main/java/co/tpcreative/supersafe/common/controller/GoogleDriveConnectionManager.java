package co.tpcreative.supersafe.common.controller;

public class GoogleDriveConnectionManager {

    private static GoogleDriveConnectionManager instance;
    private GoogleDriveConnectionManagerListener ls;
    public static GoogleDriveConnectionManager getInstance(){
        if (instance==null){
            instance = new GoogleDriveConnectionManager();
        }
        return instance;
    }

    public void setListener(GoogleDriveConnectionManagerListener listener){
        this.ls = listener;
    }

    public void onNetworkConnectionChanged(boolean is){
        if (this.ls!=null){
            this.ls.onNetworkConnectionChanged(is);
        }
    }

    public interface GoogleDriveConnectionManagerListener{
        void onNetworkConnectionChanged(boolean isConnect);
    }


}
