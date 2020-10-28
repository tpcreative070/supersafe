package co.tpcreative.supersafe.ui.trash
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import kotlinx.android.synthetic.main.activity_trash.*

fun TrashAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    presenter = TrashPresenter()
    presenter?.bindView(this)
    presenter?.getData(this)

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

fun TrashAct.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = TrashAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 3)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

fun TrashAct.onShowDialog(message: String?, isEmpty: Boolean) {
    val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
            .title(getString(R.string.confirm))
            .theme(Theme.LIGHT)
            .content(message!!)
            .titleColor(ContextCompat.getColor(getContext()!!,R.color.black))
            .negativeText(getString(R.string.cancel))
            .positiveText(getString(R.string.ok))
            .onPositive { dialog, which -> presenter?.onDeleteAll(isEmpty) }
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

