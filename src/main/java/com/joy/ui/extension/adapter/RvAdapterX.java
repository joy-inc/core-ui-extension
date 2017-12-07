package com.joy.ui.extension.adapter;

import android.support.annotation.LayoutRes;

/**
 * Created by Daisw on 2017/11/5.
 */

public abstract class RvAdapterX<T extends RvEntityX> extends RvAdapter<T> {

    protected void setItemView(int viewType, @LayoutRes int layoutId) {
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
    protected final void invalidate(RvViewHolder<T> viewHolder, int position, T t) {
        invalidate(viewHolder.getItemViewType(), viewHolder, position, t);
    }

    protected abstract void invalidate(int viewType, RvViewHolder<T> viewHolder, int position, T t);
}
