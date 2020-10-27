package co.tpcreative.supersafe.ui.dashboard
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.model.EnumPinAction
import kotlinx.android.synthetic.main.activity_dash_board.*

fun DashBoardAct.initUI(){
    TAG = this::class.java.simpleName
    btnSignUp.setOnClickListener {
        Navigator.onMoveSetPin(this, EnumPinAction.SIGN_UP)
    }
    btnLogin.setOnClickListener {
        Navigator.onMoveToLogin(this)
    }
}