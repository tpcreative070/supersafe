package co.tpcreative.supersafe.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;

/**
 * Created by Pankaj on 03-11-2017.
 */

public class Theme implements Serializable {
    private int id;
    private int primaryColor;
    private int primaryDarkColor;
    private int accentColor;
    private String accentColorHex;
    public boolean isCheck;

    private static Theme instance ;

    public static Theme getInstance(){
        if (instance==null){
            instance = new Theme();
        }
        return instance;
    }

    public Theme(){

    }

    public Theme(int primaryColor, int primaryDarkColor, int accentColor) {
        this.primaryColor = primaryColor;
        this.primaryDarkColor = primaryDarkColor;
        this.accentColor = accentColor;
        this.isCheck = false;
    }

    public Theme(int id , int primaryColor, int primaryDarkColor, int accentColor,String accentColorHex) {
        this.id = id;
        this.primaryColor = primaryColor;
        this.primaryDarkColor = primaryDarkColor;
        this.accentColor = accentColor;
        this.isCheck = false;
        this.accentColorHex = accentColorHex;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getAccentColorHex() {
        return accentColorHex;
    }

    public int getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public void setPrimaryDarkColor(int primaryDarkColor) {
        this.primaryDarkColor = primaryDarkColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }


    public Theme getThemeInfo(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_theme_object),null);
            if (value!=null){
                final Theme mTheme = new Gson().fromJson(value,Theme.class);
                if (mTheme!=null){
                    return mTheme;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new Theme(0,R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton,"#0091EA");
    }

    public List<Theme> getList(){
        try{
            final List<Theme> mList = ThemeUtil.getThemeList();
            return mList;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
