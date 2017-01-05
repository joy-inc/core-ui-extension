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
import com.joy.ui.adapter.ExLvAdapter;
import com.joy.ui.view.JListView;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.view.OnLoadMoreListener;
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
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private ListView mListView;
    private int mPageLimit = PAGE_UPPER_LIMIT;
    private int mPageIndex = PAGE_START_INDEX;
    private int mSortIndex = mPageIndex;
    private RefreshMode mRefreshMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = getDefaultListView();
        setContentView(wrapSwipeRefresh(mListView));
    }

    /**
     * 子类可以复写此方法，为自己定制ListView
     *
     * @return 自定义的ListView
     */
    protected ListView getDefaultListView() {
        JListView jlv = inflateLayout(R.layout.lib_view_listview);
        jlv.setLoadMoreView(JLoadingView.getLoadMore(this));
        jlv.setLoadMoreListener(getDefaultLoadMoreLisn());
        return jlv;
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
                    if (getAdapter().getCount() == mPageLimit) {
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
        List<?> datas = getListInvalidateContent(t);
        final int currentItemCount = CollectionUtil.size(datas);
        if (currentItemCount == 0) {
            if (mPageIndex == PAGE_START_INDEX) {
                if (adapterItemCount > 0) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
                addLoadMoreIfNotExist();
            } else {
                adapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(0);
            }
        } else {
            adapter.addAll(datas);
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
    public void showErrorTip() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getCount() == 0) {
            super.showErrorTip();
        }
    }

    @Override
    public void showEmptyTip() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getCount() == 0) {
            super.showEmptyTip();
        }
    }

    @Override
    public void hideContent() {
        if (mRefreshMode == RefreshMode.FRAME || getAdapter().getCount() == 0) {
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
        return mListView instanceof JListView && ((JListView) mListView).isLoadMoreEnable();
    }

    protected final void setLoadMoreEnable(boolean enable) {
        if (mListView instanceof JListView) {
            ((JListView) mListView).setLoadMoreEnable(enable);
        }
    }

    protected final void addLoadMoreIfNotExist() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).addLoadMoreIfNotExist();
        }
    }

    protected final boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JListView) mListView).isLoadingMore();
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

    protected final boolean isLoadMoreFailed() {
        return isLoadMoreEnable() && ((JListView) mListView).isLoadMoreFailed();
    }

    protected final void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).hideLoadMore();
        }
    }

    protected final void setLoadMoreDarkTheme() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreDarkTheme();
        }
    }

    protected final void setLoadMoreLightTheme() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreLightTheme();
        }
    }

    protected final void setLoadMoreHintTextColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
