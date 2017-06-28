package com.joy.ui.extension.view.fresco;

import android.content.Context;
import android.net.Uri;
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

    public static void shutdown() {
        Fresco.shutDown();
    }

    /**
     * Displays an image given by the uri.
     *
     * @param uri url of the image
     * @undeprecate
     */
    public void setImageURI(@Nullable String uri) {
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            LogMgr.d("~~~~FrescoImage", "w: " + w + " h: " + h);
            resize(uri, w, h);
            return;
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null) {
            int width = lp.width;
            int height = lp.height;
            if (width > 0 && height > 0) {
                LogMgr.i("~~~~FrescoImage", "width: " + width + " height: " + height);
                resize(uri, width, height);
                return;
            }
        }
        LogMgr.e("~~~~FrescoImage", "=========================");
        setImageURI(Uri.parse(uri == null ? "" : uri));
    }

    /**
     * Displays an image given by the res id.
     *
     * @param drawableResId drawableResId of the image
     * @undeprecate
     */
    public void setImageURI(@DrawableRes int drawableResId) {
        setImageURI("res://" + getContext().getPackageName() + "/" + drawableResId);
    }

    public void resize(@Nullable String url, int width, int height) {
        resize(Uri.parse(url == null ? "" : url), width, height);
    }

    public void resize(@NonNull Uri uri, int width, int height) {
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
        blur(Uri.parse(url == null ? "" : url), radius);
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
        resizeBlur(Uri.parse(url == null ? "" : url), width, height, radius);
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
}
