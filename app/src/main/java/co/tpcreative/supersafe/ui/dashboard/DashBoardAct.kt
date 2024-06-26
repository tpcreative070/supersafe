package co.tpcreative.supersafe.ui.dashboard
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.util.Utils


class DashBoardAct : BaseActivityNoneSlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        initUI()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}

    companion object {
        private val TAG = DashBoardAct::class.java.simpleName
    }
}