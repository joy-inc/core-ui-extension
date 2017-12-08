package com.joy.ui.extension.photo.select;

import com.joy.ui.extension.R;
import com.joy.ui.extension.adapter.RvAdapter;
import com.joy.ui.extension.adapter.RvViewHolder;

/**
 * Created by Daisw on 2017/11/22.
 */

public class AlbumAdapter extends RvAdapter<Album> {

    @Override
    protected void onCreateItemView() {
        setItemView(R.layout.item_photo_album);
    }

    @Override
    protected void invalidate(RvViewHolder<Album> viewHolder, int position, Album album) {
        bindOnClickListener(viewHolder);
        viewHolder.setImage(R.id.flCover, album.getCoverPath());
        viewHolder.setText(R.id.tvName, album.getDisplayName());
        viewHolder.setText(R.id.tvSize, R.string.album_item_size, album.getElementSize());
    }
}
