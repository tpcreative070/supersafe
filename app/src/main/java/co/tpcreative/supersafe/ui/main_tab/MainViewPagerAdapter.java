package co.tpcreative.supersafe.ui.main_tab;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.ui.me.MeFragment;
import co.tpcreative.supersafe.ui.privates.PrivateFragment;
/**
 *
 */
public class MainViewPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<Fragment> fragments = new ArrayList<>();
	private ArrayList<String> arrayList = new ArrayList<>();
	private static final String TAG = MainViewPagerAdapter.class.getSimpleName();
	private Fragment currentFragment;

	public MainViewPagerAdapter(FragmentManager fm) {
		super(fm);
		fragments.clear();
		arrayList.clear();
		arrayList.add("PRIVATE");
		arrayList.add("ME");
		fragments.add(PrivateFragment.newInstance(0));
		fragments.add(MeFragment.newInstance(1));
	}

	@Override
	public Fragment getItem(int position) {
		Utils.Log(TAG,"position :" + position);
		return fragments.get(position);
		//return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (getCurrentFragment() != object) {
			currentFragment = ((Fragment) object);
			if (currentFragment instanceof PrivateFragment){
				Utils.Log(TAG,"history");
				currentFragment.onResume();
				fragments.get(0).onPause();
			}
			else if (currentFragment instanceof MeFragment){
				Utils.Log(TAG,"generate");
				currentFragment.onResume();
				fragments.get(1).onPause();
			}
		}
		super.setPrimaryItem(container, position, object);
	}

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return arrayList.get(position);
    }

    /**
	 * Get the current fragment
	 */

	public Fragment getCurrentFragment() {
		return currentFragment;
	}

}