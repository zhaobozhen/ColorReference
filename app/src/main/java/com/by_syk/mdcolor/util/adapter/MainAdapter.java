/*
  Copyright 2016-2017 By_syk

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.by_syk.mdcolor.util.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.by_syk.mdcolor.R;
import com.by_syk.mdcolor.util.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2016-03-31.
 */
public class MainAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    private List<Palette> dataList;

    private int checked;

    private static class ViewHolder {
        TextView tvName;
        TextView tvHex;
        ImageView ivHead;
    }

    public MainAdapter(Context context, int checked) {
        this.context = context;
        this.checked = checked;

        layoutInflater = LayoutInflater.from(context);

        /*
         * 不直接引用传入的List对象：
         * this.dataList = dataList;
         * 避免不可控的数据变更导致崩溃：
         * java.lang.IllegalStateException:
         *     The content of the adapter has changed but ListView did not receive a notification.
         * 因此采用复制一份数据的方案，完全由该Adapter对象维护。
         */
        dataList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Palette getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
         * 使用ViewHolder模式来避免没有必要的调用findViewById()：因为太多的findViewById也会影响性能
         * ViewHolder模式通过getView()方法返回的视图的标签(Tag)中存储一个数据结构，
         * 这个数据结构包含了指向我们要绑定数据的视图的引用，从而避免每次调用getView()的时候调用findViewById()
         */
        ViewHolder viewHolder;

        // 重用缓存convertView传递给getView()方法来避免填充不必要的视图
        if (convertView == null) {
            /* 避免这样使用：
             *     layoutInflater.inflate(R.layout.list_item, null);
             * 查看
             *     https://possiblemobile.com/2013/05/layout-inflation-as-intended/
             */
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.tvHex = convertView.findViewById(R.id.tv_hex);
            viewHolder.ivHead = convertView.findViewById(R.id.iv_head);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Palette palette = dataList.get(position);

        viewHolder.tvName.setText(palette.getName());
        viewHolder.tvHex.setText(palette.getPrimaryColorStr());

        Drawable drawable = context.getDrawable(position == checked
                ? R.drawable.ic_head_checked : R.drawable.ic_head);
        viewHolder.ivHead.setImageDrawable(drawable);
        // BUG：由于用了缓存，这样变色会导致颜色错乱
        //viewHolder.ivHead.getDrawable().setTint(palette.getPrimaryColor());
        viewHolder.ivHead.setColorFilter(palette.getPrimaryColor());

        return convertView;
    }

    /*public Palette getCheckedItem() {
        if (checked < 0 || checked >= dataList.size()) {
            return null;
        }

        return dataList.get(checked);
    }*/

    public int getChecked() {
        return checked;
    }

    /*public List<Palette> getDataList() {
        return dataList;
    }*/

    public void notifyRefresh(List<Palette> dataList) {
        if (dataList == null) {
            return;
        }

        this.dataList.clear();
        this.dataList.addAll(dataList);

        notifyDataSetChanged();
    }

    /*public void notifyRefreshChecked(int checked) {
        this.checked = checked;

        notifyDataSetChanged();
    }*/
}