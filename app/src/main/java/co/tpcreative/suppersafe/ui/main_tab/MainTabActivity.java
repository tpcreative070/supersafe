package co.tpcreative.suppersafe.ui.main_tab;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.ui.me.MeFragment;
import co.tpcreative.suppersafe.ui.privates.PrivateFragment;

public class MainTabActivity extends BaseActivity {

    private static final String TAG = MainTabActivity.class.getSimpleName();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        saveFile(getApplicationContext(),"Hello World");
        String result = load(getApplicationContext());
        Log.d(TAG,"result : "+ result);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        PrefsController.putBoolean(getString(R.string.key_running),true);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PrivateFragment(), getString(R.string.privates));
        adapter.addFragment(new MeFragment(), getString(R.string.me));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public boolean saveFile(Context context, String mytext){
        Log.i("TESTE", "SAVE");
        try {
            FileOutputStream fos = context.openFileOutput("file_name"+".txt",Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(mytext);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String load(Context context){
        Log.i("TESTE", "FILE");
        try {
            FileInputStream fis = context.openFileInput("file_name"+".txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line= r.readLine();
            r.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TESTE", "FILE - false");
            return null;
        }
    }


}
