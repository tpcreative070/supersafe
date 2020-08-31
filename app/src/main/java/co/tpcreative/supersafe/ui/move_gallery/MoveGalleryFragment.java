package co.tpcreative.supersafe.ui.move_gallery;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.util.Configuration;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.common.views.VerticalSpaceItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;


public class MoveGalleryFragment extends Fragment implements MoveGalleryAdapter.ItemSelectedListener ,MoveGalleryView{

    private final String  TAG = MoveGalleryFragment.class.getSimpleName();
    private int mAlbumColumnNumber;
    private MoveGalleryAdapter mAdapterAlbumGrid;
    private Configuration mConfig;
    private BottomSheetDialog dialog;
    private BottomSheetBehavior mBehavior;
    private OnGalleryAttachedListener mListener;
    private MoveGalleryPresenter presenter;
    public interface OnGalleryAttachedListener {
        Configuration getConfiguration();
        List<ItemModel> getListItems();
        void onMoveAlbumSuccessful();
    }

    public static Fragment newInstance() {
        return new MoveGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = mListener.getConfiguration();
        if (mConfig==null){
            return;
        }
        mAdapterAlbumGrid = new MoveGalleryAdapter(getActivity().getLayoutInflater(),getActivity(),this);
        presenter = new MoveGalleryPresenter();
        presenter.bindView(this);
        Utils.Log(TAG,new Gson().toJson(mConfig));
        presenter.getData(mConfig.localCategoriesId,mConfig.isFakePIN);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case UPDATE_MOVE_NEW_ALBUM:{
                if (mConfig!=null){
                    presenter.getData(mConfig.localCategoriesId,mConfig.isFakePIN);
                    Utils.Log(TAG,"Updated UI => Warning categories id is null");
                }
                break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            mAlbumColumnNumber = getGallerWidth(container) / dp2px(mConfig.photoMaxWidth * 1.5f);
            return inflater.inflate(R.layout.layout_fragment_root, container, false);
        }
        catch (Exception e){
            mAlbumColumnNumber = getGallerWidth(container) / dp2px(120 * 1.5f);
            return inflater.inflate(R.layout.layout_fragment_root, container, false);
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            Utils.Log(TAG,"Register Listener");
            mListener = (OnGalleryAttachedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnGalleryAttachedListener");
        }
    }

    @Override
    public void onClickGalleryItem(int position) {
        presenter.onMoveItemsToAlbum(position);
        Utils.Log(TAG,"Position :"+ position);
    }

    public void openAlbum() {

        if (mConfig==null){
            dialog.dismiss();
            return;
        }

        int screenHeight = Utils.getScreenHeight(getActivity());
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            return;
        }
        dialog = new BottomSheetDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_gallery, null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
        view.setLayoutParams(lp);
        RecyclerView mGalleryView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayout llCreateAlbum = view.findViewById(R.id.llCreateAlbum);

        llCreateAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowDialog();
            }
        });

        if (mConfig.dialogMode >= Configuration.DIALOG_GRID) {
            mGalleryView.setLayoutManager(new GridLayoutManager(getActivity(), mAlbumColumnNumber));
            mGalleryView.addItemDecoration(new GridSpacingItemDecoration(mAlbumColumnNumber, dp2px(mConfig.spaceSize), true));
            mGalleryView.setItemAnimator(new DefaultItemAnimator());
            mGalleryView.setAdapter(mAdapterAlbumGrid);
            mAdapterAlbumGrid.setDataSource(presenter.mList);

        }else {
            mGalleryView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mGalleryView.addItemDecoration(new VerticalSpaceItemDecoration(dp2px(mConfig.spaceSize)));
            mGalleryView.setItemAnimator(new DefaultItemAnimator());
            mGalleryView.setAdapter(mAdapterAlbumGrid);
            mAdapterAlbumGrid.setDataSource(presenter.mList);
        }

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());

        if (mConfig.dialogHeight < 0) {
            mBehavior.setPeekHeight(mConfig.dialogHeight <= Configuration.DIALOG_HALF ? screenHeight / 2 : screenHeight);
        } else {
            mBehavior.setPeekHeight(mConfig.dialogHeight >= screenHeight ? screenHeight : mConfig.dialogHeight);
        }
        dialog.show();
    }

    public void onShowDialog(){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String value = input.toString();
                        String base64Code = Utils.getHexCode(value);
                        MainCategoryModel item = SQLHelper.getTrashItem();
                        String result = item.categories_hex_name;
                        if (base64Code.equals(result)){
                            Toast.makeText(getActivity(),"This name already existing",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            boolean response = SQLHelper.onAddCategories(base64Code,value,mConfig.isFakePIN);
                            if (response){
                                Toast.makeText(getActivity(),"Created album successful",Toast.LENGTH_SHORT).show();
                                presenter.getData(mConfig.localCategoriesId,mConfig.isFakePIN);
                                SingletonPrivateFragment.getInstance().onUpdateView();
                                ServiceManager.getInstance().onPreparingSyncCategoryData();
                            }
                            else{
                                Toast.makeText(getActivity(),"Album name already existing",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        builder.show();
    }

    private int dp2px(float dp) {
        return (int) (dp * getActivity().getResources().getDisplayMetrics().density + 0.5f);
    }

    private int getGallerWidth(ViewGroup container) {
        return Utils.getScreenWidth(getActivity()) - container.getPaddingLeft() - container.getPaddingRight();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case RELOAD:{
                if (mAdapterAlbumGrid!=null){
                    mAdapterAlbumGrid.setDataSource(presenter.mList);
                }
                break;
            }
            case MOVE:{
                dialog.dismiss();
                EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM);
                if (mListener!=null){
                    mListener.onMoveAlbumSuccessful();
                }
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, ItemModel object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List<ItemModel> list) {

    }

    @Override
    public List<ItemModel> getListItems() {
        if (mListener!=null){
            return mListener.getListItems();
        }
        return null;
    }
}
