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
