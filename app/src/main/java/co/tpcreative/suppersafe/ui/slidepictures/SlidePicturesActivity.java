package co.tpcreative.suppersafe.ui.slidepictures;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.ui.demo.Cheeses;

public class SlidePicturesActivity extends BaseActivity {

    private static final String TAG = SlidePicturesActivity.class.getSimpleName();

    @BindView(R.id.imgSlide)
    ImageView imgSlide;
    int orientation  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_pictures);

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "picture.jpg");

        RequestOptions options = new RequestOptions();
        options.centerInside();
        Glide.with(getApplicationContext())
                .load(file)
                .apply(options.fitCenter())
                .into(imgSlide);

    }

    @OnClick(R.id.btnSlide)
    public void onSlide(View view){
        if (orientation==360){
            orientation = 0;
        }
        else{
            orientation+=90;
        }
        Log.d(TAG,"displayOrientation :" + orientation);
        imgSlide.setImageBitmap(getThumbnail(orientation));
    }

    public Bitmap getThumbnail(int mOrientation){
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "picture.jpg");
        final int THUMBSIZE_HEIGHT = 600;
        final int THUMBSIZE_WIDTH = 400;
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(file.getAbsolutePath()),
                THUMBSIZE_HEIGHT,
                THUMBSIZE_WIDTH);
        try {
            Log.d(TAG, "Exif: " + mOrientation);
            Matrix matrix = new Matrix();
            matrix.setRotate(mOrientation);
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return thumbImage;
    }



}
