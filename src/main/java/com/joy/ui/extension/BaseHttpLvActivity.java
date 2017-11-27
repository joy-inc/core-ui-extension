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

import com.joy.http.LaunchMode;
import com.joy.http.volley.ObjectRequest;
import com.joy.ui.RefreshMode;
import com.joy.ui.adapter.ExLvAdapter;
import com.joy.ui.view.JListView;
import com.joy.ui.view.JLoadingView;
import com.joy.ui.view.LoadMore;
import com.joy.utils.CollectionUtil;

import java.util.List;

import rx.Subscription;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by KEVIN.DAI on 15/7/16.
 *
 * @param <T>
 * @See {@link BaseHttpRvActivity<T>}.
 */
public abstract class BaseHttpLvActivity<T> extends BaseHttpUiActivity<T> {

    protected static final int PAGE_UPPER_LIMIT = 20;// 默认分页大小
    protected int PAGE_START_INDEX = 1;// 默认起始页码
    protected SwipeRefreshLayout mSwipeRl;
    protected ListView mListView;
    protected int mPageLimit = PAGE_UPPER_LIMIT;
    protected int mPageIndex = PAGE_START_INDEX;
    protected int mSortIndex = mPageIndex;
    protected RefreshMode mRefreshMode;

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
    public ListView provideListView() {
        JListView jlv = inflateLayout(R.layout.lib_view_listview);
        jlv.setLoadMoreView(provideLoadMoreView());
        jlv.setOnLoadMoreListener(getOnLoadMoreListener());
        return jlv;
    }

    public View provideLoadMoreView() {
        return JLoadingView.getLoadMore(this);
    }

    private View wrapSwipeRefresh(View contentView) {
        mSwipeRl = new SwipeRefreshLayout(this);
        mSwipeRl.setColorSchemeResources(R.color.color_accent);
        mSwipeRl.setOnRefreshListener(getOnRefreshListener());
        mSwipeRl.addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mSwipeRl;
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
                    if (getAdapter().getCount() == mPageLimit) {
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

    public ListView getListView() {
        return mListView;
    }

    public int getHeaderViewsCount() {
        return mListView.getHeaderViewsCount();
    }

    public int getFooterViewsCount() {
        return mListView.getFooterViewsCount();
    }

    public void addHeaderView(View v) {
        mListView.addHeaderView(v);
    }

    public void addFooterView(View v) {
        mListView.addFooterView(v);
    }

    public void removeHeaderView(View v) {
        mListView.removeHeaderView(v);
    }

    public void removeFooterView(View v) {
        mListView.removeFooterView(v);
    }

    public void setAdapter(ExLvAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public ExLvAdapter getAdapter() {
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
        int adapterItemCount = adapter.getCount();
        List<?> ts = getListInvalidateContent(t);
        int currentItemCount = CollectionUtil.size(ts);
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
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRl;
    }


    public void setSwipeRefreshEnable(boolean enable) {
        mSwipeRl.setEnabled(enable);
    }

    public boolean isSwipeRefreshing() {
        return mSwipeRl.isRefreshing();
    }

    public void setOnRefreshListener(OnRefreshListener lisn) {
        mSwipeRl.setOnRefreshListener(lisn);
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

    public void setSwipeRefreshColors(@ColorRes int... colorResIds) {
        mSwipeRl.setColorSchemeResources(colorResIds);
    }
    // =============================================================================================


    // load more
    // =============================================================================================
    public void setLoadMoreEnable(boolean enable) {
        if (mListView instanceof JListView) {
            ((JListView) mListView).setLoadMoreEnable(enable);
        }
    }

    public boolean isLoadMoreEnable() {
        return mListView instanceof JListView && ((JListView) mListView).isLoadMoreEnable();
    }

    public void addLoadMoreIfNecessary() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).addLoadMoreIfNotExist();
        }
    }

    public boolean isLoadingMore() {
        return isLoadMoreEnable() && ((JListView) mListView).isLoadingMore();
    }

    public void setOnLoadMoreListener(LoadMore.OnLoadMoreListener listener) {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setOnLoadMoreListener(listener);
        }
    }

    public void stopLoadMore() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).stopLoadMore();
        }
    }

    public void setLoadMoreFailed() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreFailed();
        }
    }

    public void hideLoadMore() {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).hideLoadMore();
        }
    }

    public void setLoadMoreTheme(LoadMore.Theme theme) {
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

    public void setLoadMoreHintColor(@ColorRes int resId) {
        if (isLoadMoreEnable()) {
            ((JListView) mListView).setLoadMoreHintTextColor(resId);
        }
    }
    // =============================================================================================
}
