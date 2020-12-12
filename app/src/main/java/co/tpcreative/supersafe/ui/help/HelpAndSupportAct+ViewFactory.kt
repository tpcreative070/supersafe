package co.tpcreative.supersafe.ui.help
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.model.HelpAndSupportModel
import co.tpcreative.supersafe.ui.verifyaccount.VerifyAccountAct
import co.tpcreative.supersafe.viewmodel.HelpAndSupportViewModel
import co.tpcreative.supersafe.viewmodel.VerifyAccountViewModel
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import kotlinx.android.synthetic.main.activity_help_and_support_content.toolbar
import kotlinx.android.synthetic.main.activity_help_support.*
import kotlinx.android.synthetic.main.help_support_item_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

fun HelpAndSupportAct.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    getData()
}

fun HelpAndSupportAct.addRecyclerHeaders() {
    val sh: SectionHeaderProvider<HelpAndSupportModel> = object : SimpleSectionHeaderProvider<HelpAndSupportModel>() {
        override fun getSectionHeaderView(history: HelpAndSupportModel, i: Int): View {
            val view: View = LayoutInflater.from(this@addRecyclerHeaders).inflate(R.layout.help_support_item_header, null, false)
            val textView: AppCompatTextView? = view.tvHeader
            textView?.text = (history.getCategoryName())
            return view
        }
        override fun isSameSection(history: HelpAndSupportModel, nextHistory: HelpAndSupportModel): Boolean {
            return history.getCategoryId() == nextHistory.getCategoryId()
        }
        override fun isSticky(): Boolean {
            return false
        }
    }
    recyclerView?.setSectionHeader(sh)
}

fun HelpAndSupportAct.bindData() {
    val mData: MutableList<HelpAndSupportModel> = dataSource
    val cells: MutableList<HelpAndSupportCell> = ArrayList()
    for (index in mData) {
        val cell = HelpAndSupportCell(index)
        cell.setListener(this)
        cells.add(cell)
    }
    recyclerView?.addCells(cells)
}

fun HelpAndSupportAct.getData(){
    viewModel.getData().observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            dataSource.addAll(it)
            addRecyclerHeaders()
            bindData()
        }
    })
}

private fun HelpAndSupportAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(HelpAndSupportViewModel::class.java)
}

