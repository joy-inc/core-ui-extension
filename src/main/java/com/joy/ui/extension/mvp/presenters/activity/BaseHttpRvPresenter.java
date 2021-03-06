package com.joy.ui.extension.mvp.presenters.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.joy.http.JoyHttp;
import com.joy.http.LaunchMode;
import com.joy.http.volley.Request;
import com.joy.ui.R;
import com.joy.ui.RefreshMode;
import com.joy.ui.adapter.ExRvAdapter;
import com.joy.ui.interfaces.BaseViewNetRv;
import com.joy.ui.view.LoadMore;

import java.util.List;

import rx.Observable;

/**
 * Created by Daisw on 16/6/8.
 */
public class BaseHttpRvPresenter<T, V extends BaseViewNetRv> extends BaseHttpUiPresenter<T, V> {

    protected static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    protected int PAGE_START_INDEX = 1;// 默认起始页码
    protected int mPageLimit = PAGE_UPPER_LIMIT;
    protected int mPageIndex = PAGE_START_INDEX;
    protected int mSortIndex = mPageIndex;

    @Override
    public void attachView(V v) {
        super.attachView(v);
        getBaseView().setOnRefreshListener(getOnRefreshListener());
        getBaseView().setOnLoadMoreListener(getOnLoadMoreListener());
    }

    private OnRefreshListener getOnRefreshListener() {
        return () -> {
            if (getBaseView().isNetworkEnable()) {
                mSortIndex = mPageIndex;
                setPageIndex(PAGE_START_INDEX);
                getBaseView().setRefreshMode(RefreshMode.SWIPE);
                launch(getRequest(getParams()), LaunchMode.REFRESH_ONLY);// refresh only, don't cache
            } else {
                getBaseView().hideSwipeRefresh();
                getBaseView().showToast(R.string.toast_common_no_network);
            }
        };
    }

    private LoadMore.OnLoadMoreListener getOnLoadMoreListener() {
        return isAuto -> {
            if (getBaseView().isNetworkEnable()) {
                if (mPageIndex == PAGE_START_INDEX) {
                    if (getBaseView().getAdapter().getItemCount() == mPageLimit) {
                        mPageIndex++;
                    } else {
                        mPageIndex = mSortIndex;
                    }
                }
                getBaseView().setRefreshMode(RefreshMode.LOADMORE);
                launch(getRequest(getParams()), LaunchMode.REFRESH_ONLY);// refresh only, don't cache
            } else {
                getBaseView().setLoadMoreFailed();
                if (!isAuto) {
                    getBaseView().showToast(R.string.toast_common_no_network);
                }
            }
        };
    }

    //    @Override
    public Observable<T> launch(Request<T> request, LaunchMode mode) {
//        Observable<T> observable = super.launch(request, mode);
        Observable<T> observable = JoyHttp.getLauncher().launch(request, mode);
        observable
                .doOnSubscribe(super::doOnFirst)
                .filter(super::filterNull)
                .map(this::transform)
                .filter(this::filterEmpty)
                .subscribe(
                        this::onNext,
                        super::onError);
        return observable;
    }

    public List<?> transform(T t) {
        return (List<?>) t;
    }

    public boolean filterEmpty(List<?> ts) {
        if (ts.isEmpty()) {
            onEmpty();
        }
        return !ts.isEmpty();
    }

    @Override
    public void onEmpty() {
        ExRvAdapter adapter = getBaseView().getAdapter();
        if (adapter == null) {
            super.onEmpty();
            return;
        }
        if (mPageIndex == PAGE_START_INDEX) {
            int adapterItemCount = adapter.getItemCount();
            if (adapterItemCount > 0) {
                adapter.clear();
                adapter.notifyItemRangeRemoved(0, adapterItemCount);
            }
            super.onEmpty();
        } else {
            getBaseView().setLoadMoreEnable(false);
        }
    }

    public void onNext(List<?> ts) {
        int currentItemCount = ts.size();
        getBaseView().setLoadMoreEnable(currentItemCount >= mPageLimit);

        ExRvAdapter adapter = getBaseView().getAdapter();
        if (adapter != null) {
            int adapterItemCount = adapter.getItemCount();
            if (mPageIndex == PAGE_START_INDEX) {
                adapter.setData(ts);
                if (adapterItemCount == 0) {
                    adapter.notifyItemRangeInserted(0, currentItemCount);
                    getBaseView().getRecyclerView().getLayoutManager().scrollToPosition(0);
                    getBaseView().addLoadMoreIfNecessary();
                } else {
                    adapter.notifyItemRangeRemoved(0, adapterItemCount);
                    adapter.notifyItemRangeInserted(0, currentItemCount);// TODO 可以合并成adapter.notifyItemRangeChanged(0, adapterItemCount);
                    getBaseView().getRecyclerView().getLayoutManager().scrollToPosition(0);
                }
            } else {
                adapter.addAll(ts);
                adapter.notifyItemRangeInserted(adapterItemCount, currentItemCount);
            }
            if (isFinalResponse()) {
                mPageIndex++;
            }
        }
        super.onNext(null);
    }

    @Override
    public Observable<T> launchRefreshOnly(String... params) {
        setPageIndex(PAGE_START_INDEX);
        getBaseView().setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshOnly(params);
    }

    @Override
    public Observable<T> launchCacheOrRefresh(String... params) {
        setPageIndex(PAGE_START_INDEX);
        getBaseView().setRefreshMode(RefreshMode.FRAME);
        return super.launchCacheOrRefresh(params);
    }

    @Override
    public Observable<T> launchRefreshAndCache(String... params) {
        setPageIndex(PAGE_START_INDEX);
        getBaseView().setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshAndCache(params);
    }

    @Override
    public Observable<T> launchCacheAndRefresh(String... params) {
        setParams(params);
        setPageIndex(PAGE_START_INDEX);
        Request<T> req = getRequest(params);
        getBaseView().setRefreshMode(req.hasCache() ? RefreshMode.SWIPE : RefreshMode.FRAME);
        return launch(req, LaunchMode.CACHE_AND_REFRESH);
    }

    /**
     * show swipe refresh view {@link SwipeRefreshLayout}
     */
    public void launchSwipeRefresh(String... params) {
        setParams(params);
        setPageIndex(PAGE_START_INDEX);
        getBaseView().setRefreshMode(RefreshMode.SWIPE);
        launch(getRequest(params), getLaunchMode());
    }

    /**
     * show frame refresh view {@link com.joy.ui.view.JLoadingView}
     */
    public void launchFrameRefresh(String... params) {
        setParams(params);
        setPageIndex(PAGE_START_INDEX);
        getBaseView().setRefreshMode(RefreshMode.FRAME);
        launch(getRequest(params), getLaunchMode());
    }

    /**
     * 设置分页大小
     *
     * @param limit
     */
    public void setPageLimit(int limit) {
        mPageLimit = limit;
    }

    public int getPageLimit() {
        return mPageLimit;
    }

    /**
     * 设置页码
     *
     * @param index
     */
    public void setPageIndex(int index) {
        mPageIndex = index;
    }

    public int getPageIndex() {
        return mPageIndex;
    }
}
