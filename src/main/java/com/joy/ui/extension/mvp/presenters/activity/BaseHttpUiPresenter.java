package com.joy.ui.extension.mvp.presenters.activity;

import com.joy.http.JoyHttp;
import com.joy.http.RequestMode;
import com.joy.http.volley.ObjectRequest;
import com.joy.ui.activity.interfaces.BaseViewNet;
import com.joy.ui.extension.mvp.presenters.PresenterImpl;
import com.joy.ui.extension.mvp.presenters.RequestLauncher;

import rx.Observable;

import static com.joy.http.RequestMode.REFRESH_ONLY;

/**
 * Created by KEVIN.DAI on 16/1/18.
 */
public class BaseHttpUiPresenter<T, V extends BaseViewNet> extends PresenterImpl<V> implements RequestLauncher<T> {

    private ObjectRequest<T> mRequest;
    private String[] mParams;

//    @Override
//    public Observable<T> launch(ObjectRequest<T> request, RequestMode mode) {
//        if (request == null) {
//            throw new NullPointerException("You need override the getRequest() method.");
//        }
//        cancel();
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
//    public final void cancel() {
//        if (mRequest != null) {
//            mRequest.cancel();
//            mRequest = null;
//        }
//    }

    public Observable<T> launchRefreshOnly(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchRefreshOnly(getRequest(params));
    }

    public Observable<T> launchCacheOnly(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchCacheOnly(getRequest(params));
    }

    public Observable<T> launchRefreshAndCache(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchRefreshAndCache(getRequest(params));
    }

    public Observable<T> launchCacheAndRefresh(String... params) {
        setParams(params);
        return JoyHttp.getLauncher().launchCacheAndRefresh(getRequest(params));
    }

    protected ObjectRequest<T> getRequest(String... params) {
        return null;
    }

    public final void setParams(String... params) {
        mParams = params;
    }

    public final String[] getParams() {
        return mParams;
    }

    public final RequestMode getRequestMode() {
        return mRequest != null ? mRequest.getRequestMode() : REFRESH_ONLY;
    }

//    public final boolean isReqHasCache() {
//        return mRequest != null && mRequest.hasCache();
//    }

    public final boolean isFinalResponse() {
        return mRequest != null && mRequest.isFinalResponse();
    }

//    public final Observable<T> addRequestNoCache(ObjectRequest<T> req, Object tag) {
//        return addRequest(req, tag, false);
//    }

//    public final Observable<T> addRequestHasCache(ObjectRequest<T> req, Object tag) {
//        return addRequest(req, tag, true);
//    }

//    public final Observable<T> addRequest(ObjectRequest<T> req, Object tag, boolean shouldCache) {
//        req.setTag(tag);
//        req.setShouldCache(shouldCache);
//        return JoyHttp.getLauncher().addRequest(req);
//    }

//    public final Observable<T> addRequestNoCache(ObjectRequest<T> req) {
//        return addRequest(req, false);
//    }

//    public final Observable<T> addRequestHasCache(ObjectRequest<T> req) {
//        return addRequest(req, true);
//    }

//    public final Observable<T> addRequest(ObjectRequest<T> req, boolean shouldCache) {
//        return addRequest(req, req.getIdentifier(), shouldCache);
//    }

    public final void cancelLauncher(Object tag) {
        JoyHttp.getLauncher().cancelLauncher(tag);
    }

    public final void cancelAllLaunchers() {
        JoyHttp.getLauncher().cancelAllLauncher();
    }
}
