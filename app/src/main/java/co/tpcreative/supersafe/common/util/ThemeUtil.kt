package co.tpcreative.supersafe.common.util
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.ThemeApp
import java.util.*
object ThemeUtil {
    const val THEME_PRIMARY = 0
    const val THEME_PINK = 1
    const val THEME_PURPLE = 2
    const val THEME_DEEPPURPLE = 3
    const val THEME_CYAN = 4
    const val THEME_TEAL = 5
    const val THEME_LIGHTGREEN = 6
    const val THEME_LIME = 7
    const val THEME_AMBER = 8
    const val THEME_BROWN = 9
    const val THEME_GRAY = 10
    const val THEME_BLUEGRAY = 11
    fun getThemeId(theme: Int): Int {
        var themeId = 0
        when (theme) {
            THEME_PRIMARY -> {
                themeId = R.style.AppTheme
            }
            THEME_PINK -> themeId = R.style.AppTheme_PINK
            THEME_PURPLE -> themeId = R.style.AppTheme_PURPLE
            THEME_DEEPPURPLE -> themeId = R.style.AppTheme_DEEPPURPLE
            THEME_CYAN -> themeId = R.style.AppTheme_CYAN
            THEME_TEAL -> themeId = R.style.AppTheme_TEAL
            THEME_LIGHTGREEN -> themeId = R.style.AppTheme_LIGHTGREEN
            THEME_LIME -> themeId = R.style.AppTheme_LIME
            THEME_AMBER -> themeId = R.style.AppTheme_AMBER
            THEME_BROWN -> themeId = R.style.AppTheme_BROWN
            THEME_GRAY -> themeId = R.style.AppTheme_GRAY
            THEME_BLUEGRAY -> themeId = R.style.AppTheme_BLUEGRAY
            else -> {
            }
        }
        return themeId
    }

    fun getSlideThemeId(theme: Int): Int {
        var themeId = 0
        when (theme) {
            ThemeUtil.THEME_PRIMARY -> {
                themeId = R.style.AppTheme
            }
            THEME_PINK -> themeId = R.style.AppTheme_PINK
            THEME_PURPLE -> themeId = R.style.AppTheme_PURPLE
            THEME_DEEPPURPLE -> themeId = R.style.AppTheme_DEEPPURPLE
            THEME_CYAN -> themeId = R.style.AppTheme_CYAN
            THEME_TEAL -> themeId = R.style.AppTheme_TEAL
            THEME_LIGHTGREEN -> themeId = R.style.AppTheme_LIGHTGREEN
            THEME_LIME -> themeId = R.style.AppTheme_LIME
            THEME_AMBER -> themeId = R.style.AppTheme_AMBER
            THEME_BROWN -> themeId = R.style.AppTheme_BROWN
            THEME_GRAY -> themeId = R.style.AppTheme_GRAY
            THEME_BLUEGRAY -> themeId = R.style.AppTheme_BLUEGRAY
            else -> {
            }
        }
        return themeId
    }

    fun getThemeList(): ArrayList<ThemeApp> {
        val themeAppArrayList: ArrayList<ThemeApp> = ArrayList<ThemeApp>()
        themeAppArrayList.add(ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent))
        themeAppArrayList.add(ThemeApp(1, R.color.primaryColorPink, R.color.primaryDarkColorPink, R.color.secondaryColorPink))
        themeAppArrayList.add(ThemeApp(2, R.color.primaryColorPurple, R.color.primaryDarkColorPurple, R.color.secondaryDarkColorPurple))
        themeAppArrayList.add(ThemeApp(3, R.color.primaryColorDeepPurple, R.color.primaryDarkColorDeepPurple, R.color.secondaryColorDeepPurple))
        themeAppArrayList.add(ThemeApp(4, R.color.primaryColorCyan, R.color.primaryDarkColorCyan, R.color.secondaryColorCyan))
        themeAppArrayList.add(ThemeApp(5, R.color.primaryColorTeal, R.color.primaryDarkColorTeal, R.color.secondaryDarkColorTeal))
        themeAppArrayList.add(ThemeApp(6, R.color.primaryColorLightGreen, R.color.primaryDarkColorLightGreen, R.color.secondaryColorLightGreen))
        themeAppArrayList.add(ThemeApp(7, R.color.primaryColorLime, R.color.primaryDarkColorLime, R.color.secondaryColorLime))
        themeAppArrayList.add(ThemeApp(8, R.color.primaryColorAmber, R.color.primaryDarkColorAmber, R.color.secondaryColorAmber))
        themeAppArrayList.add(ThemeApp(9, R.color.primaryColorBrown, R.color.primaryDarkColorBrown, R.color.secondaryColorBrown))
        themeAppArrayList.add(ThemeApp(10, R.color.primaryColorGray, R.color.primaryDarkColorGray, R.color.secondaryColorGray))
        themeAppArrayList.add(ThemeApp(11, R.color.primaryColorBlueGray, R.color.primaryDarkColorBlueGray, R.color.secondaryDarkColorBlueGray))
        return themeAppArrayList
    }
}