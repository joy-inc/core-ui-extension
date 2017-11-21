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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import com.joy.http.JoyError;
import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.volley.Request;
import com.joy.ui.TipType;
import com.joy.ui.extension.R;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.widget.ExBaseWidget;
import com.joy.utils.DeviceUtil;
import com.joy.utils.LogMgr;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.joy.http.LaunchMode.CACHE_AND_REFRESH;
import static com.joy.http.LaunchMode.CACHE_OR_REFRESH;
import static com.joy.http.LaunchMode.REFRESH_AND_CACHE;
import static com.joy.http.LaunchMode.REFRESH_ONLY;
import static com.joy.utils.ViewUtil.hideView;
import static com.joy.utils.ViewUtil.showImageView;
import static com.joy.utils.ViewUtil.showView;

/**
 * Created by Daisw on 2017/6/25.
 */

public abstract class ExBaseHttpWidget<T> extends ExBaseWidget {

    protected FrameLayout mContentParent;
    protected View mTipView;
    @TipType
    protected int mTipType;
    protected View mLoadingView;
    private int LOADING_RES_ID = View.NO_ID;
    private int ERROR_RES_ID = R.drawable.ic_tip_error;
    private int EMPTY_RES_ID = R.drawable.ic_tip_empty;

    private Request<T> mRequest;
    private String[] mParams;
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

    public ViewGroup getContentParent() {
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

    protected void addTipView(FrameLayout contentParent) {
        mTipView = getTipView();
        setTipType(TipType.NULL);
        mTipView.setOnClickListener(v -> onTipViewClick());
        hideView(mTipView);
        contentParent.addView(mTipView);
    }

    @NonNull
    public View getTipView() {
        ImageView ivTip = new ImageView(getActivity());
        ivTip.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivTip.setLayoutParams(getTipViewLp());
        return ivTip;
    }

    @SuppressWarnings("ResourceType")
    protected LayoutParams getTipViewLp() {
        return new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    @TipType
    public int getTipType() {
        return mTipType;
    }

    public void setTipType(int tipType) {
        this.mTipType = tipType;
    }

    protected void addLoadingView(FrameLayout contentParent) {
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
        if (getTipType() == TipType.ERROR) {
//            if (isNetworkEnable()) {
            doOnRetry();
//            } else {
//                showToast(com.joy.ui.R.string.toast_common_no_network);
//            }
        }
    }

    public void doOnRetry() {
        launch(getRequest(mParams), getLaunchMode());
    }

    public final LaunchMode getLaunchMode() {
        return mRequest != null ? mRequest.getLaunchMode() : REFRESH_ONLY;
    }

    final boolean isFinalResponse() {
        return mRequest != null && mRequest.isFinalResponse();
    }

    public final void abortLauncher() {
        if (mRequest != null) {
            JoyHttp.abort(mRequest);
            mRequest = null;
        }
    }

    public final void setParams(String... params) {
        mParams = params;
    }

    public final String[] getParams() {
        return mParams;
    }

    /**
     * fetch net-->response.
     */
    public Subscription launchRefreshOnly(String... params) {
        setParams(params);
        return launch(getRequest(params), REFRESH_ONLY);
    }

    /**
     * fetch cache or net-->response.
     */
    public Subscription launchCacheOrRefresh(String... params) {
        setParams(params);
        return launch(getRequest(params), CACHE_OR_REFRESH);
    }

    /**
     * cache expired: fetch net, update cache-->response.
     */
    public Subscription launchRefreshAndCache(String... params) {
        setParams(params);
        return launch(getRequest(params), REFRESH_AND_CACHE);
    }

    /**
     * cache update needed: fetch cache-->response, fetch net, update cache-->response.
     */
    public Subscription launchCacheAndRefresh(String... params) {
        setParams(params);
        return launch(getRequest(params), CACHE_AND_REFRESH);
    }

    /**
     * @see {@link #getRequest(String...)}
     */
    final Subscription launch(Request<T> request, LaunchMode mode) {
        if (request == null) {
            throw new NullPointerException("You need override the getRequest(String...) method.");
        }
        abortLauncher();
        mRequest = request;
//        mRequest.setLaunchMode(mode);
//        mRequest.setListener(getResponseListener());
        return JoyHttp.getLauncher().launch(mRequest, mode)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (!isContentDisplayed) {
                            hideContent();
                        }
                        hideTipView();
                        showLoading();
                    }
                })
                .filter(new Func1<T, Boolean>() {
                    @Override
                    public Boolean call(T t) {
                        return filter(t);
                    }
                })
                .subscribe(new Action1<T>() {
                    @Override
                    public void call(T t) {
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
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (isFinishing()) {
                            return;
                        }
                        hideLoading();
                        if (throwable instanceof JoyError && !((JoyError) throwable).isCancelCaused()) {
                            onHttpFailed(mRequest.getTag(), (JoyError) throwable);
                            hideContent();
                            showErrorTip();
                        }
                    }
                });
    }

    protected boolean filter(T t) {
        return t != null;
    }

//    private ResponseListener<T> getResponseListener() {
//        if (!isContentDisplayed) {
//            hideContent();
//        }
//        hideTipView();
//        showLoading();
//        return new ResponseListenerImpl<T>() {
//            @Override
//            public void onSuccess(Object tag, T t) {
//                if (isFinishing()) {
//                    return;
//                }
//                if (isFinalResponse()) {
//                    hideLoading();
//                }
//                if (invalidateContent(t)) {
//                    hideTipView();
//                    showContent();
//                    isContentDisplayed = true;
//                } else if (isFinalResponse()) {
//                    hideContent();
//                    showEmptyTip();
//                }
//            }
//
//            @Override
//            public void onError(Object tag, Throwable error) {
//                if (!isFinishing()) {
//                    hideLoading();
//                    if (error instanceof JoyError && !((JoyError) error).isCancelCaused()) {
//                        super.onError(tag, error);
//                        hideContent();
//                        showErrorTip();
//                    }
//                }
//            }
//
//            @Override
//            public void onError(Object tag, JoyError error) {
//                onHttpFailed(tag, error);
//            }
//        };
//    }

    protected abstract boolean invalidateContent(T t);

    /**
     * 子类可以继承此方法得到失败时的错误信息，用于Toast提示
     */
    protected void onHttpFailed(Object tag, JoyError error) {
        if (LogMgr.DEBUG) {
            showToast(error.getMessage());
        } else {
            showToast(R.string.toast_common_timeout);
        }
    }

    protected abstract Request<T> getRequest(String... params);

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
        setTipType(TipType.ERROR);
        if (mTipView instanceof ImageView) {
            showImageView((ImageView) mTipView, ERROR_RES_ID);
        } else {
            showView(mTipView);
        }
    }

    public void showEmptyTip() {
        setTipType(TipType.EMPTY);
        if (mTipView instanceof ImageView) {
            showImageView((ImageView) mTipView, EMPTY_RES_ID);
        } else {
            showView(mTipView);
        }
    }

    public void hideTipView() {
        hideView(mTipView);
    }

    public boolean isNetworkEnable() {
        return DeviceUtil.isNetworkEnable(getActivity().getApplicationContext());
    }
}
