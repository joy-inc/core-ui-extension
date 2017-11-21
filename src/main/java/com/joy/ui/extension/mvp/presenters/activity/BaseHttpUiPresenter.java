package com.joy.ui.extension.mvp.presenters.activity;

import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.volley.Request;
import com.joy.ui.extension.mvp.presenters.PresenterImpl;
import com.joy.ui.extension.mvp.presenters.RequestLauncher;
import com.joy.ui.interfaces.BaseViewNet;

import rx.Observable;

import static com.joy.http.LaunchMode.REFRESH_ONLY;

/**
 * Created by KEVIN.DAI on 16/1/18.
 */
public class BaseHttpUiPresenter<T, V extends BaseViewNet> extends PresenterImpl<V> implements RequestLauncher<T> {

    private Request<T> mRequest;
    private String[] mParams;

//    @Override
//    public Observable<T> launch(Request<T> request, RequestMode mode) {
//        if (request == null) {
//            throw new NullPointerException("You need override the getRequest() method.");
//        }
//        abortLauncher();
//        mRequest = request;
//        mRequest.setRequestMode(mode);
//        Observable<T> observable = addRequest(mRequest, mode != REFRESH_ONLY);
////                .onErrorResumeNext(this::onErrorResume)
////                .share();
//
////        observable
////                .doOnSubscribe(this::doOnFirst)
////                .filter(this::filterNull)
////                .subscribe(
////                        this::onNext,
////                        this::onError);
//        return observable;
//    }

    public Observable<T> onErrorResume(Throwable e) {
        onError(e);
        return Observable.empty();
    }

    public void doOnFirst() {
        getBaseView().hideContent();
        getBaseView().hideTipView();
        getBaseView().showLoading();
    }

    public boolean filterNull(T t) {
        if (t == null) {
            onEmpty();
        }
        return t != null;
    }

    public void onEmpty() {
        if (isFinalResponse()) {
            getBaseView().hideLoading();
            getBaseView().hideContent();
            getBaseView().showEmptyTip();
        }
    }

    public void onNext(T t) {
        if (isFinalResponse()) {
            getBaseView().hideLoading();
        }
        getBaseView().hideTipView();
        getBaseView().showContent();
    }

    public void onError(Throwable e) {
        getBaseView().hideLoading();
        getBaseView().hideContent();
        getBaseView().showErrorTip();
    }

//    @Override
//    public final void abortLauncher() {
//        if (mRequest != null) {
//            JoyHttp.getLauncher().abort(mRequest);
//            mRequest = null;
//        }
//    }

    public Observable<T> launchRefreshOnly(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchRefreshOnly(getRequest(params));
    }

    public Observable<T> launchCacheOrRefresh(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchCacheOrRefresh(getRequest(params));
    }

    public Observable<T> launchRefreshAndCache(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchRefreshAndCache(getRequest(params));
    }

    public Observable<T> launchCacheAndRefresh(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchCacheAndRefresh(getRequest(params));
    }

    protected Request<T> getRequest(String... params) {
        return null;
    }

    public final void setParams(String... params) {
        mParams = params;
    }

    public final String[] getParams() {
        return mParams;
    }

    public final LaunchMode getLaunchMode() {
        return mRequest != null ? mRequest.getLaunchMode() : REFRESH_ONLY;
    }

//    public final boolean isReqHasCache() {
//        return mRequest != null && mRequest.hasCache();
//    }

    public final boolean isFinalResponse() {
        return mRequest != null && mRequest.isFinalResponse();
    }

//    public final Observable<T> addRequestNoCache(Request<T> req, Object tag) {
//        return addRequest(req, tag, false);
//    }

//    public final Observable<T> addRequestHasCache(Request<T> req, Object tag) {
//        return addRequest(req, tag, true);
//    }

//    public final Observable<T> addRequest(Request<T> req, Object tag, boolean shouldCache) {
//        req.setTag(tag);
//        req.setShouldCache(shouldCache);
//        return JoyHttp.getLauncher().addRequest(req);
//    }

//    public final Observable<T> addRequestNoCache(Request<T> req) {
//        return addRequest(req, false);
//    }

//    public final Observable<T> addRequestHasCache(Request<T> req) {
//        return addRequest(req, true);
//    }

//    public final Observable<T> addRequest(Request<T> req, boolean shouldCache) {
//        return addRequest(req, req.getIdentifier(), shouldCache);
//    }
}
