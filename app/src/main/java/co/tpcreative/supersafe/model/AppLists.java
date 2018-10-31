package co.tpcreative.supersafe.model;

import android.arch.persistence.room.Ignore;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class AppLists {

    public String ic_name;
    public String title;
    public String description;
    public String link;
    public String packageName;
    public boolean isInstalled;


    private static AppLists instance;

    public static AppLists getInstance(){
        if (instance==null){
            instance = new AppLists();
        }
        return instance;
    }

    public AppLists(){

    }

    public AppLists(String ic_name,String title,String description,boolean isInstalled,String packageName,String link){
        this.ic_name = ic_name;
        this.title = title;
        this.description = description;
        this.isInstalled = isInstalled;
        this.link = link;
        this.packageName = packageName;
    }

    public Drawable getDrawable(Context mContext, String name) {
        try {
            int resourceId = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            return mContext.getResources().getDrawable(resourceId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
