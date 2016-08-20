package cn.okayj.axui.adapter;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huangkaijie on 16/8/20.
 */
public abstract class ViewGroupAdapter {
    private ViewGroup viewGroup;

    protected abstract int getCount();
    protected abstract View getView(int position, ViewGroup parent);
    public void bindViewGroup(ViewGroup viewGroup){
        this.viewGroup = viewGroup;
        if(viewGroup != null)
            notifyDataSetChange();
    }

    public void notifyDataSetChange(){
        if(viewGroup == null){
            throw new RuntimeException("no ViewGroup bound");
        }

        viewGroup.removeAllViews();

        int count = getCount();
        if(count < 0){
            throw new RuntimeException("getCount() should return at least 0");
        }

        for (int position = 0 ; position < count ; ++position){
            View view = getView(position,viewGroup);
            viewGroup.addView(view);
        }
    }
}
