package co.tpcreative.supersafe.ui.dashboard
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.model.EnumPinAction
import kotlinx.android.synthetic.main.activity_dash_board.*

fun DashBoardAct.initUI(){
    btnSignUp.setOnClickListener {
        Navigator.onMoveSetPin(this, EnumPinAction.SIGN_UP)
    }
    btnLogin.setOnClickListener {
        Navigator.onMoveToLogin(this)
    }
}