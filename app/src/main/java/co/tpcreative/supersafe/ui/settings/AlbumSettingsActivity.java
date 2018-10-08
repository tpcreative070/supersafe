package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
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
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;


public class AlbumSettingsActivity extends BaseActivity implements BaseView {

    private static final String TAG = AlbumSettingsActivity.class.getSimpleName();
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

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = Fragment.instantiate(this, AlbumSettingsActivity.SettingsFragment.class.getName());
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        onDrawOverLay(this);
        super.onResume();
        onRegisterHomeWatcher();
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        /**
         * The {@link ListPreference}.
         */

        private Preference mName;

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
                    if (preference instanceof Preference){
                        if (preference.getKey().equals(getString(R.string.key_name))){
                            String main = Utils.getHexCode(getString(R.string.key_main_album));
                            String trash = Utils.getHexCode(getString(R.string.key_trash));
                            if (!main.equals(presenter.mMainCategories.categories_hex_name) && !trash.equals(presenter.mMainCategories.categories_hex_name)){
                                onShowChangeCategoriesNameDialog();
                            }
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
            mName = findPreference(getString(R.string.key_name));
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
                            String result = item.categories_hex_name;
                            String main = Utils.getHexCode(getString(R.string.key_main_album));

                            if (presenter.mMainCategories==null){
                                Toast.makeText(getContext(),"Can not change category name",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else if (base64Code.equals(result)){
                                Toast.makeText(getContext(),"This name already existing",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else if (base64Code.equals(main)){
                                Toast.makeText(getContext(),"This name already existing",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else{
                                presenter.mMainCategories.categories_name = value;
                                boolean response = MainCategories.getInstance().onChangeCategories(presenter.mMainCategories);
                                if (response){
                                    Toast.makeText(getContext(),"Changed album successful",Toast.LENGTH_SHORT).show();
                                    mName.setSummary(presenter.mMainCategories.categories_name);

                                    if (!presenter.mMainCategories.isFakePin){
                                        ServiceManager.getInstance().onGetListCategoriesSync();
                                    }
                                }
                                else{
                                    Toast.makeText(getContext(),"Album name already existing.",Toast.LENGTH_SHORT).show();
                                }

                                if (!presenter.mMainCategories.isFakePin){
                                    SingletonPrivateFragment.getInstance().onUpdateView();
                                }
                            }
                        }
                    });
            builder.show();
        }
    }


    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }


}
