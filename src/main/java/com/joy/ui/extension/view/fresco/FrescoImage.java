package com.joy.ui.extension.view.fresco;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.joy.utils.LogMgr;
import com.joy.utils.TextUtil;

/**
 * Created by KEVIN.DAI on 16/1/4.
 */
public class FrescoImage extends SimpleDraweeView {

    public FrescoImage(Context context) {
        super(context);
    }

    public FrescoImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrescoImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static void initialize(Context appContext) {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(appContext)
//                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())// JPG渐进式加载
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(appContext, config);
    }

    public static void shutDown() {
        Fresco.shutDown();
    }

    public static void clearMemoryCaches() {
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    public static void clearDiskCaches() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public static void clearCaches() {
        Fresco.getImagePipeline().clearCaches();
    }

    public void setImageGifURI(@DrawableRes int drawableResId, int width, int height) {
        setImageGifURI(genResourceUrl(drawableResId), width, height);
    }

    /**
     * Displays an image(include * and gif) given by the uriString.
     *
     * @param url of the image
     * @undeprecate
     */
    public void setImageGifURI(String url, int width, int height) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(trimUrl(url))
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        setController(controller);
    }

    /**
     * Displays an image given by the res id.
     *
     * @param drawableResId drawableResId of the image
     * @undeprecate
     */
    public void setImageURI(@DrawableRes int drawableResId) {
        setImageURI(genResourceUrl(drawableResId));
    }

    /**
     * Displays an image given by the url.
     *
     * @param url url of the image
     * @undeprecate
     */
    public void setImageURI(@Nullable String url) {
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            resize(url, w, h);
            return;
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null) {
            int width = lp.width;
            int height = lp.height;
            if (width > 0 && height > 0) {
                resize(url, width, height);
                return;
            }
        }
        LogMgr.e("FrescoImage", "============= FrescoImage: not resize ============");
        setImageURI(trimUrl(url));
    }

    public void resize(@DrawableRes int drawableResId, int width, int height) {
        resize(genResourceUrl(drawableResId), width, height);
    }

    public void resize(@Nullable String url, int width, int height) {
        resize(trimUrl(url), width, height);
    }

    public void resize(@NonNull Uri uri, int width, int height) {
//        LogMgr.d("FrescoImage", "resize w: " + width + " h: " + height);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .build();
        setController(controller);
    }

    public void blur(@Nullable String url, @IntRange(from = 0, to = 25) int radius) {
        blur(trimUrl(url), radius);
    }

    public void blur(@NonNull Uri uri, @IntRange(from = 0, to = 25) int radius) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setPostprocessor(new BlurPostprocessor(getContext(), radius))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .build();
        setController(controller);
    }

    public void resizeBlur(@Nullable String url, int width, int height, @IntRange(from = 0, to = 25) int radius) {
        resizeBlur(trimUrl(url), width, height, radius);
    }

    public void resizeBlur(@NonNull Uri uri, int width, int height, @IntRange(from = 0, to = 25) int radius) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .setPostprocessor(new BlurPostprocessor(getContext(), radius))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .build();
        setController(controller);
    }

    public Uri trimUrl(String url) {
        if (TextUtil.isEmptyTrim(url)) {
            return Uri.EMPTY;
        }
        Uri uri;
        if (url.startsWith("/storage")) {
            uri = Uri.parse("file:" + url);
        } else if (url.startsWith("http") || url.startsWith("res")) {
            uri = Uri.parse(url);
        } else {// id格式的
            uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, url);
        }
        return uri;
    }

    public String genResourceUrl(@DrawableRes int resId) {
        return "res://" + getContext().getPackageName() + "/" + resId;
    }
}
