package co.tpcreative.supersafe.ui.secretdoor
import android.view.View
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import kotlinx.android.synthetic.main.activity_secret_door.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun SecretDoorAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    val value: Boolean = PrefsController.getBoolean(getString(R.string.key_secret_door), false)
    val options: Boolean = PrefsController.getBoolean(getString(R.string.key_calculator), false)
    btnSwitch?.isChecked = value
    if (options) {
        tvOptionItems?.text = getString(R.string.calculator)
        imgIcons?.setImageResource(R.drawable.ic_calculator)
    } else {
        tvOptionItems?.text = getString(R.string.virus_scanner)
        imgIcons?.setImageResource(R.drawable.baseline_donut_large_white_48)
    }
    btnSwitch?.setOnClickListener(View.OnClickListener {
        if (btnSwitch?.isChecked!!) {
            Navigator.onMoveSecretDoorSetUp(this)
        }
    })
    tvPremiumDescription?.text = getString(R.string.secret_door)
    rlScanner.setOnClickListener {
        onChooseOptionItems()
    }
    rlSwitch.setOnClickListener {
        btnSwitch?.isChecked = !btnSwitch?.isChecked!!
    }
}

fun SecretDoorAct.onChooseOptionItems() {
    val dialog: MaterialDialog = MaterialDialog(this)
            .listItems (res = R.array.select_option){ dialog, position, text ->
                Utils.Log(TAG, "position $position")
                when (position) {
                    0 -> {
                        PrefsController.putBoolean(getString(R.string.key_calculator), false)
                        tvOptionItems?.text = getString(R.string.virus_scanner)
                        imgIcons?.setImageResource(R.drawable.baseline_donut_large_white_48)
                        val isFirstScanVirus: Boolean = PrefsController.getBoolean(getString(R.string.is_first_scan_virus), false)
                        if (!isFirstScanVirus) {
                            Navigator.onMoveSecretDoorSetUp(this@onChooseOptionItems)
                            PrefsController.putBoolean(getString(R.string.is_first_scan_virus), true)
                        }
                    }
                    else -> {
                        PrefsController.putBoolean(getString(R.string.key_calculator), true)
                        tvOptionItems?.text = getString(R.string.calculator)
                        imgIcons?.setImageResource(R.drawable.ic_calculator)
                        val isFirstCalculator: Boolean = PrefsController.getBoolean(getString(R.string.is_first_calculator), false)
                        if (!isFirstCalculator) {
                            Navigator.onMoveSecretDoorSetUp(this@onChooseOptionItems)
                            PrefsController.putBoolean(getString(R.string.is_first_calculator), true)
                        }
                    }
                }
            }

    dialog.show()
}