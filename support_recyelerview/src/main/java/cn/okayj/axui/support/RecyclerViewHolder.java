package cn.okayj.axui.support;

import android.support.v7.widget.RecyclerView;
import cn.okayj.axui.viewholder.ViewHolder;

/**
 * Created by huangkaijie on 16/8/20.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder{
    private ViewHolder innerHolder;

    public RecyclerViewHolder(ViewHolder viewHolder) {
        super(viewHolder.itemView);
        innerHolder = viewHolder;
    }

    public ViewHolder asNormalViewHolder(){
        return innerHolder;
    }
}
