package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectHeader;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectItem;
import com.kilogramm.mattermost.rxtest.left_menu.direct.IDirect;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectHeaderHolder;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectItemHolder;
import com.kilogramm.mattermost.rxtest.left_menu.holders.BaseDirectViewHolder;

import java.util.ArrayList;

/**
 * Created by Evgeny on 10.02.2017.
 */

public class LMDAdapter extends CursorRecyclerViewAdapter<BaseDirectViewHolder> {


    private final Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<DirectItem> mDirectItems;


    @Deprecated
    public LMDAdapter(Cursor cursor, Context context) {
        super(cursor);
        this.mContext = context;
    }


    public LMDAdapter(Cursor cursor, String[] comparisonColumn, Context context) {
        super(cursor, comparisonColumn);
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mDirectItems = new ArrayList<>();
    }

    @Override
    public void onBindViewHolder(BaseDirectViewHolder viewHolder, Cursor cursor, int position) {
        //this method need to be used if we bind from cursor
    }

    @Override
    public void onBindViewHolder(BaseDirectViewHolder viewHolder, int position) {
        DirectItem item = getItem(position);
        viewHolder.bindTo(item, mContext);
    }

    @Override
    public BaseDirectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IDirect.TYPE_HEADER) {
            return AdapterDirectHeaderHolder.create(mInflater, parent);
        } else {
            return AdapterDirectItemHolder.create(mInflater, parent);
        }

    }

    @Override
    public int getItemCount() {
//        return mDirectItems.size() != 0 ? super.getItemCount() : mDirectItems.size();
        return mDirectItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeaderPosition(position) ? IDirect.TYPE_HEADER : IDirect.TYPE_ITEM;
    }

    private boolean isHeaderPosition(int position) {
        return mDirectItems.get(position) instanceof DirectHeader;
    }

    public DirectItem getItem(int position) {
        return mDirectItems.get(position);
    }

    private void initItemList(Cursor cursor) {
        if (cursor != null) {
            DirectItem item = null;
            DirectItem itemAbove = null;

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                item = new DirectItem(new UserV2(cursor));
                if (!item.isInTeam()
                        && itemAbove != null && itemAbove.isInTeam())
                    mDirectItems.add(new DirectHeader());

                if (cursor.getPosition() != 0) itemAbove = item;

                mDirectItems.add(item);
            }
        }
    }

    @Override
    protected Cursor swapCursor(Cursor newCursor, SparseIntArray changes) {
        initItemList(newCursor);
        return super.swapCursor(newCursor, changes);
    }


}
