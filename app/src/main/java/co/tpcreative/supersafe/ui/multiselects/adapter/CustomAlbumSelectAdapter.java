package co.tpcreative.supersafe.ui.multiselects.adapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import org.apache.commons.io.FilenameUtils;
import java.util.ArrayList;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.AlbumMultiItems;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.Theme;


public class CustomAlbumSelectAdapter extends CustomGenericAdapter<AlbumMultiItems> {

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(200 ,200)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.ic_music)
            .priority(Priority.HIGH);

    final Theme theme = Theme.getInstance().getThemeInfo();
    Drawable note1 = SuperSafeApplication.getInstance().getResources().getDrawable( theme.getAccentColor());


    public CustomAlbumSelectAdapter(Context context, ArrayList<AlbumMultiItems> albums) {
        super(context, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_album_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_album_image);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view_album_name);
            viewHolder.imgAudioVideo = (ImageView) convertView.findViewById(R.id.imgAudioVideo);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.textView.setText(arrayList.get(position).name);


        final AlbumMultiItems data = arrayList.get(position);


        try {
            String extensionFile = FilenameUtils.getExtension(data.cover);
            Log.d("value customer",extensionFile);
            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(extensionFile);
            if (mimeTypeFile != null) {
                EnumFormatType formatTypeFile = EnumFormatType.values()[mimeTypeFile.formatType.ordinal()];
                switch (formatTypeFile){
                    case AUDIO:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                    case FILES:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_insert_drive_file_white_48));
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                    case VIDEO:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_videocam_white_36));
                        Glide.with(context)
                                .load(data.cover)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                    default:{
                        viewHolder.imgAudioVideo.setVisibility(View.INVISIBLE);
                        Glide.with(context)
                                .load(data.cover)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                }
            }
            else {
                Glide.with(context)
                        .load(R.drawable.ic_music)
                        .apply(options).into(viewHolder.imageView);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d("value customer","error");
        }

        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public ImageView imgAudioVideo;
        public TextView textView;
    }
}
