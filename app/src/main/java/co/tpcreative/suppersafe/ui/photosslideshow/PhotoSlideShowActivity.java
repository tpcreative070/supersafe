package co.tpcreative.suppersafe.ui.photosslideshow;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.snatik.storage.Storage;
import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;

public class PhotoSlideShowActivity extends BaseActivity implements View.OnClickListener ,PhotoSlideShowView{

    private static final String TAG = PhotoSlideShowActivity.class.getSimpleName();
    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400,500)
            .placeholder(R.drawable.ic_camera)
            .error(R.drawable.ic_aspect_ratio)
            .priority(Priority.HIGH);


    @BindView(R.id.rlTop)
    RelativeLayout rlTop;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgOverflow)
    ImageView imgOverflow;
    @BindView(R.id.imgShare)
    ImageView imgShare;
    @BindView(R.id.imgExport)
    ImageView imgExport;
    @BindView(R.id.imgMove)
    ImageView imgMove;
    @BindView(R.id.imgRotate)
    ImageView imgRotate;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    private boolean isHide;

    private PhotoSlideShowPresenter presenter;
    private Storage storage;
    private ViewPager viewPager;
    private SamplePagerAdapter adapter;
    private boolean isReload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_slideshow);
        storage = new Storage(this);
        presenter = new PhotoSlideShowPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);

        //Utils.showGotItSnackbar(findViewById(R.id.coordinator), R.string.custom_objects_hint);
        viewPager = findViewById(R.id.view_pager);
        adapter = new SamplePagerAdapter(this);
        viewPager.setAdapter(adapter);
        imgArrowBack.setOnClickListener(this);
        imgOverflow.setOnClickListener(this);
    }

    class SamplePagerAdapter extends PagerAdapter {
        private Context context;
        SamplePagerAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return presenter.mList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    Log.d(TAG,"on Clicked");
                    isHide = !isHide;
                    if (isHide){
                        Utils.slideToTopHeader(rlTop);
                        Utils.slideToBottomFooter(llBottom);
                    }
                    else {
                        Utils.slideToBottomHeader(rlTop);
                        Utils.slideToTopFooter(llBottom);
                    }
                }
            });
            try{
                String path = presenter.mList.get(position).thumbnailPath;
                File file = new File(""+path);
                if (file.exists() || file.isFile()){
                    storage.setEncryptConfiguration(SupperSafeApplication.getInstance().getConfigurationFile());
                    Glide.with(context)
                            .load(storage.readFile(path))
                            .apply(options)
                            .into(photoView);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }

    }

    @OnClick(R.id.imgDelete)
    public void onClickedDelete(View view){
        onAskDelete();
    }


    public void onAskDelete(){
        new MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .content(getString(R.string.ask_delete))
                .positiveText(getString(R.string.ok))
                .negativeText(getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.Log(TAG,"position :" + viewPager.getCurrentItem());
                       try {
                           if (presenter.mList!=null){
                               if (presenter.mList.size()>0){
                                   presenter.onDelete(viewPager.getCurrentItem());
                               }
                           }
                       }
                       catch (Exception e){
                           e.printStackTrace();
                       }
                    }
                })
                .show();
    }

    @Override
    public void onClick(View view) {
        if (isHide){
            return;
        }
        switch (view.getId()){
            case R.id.imgArrowBack :{
                onBackPressed();
                break;
            }
            case R.id.imgOverflow :{
                openOptionMenu(view);
                break;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public void openOptionMenu(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_slideshow, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){

                }
                return true;
            }
        });
        popup.show();
    }



    /*ViewPresenter*/

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void onBackPressed() {
        if (isReload){
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
        }
        super.onBackPressed();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onDeleteSuccessful() {
        isReload = true;
       adapter.notifyDataSetChanged();
    }
}
