package com.joy.ui.extension.view.banner;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.joy.ui.adapter.ExPagerAdapter;
import com.joy.ui.extension.view.fresco.FrescoImage;
import com.joy.ui.view.banner.indicator.IndicatorAdapter;
import com.joy.utils.LayoutInflater;

import java.util.List;

/**
 * Created by KEVIN.DAI on 15/12/17.
 */
public class BannerAdapter<T> extends ExPagerAdapter<T> implements IndicatorAdapter {

    private SparseArray<View> mCacheViews;
    private BannerHolder mHolder;
    private int w, h;

    public BannerAdapter(@LayoutRes int layoutResId, List<T> ts, int w, int h) {
        mCacheViews = new SparseArray<>();
        mHolder = new BannerHolder(layoutResId);
        setData(ts);
        this.w = w;
        this.h = h;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mCacheViews.get(position % getIndicatorCount());
        if (view == null) {
            view = getItemView(container, position);
            mCacheViews.put(position, view);
        }
        if (view.getParent() != null) {
            container.removeView(view);
        }
        container.addView(view);
        invalidateItemView(container, view instanceof FrescoImage ? (FrescoImage) view : null, position, getItem(position));
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public int getCount() {
        return getIndicatorCount() > 1 ? Integer.MAX_VALUE : getIndicatorCount();
    }

    @Override
    public int getIndicatorCount() {
        return super.getCount();
    }

    @Override
    public T getItem(int position) {
        return super.getItem(position % getIndicatorCount());
    }

    @Override
    protected View getItemView(ViewGroup container, int position) {
        return mHolder.getItemView(container, position);
    }

    protected void invalidateItemView(ViewGroup container, FrescoImage fivCover, int position, T t) {
        mHolder.invalidateItemView(container, fivCover, position, t);
    }

    private class BannerHolder {
        private
        @LayoutRes
        int layoutResId;

        BannerHolder(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
        }

        View getItemView(@NonNull ViewGroup parent, int position) {
            FrescoImage fivCover;
            try {
                fivCover = LayoutInflater.inflate(parent.getContext(), layoutResId, parent, false);
            } catch (Exception e) {// 退出APP把Fresco资源回收后，再去inflate frescoImage时会抛出空指针异常。
                return new View(parent.getContext());
            }
            fivCover.setOnClickListener(v -> callbackOnItemClickListener(position % getIndicatorCount(), v));
            return fivCover;
        }

        public final void invalidateItemView(ViewGroup container, FrescoImage fivCover, int position, T t) {
            if (fivCover == null || t == null) {
                return;
            }
            if (t instanceof Integer) {
                fivCover.resize((Integer) t, w, h);
            } else if (t instanceof String) {
                fivCover.resize((String) t, w, h);
            } else {
                fivCover.resize(t.toString(), w, h);
            }

            int count = container.getChildCount();
            for (int i = 0; i < count && position > 1; i++) {
                View child = container.getChildAt(i);
                if (i == 0) {
                    int left = w * (position - 2);
                    child.setLeft(left);
                    child.setRight(left + w);
                    child.setBottom(h);
                } else if (i == 1) {
                    int left = w * (position - 1);
                    child.setLeft(left);
                    child.setRight(left + w);
                    child.setBottom(h);
                }
            }
            int left = w * position;
            fivCover.setLeft(left);
            fivCover.setRight(left + w);
            fivCover.setBottom(h);
        }
    }
}
