package cn.okayj.axui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Jack on 16/5/11.
 */
public class SupportRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    T mItem;

    public SupportRecyclerViewHolder(View itemView) {
        super(itemView);
        onBindView(itemView);
    }

    public T getItem() {
        return mItem;
    }

    public void setItem(T item) {
        this.mItem = item;
        onDataSet(item);
    }

    public void onBindView(View itemView){

    }

    public void onDataSet(T item){

    }
}
