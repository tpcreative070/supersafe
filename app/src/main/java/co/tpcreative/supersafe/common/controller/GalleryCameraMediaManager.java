package co.tpcreative.supersafe.common.controller;
public class GalleryCameraMediaManager {
    private static GalleryCameraMediaManager instance;
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
}
