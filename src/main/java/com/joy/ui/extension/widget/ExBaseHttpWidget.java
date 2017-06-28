package com.joy.ui.extension.widget;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import com.joy.http.JoyError;
import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.ResponseListener;
import com.joy.http.ResponseListenerImpl;
import com.joy.http.volley.Request;
import com.joy.ui.activity.interfaces.BaseViewNet;
import com.joy.ui.extension.R;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.widget.ExBaseWidget;
import com.joy.utils.DeviceUtil;

import rx.Observable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.joy.http.LaunchMode.CACHE_AND_REFRESH;
import static com.joy.http.LaunchMode.CACHE_OR_REFRESH;
import static com.joy.http.LaunchMode.REFRESH_AND_CACHE;
import static com.joy.http.LaunchMode.REFRESH_ONLY;
import static com.joy.utils.ViewUtil.hideImageView;
import static com.joy.utils.ViewUtil.hideView;
import static com.joy.utils.ViewUtil.showImageView;
import static com.joy.utils.ViewUtil.showView;

/**
 * Created by Daisw on 2017/6/25.
 */

public abstract class ExBaseHttpWidget<T> extends ExBaseWidget {

    private FrameLayout mContentParent;
    private ImageView mIvTip;
    private View mLoadingView;
    private int mTipResId;
    private int LOADING_RES_ID = View.NO_ID;
    private int ERROR_RES_ID = R.drawable.ic_tip_error;
    private int EMPTY_RES_ID = R.drawable.ic_tip_empty;

    private Request<T> mRequest;
    private boolean isContentDisplayed;

    public ExBaseHttpWidget(Activity activity) {
        super(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            abortLauncher();
        }
    }

    @Override
    protected void wrapContentView(View contentView) {
        super.wrapContentView(contentView);
        mContentParent = new FrameLayout(getActivity());
        mContentParent.addView(contentView);
        addTipView(mContentParent);
        addLoadingView(mContentParent);
    }

    public FrameLayout getContentParent() {
        return mContentParent;
    }

    @Override
    public View getContentView() {
        return mContentParent;
    }

