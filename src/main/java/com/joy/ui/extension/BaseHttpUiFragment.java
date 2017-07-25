package com.joy.ui.extension;

import com.joy.http.JoyError;
import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.volley.Request;
import com.joy.utils.LogMgr;
import com.trello.rxlifecycle.FragmentEvent;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

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

    protected Request<T> mRequest;
    protected boolean isContentDisplayed;

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

    public LaunchMode getLaunchMode() {
        return mRequest != null ? mRequest.getLaunchMode() : REFRESH_ONLY;
    }

    boolean isFinalResponse() {
        return mRequest != null && mRequest.isFinalResponse();
    }

    public void abortLauncher() {
        if (mRequest != null) {
            JoyHttp.abort(mRequest);
            mRequest = null;
        }
    }

    /**
     * fetch net-->response.
     */
    public Subscription launchRefreshOnly() {
        return launch(getRequest(), REFRESH_ONLY);
    }

    /**
     * fetch cache or net-->response.
     */
    public Subscription launchCacheOrRefresh() {
        return launch(getRequest(), CACHE_OR_REFRESH);
    }

    /**
     * cache expired: fetch net, update cache-->response.
     */
    public Subscription launchRefreshAndCache() {
        return launch(getRequest(), REFRESH_AND_CACHE);
    }

    /**
     * cache update needed: fetch cache-->response, fetch net, update cache-->response.
     */
    public Subscription launchCacheAndRefresh() {
        return launch(getRequest(), CACHE_AND_REFRESH);
    }

    /**
     * @see {@link #getRequest()}
     */
    Subscription launch(Request<T> request, LaunchMode mode) {
        if (request == null) {
            throw new NullPointerException("You need override the getRequest() method.");
        }
        abortLauncher();
        mRequest = request;
//        mRequest.setLaunchMode(mode);
//        mRequest.setListener(getResponseListener());
        return JoyHttp.getLauncher().launch(mRequest, mode)
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
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

    protected abstract Request<T> getRequest();
}
