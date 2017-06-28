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

import rx.Observable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by KEVIN.DAI on 15/7/16.
 *
 * @param <T>
 * @See {@link BaseHttpLvActivity<T>}.
 */
public abstract class BaseHttpRvActivity<T> extends BaseHttpUiActivity<T> {

    private static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    protected static int PAGE_START_INDEX = 1;// 默认起始页码
    private SwipeRefreshLayout mSwipeRl;
    private RecyclerView mRecyclerView;
    private int mPageLimit = PAGE_UPPER_LIMIT;
    private int mPageIndex = PAGE_START_INDEX;
    private int mSortIndex = mPageIndex;
    private RefreshMode mRefreshMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView = provideRecyclerView();
        mRecyclerView.setLayoutManager(provideLayoutManager());
        setContentView(wrapSwipeRefresh(mRecyclerView));
    }

    /**
     * 子类可以复写此方法，为自己定制RecyclerView
     *
     * @return 自定义的RecyclerView
     */
    protected RecyclerView provideRecyclerView() {
        JRecyclerView jrv = inflateLayout(R.layout.lib_view_recycler);
        jrv.setLoadMoreView(provideLoadMoreView());
        jrv.setOnLoadMoreListener(getOnLoadMoreListener());
        return jrv;
    }

    protected View provideLoadMoreView() {
        return JLoadingView.getLoadMore(this);
    }

    /**
     * 子类可以复写此方法，为自己定制LayoutManager，默认为LinearLayoutManager
     * LinearLayoutManager (线性显示，类似于ListView)
     * GridLayoutManager (线性宫格显示，类似于GridView)
     * StaggeredGridLayoutManager(线性宫格显示，类似于瀑布流)
     *
     * @return 自定义的LayoutManager
     */
    protected LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(this);
    }

    private View wrapSwipeRefresh(View contentView) {
        mSwipeRl = new SwipeRefreshLayout(this);
        mSwipeRl.setColorSchemeResources(R.color.color_accent);
        mSwipeRl.setOnRefreshListener(getOnRefreshListener());
        mSwipeRl.addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mSwipeRl;
    }

    protected final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected final LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    protected final void setRefreshMode(RefreshMode mode) {
        mRefreshMode = mode;
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
    protected final ObjectRequest<T> getRequest() {
        return getRequest(mPageIndex, mPageLimit);
    }

    protected abstract ObjectRequest<T> getRequest(int pageIndex, int pageLimit);

    @Override
    protected final Observable<T> launchRefreshOnly() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshOnly();
    }

    @Override
    protected final Observable<T> launchCacheOrRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchCacheOrRefresh();
    }

    @Override
    protected final Observable<T> launchRefreshAndCache() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchRefreshAndCache();
    }

    @Override
    protected final Observable<T> launchCacheAndRefresh() {
        setPageIndex(PAGE_START_INDEX);
        ObjectRequest<T> req = getRequest();
        setRefreshMode(req.hasCache() ? RefreshMode.SWIPE : RefreshMode.FRAME);
        return launch(req, LaunchMode.CACHE_AND_REFRESH);
    }

    /**
     * show swipe refresh view {@link SwipeRefreshLayout}
     */
    protected final void launchSwipeRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.SWIPE);
        doOnRetry();
    }

    /**
     * show frame refresh view {@link JLoadingView}
     */
    protected final void launchFrameRefresh() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        doOnRetry();
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
        mRecyclerView.setAdapter(new RecyclerAdapter(adapter, getLayoutManager()));
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
        List<?> ts = getListInvalidateContent(t);
        final int currentItemCount = CollectionUtil.size(ts);
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
    public final void showLoading() {
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
    public final void hideLoading() {
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
    public final void showErrorTip() {
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
    public final void showEmptyTip() {
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getItemCount() == 0) {
            super.showEmptyTip();
        }
    }

    @Override
    public final void hideContent() {
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getItemCount() == 0) {
            super.hideContent();
        }
    }


    // swipe refresh
    // =============================================================================================
    protected final SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRl;
    }

    protected final void setSwipeRefreshEnable(boolean enable) {
        mSwipeRl.setEnabled(enable);
    }

    protected final boolean isSwipeRefreshing() {
        return mSwipeRl.isRefreshing();
    }

    protected final void setOnRefreshListener(OnRefreshListener listener) {
        mSwipeRl.setOnRefreshListener(listener);
    }

    protected final void showSwipeRefresh() {
        if (!isSwipeRefreshing()) {
            mSwipeRl.setRefreshing(true);
        }
    }

    protected final void hideSwipeRefresh() {
        if (isSwipeRefreshing()) {
            mSwipeRl.setRefreshing(false);
        }
    }

    protected final void setSwipeRefreshColors(@ColorRes int... resIds) {
        mSwipeRl.setColorSchemeResources(resIds);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
    protected final void setLoadMoreEnable(boolean enable) {
        if (mRecyclerView instanceof JRecyclerView) {
            ((JRecyclerView) mRecyclerView).setLoadMoreEnable(enable);
        }
    }

    protected final boolean isLoadMoreEnable() {
        return mRecyclerView instanceof JRecyclerView && ((JRecyclerView) mRecyclerView).isLoadMoreEnable();
    }

    protected final void addLoadMoreIfNecessary() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).addLoadMoreIfNotExist();
        }
    }

    protected final boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JRecyclerView) mRecyclerView).isLoadingMore();
    }

    protected final void setOnLoadMoreListener(LoadMore.OnLoadMoreListener listener) {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setOnLoadMoreListener(listener);
        }
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

    protected final void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).hideLoadMore();
        }
    }

    protected final void setLoadMoreTheme(LoadMore.Theme theme) {
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

    protected final void setLoadMoreHintColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JRecyclerView) mRecyclerView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
