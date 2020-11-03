package co.tpcreative.supersafe.ui.fakepin
import android.Manifest
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.ui.albumdetail.initSpeedDial
import co.tpcreative.supersafe.ui.main_tab.initSpeedDial
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.activity_fake_pin_component.*
import kotlinx.android.synthetic.main.activity_fake_pin_component.speedDial
import kotlinx.android.synthetic.main.activity_fake_pin_component.toolbar
import kotlinx.android.synthetic.main.activity_main_tab.*

fun FakePinComponentAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    initSpeedDial()
    initRecycleView(layoutInflater)
    presenter = FakePinComponentPresenter()
    presenter?.bindView(this)
}

fun FakePinComponentAct.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = FakePinComponentAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getContext(), 2)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(2, 10, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

fun FakePinComponentAct.initSpeedDial() {
    val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var drawable: Drawable? = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_camera_white_24)
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(getString(R.string.camera))
            .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
            .setLabelColor(Color.WHITE)
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    drawable = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_white_24)
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(R.string.photo)
            .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
            .setLabelColor(ContextCompat.getColor(this,R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
            .setLabel(getString(R.string.album))
            .setLabelColor(ContextCompat.getColor(this,R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    speedDial?.mainFabAnimationRotateAngle = 180f

    //Set main action clicklistener.
    speedDial?.setOnChangeListener(object : SpeedDialView.OnChangeListener {
        override fun onMainActionSelected(): Boolean {
            return false // True to keep the Speed Dial open
        }
        override fun onToggleChanged(isOpen: Boolean) {
            Utils.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
        }
    })

    //Set option fabs clicklisteners.
    speedDial?.setOnActionSelectedListener(object : SpeedDialView.OnActionSelectedListener {
        override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
            when (actionItem?.id) {
                R.id.fab_album -> {
                    onShowDialog()
                    return false // false will close it without animation
                }
                R.id.fab_photo -> {
                    Navigator.onMoveToAlbum(this@initSpeedDial)
                    return false // closes without animation (same as mSpeedDialView.close(false); return false;)
                }
                R.id.fab_camera -> {
                    onAddPermissionCamera()
                    return false
                }
            }
            return true // To keep the Speed Dial open
        }
    })
    speedDial.mainFab.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
}

fun FakePinComponentAct.onShowDialog() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.create_album))
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .input(inputType = InputType.TYPE_CLASS_TEXT,hint = null,hintRes = null) { dialog, input ->
                Utils.Log(TAG, "Value")
                val value = input.toString()
                val base64Code: String = Utils.getHexCode(value)
                val item: MainCategoryModel? = SQLHelper.getTrashItem()
                val result: String? = item?.categories_hex_name
                if (base64Code == result) {
                    Toast.makeText(this@onShowDialog, "This name already existing", Toast.LENGTH_SHORT).show()
                } else {
                    val response: Boolean = SQLHelper.onAddFakePinCategories(base64Code, value, true)
                    if (response) {
                        Toast.makeText(this@onShowDialog, "Created album successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@onShowDialog, "Album name already existing", Toast.LENGTH_SHORT).show()
                    }
                    presenter?.getData()
                }
            }
    builder.show()
}

fun FakePinComponentAct.onAddPermissionCamera() {
    Dexter.withContext(this)
            .withPermissions(
                    Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {
                        val list: MutableList<MainCategoryModel>? = SQLHelper.getListFakePin()
                        if (list != null) {
                            Navigator.onMoveCamera(this@onAddPermissionCamera, list[0])
                        }
                    } else {
                        Utils.Log(TAG, "Permission is denied")
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        /*Miss add permission in manifest*/
                        Utils.Log(TAG, "request permission is failed")
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                    /* ... */
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
}