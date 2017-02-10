package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectItemHolder;

/**
 * Created by Evgeny on 10.02.2017.
 */

public class LMDAdapter extends CursorRecyclerViewAdapter<AdapterDirectItemHolder> {


    private final Context mContext;

    public LMDAdapter(Cursor cursor, Context context) {
        super(cursor);
        this.mContext = context;
    }

    public LMDAdapter(Cursor cursor, String[] comparisonColumn, Context context) {
        super(cursor, comparisonColumn);
        this.mContext = context;
    }

    @Override
    public void onBindViewHolder(AdapterDirectItemHolder viewHolder, Cursor cursor, int position) {

        UserV2 user = getItem(viewHolder.getAdapterPosition());

        viewHolder.bindTo(user, mContext);
    }


    @Override
    public AdapterDirectItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AdapterDirectItemHolder.create(LayoutInflater.from(parent.getContext()), parent);
    }

    public UserV2 getItem(int position){
        if(!getCursor().moveToPosition(position)){
            throw new IllegalArgumentException("Invalid item position requested");
        }
        else return new UserV2(getCursor());
    }
}
