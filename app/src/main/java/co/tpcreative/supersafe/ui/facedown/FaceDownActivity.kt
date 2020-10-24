package co.tpcreative.supersafe.ui.facedown
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivityNone
import co.tpcreative.supersafe.common.util.Utils

class FaceDownActivity : BaseActivityNone() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_down)
    }

    override fun onResume() {
        super.onResume()
        finish()
        Utils.Log(TAG, "Finish")
    }

    companion object {
        private val TAG = FaceDownActivity::class.java.simpleName
    }
}