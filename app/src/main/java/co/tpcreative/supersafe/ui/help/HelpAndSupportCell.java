package co.tpcreative.supersafe.ui.help;

/**
 * Created by Oclemy on 2017 for ProgrammingWizards TV Channel and http://www.camposha.info.
 - Our galaxycell class
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.model.HelpAndSupport;


public class HelpAndSupportCell extends SimpleCell<HelpAndSupport,HelpAndSupportCell.ViewHolder> {


    private ItemSelectedListener listener;

    private static final String TAG = HelpAndSupportCell.class.getSimpleName();

    public HelpAndSupportCell(@NonNull HelpAndSupport item) {
        super(item);
    }

    protected void setListener(ItemSelectedListener listener){
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.help_support_items;
    }

    /*
    - Return a ViewHolder instance
     */
    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, View cellView) {
        return new ViewHolder(cellView);
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder viewHolder,final int i, @NonNull Context context, Object o) {
          final HelpAndSupport data = getItem();
          viewHolder.tvTitle.setText(data.title);
          if (data.nummberName!=null){
              viewHolder.tvPosition.setText(data.nummberName);
              viewHolder.imgIcon.setVisibility(View.GONE);
          }
          viewHolder.llHome.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if (listener!=null){
                      listener.onClickItem(i);
                  }
              }
          });
    }
    /**
     - Our ViewHolder class.
     - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     * */
    static class ViewHolder extends SimpleViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        @BindView(R.id.llHome)
        LinearLayout llHome;
        @BindView(R.id.tvPosition)
        TextView tvPosition;
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public  interface ItemSelectedListener{
        void onClickItem(int position);
    }
}

