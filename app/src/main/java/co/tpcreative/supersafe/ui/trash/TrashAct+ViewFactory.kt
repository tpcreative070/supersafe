package co.tpcreative.supersafe.ui.trash
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_trash.*
import kotlinx.android.synthetic.main.activity_trash.recyclerView
import kotlinx.android.synthetic.main.activity_trash.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun TrashAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = TrashPresenter()
    presenter?.bindView(this)
    onInit()
    btnTrash.setOnClickListener {
        if (presenter?.mList?.size!! > 0) {
            if (countSelected == 0) {
                onShowDialog(getString(R.string.empty_all_trash), true)
            } else {
                onShowDialog(getString(R.string.restore), false)
            }
        }
    }
    btnUpgradeVersion.setOnClickListener {
        Navigator.onMoveToPremium(applicationContext)
    }
}

suspend fun TrashAct.initRecycleView(layoutInflater: LayoutInflater) = withContext(Dispatchers.Main) {
    adapter = TrashAdapter(layoutInflater, applicationContext, this@initRecycleView)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 3)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
    adapter?.setDataSource(presenter?.mList)
}

fun TrashAct.onShowDialog(message: String, isEmpty: Boolean) {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.confirm)
            .message(text = message)
            .negativeButton(R.string.cancel)
            .positiveButton(R.string.ok)
            .positiveButton {  presenter?.onDeleteAll(isEmpty) }
    builder.show()
}

fun TrashAct.toggleSelection(position: Int) {
    presenter?.mList?.get(position)?.isChecked = !(presenter?.mList?.get(position)?.isChecked!!)
    if (presenter?.mList?.get(position)?.isChecked!!) {
        countSelected++
    } else {
        countSelected--
    }
    onShowUI()
    adapter?.notifyItemChanged(position)
}

fun TrashAct.deselectAll() {
    var i = 0
    val l: Int = presenter?.mList?.size!!
    while (i < l) {
        presenter?.mList?.get(i)?.isChecked = false
        i++
    }
    countSelected = 0
    onShowUI()
    adapter?.notifyDataSetChanged()
}

fun TrashAct.selectAll() {
    var countSelect = 0
    for (i in presenter?.mList?.indices!!) {
        presenter?.mList?.get(i)?.isChecked = isSelectAll
        if (presenter?.mList?.get(i)?.isChecked!!) {
            countSelect++
        }
    }
    countSelected = countSelect
    onShowUI()
    adapter?.notifyDataSetChanged()
    actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
}

fun TrashAct.onShowUI() {
    if (countSelected == 0) {
        btnTrash?.text = getString(R.string.key_empty_trash)
    } else {
        btnTrash?.text = getString(R.string.key_restore)
    }
}


fun TrashAct.onInit() {
    progress_bar.visibility = View.VISIBLE
    onCallData()
}

fun TrashAct.onCallData(){
    mainScope.launch {
        val mInitRecyclerView = async {
            initRecycleView(layoutInflater)
        }
        val mResultData = async {
            presenter?.getData(this@onCallData)
        }
        val mRecyclerViewLoading = async {
            onLoading()
        }
        mInitRecyclerView.await()
        mResultData.await()
        mRecyclerViewLoading.await()
        progress_bar.visibility = View.INVISIBLE
        Utils.Log(TAG,"Loading data")
    }
}

suspend fun TrashAct.onLoading() = withContext(Dispatchers.Main){
    adapter?.setDataSource(presenter?.mList)
}

fun TrashAct.onPushDataToList(){
    mainScope.launch {
        val mResultData = async {
            presenter?.getData(this@onPushDataToList)
        }
        val mResult = async {
            onLoading()
        }
        mResultData.await()
        mResult.await()
        Utils.Log(TAG,"Completed")
    }
}





