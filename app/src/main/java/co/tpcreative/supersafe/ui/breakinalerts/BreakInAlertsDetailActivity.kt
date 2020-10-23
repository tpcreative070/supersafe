package co.tpcreative.supersafe.ui.breakinalertsimport
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.BreakInAlertsModel
import co.tpcreative.supersafe.model.EnumStatus
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class BreakInAlertsDetailActivity : BaseActivity() {
    @BindView(R.id.imgPicture)
    var imageView: AppCompatImageView? = null
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 600)
            .priority(Priority.HIGH)

    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_in_alerts_detail)
        val bundle: Bundle? = getIntent().getExtras()
        val inAlerts: BreakInAlertsModel? = bundle?.get(getString(R.string.key_break_in_alert)) as BreakInAlertsModel
        if (inAlerts != null) {
            Glide.with(this)
                    .load(File(inAlerts.fileName))
                    .apply(options!!).into(imageView!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        //SuperSafeApplication.getInstance().writeKeyHomePressed(BreakInAlertsDetailActivity.class.getSimpleName());
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(BaseActivity.Companion.TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }
}