    public final void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mContentParent.setBackground(background);
        } else {
            mContentParent.setBackgroundDrawable(background);
        }
    }

    public final void setBackgroundResource(@DrawableRes int resId) {
        mContentParent.setBackgroundResource(resId);
    }

    public final void setBackgroundColor(@ColorInt int color) {
        mContentParent.setBackgroundColor(color);
    }

    private void addTipView(FrameLayout contentParent) {
        mIvTip = new ImageView(getActivity());
        mIvTip.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mIvTip.setOnClickListener(v -> onTipViewClick());
        hideImageView(mIvTip);
        contentParent.addView(mIvTip, getTipViewLp());
    }

    @SuppressWarnings("ResourceType")
    private LayoutParams getTipViewLp() {
        return new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    private void addLoadingView(FrameLayout contentParent) {
        mLoadingView = getLoadingView();
        hideView(mLoadingView);
        contentParent.addView(mLoadingView, getLoadingViewLp());
    }

    @SuppressWarnings("ResourceType")
    private LayoutParams getLoadingViewLp() {
        LayoutParams lp = (LayoutParams) mLoadingView.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER);
        }
        return lp;
    }

    public View getLoadingView() {
        if (LOADING_RES_ID == View.NO_ID) {
            return JLoadingView.get(getActivity());
        } else {
            ImageView ivLoading = new ImageView(getActivity());
            ivLoading.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER));
            ivLoading.setImageResource(LOADING_RES_ID);
            return ivLoading;
        }
    }

    public void onTipViewClick() {
        if (getTipType() == BaseViewNet.TipType.ERROR) {
            if (isNetworkEnable()) {
                doOnRetry();
            } else {
                showToast(com.joy.ui.R.string.toast_common_no_network);
            }
        }
    }

    public void doOnRetry() {
        launch(getRequest(), getLaunchMode());
    }

    protected final LaunchMode getLaunchMode() {
        return mRequest != null ? mRequest.getLaunchMode() : REFRESH_ONLY;
    }

    final boolean isFinalResponse() {
        return mRequest != null && mRequest.isFinalResponse();
    }

    protected final void abortLauncher() {
        if (mRequest != null) {
            JoyHttp.getLauncher().abort(mRequest);
            mRequest = null;
        }
    }

    /**
     * fetch net-->response.
     */
    protected Observable<T> launchRefreshOnly() {
        return launch(getRequest(), REFRESH_ONLY);
    }

    /**
     * fetch cache or net-->response.
     */
    protected Observable<T> launchCacheOrRefresh() {
        return launch(getRequest(), CACHE_OR_REFRESH);
    }

    /**
     * cache expired: fetch net, update cache-->response.
     */
    protected Observable<T> launchRefreshAndCache() {
        return launch(getRequest(), REFRESH_AND_CACHE);
    }

    /**
     * cache update needed: fetch cache-->response, fetch net, update cache-->response.
     */
    protected Observable<T> launchCacheAndRefresh() {
        return launch(getRequest(), CACHE_AND_REFRESH);
    }

    /**
     * @see {@link #getRequest()}
     */
    final Observable<T> launch(Request<T> request, LaunchMode mode) {
        if (request == null) {
            throw new NullPointerException("You need override the getRequest() method.");
        }
        abortLauncher();
        mRequest = request;
        mRequest.setLaunchMode(mode);
        mRequest.setListener(getResponseListener());
        return JoyHttp.getLauncher().launch(mRequest, mode);
    }

    private ResponseListener<T> getResponseListener() {
        if (!isContentDisplayed) {
            hideContent();
        }
        hideTipView();
        showLoading();
        return new ResponseListenerImpl<T>() {
            @Override
            public void onSuccess(Object tag, T t) {
                if (isFinishing()) {
                    return;
                }
                if (isFinalResponse()) {
                    hideLoading();
                }
                if (invalidateContent(t)) {
                    hideTipView();
                    showContent();
                    isContentDisplayed = true;
                } else if (isFinalResponse()) {
                    hideContent();
                    showEmptyTip();
                }
            }

            @Override
            public void onError(Object tag, Throwable error) {
                if (!isFinishing()) {
                    hideLoading();
                    if (error instanceof JoyError && !((JoyError) error).isCancelCaused()) {
                        super.onError(tag, error);
                        hideContent();
                        showErrorTip();
                    }
                }
            }

            @Override
            public void onError(Object tag, JoyError error) {
                onHttpFailed(tag, error);
            }
        };
    }

    protected abstract boolean invalidateContent(T t);

    /**
     * 子类可以继承此方法得到失败时的错误信息，用于Toast提示
     */
    protected void onHttpFailed(Object tag, JoyError error) {
        showToast(error.getMessage());
    }

    protected abstract Request<T> getRequest();

    public void showLoading() {
        if (mLoadingView instanceof ImageView) {
            ImageView loadingIv = (ImageView) mLoadingView;
            Drawable d = loadingIv.getDrawable();
            if (d instanceof AnimationDrawable) {
                AnimationDrawable ad = (AnimationDrawable) d;
                ad.start();
            }
        }
        showView(mLoadingView);
    }

    public void hideLoading() {
        hideView(mLoadingView);
        if (mLoadingView instanceof ImageView) {
            ImageView loadingIv = (ImageView) mLoadingView;
            Drawable d = loadingIv.getDrawable();
            if (d instanceof AnimationDrawable) {
                AnimationDrawable ad = (AnimationDrawable) d;
                ad.stop();
            }
        }
    }

    public void showContent() {
        showView(getContentView());
    }

    public void hideContent() {
        hideView(getContentView());
    }

    public void showErrorTip() {
        mTipResId = ERROR_RES_ID;
        showImageView(mIvTip, mTipResId);
    }

    public void showEmptyTip() {
        mTipResId = EMPTY_RES_ID;
        showImageView(mIvTip, mTipResId);
    }

    public void hideTipView() {
        hideImageView(mIvTip);
    }

    @NonNull
    public ImageView getTipView() {
        return mIvTip;
    }

    public BaseViewNet.TipType getTipType() {
        if (mIvTip.getDrawable() != null) {
            if (mTipResId == EMPTY_RES_ID) {
                return BaseViewNet.TipType.EMPTY;
            } else if (mTipResId == ERROR_RES_ID) {
                return BaseViewNet.TipType.ERROR;
            }
        }
        return BaseViewNet.TipType.NULL;
    }

    public boolean isNetworkEnable() {
        return DeviceUtil.isNetworkEnable(getActivity().getApplicationContext());
    }
}
