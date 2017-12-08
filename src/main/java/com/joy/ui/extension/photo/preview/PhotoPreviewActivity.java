package com.joy.ui.extension.photo.preview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.joy.ui.activity.BaseUiActivity;
import com.joy.ui.extension.R;
import com.joy.ui.view.viewpager.JViewPager;

import java.util.ArrayList;

import static com.joy.ui.extension.photo.select.AlbumPickActivity.KEY_EXTRA_PHOTOS;

/**
 * Created by Daisw on 2017/12/1.
 */

public class PhotoPreviewActivity extends BaseUiActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_photo_preview);
    }

    @Override
    protected void initContentView() {
        PhotoPreviewAdapter adapter = new PhotoPreviewAdapter();
        adapter.setData(getIntent().getStringArrayListExtra(KEY_EXTRA_PHOTOS));
        JViewPager viewPager = findViewById(R.id.vpPhoto);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(getIntent().getIntExtra("position", 0), false);
    }

    public static void startActivity(Activity activity, ArrayList<String> photos, int position) {
        Intent intent = new Intent(activity, PhotoPreviewActivity.class);
        intent.putStringArrayListExtra(KEY_EXTRA_PHOTOS, photos);
        intent.putExtra("position", position);
        activity.startActivity(intent);
    }
}
