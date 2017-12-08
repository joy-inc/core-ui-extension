package com.joy.ui.extension.photo.preview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.joy.ui.adapter.ExPagerAdapter;
import com.joy.ui.utils.DimenCons;

/**
 * Created by Daisw on 2017/12/1.
 */

public class PhotoPreviewAdapter extends ExPagerAdapter<String> {

    @Override
    protected View getItemView(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        photoView.resize(getItem(position), DimenCons.SCREEN_WIDTH, DimenCons.SCREEN_HEIGHT);
        return photoView;
    }
}
