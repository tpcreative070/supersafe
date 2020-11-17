package co.tpcreative.supersafe.ui.trash
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.viewmodel.TrashViewModel
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_trash.*
import kotlinx.android.synthetic.main.activity_trash.recyclerView
import kotlinx.android.synthetic.main.activity_trash.toolbar
import kotlinx.coroutines.*

fun TrashAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnTrash.setOnClickListener {
        if (viewModel.mList?.size!! > 0) {
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

    viewModel.isLoading.observe(this, Observer {
        if (it){
            progress_bar.visibility = View.VISIBLE
        }else{
            progress_bar.visibility = View.INVISIBLE
        }
    })

    viewModel.photos.observe(this, Observer {
        tv_Photos.text = kotlin.String.format(getString(R.string.photos_default), "$it")
    })
    viewModel.videos.observe(this, Observer {
        tv_Videos.text = kotlin.String.format(getString(R.string.videos_default), "$it")
    })
    viewModel.audios.observe(this, Observer {
        tv_Audios.text = kotlin.String.format(getString(R.string.audios_default), "$it")
    })
    viewModel.others.observe(this, Observer {
        tv_Others.text = kotlin.String.format(getString(R.string.others_default), "$it")
    })

    viewModel.count.observe(this, Observer {
        if (it == 0) {
            btnTrash?.text = getString(R.string.key_empty_trash)
        } else {
            btnTrash?.text = getString(R.string.key_restore)
        }
    })
    initRecycleView(layoutInflater)
    getIntentData()
}

fun TrashAct.initRecycleView(layoutInflater: LayoutInflater){
    adapter = TrashAdapter(layoutInflater, applicationContext, this@initRecycleView)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 3)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

fun TrashAct.onShowDialog(message: String, isEmpty: Boolean) {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.confirm)
            .message(text = message)
            .negativeButton(R.string.cancel)
            .positiveButton(R.string.ok)
            .positiveButton {  deleteItems(isEmpty) }
    builder.show()
}

fun TrashAct.toggleSelection(position: Int) {
    viewModel.mList?.get(position)?.isChecked = !(viewModel.mList?.get(position)?.isChecked!!)
    if (viewModel.mList?.get(position)?.isChecked!!) {
        countSelected++
    } else {
        countSelected--
    }
    viewModel.count.postValue(countSelected)
    adapter?.notifyItemChanged(position)
}

fun TrashAct.deselectAll() {
    var i = 0
    val l: Int = viewModel.mList?.size!!
    while (i < l) {
        viewModel.mList?.get(i)?.isChecked = false
        i++
    }
    countSelected = 0
    viewModel.count.postValue(countSelected)
    adapter?.notifyDataSetChanged()
}

fun TrashAct.selectAll() {
    var countSelect = 0
    for (i in viewModel.mList?.indices!!) {
        viewModel.mList?.get(i)?.isChecked = isSelectAll
        if (viewModel.mList?.get(i)?.isChecked!!) {
            countSelect++
        }
    }
    countSelected = countSelect
    viewModel.count.postValue(countSelected)
    adapter?.notifyDataSetChanged()
    actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)

}

private fun TrashAct.getIntentData(){
    viewModel.isLoading.postValue(true)
    viewModel.getData().observe(this, Observer {
        when(it.status){
            Status.SUCCESS ->{
                CoroutineScope(Dispatchers.Main).launch {
                    val mResult = async {
                        adapter?.setDataSource(it.data)
                    }
                    mResult.await()
                    viewModel.isLoading.postValue(false)
                }
            }
            else -> {
                Utils.Log(TAG,"Nothing")
                viewModel.isLoading.postValue(false)
            }
        }
    })
}

private fun TrashAct.deleteItems(isEmpty: Boolean){
    viewModel.onDeleteAll(isEmpty).observe(this, Observer {
        if (actionMode != null) {
            actionMode?.finish()
        }
        getIntentData()
        btnTrash?.text = getString(R.string.key_empty_trash)
        SingletonPrivateFragment.getInstance()?.onUpdateView()
        ServiceManager.getInstance()?.onPreparingSyncData()
    })
}

private fun TrashAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(TrashViewModel::class.java)
}