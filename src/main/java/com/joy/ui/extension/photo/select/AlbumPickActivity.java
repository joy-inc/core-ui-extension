package com.joy.ui.extension.photo.select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.joy.ui.activity.BaseHttpRvActivity;
import com.joy.ui.extension.R;
import com.joy.ui.view.recyclerview.ItemDecoration;
import com.joy.utils.CollectionUtil;

import java.util.ArrayList;

/**
 * Created by Daisw on 2017/11/22.
 */

public class AlbumPickActivity extends BaseHttpRvActivity {

    public static final String KEY_EXTRA_PHOTOS = "photos";
    AlbumPickPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new AlbumPickPresenter(this);
        mPresenter.launchSystemAlbum();
    }

    @Override
    protected void initTitleView() {
        addTitleBackView();
        setTitle(R.string.album_list);
//        addTitleRightView(R.drawable.ic_local_see_white_24dp, v -> mPresenter.takePicture());// TODO: 2017/12/8 拍照功能
    }

    @Override
    protected void initContentView() {
        setSwipeRefreshEnable(false);
        getRecyclerView().addItemDecoration(ItemDecoration.builder(this).dividerSize(1).build());
        AlbumAdapter adapter = new AlbumAdapter();
        adapter.setOnItemClickListener((position, v, album) -> PhotoPickActivity.startActivityForResult(this, album, getIntent().getIntExtra("maxLimit", 1), 0X9001));
        getRecyclerView().setAdapter(adapter);
    }

    @Override
    public void doOnRetry() {
        mPresenter.launchSystemAlbum();
    }

    @Override
    public void setLoadMoreEnable(boolean enable) {// TODO: 2017/11/27 setLoadMoreEnable无效
        super.setLoadMoreEnable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.hasExtra(KEY_EXTRA_PHOTOS)) {
            ArrayList<String> photos = data.getStringArrayListExtra(KEY_EXTRA_PHOTOS);
            if (CollectionUtil.isNotEmpty(photos)) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    public static void startActivityForResult(Activity activity, int maxLimit, int requestCode) {
        Intent intent = new Intent(activity, AlbumPickActivity.class);
        intent.putExtra("maxLimit", maxLimit);
        activity.startActivityForResult(intent, requestCode);
    }
}
