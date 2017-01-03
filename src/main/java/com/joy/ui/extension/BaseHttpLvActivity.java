package com.joy.ui.extension;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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

/**
 * Created by KEVIN.DAI on 15/7/16.
 *
 * @param <T>
 * @See {@link BaseHttpRvActivity<T>}.
 */
public abstract class BaseHttpLvActivity<T> extends BaseHttpUiActivity<T> {

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private ListView mListView;
    private int mPageLimit = 20;
    private static final int PAGE_START_INDEX = 1;// 默认从第一页开始
    private int mPageIndex = PAGE_START_INDEX;
    private int mSortIndex = mPageIndex;

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
        mSwipeRefreshWidget.addView(contentView);
        return mSwipeRefreshWidget;
    }

    private OnRefreshListener getDefaultRefreshLisn() {
        return () -> {
            if (isNetworkEnable()) {
                mSortIndex = mPageIndex;
                mPageIndex = PAGE_START_INDEX;
                stopLoadMore();
                startRefresh();
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
                hideSwipeRefresh();
                startRefresh();
            } else {
                setLoadMoreFailed();
                if (!isAuto) {
                    showToast(R.string.toast_common_no_network);
                }
            }
        };
    }

    private void startRefresh() {
        launchRefreshOnly();
    }

    @Override
    protected final ObjectRequest<T> getRequest() {
        return getObjectRequest(mPageIndex, mPageLimit);
    }

    protected abstract ObjectRequest<T> getObjectRequest(int pageIndex, int pageLimit);

    /**
     * show swipe refresh view {@link SwipeRefreshLayout}
     */
    protected void launchSwipeRefresh() {
        showSwipeRefresh();
        mPageIndex = PAGE_START_INDEX;
        doOnRetry();
    }

    /**
     * show frame refresh view {@link JLoadingView}
     */
    protected void launchFrameRefresh() {
        mPageIndex = PAGE_START_INDEX;
        doOnRetry();
    }

    /**
     * 设置分页大小
     *
     * @param pageLimit
     */
    protected void setPageLimit(int pageLimit) {
        mPageLimit = pageLimit;
    }

    /**
     * 设置页码
     *
     * @param index
     */
    protected void setPageIndex(int index) {
        mPageIndex = index;
    }

    protected ListView getListView() {
        return mListView;
    }

    protected void addHeaderView(View v) {
        mListView.addHeaderView(v);
    }

    protected void addFooterView(View v) {
        mListView.addFooterView(v);
    }

    protected void setOnItemClickListener(OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    protected void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mListView.setOnItemLongClickListener(listener);
    }

    protected void setAdapter(ExLvAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    protected ExLvAdapter getAdapter() {
        ListAdapter adapter = mListView.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return (ExLvAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return (ExLvAdapter) adapter;
        }
    }

    @Override
    protected boolean invalidateContent(T t) {
        List<?> datas = getListInvalidateContent(t);
        if (CollectionUtil.isEmpty(datas)) {
            return false;
        }

        setLoadMoreEnable(datas.size() >= mPageLimit);
        stopLoadMore();

        ExLvAdapter adapter = getAdapter();
        if (adapter != null) {
            if (mPageIndex == PAGE_START_INDEX) {
                adapter.setData(datas);
            } else {
                adapter.addAll(datas);
            }
            adapter.notifyDataSetChanged();
            if (isFinalResponse()) {
                mPageIndex++;
            }
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
    public final void showLoading() {
        if (getRequestMode() == RequestMode.CACHE_AND_REFRESH && isReqHasCache())
            showSwipeRefresh();
        else if (!isSwipeRefreshing() && !isLoadingMore())
            super.showLoading();
    }

    @Override
    public final void hideLoading() {
        if (isSwipeRefreshing())
            hideSwipeRefresh();
        else
            super.hideLoading();
    }

    @Override
    public final void showErrorTip() {
        if (getItemCount() - 1 == 0)
            super.showErrorTip();
    }

    @Override
    public final void showEmptyTip() {
        if (getItemCount() - 1 == 0)
            super.showEmptyTip();
    }

    @Override
    public final void hideContent() {
        if (getItemCount() - 1 == 0)
            super.hideContent();
    }

    private int getItemCount() {
        return mListView.getHeaderViewsCount() + mListView.getFooterViewsCount() + getAdapter().getCount();
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
        if (isSwipeRefreshing())
            return;
        mSwipeRefreshWidget.setRefreshing(true);
    }

    protected void hideSwipeRefresh() {
        if (!isSwipeRefreshing())
            return;
        mSwipeRefreshWidget.setRefreshing(false);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
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
            ((JListView) mListView).stopLoadMoreFailed();
        }
    }

    protected final boolean isLoadMoreFailed() {
        return isLoadMoreEnable() && ((JListView) mListView).isLoadMoreFailed();
    }

    protected final void setLoadMoreEnable(boolean enable) {
        if (mListView instanceof JListView) {
            ((JListView) mListView).setLoadMoreEnable(enable);
        }
    }

    protected final boolean isLoadMoreEnable() {
        return mListView instanceof JListView && ((JListView) mListView).isLoadMoreEnable();
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