package com.joy.ui.extension.adapter;

import android.support.annotation.LayoutRes;

/**
 * Created by Daisw on 2017/11/5.
 */

public abstract class RvAdapterX<T extends RvEntityX> extends RvAdapter<T> {

    public void addLayout(int viewType, @LayoutRes int layoutId) {
        layouts.put(viewType, layoutId);
    }

    @Override
    public int getItemViewType(int position) {
        T t = getItem(position);
        if (t == null) {
            return 0;
        }
        Integer type = t.getType();
        return type == null ? 0 : type.intValue();
    }

    @Override
    public final void invalidate(RvViewHolder<T> viewHolder, int position, T t) {
        invalidate(viewHolder.getItemViewType(), viewHolder, position, t);
    }

    public abstract void invalidate(int viewType, RvViewHolder<T> viewHolder, int position, T t);
}
