package cn.okayj.axui.viewholder;

import android.view.View;

import cn.okayj.axui.R;

/**
 * Created by Jack on 16/4/20.
 */
public abstract class NormalViewHolder<T> {
    private static final int ID_VIEW_TAG_HOLDER = R.id.view_tag_holder;

    public final View itemView;

    private T item;

    public NormalViewHolder(View itemView) {
        this.itemView = itemView;
        itemView.setTag(ID_VIEW_TAG_HOLDER,this);
        onBindView(itemView);
    }

    public static Object getHolder(View itemView){
        return itemView.getTag(ID_VIEW_TAG_HOLDER);
    }

    public void setItem(T item){
        this.item = item;
        onSetItem(item);
    }

    protected abstract void onSetItem(T item);

    public T getItem(){
        return item;
    }

    protected void onBindView(View itemView){

    }
}
