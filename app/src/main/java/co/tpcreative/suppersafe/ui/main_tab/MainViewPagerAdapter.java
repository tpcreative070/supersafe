package co.tpcreative.suppersafe.ui.main_tab;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import java.util.ArrayList;
import co.tpcreative.suppersafe.ui.me.MeFragment;
import co.tpcreative.suppersafe.ui.privates.PrivateFragment;


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
        Log.d(TAG,"position :" + position);
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
				Log.d(TAG,"history");
				currentFragment.onResume();
				fragments.get(0).onPause();
			}
			else if (currentFragment instanceof MeFragment){
				Log.d(TAG,"generate");
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