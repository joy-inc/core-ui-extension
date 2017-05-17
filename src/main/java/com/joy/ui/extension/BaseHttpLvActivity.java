package com.joy.ui.extension;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.joy.http.RequestMode;
import com.joy.http.volley.ObjectRequest;
import com.joy.ui.RefreshMode;
import com.joy.ui.adapter.ExLvAdapter;
import com.joy.ui.view.JListView;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.view.LoadMore;
import com.joy.utils.CollectionUtil;

import java.util.List;

import rx.Observable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by KEVIN.DAI on 15/7/16.
 *
 * @param <T>
 * @See {@link BaseHttpRvActivity<T>}.
 */
public abstract class BaseHttpLvActivity<T> extends BaseHttpUiActivity<T> {

    private static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    private static final int PAGE_START_INDEX = 1;// 默认起始页码
    private SwipeRefreshLayout mSwipeRl;
    private ListView mListView;
    private int mPageLimit = PAGE_UPPER_LIMIT;
    private int mPageIndex = PAGE_START_INDEX;
    private int mSortIndex = mPageIndex;
    private RefreshMode mRefreshMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = provideListView();
        setContentView(wrapSwipeRefresh(mListView));
    }

    /**
     * 子类可以复写此方法，为自己定制ListView
     *
     * @return 自定义的ListView
     */
    protected ListView provideListView() {
        JListView jlv = inflateLayout(R.layout.lib_view_listview);
        jlv.setLoadMoreView(JLoadingView.getLoadMore(this));
        jlv.setOnLoadMoreListener(getOnLoadMoreListener());
        return jlv;
    }

    private View wrapSwipeRefresh(View contentView) {
        mSwipeRl = new SwipeRefreshLayout(this);
        mSwipeRl.setColorSchemeResources(R.color.color_accent);
        mSwipeRl.setOnRefreshListener(getOnRefreshListener());
        mSwipeRl.addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mSwipeRl;
    }

    private void setRefreshMode(RefreshMode mode) {
        mRefreshMode = mode;
    }

    private OnRefreshListener getOnRefreshListener() {
        return () -> {
            if (isNetworkEnable()) {
                mSortIndex = mPageIndex;
                setPageIndex(PAGE_START_INDEX);
                setRefreshMode(RefreshMode.SWIPE);
                launch(getRequest(), RequestMode.REFRESH_ONLY);
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
                    if (getAdapter().getCount() == mPageLimit) {
                        mPageIndex++;
                    } else {
                        mPageIndex = mSortIndex;
                    }
                }
                setRefreshMode(RefreshMode.LOADMORE);
                launch(getRequest(), RequestMode.REFRESH_ONLY);
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
    protected final Observable<T> launchCacheOnly() {
        setPageIndex(PAGE_START_INDEX);
        setRefreshMode(RefreshMode.FRAME);
        return super.launchCacheOnly();
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
        return launch(req, RequestMode.CACHE_AND_REFRESH);
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

    protected final ListView getListView() {
        return mListView;
    }

    protected final int getHeaderViewsCount() {
        return mListView.getHeaderViewsCount();
    }

    protected final int getFooterViewsCount() {
        return mListView.getFooterViewsCount();
    }

    protected final void addHeaderView(View v) {
        mListView.addHeaderView(v);
    }

    protected final void addFooterView(View v) {
        mListView.addFooterView(v);
    }

    protected final void removeHeaderView(View v) {
        mListView.removeHeaderView(v);
    }

    protected final void removeFooterView(View v) {
        mListView.removeFooterView(v);
    }

    protected final void setAdapter(ExLvAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    protected final ExLvAdapter getAdapter() {
        ListAdapter adapter = mListView.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return (ExLvAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return (ExLvAdapter) adapter;
        }
    }

    @Override
    protected boolean invalidateContent(T t) {
        ExLvAdapter adapter = getAdapter();
        if (adapter == null) {
            return false;
        }
        final int adapterItemCount = adapter.getCount();
        List<?> ts = getListInvalidateContent(t);
        final int currentItemCount = CollectionUtil.size(ts);
        if (currentItemCount == 0) {
            if (mPageIndex == PAGE_START_INDEX) {
                if (adapterItemCount > 0) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
                addLoadMoreIfNecessary();
            } else {
                adapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(0);
            }
        } else {
            adapter.addAll(ts);
            adapter.notifyDataSetChanged();
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
    public void showErrorTip() {
        switch (mRefreshMode) {
            case SWIPE:
                showToast(R.string.toast_common_timeout);
                break;
            case FRAME:
                if (getAdapter().getCount() == 0) {
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
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getCount() == 0) {
            super.showEmptyTip();
        }
    }

    @Override
    public void hideContent() {
        if ((mRefreshMode == RefreshMode.SWIPE || mRefreshMode == RefreshMode.FRAME) && getAdapter().getCount() == 0) {
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

    protected final void setOnRefreshListener(OnRefreshListener lisn) {
        mSwipeRl.setOnRefreshListener(lisn);
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

    protected final void setSwipeRefreshColors(@ColorRes int... colorResIds) {
        mSwipeRl.setColorSchemeResources(colorResIds);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
    protected final void setLoadMoreEnable(boolean enable) {
        if (mListView instanceof JListView) {
            ((JListView) mListView).setLoadMoreEnable(enable);
        }
    }

    protected final boolean isLoadMoreEnable() {
        return mListView instanceof JListView && ((JListView) mListView).isLoadMoreEnable();
    }

    protected final void addLoadMoreIfNecessary() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).addLoadMoreIfNotExist();
        }
    }

    protected final boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JListView) mListView).isLoadingMore();
    }

    protected final void setOnLoadMoreListener(LoadMore.OnLoadMoreListener listener) {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setOnLoadMoreListener(listener);
        }
    }

    protected final void stopLoadMore() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).stopLoadMore();
        }
    }

    protected final void setLoadMoreFailed() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreFailed();
        }
    }

    protected final void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).hideLoadMore();
        }
    }

    protected final void setLoadMoreTheme(LoadMore.Theme theme) {
        if (isLoadMoreEnable()) {
            switch (theme) {
                case LIGHT:
                    ((JListView) mListView).setLoadMoreLightTheme();
                    break;
                case DARK:
                    ((JListView) mListView).setLoadMoreDarkTheme();
                    break;
                default:
                    break;
            }
        }
    }

    protected final void setLoadMoreHintColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
