package com.joy.ui.extension;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.joy.http.RequestMode;
import com.joy.http.volley.ObjectRequest;
import com.joy.ui.adapter.ExRvAdapter;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.view.OnLoadMoreListener;
import com.joy.ui.view.recyclerview.JRecyclerView;
import com.joy.ui.view.recyclerview.RecyclerAdapter;
import com.joy.utils.CollectionUtil;

import java.util.List;

import rx.Observable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by KEVIN.DAI on 15/7/16.
 *
 * @param <T>
 */
public abstract class BaseHttpRvActivity<T> extends BaseHttpUiActivity<T> {

    private static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    private static final int PAGE_START_INDEX = 1;// 默认起始页码
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private int mPageLimit = PAGE_UPPER_LIMIT;
    private int mPageIndex = PAGE_START_INDEX;
    private int mSortIndex = mPageIndex;
    private RefreshMode mRefreshMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView = getDefaultRecyclerView();
        mRecyclerView.setLayoutManager(getDefaultLayoutManager());
        setContentView(wrapSwipeRefresh(mRecyclerView));
    }

    /**
     * 子类可以复写此方法，为自己定制RecyclerView
     *
     * @return 自定义的RecyclerView
     */
    protected RecyclerView getDefaultRecyclerView() {
        JRecyclerView jrv = inflateLayout(R.layout.lib_view_recycler);
        jrv.setLoadMoreView(JLoadingView.getLoadMore(this));
        jrv.setLoadMoreListener(getDefaultLoadMoreLisn());
        return jrv;
    }

    /**
     * 子类可以复写此方法，为自己定制LayoutManager，默认为LinearLayoutManager
     * LinearLayoutManager (线性显示，类似于ListView)
     * GridLayoutManager (线性宫格显示，类似于GridView)
     * StaggeredGridLayoutManager(线性宫格显示，类似于瀑布流)
     *
     * @return 自定义的LayoutManager
     */
    protected LayoutManager getDefaultLayoutManager() {
        return new LinearLayoutManager(this);
    }

    private View wrapSwipeRefresh(View contentView) {
        mSwipeRefreshWidget = new SwipeRefreshLayout(this);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.color_accent);
        mSwipeRefreshWidget.setOnRefreshListener(getDefaultRefreshLisn());
        mSwipeRefreshWidget.addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mSwipeRefreshWidget;
    }

    private OnRefreshListener getDefaultRefreshLisn() {
        return () -> {
            if (isNetworkEnable()) {
                mSortIndex = mPageIndex;
                setRefreshMode(RefreshMode.SWIPE);
                setPageIndex(PAGE_START_INDEX);
                launch(RequestMode.REFRESH_ONLY);
            } else {
                hideSwipeRefresh();
                showToast(R.string.toast_common_no_network);
            }
        };
    }

    private OnLoadMoreListener getDefaultLoadMoreLisn() {
        return (isAuto) -> {
            if (isNetworkEnable()) {
                if (mPageIndex == PAGE_START_INDEX) {
                    if (getAdapter().getItemCount() == mPageLimit) {
                        mPageIndex++;
                    } else {
                        mPageIndex = mSortIndex;
                    }
                }
                setRefreshMode(RefreshMode.LOADMORE);
                launch(RequestMode.REFRESH_ONLY);
            } else {
                setLoadMoreFailed();
                if (!isAuto) {
                    showToast(R.string.toast_common_no_network);
                }
            }
        };
    }

    @Override
    protected final ObjectRequest<T> getRequest() {
        return getRequest(mPageIndex, mPageLimit);
    }

    protected abstract ObjectRequest<T> getRequest(int pageIndex, int pageLimit);

    @Override
    protected final Observable<T> launchRefreshOnly() {
        setRefreshMode(RefreshMode.FRAME);
        setPageIndex(PAGE_START_INDEX);
        return super.launchRefreshOnly();
    }

    @Override
    protected final Observable<T> launchCacheOnly() {
        setRefreshMode(RefreshMode.FRAME);
        setPageIndex(PAGE_START_INDEX);
        return super.launchCacheOnly();
    }

    @Override
    protected final Observable<T> launchRefreshAndCache() {
        setRefreshMode(RefreshMode.FRAME);
        setPageIndex(PAGE_START_INDEX);
        return super.launchRefreshAndCache();
    }

    @Override
    protected final Observable<T> launchCacheAndRefresh() {
        setRefreshMode(RefreshMode.FRAME);
        setPageIndex(PAGE_START_INDEX);
        return super.launchCacheAndRefresh();
    }

    /**
     * show swipe refresh view {@link SwipeRefreshLayout}
     */
    protected final void launchSwipeRefresh() {
        setRefreshMode(RefreshMode.SWIPE);
        setPageIndex(PAGE_START_INDEX);
        doOnRetry();
    }

    /**
     * show frame refresh view {@link JLoadingView}
     */
    protected final void launchFrameRefresh() {
        setRefreshMode(RefreshMode.FRAME);
        setPageIndex(PAGE_START_INDEX);
        doOnRetry();
    }

    private void setRefreshMode(RefreshMode mode) {
        mRefreshMode = mode;
    }

    /**
     * 设置分页大小
     *
     * @param pageLimit 分页大小
     */
    protected final void setPageLimit(int pageLimit) {
        mPageLimit = pageLimit;
    }

    protected final int getPageLimit() {
        return mPageLimit;
    }

    /**
     * 设置页码
     *
     * @param index 页码
     */
    protected final void setPageIndex(int index) {
        mPageIndex = index;
    }

    protected final int getPageIndex() {
        return mPageIndex;
    }

    protected final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected final int getHeaderViewsCount() {
        return ((RecyclerAdapter) mRecyclerView.getAdapter()).getHeadersCount();
    }

    protected final int getFooterViewsCount() {
        return ((RecyclerAdapter) mRecyclerView.getAdapter()).getFootersCount();
    }

    protected final void addHeaderView(View v) {
        Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException(
                    "Cannot add header view to recycler -- setAdapter has not been called.");
        ((RecyclerAdapter) adapter).addHeaderView(v);
    }

    protected final void addFooterView(View v) {
        Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException(
                    "Cannot add footer view to recycler -- setAdapter has not been called.");
        ((RecyclerAdapter) adapter).addFooterView(v);
    }

    protected final void removeHeaderView(View v) {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeHeader(v);
    }

    protected final void removeFooterView(View v) {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeFooter(v);
    }

    protected final void setAdapter(ExRvAdapter adapter) {
        mRecyclerView.setAdapter(new RecyclerAdapter(adapter, getDefaultLayoutManager()));
    }

    protected final ExRvAdapter getAdapter() {
        Adapter adapter = mRecyclerView.getAdapter();
        if (adapter instanceof RecyclerAdapter) {
            return (ExRvAdapter) ((RecyclerAdapter) adapter).getWrappedAdapter();
        } else {
            return (ExRvAdapter) adapter;
        }
    }

    @Override
    protected boolean invalidateContent(T t) {
        ExRvAdapter adapter = getAdapter();
        if (adapter == null) {
            return false;
        }
        final int adapterItemCount = adapter.getItemCount();
        List<?> datas = getListInvalidateContent(t);
        final int currentItemCount = CollectionUtil.size(datas);
        if (currentItemCount == 0) {
            if (mPageIndex == PAGE_START_INDEX) {
                if (adapterItemCount > 0) {
                    adapter.clear();
                    adapter.notifyItemRangeRemoved(0, adapterItemCount);
                }
            } else {
                setLoadMoreEnable(false);
                return true;
            }
            return false;
        }

        stopLoadMore();
        setLoadMoreEnable(currentItemCount >= mPageLimit);

        if (mPageIndex == PAGE_START_INDEX) {
            adapter.setData(datas);
            if (adapterItemCount == 0) {
                adapter.notifyItemRangeInserted(0, currentItemCount);
                addLoadMoreIfNotExist();
            } else {
                adapter.notifyItemRangeRemoved(0, adapterItemCount);
                adapter.notifyItemRangeInserted(0, currentItemCount);
                mRecyclerView.getLayoutManager().scrollToPosition(0);
            }
        } else {
            adapter.addAll(datas);
            adapter.notifyItemRangeInserted(adapterItemCount, currentItemCount);
        }
        if (isFinalResponse()) {
            mPageIndex++;
        }
        return true;
    }

    protected List<?> getListInvalidateContent(T t) {
        return (List<?>) t;
    }

    @Override
    final void onHttpFailed(String msg) {
        super.onHttpFailed(msg);
        if (isSwipeRefreshing()) {// 下拉刷新触发
        } else if (isLoadingMore()) {// 加载更多触发
            setLoadMoreFailed();
        } else {// 首次加载触发
        }
    }

    @Override
    public final void showLoading() {// dispatch loading view
        if (getRequestMode() == RequestMode.CACHE_AND_REFRESH && isReqHasCache()) {
            setRefreshMode(RefreshMode.SWIPE);
        }
        switch (mRefreshMode) {
            case SWIPE:
                showSwipeRefresh();
                stopLoadMore();
                super.hideLoading();
                break;
            case FRAME:
                hideSwipeRefresh();
                hideLoadMore();
                super.showLoading();
                break;
            case LOADMORE:
                hideSwipeRefresh();
                break;
        }
    }

    @Override
    public final void hideLoading() {
        switch (mRefreshMode) {
            case SWIPE:
                hideSwipeRefresh();
                break;
            case FRAME:
                super.hideLoading();
                break;
        }
    }

    @Override
    public final void showErrorTip() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getItemCount() == 0) {
            super.showErrorTip();
        }
    }

    @Override
    public final void showEmptyTip() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getItemCount() == 0) {
            super.showEmptyTip();
        }
    }

    @Override
    public final void hideContent() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getItemCount() == 0) {
            super.hideContent();
        }
    }


    // swipe refresh
    // =============================================================================================
    protected void setSwipeRefreshEnable(boolean enable) {
        mSwipeRefreshWidget.setEnabled(enable);
    }

    protected void setColorSchemeResources(int... colorResIds) {
        mSwipeRefreshWidget.setColorSchemeResources(colorResIds);
    }

    protected void setOnRefreshListener(OnRefreshListener lisn) {
        mSwipeRefreshWidget.setOnRefreshListener(lisn);
    }

    protected boolean isSwipeRefreshing() {
        return mSwipeRefreshWidget.isRefreshing();
    }

    protected void showSwipeRefresh() {
        if (isSwipeRefreshing()) {
            return;
        }
        mSwipeRefreshWidget.setRefreshing(true);
    }

    protected void hideSwipeRefresh() {
        if (!isSwipeRefreshing()) {
            return;
        }
        mSwipeRefreshWidget.setRefreshing(false);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
    protected final boolean isLoadMoreEnable() {
        return mRecyclerView instanceof JRecyclerView && ((JRecyclerView) mRecyclerView).isLoadMoreEnable();
    }

    protected final void setLoadMoreEnable(boolean enable) {
        if (mRecyclerView instanceof JRecyclerView) {
            ((JRecyclerView) mRecyclerView).setLoadMoreEnable(enable);
        }
    }

    protected final void addLoadMoreIfNotExist() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).addLoadMoreIfNotExist();
        }
    }

    protected final boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JRecyclerView) mRecyclerView).isLoadingMore();
    }

    protected final void stopLoadMore() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).stopLoadMore();
        }
    }

    protected final void setLoadMoreFailed() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreFailed();
        }
    }

    protected final boolean isLoadMoreFailed() {
        return isLoadMoreEnable() && ((JRecyclerView) mRecyclerView).isLoadMoreFailed();
    }

    protected final void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).hideLoadMore();
        }
    }

    protected final void setLoadMoreDarkTheme() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreDarkTheme();
        }
    }

    protected final void setLoadMoreLightTheme() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreLightTheme();
        }
    }

    protected final void setLoadMoreHintTextColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
