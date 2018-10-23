package co.tpcreative.supersafe.common.preference;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;


public class MyPreferenceAlbumSettings extends Preference {

    private static final String TAG = MyPreferenceAlbumSettings.class.getSimpleName();
    private Context context;
    private ImageView imageView;
    private ImageView imgIcon;
    private MyPreferenceListener listener;

    public void setListener(MyPreferenceListener listener) {
        this.listener = listener;
    }

    public MyPreferenceAlbumSettings(Context context) {
        super(context);
        this.context = context ;
    }

    public MyPreferenceAlbumSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyPreferenceAlbumSettings(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        imageView = (ImageView) view.findViewById(R.id.imgCover);
        imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
        if (listener!=null){
            listener.onUpdatePreference();
        }
        Utils.Log(TAG,"onBind....");
    }

    public ImageView getImageView() {
        return imageView;
    }

    public ImageView getImgIcon() {
        return imgIcon;
    }

    public interface MyPreferenceListener{
        void onUpdatePreference();
    }

}
