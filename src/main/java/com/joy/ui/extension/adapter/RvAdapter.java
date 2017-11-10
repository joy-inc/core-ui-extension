package com.joy.ui.extension.adapter;

import android.support.annotation.LayoutRes;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.joy.ui.adapter.ExRvAdapter;
import com.joy.ui.extension.R;
import com.joy.utils.CollectionUtil;

/**
 * Created by Daisw on 2017/11/5.
 */

public abstract class RvAdapter<T> extends ExRvAdapter<RvViewHolder<T>, T> {

    SparseIntArray layouts;

    public RvAdapter() {
        layouts = new SparseIntArray();
        addLayouts();
    }

    public abstract void addLayouts();

    public void addLayout(@LayoutRes int layoutId) {
        layouts.put(0, layoutId);
    }

    @Override
    public final RvViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RvViewHolder<>(inflateLayout(parent, layouts.get(viewType)));
    }

    @Override
    public final void onBindViewHolder(RvViewHolder<T> viewHolder, int position) {
        invalidate(viewHolder, position, getItem(position));
    }

    public abstract void invalidate(RvViewHolder<T> viewHolder, int position, T t);

    @Override
    protected void bindOnClickListener(RvViewHolder<T> viewHolder, View... targetViews) {
        if (CollectionUtil.isEmpty(targetViews)) {
            if (isViewHoldClickListener(viewHolder.getItemView())) {
                return;
            }
            targetViews = new View[]{viewHolder.getItemView()};
        }
        int position = viewHolder.getAdapterPosition() - getHeadersCount();
        for (View targetView : targetViews) {
            if (!isViewHoldClickListener(targetView)) {
                setViewHoldClickListener(targetView);
                targetView.setOnClickListener((v) -> callbackOnItemClickListener(position, v));
            }
        }
    }

    @Override
    protected void bindOnLongClickListener(RvViewHolder<T> viewHolder, View... targetViews) {
        if (CollectionUtil.isEmpty(targetViews)) {
            if (isViewHoldLongClickListener(viewHolder.getItemView())) {
                return;
            }
            targetViews = new View[]{viewHolder.getItemView()};
        }
        int position = viewHolder.getAdapterPosition() - getHeadersCount();
        for (View targetView : targetViews) {
            if (!isViewHoldLongClickListener(targetView)) {
                setViewHoldLongClickListener(targetView);
                targetView.setOnLongClickListener(v -> {
                    callbackOnItemLongClickListener(position, v);
                    return true;
                });
            }
        }
    }

    private boolean isViewHoldClickListener(View v) {
        return (boolean) v.getTag(R.id.tag_view_click_listener_id);
    }

    private boolean isViewHoldLongClickListener(View v) {
        return (boolean) v.getTag(R.id.tag_view_long_click_listener_id);
    }

    private void setViewHoldClickListener(View v) {
        v.setTag(R.id.tag_view_click_listener_id, true);
    }

    private void setViewHoldLongClickListener(View v) {
        v.setTag(R.id.tag_view_long_click_listener_id, true);
    }
}
