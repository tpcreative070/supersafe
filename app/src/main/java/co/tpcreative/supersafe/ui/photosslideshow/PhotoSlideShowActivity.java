package co.tpcreative.supersafe.ui.photosslideshow;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;
import java.io.File;
import javax.crypto.Cipher;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class PhotoSlideShowActivity extends BaseActivity implements View.OnClickListener ,PhotoSlideShowView{

    private static final String TAG = PhotoSlideShowActivity.class.getSimpleName();
    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400,600)
            .placeholder(R.color.black38)
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
    private AlertDialog dialog;
    private Cipher mCipher;

    private Disposable subscriptions;
    private boolean isProgressing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_slideshow);
        storage = new Storage(this);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        presenter = new PhotoSlideShowPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);

        //Utils.showGotItSnackbar(findViewById(R.id.coordinator), R.string.custom_objects_hint);
        viewPager = findViewById(R.id.view_pager);
        adapter = new SamplePagerAdapter(this);
        viewPager.setAdapter(adapter);
        imgArrowBack.setOnClickListener(this);
        imgOverflow.setOnClickListener(this);
        imgDelete.setOnClickListener(this);
        imgExport.setOnClickListener(this);
        imgRotate.setOnClickListener(this);
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
            //PhotoView photoView = new PhotoView(container.getContext());

            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.content_view, null);
            PhotoView photoView = myView.findViewById(R.id.imgPhoto);
            ImageView imgPlayer = myView.findViewById(R.id.imgPlayer);


            final Items mItems = presenter.mList.get(position);
            EnumFormatType enumTypeFile = EnumFormatType.values()[mItems.formatType];

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

            imgPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Items items = presenter.mList.get(viewPager.getCurrentItem());
                    Navigator.onPlayer(PhotoSlideShowActivity.this,items);
                }
            });

            try{
                String path = mItems.thumbnailPath;
                File file = new File(""+path);
                if (file.exists() || file.isFile()){
                    Glide.with(context)
                            .load(storage.readFile(path))
                            .apply(options)
                            .into(photoView);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }


            switch (enumTypeFile){
                case VIDEO:{
                    imgPlayer.setVisibility(View.VISIBLE);
                    break;
                }
                default:{
                    imgPlayer.setVisibility(View.INVISIBLE);
                    break;
                }
            }
            container.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            photoView.setTag("myview" + position);
            return myView;
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
                               else{
                                   onBackPressed();
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
        switch (view.getId()){
            case R.id.imgArrowBack :{
                if (isHide){
                    break;
                }
                onBackPressed();
                break;
            }
            case R.id.imgOverflow :{
                openOptionMenu(view);
                break;
            }
            case R.id.imgDelete :{
                if (isHide){
                    break;
                }
                onAskDelete();
                break;
            }
            case R.id.imgExport :{
                if (isHide){
                    break;
                }
                Utils.Log(TAG,"Action here");
                try {
                    if (presenter.mList!=null){
                        if (presenter.mList.size()>0){
                            final Items items = presenter.mList.get(viewPager.getCurrentItem());
                            String  input = items.originalPath;
                            if (storage.isFileExist(input)){
                                String output = SuperSafeApplication.getInstance().getSuperSafe()+items.originalName +items.fileExtension;
                                Utils.Log(TAG,output);
                                exportFile(output,input);
                            }
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imgRotate : {
                if (isHide){
                    break;
                }
                try {
                    if (isProgressing){
                        return;
                    }
                    final Items items = InstanceGenerator.getInstance(this).getItemId(presenter.mList.get(viewPager.getCurrentItem()).local_id);
                    if (items!=null) {
                        onRotateBitmap(items);
                        isReload = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        if (presenter.mList.size()==0){
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"Destroy");
        if (subscriptions!=null){
            subscriptions.dispose();
        }
        try {
            storage.deleteFile(Utils.getPackagePath(getApplicationContext()).getAbsolutePath());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStartProgressing(){
        if (dialog==null){
            dialog = new SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage(getString(R.string.progressing))
                    .setCancelable(true)
                    .build();
        }
        if (!dialog.isShowing()){
            dialog.show();
        }
    }

    public void onStopProgressing(){
        if (dialog!=null){
            dialog.dismiss();
        }
    }

    public void exportFile(String output,String input){
        onStartProgressing();
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mCipher = storage.getCipher(Cipher.DECRYPT_MODE);
        storage.createLargeFile(new File(output), new File(input),mCipher, new OnStorageListener() {
            @Override
            public void onSuccessful() {
                onStopProgressing();
                Utils.Log(TAG,"Exporting successful");
                Toast.makeText(PhotoSlideShowActivity.this,"Exported successful",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailed() {
                onStopProgressing();
                Utils.Log(TAG,"Exporting failed");
            }
        });
    }

    public void onRotateBitmap(final Items items){
        subscriptions = Observable.create(subscriber -> {
            isProgressing = true;
            Utils.Log(TAG,"Start Progressing encrypt thumbnail data");
            final  Items mItem = items;
            final String mThumbnailPath = mItem.thumbnailPath;
            final String mOriginalPath = mItem.originalPath;
            int mDegrees = mItem.degrees;

            if (mDegrees>=360){
                mDegrees = 90;
            }
            else{
                if (mDegrees>90){
                    mDegrees = mDegrees+90;
                }
                else{
                    mDegrees = 180;
                }
            }

            final  int valueDegrees = mDegrees;
            mItem.degrees = valueDegrees;

            final File mPath = Utils.getPackagePath(getApplicationContext());
            try {
                storage.createFile(mPath, new File(mOriginalPath),Cipher.DECRYPT_MODE, new OnStorageListener() {
                    @Override
                    public void onSuccessful() {
                        Bitmap thumbImage = null;
                        try {
                            Utils.Log(TAG,"degrees :" +valueDegrees );
                            thumbImage = Utils.getThumbnailScaleRotate(mPath.getAbsolutePath(),valueDegrees);
                            storage.createFile(mThumbnailPath, thumbImage);
                            subscriber.onNext(mItem);
                            subscriber.onComplete();
                        }
                        catch (Exception e) {
                            subscriber.onNext(null);
                            subscriber.onComplete();
                            e.printStackTrace();
                        }
                        finally {
                        }

                    }
                    @Override
                    public void onFailed() {
                        subscriber.onNext(null);
                        subscriber.onComplete();
                    }
                });
            } catch (Exception e) {
                subscriber.onNext(null);
                subscriber.onComplete();
                e.printStackTrace();
            } finally {
                isProgressing = false;
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final Items mItem = (Items) response;
                    if (mItem!=null){
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewPager.getAdapter().notifyDataSetChanged();
                            }
                        });
                        Utils.Log(TAG,"Thumbnail saved successful");
                    }
                    else{
                        Utils.Log(TAG,"Thumbnail saved failed");
                    }
                });
    }

}
