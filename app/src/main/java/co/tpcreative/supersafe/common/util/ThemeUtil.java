package co.tpcreative.supersafe.common.util;
import java.util.ArrayList;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.model.Theme;

/**
 * Created by Pankaj on 12-11-2017.
 */

public class ThemeUtil {



    public static final int THEME_PRIMARY = 0;
    public static final int THEME_PINK = 1;
    public static final int THEME_PURPLE = 2;
    public static final int THEME_DEEPPURPLE = 3;
    public static final int THEME_CYAN = 4;
    public static final int THEME_TEAL = 5;
    public static final int THEME_LIGHTGREEN = 6;
    public static final int THEME_LIME = 7;
    public static final int THEME_AMBER = 8;
    public static final int THEME_BROWN = 9;
    public static final int THEME_GRAY = 10;
    public static final int THEME_BLUEGRAY = 11;

    public static int getThemeId(int theme){
        int themeId=0;
        switch (theme){
            case THEME_PRIMARY :{
                themeId = R.style.AppTheme;
                break;
            }
            case THEME_PINK  :
                themeId = R.style.AppTheme_PINK;
                break;
            case THEME_PURPLE  :
                themeId = R.style.AppTheme_PURPLE;
                break;
            case THEME_DEEPPURPLE  :
                themeId = R.style.AppTheme_DEEPPURPLE;
                break;
            case THEME_CYAN  :
                themeId = R.style.AppTheme_CYAN;
                break;
            case THEME_TEAL  :
                themeId = R.style.AppTheme_TEAL;
                break;
            case THEME_LIGHTGREEN  :
                themeId = R.style.AppTheme_LIGHTGREEN;
                break;
            case THEME_LIME  :
                themeId = R.style.AppTheme_LIME;
                break;
            case THEME_AMBER  :
                themeId = R.style.AppTheme_AMBER;
                break;
            case THEME_BROWN  :
                themeId = R.style.AppTheme_BROWN;
                break;
            case THEME_GRAY  :
                themeId = R.style.AppTheme_GRAY;
                break;
            case THEME_BLUEGRAY  :
                themeId = R.style.AppTheme_BLUEGRAY;
                break;
            default:
                break;
        }
        return themeId;
    }

    public static int getSlideThemeId(int theme){
        int themeId=0;
        switch (theme){
            case THEME_PRIMARY :{
                themeId = R.style.AppTheme_Slide;
                break;
            }
            case THEME_PINK  :
                themeId = R.style.AppTheme_PINK_Slide;
                break;
            case THEME_PURPLE  :
                themeId = R.style.AppTheme_PURPLE_Slide;
                break;
            case THEME_DEEPPURPLE  :
                themeId = R.style.AppTheme_DEEPPURPLE_Slide;
                break;
            case THEME_CYAN  :
                themeId = R.style.AppTheme_CYAN_Slide;
                break;
            case THEME_TEAL  :
                themeId = R.style.AppTheme_TEAL_Slide;
                break;
            case THEME_LIGHTGREEN  :
                themeId = R.style.AppTheme_LIGHTGREEN_Slide;
                break;
            case THEME_LIME  :
                themeId = R.style.AppTheme_LIME_Slide;
                break;
            case THEME_AMBER  :
                themeId = R.style.AppTheme_AMBER_Slide;
                break;
            case THEME_BROWN  :
                themeId = R.style.AppTheme_BROWN_Slide;
                break;
            case THEME_GRAY  :
                themeId = R.style.AppTheme_GRAY_Slide;
                break;
            case THEME_BLUEGRAY  :
                themeId = R.style.AppTheme_BLUEGRAY_Slide;
                break;
            default:
                break;
        }
        return themeId;
    }
    
    public static ArrayList<Theme> getThemeList(){
        ArrayList<Theme> themeArrayList = new ArrayList<>();
        themeArrayList.add(new Theme(0,R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton,"#0091EA"));
        themeArrayList.add(new Theme(1,R.color.primaryColorPink, R.color.primaryDarkColorPink, R.color.secondaryColorPink,"#448AFF"));
        themeArrayList.add(new Theme(2,R.color.primaryColorPurple, R.color.primaryDarkColorPurple, R.color.secondaryDarkColorPurple,"#b4c320"));
        themeArrayList.add(new Theme(3,R.color.primaryColorDeepPurple, R.color.primaryDarkColorDeepPurple, R.color.primaryColorPurple,"#9C27B0"));
        themeArrayList.add(new Theme(4,R.color.primaryColorCyan, R.color.primaryDarkColorCyan, R.color.primaryColorBrown,"#795548"));
        themeArrayList.add(new Theme(5,R.color.primaryColorTeal, R.color.primaryDarkColorTeal, R.color.secondaryDarkColorTeal,"#caa052"));
        themeArrayList.add(new Theme(6,R.color.primaryColorLightGreen, R.color.primaryDarkColorLightGreen, R.color.secondaryColorLightGreen,"#448AFF"));
        themeArrayList.add(new Theme(7,R.color.primaryColorLime, R.color.primaryDarkColorLime, R.color.primaryColorBlueGray,"#607D8B"));
        themeArrayList.add(new Theme(8,R.color.primaryColorAmber, R.color.primaryDarkColorAmber, R.color.secondaryColorAmber,"#03A9F4"));
        themeArrayList.add(new Theme(9,R.color.primaryColorBrown, R.color.primaryDarkColorBrown, R.color.md_teal_500,"#009688"));
        themeArrayList.add(new Theme(10,R.color.primaryColorGray, R.color.primaryDarkColorGray, R.color.secondaryColorGray,"#536DFE"));
        themeArrayList.add(new Theme(11,R.color.primaryColorBlueGray, R.color.primaryDarkColorBlueGray, R.color.secondaryDarkColorBlueGray,"#a6b320"));
        return themeArrayList;
    }

}
