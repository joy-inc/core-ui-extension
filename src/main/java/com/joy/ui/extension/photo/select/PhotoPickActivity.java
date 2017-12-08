package com.joy.ui.extension.photo.select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.TypedValue;
import android.widget.TextView;

import com.joy.ui.activity.BaseHttpRvActivity;
import com.joy.ui.extension.R;
import com.joy.ui.extension.photo.preview.PhotoPreviewActivity;
import com.joy.ui.view.recyclerview.ItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.joy.ui.extension.photo.select.AlbumPickActivity.KEY_EXTRA_PHOTOS;
import static com.joy.ui.utils.DimenCons.DP;

/**
 * Created by Daisw on 2017/11/22.
 */

public class PhotoPickActivity extends BaseHttpRvActivity {

    PhotoPickPresenter mPresenter;
    Album mAlbum;
    int mMaxLimit = 1;
    TextView mTvTitleRight;
    PhotoAdapter mPhotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mPresenter = new PhotoPickPresenter();
        mAlbum = getIntent().getParcelableExtra("album");
        mMaxLimit = getIntent().getIntExtra("maxLimit", 1);
    }

    @Override
    protected void initTitleView() {
        addTitleBackView();
        setTitle(mAlbum.getDisplayName());
        mTvTitleRight = addTitleRightView("", v -> {
            Intent data = new Intent();
            data.putStringArrayListExtra(KEY_EXTRA_PHOTOS, new ArrayList<>(mPhotoAdapter.getSelectedPhotos()));
            setResult(RESULT_OK, data);
            finish();
        });
        mTvTitleRight.setPadding(DP(8), 0, DP(8), 0);
        mTvTitleRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, DP(16));
    }

    @Override
    protected void initContentView() {
        setSwipeRefreshEnable(false);
        getRecyclerView().setLayoutManager(new GridLayoutManager(this, 4));
        getRecyclerView().addItemDecoration(ItemDecoration.builder(this)
                .divider(null)
                .paddingParent(DP(3))
                .margin(DP(1.5f))
                .build());
        getRecyclerView().setHasFixedSize(true);
        mPhotoAdapter = new PhotoAdapter(mMaxLimit);
        mPhotoAdapter.setData(mAlbum.getElements());
        mPhotoAdapter.setOnItemClickListener((position, v, photo) -> {
            if (v.getId() == R.id.ivSelected) {
                List<String> selectedPhotos = mPhotoAdapter.getSelectedPhotos();
                mTvTitleRight.setText(getString(R.string.done_format, selectedPhotos.size()));
                mTvTitleRight.setVisibility(selectedPhotos.isEmpty() ? INVISIBLE : VISIBLE);
            } else {
                List<Photo> photos = mPhotoAdapter.getData();
                ArrayList<String> photoUrls = new ArrayList<>(photos.size());
                for (Photo p : photos) {
                    photoUrls.add(p.getPath());
                }
                PhotoPreviewActivity.startActivity(PhotoPickActivity.this, photoUrls, position);
            }
        });
        getRecyclerView().setAdapter(mPhotoAdapter);
    }

    @Override
    public void doOnRetry() {
    }

    public static void startActivityForResult(Activity activity, Album album, int maxLimit, int requestCode) {
        Intent intent = new Intent(activity, PhotoPickActivity.class);
        intent.putExtra("album", album);
        intent.putExtra("maxLimit", maxLimit);
        activity.startActivityForResult(intent, requestCode);
    }
}
