package com.joy.ui.extension.photo.select;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.joy.ui.extension.R;
import com.joy.ui.extension.mvp.presenters.activity.BaseHttpRvPresenter;
import com.joy.ui.interfaces.BaseViewNetRv;
import com.joy.ui.permissions.Permissions;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Daisw on 2017/11/22.
 */

public class AlbumPickPresenter {

    private BaseHttpRvPresenter<List<Album>, BaseViewNetRv<ActivityEvent>> mRvPresenter;

    public AlbumPickPresenter(BaseViewNetRv<ActivityEvent> baseViewNetRv) {
        mRvPresenter = new BaseHttpRvPresenter<>();
        mRvPresenter.attachView(baseViewNetRv);
    }

    public void takePicture() {
        if (SDK_INT < M || mRvPresenter.getBaseView().checkSelfPermission(CAMERA) == PERMISSION_GRANTED) {

        } else {
            mRvPresenter.getBaseView().requestPermissions(CAMERA)
                    .subscribe(new Action1<Permissions>() {
                        @Override
                        public void call(Permissions permissions) {

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        }
    }

    public static Uri createImageUri(Context context, boolean cacheCamera) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String fileName;
        if (cacheCamera) {
            fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) + ".jpg";
        } else {
            fileName = "camera_cache.jpg";
        }
        cv.put(MediaStore.Images.Media.TITLE, fileName);
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
    }

    private File mCurrentCameraPhotoFile;

//    private void onPickCameraClick() {
//        Uri uri = createImageUri(this, getIntent().getBooleanExtra(EXTRA_BOOLEAN_CAMERA_CACHE, true));
//        mCurrentCameraPhotoFile = new File(ActivityUtil.getFilePath(this, uri));
//        ActivityUtil.startCameraActivityForResult(this, uri, REQUEST_CODE_CAMERA);
//    }

    public void launchSystemAlbum() {
        if (SDK_INT < M || mRvPresenter.getBaseView().checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            getSystemAlbumObservable()
                    .subscribe(albums -> mRvPresenter.onNext(mRvPresenter.transform(albums)),
                            error -> {
                                mRvPresenter.onError(error);
                                error.printStackTrace();
                            });
        } else {
            mRvPresenter.getBaseView().requestPermissions(READ_EXTERNAL_STORAGE)
                    .flatMap(permissions -> {
                        if (permissions.isGranted(READ_EXTERNAL_STORAGE)) {
                            return getSystemAlbumObservable();
                        } else {
                            mRvPresenter.getBaseView().showToast(R.string.toast_please_grant_permissons);
                            return Observable.just(null);
                        }
                    })
                    .subscribe(albums -> mRvPresenter.onNext(mRvPresenter.transform(albums)),
                            error -> mRvPresenter.onError(error));
        }
    }

    private Observable<List<Album>> getSystemAlbumObservable() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(v -> getSystemPhotoAlbum())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private List<Album> getSystemPhotoAlbum() {
        Cursor cursor = null;
        List<Album> albums = new ArrayList<>();
        try {
            cursor = MediaStore.Images.Media.query(
                    mRvPresenter.getBaseView().getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    getImageFields(), "",
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " desc");

            Map<String, Album> bucketMaps = new HashMap<>();

            Album album;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String path = cursor.getString(1);
                String bucketId = cursor.getString(2);
                String bucketName = cursor.getString(3);

                if (!bucketMaps.containsKey(bucketId)) {
                    album = new Album();
                    album.setDisplayName(bucketName);
                    album.setElementSize(1);
                    album.setElements(new ArrayList<>());
                    album.setCoverId(id);
                    album.setCoverPath(path);
                    bucketMaps.put(bucketId, album);
                } else {
                    album = bucketMaps.get(bucketId);
                    album.setElementSize(album.getElementSize() + 1);
                }
                Photo photo = new Photo();
                photo.setId(id);
                photo.setPath(path);
                album.getElements().add(photo);
            }
            Iterable<String> it = bucketMaps.keySet();
            for (String key : it) {
                albums.add(bucketMaps.get(key));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return albums;
    }

    private String[] getImageFields() {
        return new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
        };
    }
}
