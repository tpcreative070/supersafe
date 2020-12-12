package co.tpcreative.supersafe.ui.askpermission
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
class AskPermissionAct : BaseActivityNoneSlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_permission)
        initUI()
    }
    override fun onOrientationChange(isFaceDown: Boolean) {}
}