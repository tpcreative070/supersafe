package co.tpcreative.suppersafe.ui.privates;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.model.Album;

public class PrivateAdapter extends BaseAdapter{

    private final Context mContext;
    private final List<Album> mList;


    public PrivateAdapter(Context context , List<Album> mList) {
        this.mContext = context;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Album book = mList.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.private_gridview_item, null);

            final ImageView imageViewCoverArt = (ImageView)convertView.findViewById(R.id.grid_item_iv);
            final ViewHolder viewHolder = new ViewHolder( imageViewCoverArt);
            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        viewHolder.imageViewFavorite.setImageResource(book.getImageResource());

        return convertView;
    }

    private class ViewHolder {
        private final ImageView imageViewFavorite;
        public ViewHolder(ImageView imageViewFavorite) {
            this.imageViewFavorite = imageViewFavorite;
        }
    }

}
