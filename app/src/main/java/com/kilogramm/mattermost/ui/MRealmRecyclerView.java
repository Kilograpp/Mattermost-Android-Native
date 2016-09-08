package com.kilogramm.mattermost.ui;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.tonicartos.superslim.LayoutManager;

import co.moonmonkeylabs.realmrecyclerview.R.id;
import co.moonmonkeylabs.realmrecyclerview.R.layout;
import co.moonmonkeylabs.realmrecyclerview.R.styleable;
import co.moonmonkeylabs.realmrecyclerview.RealmSimpleItemTouchHelperCallback;
import io.realm.RealmBasedRecyclerViewAdapter;

public class MRealmRecyclerView extends FrameLayout {


    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ViewStub emptyContentContainer;
    private RealmBasedRecyclerViewAdapter adapter;
    private RealmSimpleItemTouchHelperCallback realmSimpleItemTouchHelperCallback;
    private boolean hasLoadMoreFired;
    private boolean showShowLoadMore;
    private boolean isRefreshable;
    private int emptyViewId;
    private Type type;
    private int gridSpanCount;
    private int gridWidthPx;
    private boolean swipeToDelete;
    private int bufferItems = 3;
    private GridLayoutManager gridManager;
    private int lastMeasuredWidth = -1;
    private boolean isRefreshing;
    private MRealmRecyclerView.OnRefreshListener onRefreshListener;
    private MRealmRecyclerView.OnLoadMoreListener onLoadMoreListener;
    private android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener recyclerViewRefreshListener = new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
        public void onRefresh() {
            if(!MRealmRecyclerView.this.isRefreshing && MRealmRecyclerView.this.onRefreshListener != null) {
                MRealmRecyclerView.this.onRefreshListener.onRefresh();
            }

            MRealmRecyclerView.this.isRefreshing = true;
        }
    };

    public MRealmRecyclerView(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public MRealmRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public MRealmRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    public MRealmRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int bufferItems) {
        super(context, attrs, defStyleAttr);
        if(bufferItems <= 0) {
            bufferItems = 0;
        }

        this.bufferItems = bufferItems;
        this.init(context, attrs);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if(this.gridWidthPx != -1 && this.gridManager != null && this.lastMeasuredWidth != this.getMeasuredWidth()) {
            int spanCount = Math.max(1, this.getMeasuredWidth() / this.gridWidthPx);
            this.gridManager.setSpanCount(spanCount);
            this.lastMeasuredWidth = this.getMeasuredWidth();
        }

    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, layout.realm_recycler_view, this);
        this.initAttrs(context, attrs);
        this.swipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(id.rrv_swipe_refresh_layout);
        this.recyclerView = (RecyclerView)this.findViewById(id.rrv_recycler_view);
        this.emptyContentContainer = (ViewStub)this.findViewById(id.rrv_empty_content_container);
        this.swipeRefreshLayout.setEnabled(this.isRefreshable);
        if(this.isRefreshable) {
            this.swipeRefreshLayout.setOnRefreshListener(this.recyclerViewRefreshListener);
        }

        if(this.emptyViewId != 0) {
            this.emptyContentContainer.setLayoutResource(this.emptyViewId);
            this.emptyContentContainer.inflate();
        }

        if(this.type == null) {
            throw new IllegalStateException("A type has to be specified via XML attribute");
        } else {
            switch(type) {
                case LINEARLAYOUT:
                    LinearLayoutManager manager = new LinearLayoutManager(this.getContext());
                    manager.setStackFromEnd(true);
                    manager.setReverseLayout(false);
                    this.recyclerView.setLayoutManager(manager);
                    break;
                case GRID:
                    this.throwIfSwipeToDeleteEnabled();
                    if(this.gridSpanCount == -1 && this.gridWidthPx == -1) {
                        throw new IllegalStateException("For GridLayout, a span count or item width has to be set");
                    }

                    if(this.gridSpanCount != -1 && this.gridWidthPx != -1) {
                        throw new IllegalStateException("For GridLayout, a span count and item width can not both be set");
                    }

                    int spanCount = this.gridSpanCount == -1?1:this.gridSpanCount;
                    this.gridManager = new GridLayoutManager(this.getContext(), spanCount);
                    this.recyclerView.setLayoutManager(this.gridManager);
                    break;
                case LINEARLAYOUTWITHHEADERS:
                    this.throwIfSwipeToDeleteEnabled();
                    this.recyclerView.setLayoutManager(new LayoutManager(this.getContext()));
                    break;
                default:
                    throw new IllegalStateException("The type attribute has to be set.");
            }

            this.recyclerView.setHasFixedSize(true);
            this.recyclerView.addOnScrollListener(new OnScrollListener() {
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
            this.recyclerView.addOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    MRealmRecyclerView.this.maybeFireLoadMore();
                }
            });
            if(this.swipeToDelete) {
                this.realmSimpleItemTouchHelperCallback = new RealmSimpleItemTouchHelperCallback();
                (new ItemTouchHelper(this.realmSimpleItemTouchHelperCallback)).attachToRecyclerView(this.recyclerView);
            }

        }
    }

    public void setOrientation(int orientation) {
        if(this.gridManager == null) {
            throw new IllegalStateException("Error init of GridLayoutManager");
        } else {
            this.gridManager.setOrientation(orientation);
        }
    }

    private void throwIfSwipeToDeleteEnabled() {
        if(this.swipeToDelete) {
            throw new IllegalStateException("SwipeToDelete not supported with this layout type: " + this.type.name());
        }
    }

    public void setOnLoadMoreListener(MRealmRecyclerView.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void enableShowLoadMore() {
        this.showShowLoadMore = true;
        ((RealmBasedRecyclerViewAdapter)this.recyclerView.getAdapter()).addLoadMore();
    }

    public void disableShowLoadMore() {
        this.showShowLoadMore = false;
        ((RealmBasedRecyclerViewAdapter)this.recyclerView.getAdapter()).removeLoadMore();
    }

    private void maybeFireLoadMore() {
        if(!this.hasLoadMoreFired) {
            if(this.showShowLoadMore) {
                android.support.v7.widget.RecyclerView.LayoutManager layoutManager = this.recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = this.findFirstVisibleItemPosition();
                if(totalItemCount != 0) {
                    if(firstVisibleItemPosition + visibleItemCount + this.bufferItems > totalItemCount && this.onLoadMoreListener != null) {
                        this.hasLoadMoreFired = true;
                        this.onLoadMoreListener.onLoadMore(this.adapter.getLastItem());
                    }

                }
            }
        }
    }

    public int findFirstVisibleItemPosition() {
        switch(type) {
            case LINEARLAYOUT:
                return ((LinearLayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            case GRID:
                return ((GridLayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            case LINEARLAYOUTWITHHEADERS:
                return ((LayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            default:
                throw new IllegalStateException("Type of layoutManager unknown.In this case this method needs to be overridden");
        }
    }

    public int findLastCompletelyVisibleItemPosition() {
        switch(type) {
            case LINEARLAYOUT:
                return ((LinearLayoutManager)this.recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            case GRID:
                return ((GridLayoutManager)this.recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            case LINEARLAYOUTWITHHEADERS:
                return ((LayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            default:
                throw new IllegalStateException("Type of layoutManager unknown.In this case this method needs to be overridden");
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable.RealmRecyclerView);
        this.isRefreshable = typedArray.getBoolean(styleable.RealmRecyclerView_rrvIsRefreshable, false);
        this.emptyViewId = typedArray.getResourceId(styleable.RealmRecyclerView_rrvEmptyLayoutId, 0);
        int typeValue = typedArray.getInt(styleable.RealmRecyclerView_rrvLayoutType, -1);
        if(typeValue != -1) {
            this.type = MRealmRecyclerView.Type.values()[typeValue];
        }

        this.gridSpanCount = typedArray.getInt(styleable.RealmRecyclerView_rrvGridLayoutSpanCount, -1);
        this.gridWidthPx = typedArray.getDimensionPixelSize(styleable.RealmRecyclerView_rrvGridLayoutItemWidth, -1);
        this.swipeToDelete = typedArray.getBoolean(styleable.RealmRecyclerView_rrvSwipeToDelete, false);
        typedArray.recycle();
    }

    public void addItemDecoration(ItemDecoration decor) {
        this.recyclerView.addItemDecoration(decor);
    }

    public void addItemDecoration(ItemDecoration decor, int index) {
        this.recyclerView.addItemDecoration(decor, index);
    }

    public void removeItemDecoration(ItemDecoration decor) {
        this.recyclerView.removeItemDecoration(decor);
    }

    public void setAdapter(final RealmBasedRecyclerViewAdapter adapter) {
        this.adapter = adapter;
        this.recyclerView.setAdapter(adapter);
        if(this.swipeToDelete) {
            this.realmSimpleItemTouchHelperCallback.setAdapter(adapter);
        }

        if(adapter != null) {
            adapter.registerAdapterDataObserver(new AdapterDataObserver() {
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    this.update();
                }

                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    this.update();
                }

                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    this.update();
                }

                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    this.update();
                }

                public void onChanged() {
                    super.onChanged();
                    this.update();
                }

                private void update() {
                    MRealmRecyclerView.this.updateEmptyContentContainerVisibility(adapter);
                }
            });
            this.updateEmptyContentContainerVisibility(adapter);
        }

    }

    private void updateEmptyContentContainerVisibility(Adapter adapter) {
        if(this.emptyViewId != 0) {

            this.emptyContentContainer.setVisibility((adapter.getItemCount() == 0)?View.VISIBLE:View.GONE);
        }
    }

    public void setItemViewCacheSize(int size) {
        this.recyclerView.setItemViewCacheSize(size);
    }

    public void smoothScrollToPosition(int position) {
        this.recyclerView.getLayoutManager().scrollToPosition(position);
    }

    public void scrollToPosition(int position) {
        this.recyclerView.scrollToPosition(position);
    }

    public RecyclerView getRecycleView() {
        return this.recyclerView;
    }

    public void setOnRefreshListener(MRealmRecyclerView.OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setRefreshing(boolean refreshing) {
        if(this.isRefreshable) {
            this.isRefreshing = refreshing;
            this.swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    public void resetHasLoadMoreFired() {
        this.hasLoadMoreFired = false;
    }

    public void setBufferItems(int bufferItems) {
        if(bufferItems <= 0) {
            bufferItems = 0;
        }

        this.bufferItems = bufferItems;
    }

    public enum Type {
        LINEARLAYOUT,
        GRID,
        LINEARLAYOUTWITHHEADERS;

        private Type() {
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(Object var1);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
