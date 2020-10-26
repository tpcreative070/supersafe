package co.tpcreative.supersafe.ui.main_tab
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.ui.me.MeFragment
import co.tpcreative.supersafe.ui.privates.PrivateFragment
import java.util.*
/**
 *
 */
class MainViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val fragments: ArrayList<Fragment>? = ArrayList()
    private val arrayList: ArrayList<String>? = ArrayList()
    private var currentFragment: Fragment? = null
    override fun getItem(position: Int): Fragment {
        Utils.Log(TAG, "position :$position")
        return fragments?.get(position)!!
        //return fragments.get(position);
    }

    override fun getCount(): Int {
        return fragments?.size!!
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (getCurrentFragment() !== `object`) {
            currentFragment = `object` as Fragment?
            if (currentFragment is PrivateFragment) {
                Utils.Log(TAG, "history")
                currentFragment?.onResume()
                fragments?.get(0)?.onPause()
            } else if (currentFragment is MeFragment) {
                Utils.Log(TAG, "generate")
                currentFragment?.onResume()
                fragments?.get(1)?.onPause()
            }
        }
        super.setPrimaryItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return arrayList?.get(position)
    }

    /**
     * Get the current fragment
     */
    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    companion object {
        private val TAG = MainViewPagerAdapter::class.java.simpleName
    }

    init {
        fragments?.clear()
        arrayList?.clear()
        arrayList?.add("PRIVATE")
        arrayList?.add("ME")
        PrivateFragment.newInstance(0)?.let { fragments?.add(it) }
        MeFragment.newInstance(1)?.let { fragments?.add(it) }
    }
}