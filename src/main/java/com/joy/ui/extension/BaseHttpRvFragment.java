package com.joy.ui.extension;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.joy.http.LaunchMode;
import com.joy.http.volley.ObjectRequest;
import com.joy.ui.RefreshMode;
import com.joy.ui.adapter.ExRvAdapter;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.view.LoadMore;
import com.joy.ui.view.recyclerview.JRecyclerView;
import com.joy.ui.view.recyclerview.RecyclerAdapter;
import com.joy.utils.CollectionUtil;

import java.util.List;

import rx.Subscription;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by Daisw on 2017/5/16.
 *
 * @param <T>
 * @See {@link BaseHttpLvFragment<T>}.
 */

public abstract class BaseHttpRvFragment<T> extends BaseHttpUiFragment<T> {

    protected static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    protected int PAGE_START_INDEX = 1;// 默认起始页码
    protected SwipeRefreshLayout mSwipeRl;
    protected RecyclerView mRecyclerView;
    protected int mPageLimit = PAGE_UPPER_LIMIT;
    protected int mPageIndex = PAGE_START_INDEX;
    protected int mSortIndex = mPageIndex;
    protected RefreshMode mRefreshMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mRecyclerView = provideRecyclerView();
        mRecyclerView.setLayoutManager(provideLayoutManager());
        setContentView(wrapSwipeRefresh(mRecyclerView));
        return v;
    }

    /**
     * 子类可以复写此方法，为自己定制RecyclerView
     *
     * @return 自定义的RecyclerView
     */
    public RecyclerView provideRecyclerView() {
        JRecyclerView jrv = inflateLayout(R.layout.lib_view_recycler);
        jrv.setLoadMoreView(provideLoadMoreView());
        jrv.setOnLoadMoreListener(getOnLoadMoreListener());
        return jrv;
    }

    public View provideLoadMoreView() {
        return JLoadingView.getLoadMore(getActivity());
    }

    /**
     * 子类可以复写此方法，为自己定制LayoutManager，默认为LinearLayoutManager
     * LinearLayoutManager (线性显示，类似于ListView)
     * GridLayoutManager (线性宫格显示，类似于GridView)
     * StaggeredGridLayoutManager(线性宫格显示，类似于瀑布流)
     *
     * @return 自定义的LayoutManager
     */
    public LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    private View wrapSwipeRefresh(View contentView) {
        mSwipeRl = new SwipeRefreshLayout(getActivity());
        mSwipeRl.setColorSchemeResources(R.color.color_accent);
        mSwipeRl.setOnRefreshListener(getOnRefreshListener());
        mSwipeRl.addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mSwipeRl;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    public void setRefreshMode(RefreshMode mode) {
        mRefreshMode = mode;
    }

    public RefreshMode getRefreshMode() {
        return mRefreshMode;
    }

    private OnRefreshListener getOnRefreshListener() {
        return () -> {
            if (isNetworkEnable()) {
                mSortIndex = mPageIndex;
                setPageIndex(PAGE_START_INDEX);
                setRefreshMode(RefreshMode.SWIPE);
                launch(getRequest(), LaunchMode.REFRESH_ONLY);
            } else {
                hideSwipeRefresh();
                showToast(R.string.toast_common_no_network);
            }
        };
    }

    private LoadMore.OnLoadMoreListener getOnLoadMoreListener() {
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
                launch(getRequest(), LaunchMode.REFRESH_ONLY);
            } else {
                setLoadMoreFailed();
                if (!isAuto) {
                    showToast(R.string.toast_common_no_network);
                }
            }
        };
    }

    @Override
    protected ObjectRequest<T> getRequest() {
        return getRequest(mPageIndex, mPageLimit);
    }

    protected abstract ObjectRequest<T> getRequest(int pageIndex, int pageLimit);

    @Override
    public Subscription launchRefreshOnly() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshOnly();
    }

    @Override
    public Subscription launchCacheOrRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchCacheOrRefresh();
    }

    @Override
    public Subscription launchRefreshAndCache() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshAndCache();
    }

    @Override
    public Subscription launchCacheAndRefresh() {
        setPageIndex(PAGE_START_INDEX);
        ObjectRequest<T> req = getRequest();
        setRefreshMode(req.hasCache() ? RefreshMode.SWIPE : RefreshMode.FRAME);
        return launch(req, LaunchMode.CACHE_AND_REFRESH);
    }

    /**
     * show swipe refresh view {@link SwipeRefreshLayout}
     */
    public void launchSwipeRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.SWIPE);
        doOnRetry();
    }

    /**
     * show frame refresh view {@link JLoadingView}
     */
    public void launchFrameRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        doOnRetry();
    }

    /**
     * 设置分页大小
     *
     * @param pageLimit 分页大小
     */
    public void setPageLimit(int pageLimit) {
        mPageLimit = pageLimit;
    }

    public int getPageLimit() {
        return mPageLimit;
    }

    /**
     * 设置页码
     *
     * @param index 页码
     */
    public void setPageIndex(int index) {
        mPageIndex = index;
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    public int getHeaderViewsCount() {
        return ((RecyclerAdapter) mRecyclerView.getAdapter()).getHeadersCount();
    }

    public int getFooterViewsCount() {
        return ((RecyclerAdapter) mRecyclerView.getAdapter()).getFootersCount();
    }

    public void addHeaderView(View v) {
        Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException(
                    "Cannot add header view to recycler -- setAdapter has not been called.");
        ((RecyclerAdapter) adapter).addHeaderView(v);
    }

    public void addFooterView(View v) {
        Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException(
                    "Cannot add footer view to recycler -- setAdapter has not been called.");
        ((RecyclerAdapter) adapter).addFooterView(v);
    }

    public void removeHeaderView(View v) {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeHeader(v);
    }

    public void removeAllHeaders() {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeAllHeaders();
    }

    public void removeFooterView(View v) {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeFooter(v);
    }

    public void removeAllFooters() {
        ((RecyclerAdapter) mRecyclerView.getAdapter()).removeAllFooters();
    }

    public void setAdapter(ExRvAdapter adapter) {
        mRecyclerView.setAdapter(new RecyclerAdapter(adapter, getLayoutManager()));
    }

    public ExRvAdapter getAdapter() {
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
        int adapterItemCount = adapter.getItemCount();
        List<?> ts = getListInvalidateContent(t);
        int currentItemCount = CollectionUtil.size(ts);
        if (currentItemCount == 0) {
            if (mPageIndex == PAGE_START_INDEX) {
                if (adapterItemCount > 0) {
                    adapter.clear();
                    adapter.notifyItemRangeRemoved(0, adapterItemCount);
                }
                return false;
            } else {
                setLoadMoreEnable(false);
                return true;
            }
        }
        setLoadMoreEnable(currentItemCount >= mPageLimit);

        if (mPageIndex == PAGE_START_INDEX) {
            adapter.setData(ts);
            if (adapterItemCount == 0) {
                adapter.notifyItemRangeInserted(0, currentItemCount);
                getLayoutManager().scrollToPosition(0);
                addLoadMoreIfNecessary();
            } else {
                adapter.notifyItemRangeRemoved(0, adapterItemCount);
                adapter.notifyItemRangeInserted(0, currentItemCount);// TODO 可以合并成adapter.notifyItemRangeChanged(0, adapterItemCount);
                getLayoutManager().scrollToPosition(0);
            }
        } else {
            adapter.addAll(ts);
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
    public void showLoading() {
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
            default:
                break;
        }
    }

    @Override
    public void hideLoading() {
        switch (mRefreshMode) {
            case SWIPE:
                hideSwipeRefresh();
                break;
            case FRAME:
                super.hideLoading();
                break;
            case LOADMORE:
                stopLoadMore();
                break;
            default:
                break;
        }
    }

    @Override
    public void showErrorTip() {
        switch (mRefreshMode) {
            case SWIPE:
                showToast(R.string.toast_common_timeout);
                break;
            case FRAME:
                if (getAdapter().getItemCount() == 0) {
                    super.showErrorTip();
                }
                break;
            case LOADMORE:
                setLoadMoreFailed();
                break;
            default:
                break;
        }
    }

    @Override
    public void showEmptyTip() {
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getItemCount() == 0) {
            super.showEmptyTip();
        }
    }

    @Override
    public void hideContent() {
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getItemCount() == 0) {
            super.hideContent();
        }
    }


    // swipe refresh
    // =============================================================================================
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRl;
    }

    public void setSwipeRefreshEnable(boolean enable) {
        mSwipeRl.setEnabled(enable);
    }

    public boolean isSwipeRefreshing() {
        return mSwipeRl.isRefreshing();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mSwipeRl.setOnRefreshListener(listener);
    }

    public void showSwipeRefresh() {
        if (!isSwipeRefreshing()) {
            mSwipeRl.setRefreshing(true);
        }
    }

    public void hideSwipeRefresh() {
        if (isSwipeRefreshing()) {
            mSwipeRl.setRefreshing(false);
        }
    }

    public void setSwipeRefreshColors(@ColorRes int... resIds) {
        mSwipeRl.setColorSchemeResources(resIds);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
    public void setLoadMoreEnable(boolean enable) {
        if (mRecyclerView instanceof JRecyclerView) {
            ((JRecyclerView) mRecyclerView).setLoadMoreEnable(enable);
        }
    }

    public boolean isLoadMoreEnable() {
        return mRecyclerView instanceof JRecyclerView && ((JRecyclerView) mRecyclerView).isLoadMoreEnable();
    }

    public void addLoadMoreIfNecessary() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).addLoadMoreIfNotExist();
        }
    }

    public boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JRecyclerView) mRecyclerView).isLoadingMore();
    }

    public void setOnLoadMoreListener(LoadMore.OnLoadMoreListener listener) {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setOnLoadMoreListener(listener);
        }
    }

    public void stopLoadMore() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).stopLoadMore();
        }
    }

    public void setLoadMoreFailed() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreFailed();
        }
    }

    public void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).hideLoadMore();
        }
    }

    public void setLoadMoreTheme(LoadMore.Theme theme) {
        if (isLoadMoreEnable()) {
            switch (theme) {
                case LIGHT:
                    ((JRecyclerView) mRecyclerView).setLoadMoreLightTheme();
                    break;
                case DARK:
                    ((JRecyclerView) mRecyclerView).setLoadMoreDarkTheme();
                    break;
                default:
                    break;
            }
        }
    }

    public void setLoadMoreHintColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
