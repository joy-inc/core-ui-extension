package com.joy.ui.extension;

import com.joy.http.JoyError;
import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.ResponseListener;
import com.joy.http.ResponseListenerImpl;
import com.joy.http.volley.Request;

import rx.Observable;

import static com.joy.http.LaunchMode.CACHE_AND_REFRESH;
import static com.joy.http.LaunchMode.CACHE_OR_REFRESH;
import static com.joy.http.LaunchMode.REFRESH_AND_CACHE;
import static com.joy.http.LaunchMode.REFRESH_ONLY;

/**
 * Created by Daisw on 2017/5/16.
 *
 * @param <T>
 * @See {@link com.joy.ui.fragment.BaseHttpUiFragment}.
 */

public abstract class BaseHttpUiFragment<T> extends com.joy.ui.fragment.BaseHttpUiFragment {

    private Request<T> mRequest;
    private boolean isContentDisplayed;

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            abortLauncher();
        }
    }

    @Override
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
}
