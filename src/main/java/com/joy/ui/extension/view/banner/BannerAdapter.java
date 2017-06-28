package com.joy.ui.extension.view.banner;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.joy.ui.adapter.ExPagerAdapter;
import com.joy.ui.extension.view.fresco.FrescoImage;
import com.joy.ui.view.banner.indicator.IndicatorAdapter;
import com.joy.utils.LayoutInflater;
import com.joy.utils.LogMgr;

import java.util.List;

/**
 * Created by KEVIN.DAI on 15/12/17.
 */
public class BannerAdapter<T> extends ExPagerAdapter<T> implements IndicatorAdapter {

    private BannerHolder<T> mHolder;

    public BannerAdapter(@LayoutRes int layoutResId, List<T> ts) {
        mHolder = new BannerHolder<>(layoutResId);
        setData(ts);
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

    @Override
    protected void invalidateItemView(int position, T t) {
        mHolder.invalidateItemView((FrescoImage) getCacheView(position), t);
    }

    public class BannerHolder<T> {
        private @LayoutRes int layoutResId;

        public BannerHolder(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
        }

        public View getItemView(@NonNull ViewGroup parent, int position) {
            View v = LayoutInflater.inflate(parent.getContext(), layoutResId, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackOnItemClickListener(position % getIndicatorCount(), v);
                }
            });
            return v;
        }

        public final void invalidateItemView(FrescoImage fivCover, T t) {
            LogMgr.e("daisw", "~~~BannerAdapter invalidateItemView:::" + fivCover + "~~~" + t);
            if (fivCover == null || t == null) {
                return;
            }
            if (t instanceof Integer) {
                fivCover.setImageURI((Integer) t);
            } else if (t instanceof String) {
                fivCover.setImageURI((String) t);
            } else {
                fivCover.setImageURI(t.toString());
            }
        }
    }
}
