/*
 * Copyright 2016 Kaijie Huang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
