package co.tpcreative.supersafe.common.controller;

public class GalleryCameraMediaManager {

    private static GalleryCameraMediaManager instance;
    private AlbumDetailManagerListener listener;

    public boolean isProgressing() {
        return isProgressing;
    }

    public void setProgressing(boolean progressing) {
        isProgressing = progressing;
    }

    private boolean isProgressing;

    public static GalleryCameraMediaManager getInstance(){
        if (instance==null){
            instance = new GalleryCameraMediaManager();
        }
        return instance;
    }

    public interface AlbumDetailManagerListener{
        void onUpdatedView();
        void onStartProgress();
        void onStopProgress();
    }

    public void onStartProgress(){
        if (listener!=null){
            listener.onStartProgress();
        }
    }

    public void onStopProgress(){
        if (listener!=null){
            listener.onStopProgress();
        }
    }


    public void setListener(AlbumDetailManagerListener ls){
        this.listener = ls;
    }

    public void onUpdatedView(){
        if (listener!=null){
            listener.onUpdatedView();
        }
    }

}
