package com.joy.ui.extension.photo.select;

import android.widget.ImageView;

import com.joy.ui.extension.R;
import com.joy.ui.extension.adapter.RvAdapter;
import com.joy.ui.extension.adapter.RvViewHolder;

import java.util.LinkedList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.joy.ui.utils.DimenCons.SCREEN_WIDTH;

/**
 * Created by Daisw on 2017/11/22.
 */

public class PhotoAdapter extends RvAdapter<Photo> {

    private static final int SIZE = SCREEN_WIDTH / 8;
    private int mMaxLimit;
    private LinkedList<String> mSelectedPhotos;

    public PhotoAdapter(int maxLimit) {
        mMaxLimit = maxLimit;
        mSelectedPhotos = new LinkedList<>();
    }

    public LinkedList<String> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    @Override
    protected void onCreateItemView() {
        setItemView(R.layout.item_photo_item);
    }

    @Override
    protected void invalidate(RvViewHolder<Photo> viewHolder, int position, Photo photo) {
        bindOnClickListener(viewHolder);
        viewHolder.setImage(R.id.flImg, photo.getPath(), SIZE, SIZE);

        ImageView ivSelected = viewHolder.getView(R.id.ivSelected);
        CheckDrawable cd = (CheckDrawable) ivSelected.getDrawable();
        if (cd == null) {
            cd = new CheckDrawable(viewHolder.getItemView().getContext());
            ivSelected.setImageDrawable(cd);
        }
        cd.setSelect(photo.isSelected(), false);
        ivSelected.setVisibility(photo.isEnable() ? VISIBLE : INVISIBLE);

        final CheckDrawable finalCd = cd;
        ivSelected.setOnClickListener(v -> {
            boolean isSelected = finalCd.toggleSelected();
            photo.setSelected(isSelected);
            if (isSelected) {
                mSelectedPhotos.add(photo.getPath());
            } else {
                mSelectedPhotos.remove(photo.getPath());
            }

            List<Photo> photos = getData();
            for (int i = 0; i < photos.size(); i++) {
                Photo p = photos.get(i);
                if (!p.isSelected()) {
                    boolean enable = mSelectedPhotos.size() < mMaxLimit;
                    if (p.isEnable() != enable) {
                        p.setEnable(enable);
                        notifyItemChanged(i);
                    }
                }
            }

            callbackOnItemClickListener(viewHolder.getAdapterPosition(), v);
        });
    }
}
