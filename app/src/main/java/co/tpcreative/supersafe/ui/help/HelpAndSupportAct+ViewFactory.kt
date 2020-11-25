package co.tpcreative.supersafe.ui.help
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.HelpAndSupportModel
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import kotlinx.android.synthetic.main.activity_help_and_support_content.toolbar
import kotlinx.android.synthetic.main.activity_help_support.*
import kotlinx.android.synthetic.main.help_support_item_header.view.*
import java.util.ArrayList

fun HelpAndSupportAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = HelpAndSupportPresenter()
    presenter?.bindView(this)
    presenter?.onGetList()
    addRecyclerHeaders()
    bindData()
}

fun HelpAndSupportAct.addRecyclerHeaders() {
    val sh: SectionHeaderProvider<HelpAndSupportModel> = object : SimpleSectionHeaderProvider<HelpAndSupportModel>() {
        override fun getSectionHeaderView(history: HelpAndSupportModel, i: Int): View {
            val view: View = LayoutInflater.from(getContext()).inflate(R.layout.help_support_item_header, null, false)
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
    val mData: MutableList<HelpAndSupportModel>? = presenter?.mList
    //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
    val cells: MutableList<HelpAndSupportCell>? = ArrayList()
    //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
    if (mData != null) {
        for (index in mData) {
            val cell = HelpAndSupportCell(index)
            cell.setListener(this)
            cells?.add(cell)
        }
    }
    recyclerView?.addCells(cells!!)
}
