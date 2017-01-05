package com.joy.ui.extension;

import com.joy.http.JoyError;
import com.joy.http.JoyHttp;
import com.joy.http.RequestMode;
import com.joy.http.ResponseListener;
import com.joy.http.volley.ObjectRequest;
import com.joy.http.volley.RetroRequestQueue;
import com.joy.utils.LogMgr;

import rx.Observable;

import static com.joy.http.RequestMode.CACHE_AND_REFRESH;
import static com.joy.http.RequestMode.CACHE_ONLY;
import static com.joy.http.RequestMode.REFRESH_AND_CACHE;
import static com.joy.http.RequestMode.REFRESH_ONLY;

/**
 * Created by KEVIN.DAI on 15/7/10.
 *
 * @param <T>
 * @See {@link com.joy.ui.activity.BaseHttpUiActivity<T>}.
 */
public abstract class BaseHttpUiActivity<T> extends com.joy.ui.activity.BaseHttpUiActivity {

    private ObjectRequest<T> mObjReq;
    private boolean mContentHasDisplayed;

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            cancelLauncher();
        }
    }

    @Override
    public void doOnRetry() {
        launch(getRequestMode());
    }

    protected final RequestMode getRequestMode() {
        return mObjReq != null ? mObjReq.getRequestMode() : REFRESH_ONLY;
    }

    protected final boolean isReqHasCache() {
        return mObjReq != null && mObjReq.hasCache();
    }

    final boolean isFinalResponse() {
        return mObjReq != null && mObjReq.isFinalResponse();
    }

    protected final void cancelLauncher() {
        if (mObjReq != null) {
            mObjReq.cancel();
            mObjReq = null;
        }
    }

    /**
     * fetch net-->response.
     */
    protected Observable<T> launchRefreshOnly() {
        return launch(REFRESH_ONLY);
    }

    /**
     * fetch cache-->response.
     */
    protected Observable<T> launchCacheOnly() {
        return launch(CACHE_ONLY);
    }

    /**
     * cache expired: fetch net, update cache-->response.
     */
    protected Observable<T> launchRefreshAndCache() {
        return launch(REFRESH_AND_CACHE);
    }

    /**
     * cache update needed: fetch cache-->response, fetch net, update cache-->response.
     */
    protected Observable<T> launchCacheAndRefresh() {
        return launch(CACHE_AND_REFRESH);
    }

    final Observable<T> launch(RequestMode mode) {
        cancelLauncher();
        mObjReq = getRequest();
        mObjReq.setRequestMode(mode);
        mObjReq.setResponseListener(getObjRespLis());
        return getLauncher().launch(mObjReq, mode);
    }

    private ResponseListener<T> getObjRespLis() {
        if (!mContentHasDisplayed) {
            hideContent();
        }
        hideTipView();
        showLoading();

        return new ResponseListener<T>() {

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
                    mContentHasDisplayed = true;
                } else {
                    if (isFinalResponse()) {
                        hideContent();
                        showEmptyTip();
                    }
                }
            }

            @Override
            public void onError(Object tag, JoyError error) {
                if (isFinishing()) {
                    return;
                }
                onHttpFailed(error.getMessage());
                onHttpFailed(tag, error.getMessage());

                hideLoading();
                hideContent();
                showErrorTip();
            }
        };
    }

    protected abstract boolean invalidateContent(T t);

    /**
     * 子类可以继承此方法得到失败时的错误信息，用于Toast提示
     */
    protected void onHttpFailed(Object tag, String msg) {
    }

    void onHttpFailed(String msg) {
        if (LogMgr.DEBUG) {
            showToast(getClass().getSimpleName() + ": " + msg);
        }
    }

    protected final RetroRequestQueue getLauncher() {
        return JoyHttp.getLauncher();
    }

    protected abstract ObjectRequest<T> getRequest();

    protected final void cancelLauncher(Object tag) {
        getLauncher().cancelLauncher(tag);
    }

    protected final void cancelAllLauncher() {
        getLauncher().cancelAllLauncher();
    }
}
