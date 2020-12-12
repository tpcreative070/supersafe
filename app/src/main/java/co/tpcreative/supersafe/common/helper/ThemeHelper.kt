package co.tpcreative.supersafe.common.helper
import androidx.appcompat.app.AppCompatDelegate
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumThemeModel
object ThemeHelper {
    fun applyTheme(themeMode: EnumThemeModel) {
        when (themeMode) {
            EnumThemeModel.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Utils.Log(ThemeHelper::class.java, "Call light")
            }
            EnumThemeModel.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Utils.Log(ThemeHelper::class.java, "Call dark")
            }
            else -> Utils.Log("TAG","Nothing")
        }
    }
}
