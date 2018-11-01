package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.model.room.InstanceGenerator;


public class AlbumSettingsActivity extends BaseActivity implements BaseView {

    private static final String TAG = AlbumSettingsActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    private static AlbumSettingsPresenter presenter;
    private static Storage storage;

    static RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_settings);
        presenter = new AlbumSettingsPresenter();
        presenter.bindView(this);
        presenter.getData(this);
        onDrawOverLay(this);
        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = new Storage(getApplicationContext());
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        onSetUpPreference();
    }

    @Override
    public void onNotifier(EnumStatus status) {
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(AlbumSettingsActivity.class.getSimpleName());
        presenter.getData();
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

        private MyPreferenceAlbumSettings mName;
        private MyPreferenceAlbumSettings mLockAlbum;
        private MyPreferenceAlbumSettings mAlbumCover;

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
                            String name = preference.getSummary().toString();
                            if (!main.equals(presenter.mMainCategories.categories_hex_name) && !trash.equals(presenter.mMainCategories.categories_hex_name)){
                                onShowChangeCategoriesNameDialog(EnumStatus.CHANGE,name);
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_album_lock))){
                            String name = preference.getSummary().toString();
                            onShowChangeCategoriesNameDialog(EnumStatus.SET,null);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_album_cover))){
                            Navigator.onMoveAlbumCover(getActivity(),presenter.mMainCategories);
                        }
                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*change categories name*/
            mName = (MyPreferenceAlbumSettings) findPreference(getString(R.string.key_name));
            mName.setOnPreferenceChangeListener(createChangeListener());
            mName.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mName.setSummary(presenter.mMainCategories.categories_name);

            mLockAlbum = (MyPreferenceAlbumSettings) findPreference(getString(R.string.key_album_lock));
            mLockAlbum.setOnPreferenceChangeListener(createChangeListener());
            mLockAlbum.setOnPreferenceClickListener(createActionPreferenceClickListener());

            final String isPin = presenter.mMainCategories.pin;
            if (isPin.equals("")){
                mLockAlbum.setSummary(getString(R.string.unlocked));
            }
            else {
                mLockAlbum.setSummary(getString(R.string.locked));
            }

            /*Album cover*/

            mAlbumCover = (MyPreferenceAlbumSettings) findPreference(getString(R.string.key_album_cover));
            mAlbumCover.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mAlbumCover.setOnPreferenceChangeListener(createChangeListener());

            mAlbumCover.setListener(new MyPreferenceAlbumSettings.MyPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    if (mAlbumCover.getImageView()!=null){
                        final MainCategories main = presenter.mMainCategories;
                        if (main.pin.equals("")) {
                            final Items items = Items.getInstance().getObject(main.item);
                            if (items != null) {
                                EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
                                switch (formatTypeFile) {
                                    case AUDIO: {
                                        Theme theme = Theme.getInstance().getThemeInfo();
                                        Drawable note1 = getContext().getResources().getDrawable(theme.getAccentColor());
                                        Glide.with(getContext())
                                                .load(note1)
                                                .apply(options)
                                                .into(mAlbumCover.getImageView());
                                        mAlbumCover.getImgIcon().setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                                        break;
                                    }
                                    case FILES:{
                                        Theme theme = Theme.getInstance().getThemeInfo();
                                        Drawable note1 = getContext().getResources().getDrawable(theme.getAccentColor());
                                        Glide.with(getContext())
                                                .load(note1)
                                                .apply(options)
                                                .into(mAlbumCover.getImageView());
                                        mAlbumCover.getImgIcon().setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_insert_drive_file_white_48));
                                        break;
                                    }
                                    default: {
                                        try {
                                            if (storage.isFileExist("" + items.thumbnailPath)) {
                                                mAlbumCover.getImageView().setRotation(items.degrees);
                                                Glide.with(getContext())
                                                        .load(storage.readFile(items.thumbnailPath))
                                                        .apply(options)
                                                        .into(mAlbumCover.getImageView());
                                                mAlbumCover.getImgIcon().setVisibility(View.INVISIBLE);
                                            } else {
                                                mAlbumCover.getImageView().setImageResource(0);
                                                int myColor = Color.parseColor(main.image);
                                                mAlbumCover.getImageView().setBackgroundColor(myColor);
                                                mAlbumCover.getImgIcon().setImageDrawable(MainCategories.getInstance().getDrawable(getContext(), main.icon));
                                                mAlbumCover.getImgIcon().setVisibility(View.VISIBLE);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                }
                            } else {
                                mAlbumCover.getImageView().setImageResource(0);
                                mAlbumCover.getImgIcon().setImageDrawable(MainCategories.getInstance().getDrawable(getContext(), main.icon));
                                mAlbumCover.getImgIcon().setVisibility(View.VISIBLE);
                                try {
                                    int myColor = Color.parseColor(main.image);
                                    mAlbumCover.getImageView().setBackgroundColor(myColor);
                                } catch (Exception e) {

                                }
                            }
                        }
                        else{
                            mAlbumCover.getImageView().setImageResource(0);
                            mAlbumCover.getImgIcon().setImageResource(R.drawable.baseline_https_white_48);
                            mAlbumCover.getImgIcon().setVisibility(View.VISIBLE);
                            try {
                                int myColor = Color.parseColor(main.image);
                                mAlbumCover.getImageView().setBackgroundColor(myColor);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Utils.Log(TAG,"Log album cover.........");
                    }
                    else{
                        Utils.Log(TAG,"Log album cover is null.........");
                    }
                }
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_album_settings);
        }

        public void onShowChangeCategoriesNameDialog(EnumStatus enumStatus,String name){
            String title = "";
            String content = "";
            String positiveAction = "";
            final String isPin = presenter.mMainCategories.pin;
            int inputType = 0;
            switch (enumStatus){
                case CHANGE:{
                    title = getString(R.string.change_album);
                    content = "";
                    positiveAction = getString(R.string.ok);
                    inputType = InputType.TYPE_CLASS_TEXT;
                    break;
                }
                case SET:{
                    if (!isPin.equals("")){
                        title = getString(R.string.remove_password);
                        content = getString(R.string.enter_a_password_for_this_album);
                        positiveAction = getString(R.string.unlock);
                        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    }
                    else{
                        title = getString(R.string.lock_album);
                        content = getString(R.string.enter_a_password_for_this_album);
                        positiveAction = getString(R.string.lock);
                        inputType = InputType.TYPE_CLASS_TEXT;
                    }
                    break;
                }
            }

            MaterialDialog.Builder builder =  new MaterialDialog.Builder(getActivity())
                    .title(title)
                    .content(content)
                    .theme(com.afollestad.materialdialogs.Theme.LIGHT)
                    .titleColor(getResources().getColor(R.color.black))
                    .inputType(inputType)
                    .autoDismiss(false)
                    .negativeText(getString(R.string.cancel))
                    .positiveText(positiveAction)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .input(null, name, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            switch (enumStatus){
                                case CHANGE:{
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
                                    break;
                                }
                                case SET:{
                                    if (!isPin.equals("")){
                                        if (isPin.equals(input.toString())){
                                            presenter.mMainCategories.pin = "";
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(presenter.mMainCategories);
                                            mLockAlbum.setSummary(getString(R.string.unlocked));
                                            SingletonPrivateFragment.getInstance().onUpdateView();
                                            dialog.dismiss();
                                        }
                                        else{
                                            Utils.showInfoSnackbar(getView(),R.string.wrong_password,true);
                                            dialog.getInputEditText().setText("");
                                        }
                                    }
                                    else{
                                        presenter.mMainCategories.pin = input.toString();
                                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(presenter.mMainCategories);
                                        mLockAlbum.setSummary(getString(R.string.locked));
                                        SingletonPrivateFragment.getInstance().onUpdateView();
                                        dialog.dismiss();
                                    }
                                    break;
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
        switch (status){
            case RELOAD:{
                if (presenter.mMainCategories!=null){
                    setTitle(presenter.mMainCategories.categories_name);
                }
                break;
            }
        }
    }

    public void onSetUpPreference(){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = Fragment.instantiate(this, AlbumSettingsActivity.SettingsFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Navigator.ALBUM_COVER: {
                if (resultCode == RESULT_OK) {
                    onSetUpPreference();
                    SingletonPrivateFragment.getInstance().onUpdateView();
                }
                break;
            }
        }
    }

}
