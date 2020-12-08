package co.tpcreative.supersafe.ui.privates
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.ui.main_tab.onShowDialog
import co.tpcreative.supersafe.viewmodel.PrivateViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.fragment_private.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun PrivateFragment.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    initRecycleView(layoutInflater)
    getData()
}

fun PrivateFragment.getData(){
    viewModel.getData().observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            val mResult = async {
                Utils.Log(TAG,"updated data source")
                adapter?.setDataSource(it)
            }
            mResult.await()
            Utils.onPushEventBus(EnumStatus.PRIVATE_DONE)
        }
    })
}

fun PrivateFragment.emptyData(){
    viewModel.onEmptyTrash().observe(this, Observer {
        getData()
    })
}

fun PrivateFragment.deletedAlbum(position : Int){
    viewModel.onDeleteAlbum(position).observe(this, Observer {
        getData()
    })
}

fun PrivateFragment.onShowChangeCategoriesNameDialog(mainCategories: MainCategoryModel?) {
    val builder: MaterialDialog = MaterialDialog(activity!!)
            .title(R.string.album_is_locked)
            .message(R.string.enter_a_password_for_this_album)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
                isClicked = false
            }
            .positiveButton(R.string.open)
            .input(hintRes = R.string.type_password, inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD), allowEmpty = false){ dialog, text->
                isClicked = false
                if (mainCategories?.pin == text.toString()) {
                    Navigator.onMoveAlbumDetail(activity!!, mainCategories)
                    dialog.dismiss()
                } else {
                    Utils.onBasicAlertNotify(activity!!,message = getString(R.string.wrong_password))
                    dialog.getInputField().setText("")
                }
            }
    val input: EditText = builder.getInputField()
    input.setBackgroundColor( ContextCompat.getColor(activity!!,R.color.transparent))
    builder.show()
}

fun PrivateFragment.initRecycleView(layoutInflater: LayoutInflater?) {
    adapter = layoutInflater?.let { PrivateAdapter(it, context, this) }
    val mLayoutManager: RecyclerView.LayoutManager = NpaGridLayoutManager(context, 2)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(2, 10, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

private fun PrivateFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(PrivateViewModel::class.java)
}
