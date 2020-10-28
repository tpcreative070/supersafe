package co.tpcreative.supersafe.ui.fakepin

import android.view.View
import androidx.appcompat.widget.Toolbar
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumPinAction
import kotlinx.android.synthetic.main.activity_fake_pin.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun FakePinAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    val value: Boolean = PrefsController.getBoolean(getString(R.string.key_fake_pin), false)
    btnSwitch?.isChecked = value
    tvCreatePin?.isEnabled = value
    val fakePin: String? = SuperSafeApplication.getInstance().readFakeKey()
    if (fakePin == "") {
        tvCreatePin?.text = (getText(R.string.create_fake_pin))
    } else {
        tvCreatePin?.text = (getText(R.string.change_fake_pin))
    }
    tvPremiumDescription?.text = (getString(R.string.fake_pin))

    tvCreatePin.setOnClickListener {
        Navigator.onMoveToFakePin(this, EnumPinAction.NONE)
    }

    imgView.setOnClickListener {
        Navigator.onMoveFakePinComponentInside(this)
    }

    rlSwitch.setOnClickListener {
        btnSwitch?.isChecked = (!btnSwitch?.isChecked!!)
    }
}