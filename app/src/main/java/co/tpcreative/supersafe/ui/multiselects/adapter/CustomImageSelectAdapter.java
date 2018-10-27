package co.tpcreative.supersafe.ui.multiselects.adapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import org.apache.commons.io.FilenameUtils;
import java.util.ArrayList;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Image;
import co.tpcreative.supersafe.model.MimeTypeFile;

public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {
    private static final String TAG = CustomImageSelectAdapter.class.getSimpleName();
    private Context context;
    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
        super(context, images);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_image_select, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_image_select);
            viewHolder.imgAudioVideo = (ImageView) convertView.findViewById(R.id.imgAudioVideo);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.view = convertView.findViewById(R.id.view_alpha);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.view.getLayoutParams().width = size;
        viewHolder.view.getLayoutParams().height = size;

        if (arrayList.get(position).isSelected) {
            viewHolder.view.setAlpha(0.5f);
            ((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_done_white));

        } else {
            viewHolder.view.setAlpha(0.0f);
            ((FrameLayout) convertView).setForeground(null);
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .priority(Priority.HIGH);


        final Image data = arrayList.get(position);
        try {
            String extensionFile = FilenameUtils.getExtension(data.path);
            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(extensionFile);
            if (mimeTypeFile != null) {
                EnumFormatType formatTypeFile = EnumFormatType.values()[mimeTypeFile.formatType.ordinal()];

                switch (formatTypeFile){
                    case AUDIO:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                        viewHolder.tvTitle.setVisibility(View.VISIBLE);
                        viewHolder.tvTitle.setText(data.name);
                        Glide.with(context)
                                .load(R.drawable.image_background_audio_video)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                    case VIDEO:{
                        viewHolder.imgAudioVideo.setVisibility(View.VISIBLE);
                        viewHolder.imgAudioVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_videocam_white_36));
                        viewHolder.tvTitle.setVisibility(View.INVISIBLE);;
                        Glide.with(context)
                                .load(data.path)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                    default:{
                        viewHolder.tvTitle.setVisibility(View.INVISIBLE);
                        viewHolder.imgAudioVideo.setVisibility(View.INVISIBLE);
                        Glide.with(context)
                                .load(data.path)
                                .apply(options).into(viewHolder.imageView);
                        break;
                    }
                }
            }
            else {
                viewHolder.tvTitle.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(R.drawable.ic_music)
                        .apply(options).into(viewHolder.imageView);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public ImageView imgAudioVideo;
        public View view;
        public TextView tvTitle;
    }
}
