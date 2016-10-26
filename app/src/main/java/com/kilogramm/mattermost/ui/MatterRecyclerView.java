package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

import com.kilogramm.mattermost.adapters.AdapterPost;
import com.kilogramm.mattermost.rxtest.OnMoreLoadListener;

import icepick.Icepick;


/**
 * Created by Evgeny on 11.10.2016.
 */
public class MatterRecyclerView extends RecyclerView {

    private static final String TAG = "MatterRecyclerView";

    @icepick.State
    boolean showShowLoadMoreTop = false;
    @icepick.State
    boolean showShowLoadMoreBot = false;
    @icepick.State
    boolean canPagination = false;
    private OnMoreLoadListener listener;
    @icepick.State
    boolean canPaginationTop = true;
    @icepick.State
    boolean canPaginationBot = true;


    public MatterRecyclerView(Context context) {
        super(context);
        this.init(context, (AttributeSet) null);
    }

    public MatterRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public MatterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LinearLayoutManager manager = new LinearLayoutManager(this.getContext());
        manager.setStackFromEnd(true);
        this.setLayoutManager(manager);
        this.setHasFixedSize(true);
        this.addOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        this.addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                MatterRecyclerView.this.maybeFireLoadMore();
            }
        });

    }

    private void maybeFireLoadMore() {

        if (isCanPagination()) {
            if (canPaginationTop) {
                if (!showShowLoadMoreTop && !((AdapterPost) getAdapter()).getTopLoading()) {
                    int firstvisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                   // int firstvisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    //Log.d(TAG, "firstNoCompleteItem = " + firstNoCompleteItem + "\nfirstvisibleItem = " + firstvisibleItem);
                    if (firstvisibleItem < 1
                            && firstvisibleItem != -1) {
                        Log.d(TAG, "Log scrolling recyclerview: \n" +
                                "findFirstCompletelyVisibleItemPosition = " + firstvisibleItem + "\n" +
                                "countItem = " + getAdapter().getItemCount());
                        Log.d(TAG, "Top Loader" + firstvisibleItem);
                        if (listener != null) {
                            this.listener.onTopLoadMore();
                            this.enableShowLoadMoreTop();
                        }
                    }
                }
            }
            if (canPaginationBot) {
                if (!showShowLoadMoreBot && !((AdapterPost) getAdapter()).getBottomLoading()) {
                    int lastvisibleItem = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    int countItems = getAdapter().getItemCount();
                    if (countItems - lastvisibleItem == 1) {
                        Log.d(TAG, "Log scrolling recyclerview: \n" +
                                "findLastCompletelyVisibleItemPosition = " + lastvisibleItem + "\n" +
                                "countItem = " + getAdapter().getItemCount());
                        Log.d(TAG, "Bottom loader");
                        if (listener != null) {
                            this.listener.onBotLoadMore();
                            this.enableShowLoadMoreBot();
                        }
                    }
                }
            }
        }
    }

    //region controlLoadMore
    public void enableShowLoadMoreBot() {
        this.showShowLoadMoreBot = true;
        ((AdapterPost) getAdapter()).setLoadingBottom(true);
    }

    public void disableShowLoadMoreBot() {
        this.showShowLoadMoreBot = false;
        ((AdapterPost) getAdapter()).setLoadingBottom(false);
    }

    public void enableShowLoadMoreTop() {
        this.showShowLoadMoreTop = true;
        ((AdapterPost) getAdapter()).setLoadingTop(true);
    }

    public void disableShowLoadMoreTop() {
        this.showShowLoadMoreTop = false;
        ((AdapterPost) getAdapter()).setLoadingTop(false);
    }

    public boolean isCanPagination() {
        return canPagination;
    }

    public void setCanPagination(boolean canPagination) {
        this.canPagination = canPagination;
    }

    public void setCanPaginationTop(boolean canPaginationTop) {
        this.canPaginationTop = canPaginationTop;
    }

    public void setCanPaginationBot(boolean canPaginationBot) {
        this.canPaginationBot = canPaginationBot;
    }
    //endregion


    public void setListener(OnMoreLoadListener listener) {
        this.listener = listener;
    }

    @Override public Parcelable onSaveInstanceState() {
        return Icepick.saveInstanceState(this, super.onSaveInstanceState());
    }

    @Override public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(Icepick.restoreInstanceState(this, state));
    }

}
