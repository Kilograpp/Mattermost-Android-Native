package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectItemHolder;

/**
 * Created by ivan on 09.02.17.
 */

public class AdapterDirectMenuLeftV2 extends RecyclerView.Adapter<AdapterDirectItemHolder> {

    private Context mContext;
    private Cursor mCursor;

    public AdapterDirectMenuLeftV2(Context context, Cursor cursor){
        this.mContext = context;
        this.mCursor = cursor;
    }
    @Override
    public AdapterDirectItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AdapterDirectItemHolder.create(LayoutInflater.from(parent.getContext()), parent);

    }

    @Override
    public void onBindViewHolder(AdapterDirectItemHolder holder, int position) {
//        DirectItem item = (DirectItem) mAdapterData.get(position);
        UserV2 user = getItem(holder.getAdapterPosition());

        holder.bindTo(user, mContext);
    }


    public UserV2 getItem(int position){
        if(!mCursor.moveToPosition(position)){
            throw new IllegalArgumentException("Invalid item position requested");
        }
        else return new UserV2(mCursor);
    }
    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor){
        if (mCursor != null){
            mCursor.close();
        }
        mCursor = cursor;
//        notifyDataSetChanged();
    }
}
