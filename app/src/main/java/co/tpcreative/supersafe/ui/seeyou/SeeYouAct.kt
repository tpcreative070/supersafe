package co.tpcreative.supersafe.ui.seeyou
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SeeYouAct : BaseActivityNoneSlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_you)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1200)
            finish()
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
    }
}