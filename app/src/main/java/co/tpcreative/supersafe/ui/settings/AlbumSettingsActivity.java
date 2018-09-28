package co.tpcreative.supersafe.ui.settings;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ftinc.kit.util.SizeUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.preference.MyPreference;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.main_tab.MainTabActivity;
import de.mrapp.android.preference.ListPreference;

public class AlbumSettingsActivity extends BaseActivity implements AlbumSettingsView {

    private static final String TAG = AlbumSettingsActivity.class.getSimpleName();
    private SlidrConfig mConfig;
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    private static AlbumSettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_settings);
        presenter = new AlbumSettingsPresenter();
        presenter.bindView(this);
        presenter.getData(this);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int primary = getResources().getColor(R.color.colorPrimary);
        int secondary = getResources().getColor(R.color.colorPrimaryDark);

        mConfig = new SlidrConfig.Builder()
                .primaryColor(primary)
                .secondaryColor(secondary)
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(this, mConfig);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = Fragment.instantiate(this, AlbumSettingsActivity.SettingsFragment.class.getName());
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        /**
         * The {@link ListPreference}.
         */



        private MyPreference mName;

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type {@link
         * Preference.OnPreferenceChangeListener}
         */

        private Preference.OnPreferenceChangeListener createChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {

                    return true;
                }
            };
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof MyPreference){
                        if (preference.getKey().equals(getString(R.string.key_name))){
                            onShowChangeCategoriesNameDialog();
                        }
                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Help*/
            mName = (MyPreference)findPreference(getString(R.string.key_name));
            mName.setOnPreferenceChangeListener(createChangeListener());
            mName.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mName.setSummary(presenter.mMainCategories.categories_name);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_album_settings);
        }

        public void onShowChangeCategoriesNameDialog(){
            MaterialDialog.Builder builder =  new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.change_album))
                    .theme(Theme.LIGHT)
                    .titleColor(getResources().getColor(R.color.black))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .negativeText(getString(R.string.cancel))
                    .positiveText(getString(R.string.ok))
                    .input(null, null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            Utils.Log(TAG,"Value");

                            String value = input.toString();
                            String base64Code = Utils.getHexCode(value);
                            MainCategories item = MainCategories.getInstance().getTrashItem();
                            String result = item.categories_local_id;

                            if (presenter.mMainCategories==null){
                                Toast.makeText(getContext(),"Can not change category name",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else if (base64Code.equals(result)){
                                Toast.makeText(getContext(),"This name already existing",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else{
                                presenter.mMainCategories.categories_name = value;
                                boolean response = MainCategories.getInstance().onChangeCategories(presenter.mMainCategories);
                                if (response){
                                    Toast.makeText(getContext(),"Changed album successful",Toast.LENGTH_SHORT).show();
                                    mName.setSummary(presenter.mMainCategories.categories_name);
                                    ServiceManager.getInstance().onGetListCategoriesSync(false);
                                }
                                else{
                                    Toast.makeText(getContext(),"Album name already existing",Toast.LENGTH_SHORT).show();
                                }
                                SingletonPrivateFragment.getInstance().onUpdateView();
                            }
                        }
                    });
            builder.show();
        }

    }
}