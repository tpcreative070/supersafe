package co.tpcreative.supersafe.ui.move_gallery;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.util.Configuration;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.common.views.VerticalSpaceItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;


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
        List<Items> getListItems();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAlbumColumnNumber = getGallerWidth(container) / dp2px(mConfig.photoMaxWidth * 1.5f);
        return inflater.inflate(R.layout.layout_fragment_root, container, false);
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

                        MainCategories item = MainCategories.getInstance().getTrashItem();
                        String result = item.categories_hex_name;
                        if (base64Code.equals(result)){
                            Toast.makeText(getActivity(),"This name already existing",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            boolean response = MainCategories.getInstance().onAddCategories(base64Code,value,mConfig.isFakePIN);
                            if (response){
                                Toast.makeText(getActivity(),"Created album successful",Toast.LENGTH_SHORT).show();
                                presenter.getData(mConfig.localCategoriesId,mConfig.isFakePIN);
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
                GalleryCameraMediaManager.getInstance().onUpdatedView();
                if (mListener!=null){
                    mListener.onMoveAlbumSuccessful();
                }
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Items object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List<Items> list) {

    }

    @Override
    public List<Items> getListItems() {
        if (mListener!=null){
            return mListener.getListItems();
        }
        return null;
    }
}
