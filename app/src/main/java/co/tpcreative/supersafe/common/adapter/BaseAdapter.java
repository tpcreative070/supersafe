package co.tpcreative.supersafe.common.adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.tpcreative.supersafe.common.util.Utils;

public class BaseAdapter<V, VH extends BaseHolder> extends RecyclerView.Adapter<VH> {

    protected LayoutInflater inflater;
    protected List<V> dataSource = Collections.emptyList();
    private static final String TAG = BaseAdapter.class.getSimpleName();


    public BaseAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (holder instanceof FooterViewHolder){
            return;
        }
        holder.bind(dataSource.get(position), position);
        holder.event();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    public V getItem(int position){
        return dataSource.get(position);
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public void setDataSource(List<V> dataSource) {
      try {
        this.dataSource = new ArrayList<>(dataSource);
        notifyDataSetChanged();
      }catch (IllegalStateException e){      }
    }

    public List<V> getDataSource() {
        return dataSource;
    }

    public void appendItem(V item) {
        if (this.dataSource.isEmpty()) {
            this.dataSource = new ArrayList<>();
        }
        this.dataSource.add(item);
        notifyItemInserted(getItemCount());
    }

    public void removeAtPosition(int position){
        if(this.dataSource.size()> position){
            dataSource.remove(position);
            notifyItemRangeRemoved(position, 1);
        }
    }

    public void removeAt(int position) {
        try {
            Utils.Log(TAG,"Position "+ position +" size of dataSource "+dataSource.size());
            if(this.dataSource.size()> position){
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,dataSource.size());
            }
            else{
                Utils.Log(TAG,"Can not delete ");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void appendItems(@NonNull List<V> items) {
        if (dataSource.isEmpty()) {
            setDataSource(items);
        } else {
            int positionStart = getItemCount() - 1;
            dataSource.addAll(items);
            notifyItemRangeInserted(positionStart, items.size());
        }
    }

    public void addItemAtFirst(V item) {
        if (this.dataSource.isEmpty()) {
            this.dataSource = new ArrayList<>();
        }
        this.dataSource.add(0, item);
        notifyItemInserted(0);
    }

    public void addAtFirstAndRemoveEnd(V item) {
        if (this.dataSource.isEmpty()) {
            this.dataSource = new ArrayList<>();
        }
        this.dataSource.add(0, item);
        this.dataSource.remove(getItemCount() - 1);
        notifyItemRemoved(getItemCount() - 1);
        notifyItemInserted(0);
    }
}
