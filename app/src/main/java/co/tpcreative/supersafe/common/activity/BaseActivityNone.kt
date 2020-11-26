package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseActivityNone : AppCompatActivity() {
    var TAG : String = this::class.java.simpleName
    val mainScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(R.style.AppTheme_Share, true)
        }
        return theme
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop....")
    }

    override fun onDestroy() {
        Utils.Log(TAG, "onDestroy....")
        super.onDestroy()
        mainScope.cancel()
    }

    override fun onResume() {
        Utils.Log(TAG, "onResume....")
        super.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
    }

    companion object {
        val TAG: String? = BaseActivity::class.java.getSimpleName()
    }
}