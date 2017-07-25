package com.joy.ui.extension.view.banner;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    private BannerHolder mHolder;
    private int w, h;

    public BannerAdapter(@LayoutRes int layoutResId, List<T> ts, int w, int h) {
        mHolder = new BannerHolder(layoutResId);
        setData(ts);
        this.w = w;
        this.h = h;
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

//    @Override
//    protected void invalidateItemView(int position, T t) {
//        mHolder.invalidateItemView((FrescoImage) getCacheView(position), t);
//    }

    private class BannerHolder {
        private
        @LayoutRes
        int layoutResId;

        BannerHolder(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
        }

        View getItemView(@NonNull ViewGroup parent, int position) {
//            FrescoImage1 fivCover;
//            try {
//                fivCover = LayoutInflater.inflate(parent.getContext(), layoutResId, parent, false);
//            } catch (Exception e) {// 退出APP把Fresco资源回收后，再去inflate frescoImage时会抛出空指针异常。
//                return new View(parent.getContext());
//            }
//            fivCover.setOnClickListener(v -> callbackOnItemClickListener(position % getIndicatorCount(), v));
//            T t = getItem(position);
//            if (t instanceof Integer) {
//                fivCover.resize((Integer) t, w, h);
//            } else if (t instanceof String) {
//                fivCover.resize((String) t, w, h);
//            } else {
//                fivCover.resize(t.toString(), w, h);
//            }
//            LogMgr.i("daisw","====getItemView:::"+position+"======="+fivCover.getVisibility());
//            return fivCover;

            View imageView = LayoutInflater.inflate(parent.getContext(), layoutResId, parent, false);
//            LogMgr.i("daisw", "====" + imageView.getLeft() + "==" + imageView.getTop() + "==" + imageView.getRight() + "==" + imageView.getBottom() + "==" + position);


            int count = parent.getChildCount();
            for (int i = 0; i < count && position > 1; i++) {
                View child = parent.getChildAt(i);
                if (i == 0) {
                    int left = 1440 * (position - 2);
                    child.setLeft(left);
                    child.setRight(left + 1440);
                    child.setBottom(460);
                } else if (i == 1) {
                    int left = 1440 * (position - 1);
                    child.setLeft(left);
                    child.setRight(left + 1440);
                    child.setBottom(460);
                }
            }
            int left = 1440 * position;
            imageView.setLeft(left);
            imageView.setRight(left + 1440);
            imageView.setBottom(460);
            return imageView;
        }

//        public final void invalidateItemView(FrescoImage fivCover, T t) {
//            if (fivCover == null || t == null) {
//                return;
//            }
//            if (t instanceof Integer) {
//                fivCover.resize((Integer) t, w, h);
//            } else if (t instanceof String) {
//                fivCover.resize((String) t, w, h);
//            } else {
//                fivCover.resize(t.toString(), w, h);
//            }
//        }
    }
}
