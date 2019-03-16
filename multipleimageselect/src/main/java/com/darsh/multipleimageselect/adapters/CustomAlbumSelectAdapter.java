package com.darsh.multipleimageselect.adapters;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.models.EnumFormatType;
import com.darsh.multipleimageselect.models.MimeTypeFile;
import com.darsh.multipleimageselect.models.Utils;
import org.apache.commons.io.FilenameUtils;
import java.util.ArrayList;


public class CustomAlbumSelectAdapter extends CustomGenericAdapter<Album> {
    public CustomAlbumSelectAdapter(Context context, ArrayList<Album> albums) {
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
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.ic_music)
                .priority(Priority.HIGH);


        final Album data = arrayList.get(position);


        try {
            String extensionFile = Utils.getFileExtension(data.cover);
            Log.d("value customer",extensionFile);
            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(extensionFile);
            if (mimeTypeFile != null) {
                EnumFormatType formatTypeFile = EnumFormatType.values()[mimeTypeFile.formatType.ordinal()];
                switch (formatTypeFile){
                    case AUDIO:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                        Glide.with(context)
                                .load(R.drawable.image_background_audio_video)
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
