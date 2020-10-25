package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.Unbinder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage

abstract class BaseActivityNone : AppCompatActivity() {
    private val TAG = BaseActivityNone::class.java.simpleName
    var unbinder: Unbinder? = null
    protected var actionBar: ActionBar? = null
    var onStartCount = 0
    protected open var storage: Storage? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = getSupportActionBar()
        onStartCount = 1
        storage = Storage(this)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(R.style.AppTheme_Share, true)
        }
        return theme
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        Log.d(TAG, "action here")
        unbinder = ButterKnife.bind(this)
    }

    protected override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    protected override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop....")
    }

    protected override fun onDestroy() {
        Utils.Log(TAG, "onDestroy....")
        unbinder?.unbind()
        super.onDestroy()
    }

    protected override fun onResume() {
        Utils.Log(TAG, "onResume....")
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onStart() {
        super.onStart()
    }

    companion object {
        val TAG: String? = BaseActivity::class.java.getSimpleName()
    }
}