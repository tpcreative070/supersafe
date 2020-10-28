package co.tpcreative.supersafe.ui.help
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.HelpAndSupport
import co.tpcreative.supersafe.ui.help.HelpAndSupportCell
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
    val sh: SectionHeaderProvider<HelpAndSupport> = object : SimpleSectionHeaderProvider<HelpAndSupport>() {
        override fun getSectionHeaderView(history: HelpAndSupport, i: Int): View {
            val view: View = LayoutInflater.from(getContext()).inflate(R.layout.help_support_item_header, null, false)
            val textView: AppCompatTextView? = view.tvHeader
            textView?.text = (history.getCategoryName())
            return view
        }

        override fun isSameSection(history: HelpAndSupport, nextHistory: HelpAndSupport): Boolean {
            return history.getCategoryId() == nextHistory.getCategoryId()
        }

        override fun isSticky(): Boolean {
            return false
        }
    }
    recyclerView?.setSectionHeader(sh)
}

fun HelpAndSupportAct.bindData() {
    val Galaxys: MutableList<HelpAndSupport>? = presenter?.mList
    //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
    val cells: MutableList<HelpAndSupportCell>? = ArrayList()
    //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
    if (Galaxys != null) {
        for (galaxy in Galaxys) {
            val cell = HelpAndSupportCell(galaxy)
            cell.setListener(this)
            cells?.add(cell)
        }
    }
    recyclerView?.addCells(cells!!)
}
