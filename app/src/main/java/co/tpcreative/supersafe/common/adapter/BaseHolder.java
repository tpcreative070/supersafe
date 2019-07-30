package co.tpcreative.supersafe.common.adapter;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

public class BaseHolder<V> extends RecyclerView.ViewHolder {
    public BaseHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
    public void bind(V data, int position){}
    public void event(){}
}
