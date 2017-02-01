package com.kilogramm.mattermost.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kepar on 17.11.16.
 */

public abstract class HeaderFooterRecyclerArrayAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<T> items;
    private List<View> headers = new ArrayList<>();
    private List<View> footers = new ArrayList<>();

    private static final int TYPE_HEADER = 111;
    private static final int TYPE_FOOTER = 222;
    private static final int TYPE_ITEM = 333;

    public HeaderFooterRecyclerArrayAdapter() {
        items = new ArrayList<>();
    }

    /**
     * Using for creating user custom viewHolder, that will be use for list items
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    public abstract VH onCreateGenericViewHolder(ViewGroup parent, int viewType);

    /**
     * Called by RecyclerView to display the item at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    public abstract void onBindGenericViewHolder(VH holder, int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return onCreateGenericViewHolder(parent, viewType);
        } else {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderFooterViewHolder(frameLayout);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < headers.size()) {
            View v = headers.get(position);
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else if (position >= headers.size() + items.size()) {
            View v = footers.get(position - items.size() - headers.size());
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else {
            onBindGenericViewHolder((VH) holder, position - headers.size());
        }
    }

    @Override
    public int getItemCount() {
        return headers.size() + items.size() + footers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < headers.size()) {
            return TYPE_HEADER;
        } else if (position >= headers.size() + items.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    public int getDataCount() {
        return items.size();
    }


    /**
     * Add an item of the specified type to the dataset
     *
     * @param item adding element
     */
    public void add(T item) {
        // TODO прочекать, как будет работать при наличии хедеров
        if (items == null) items = new ArrayList<>();
        items.add(item);
        notifyItemInserted(items.size() - 1);
        notifyItemChanged(0);
    }

    /**
     * Get an item of the specified type from the dataset by position
     *
     * @return element by position
     */
    public T getItem(int position) {
        return items.get(position);
    }

    public List<T> getData() {
        return items;
    }

    /**
     * Removes item from the dataset with animation
     *
     * @param position the position of removing item in dataset
     */
    public void removeItem(int position) {
        if (items.size() <= position) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

    public int getHeaderItemCount() {
        return headers.size();
    }

    public int getFooterItemCount() {
        return footers.size();
    }

    /**
     * Prepare HeaderFooter viewHolder by removing old views and add specified view to the list
     *
     * @param vh   viewHoler for headers o footers
     * @param view the view that will be added to headers or footers list
     */
    private void prepareHeaderFooter(HeaderFooterViewHolder vh, View view) {
        vh.base.removeAllViews();
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        vh.base.addView(view);
    }

    public void addHeader(View header) {
        if (!headers.contains(header)) {
            headers.add(header);
            notifyItemInserted(headers.size() - 1);
        }
    }

    public void removeHeader(View header) {
        if (headers.contains(header)) {
            notifyItemRemoved(headers.indexOf(header));
            headers.remove(header);
            if (header.getParent() != null) {
                ((ViewGroup) header.getParent()).removeView(header);
            }
        }
    }

    public void addFooter(View footer) {
        if (!footers.contains(footer)) {
            footers.add(footer);
            notifyItemInserted(headers.size() + items.size() + footers.size() - 1);
        }
    }

    public void removeFooter(View footer) {
        if (footers.contains(footer)) {
            notifyItemRemoved(headers.size() + items.size() + footers.indexOf(footer));
            footers.remove(footer);
            if (footer.getParent() != null) {
                ((ViewGroup) footer.getParent()).removeView(footer);
            }
        }
    }

    public static class HeaderFooterViewHolder extends RecyclerView.ViewHolder {
        FrameLayout base;

        public HeaderFooterViewHolder(View itemView) {
            super(itemView);
            this.base = (FrameLayout) itemView;
        }
    }
}