package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.database.repository.UsersRepository;
import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectItemHolder;

/**
 * Created by Evgeny on 10.02.2017.
 */

public class LMDAdapter extends CursorRecyclerViewAdapter<AdapterDirectItemHolder> {


    private final Context mContext;
    private final int ITEM_IN_TEAM = 1;
    private final int ITEM_NOT_IN_TEAM = 2;


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
        int type = getItemViewType(position);

        UserV2 user = getItem(viewHolder.getAdapterPosition());

        viewHolder.bindTo(user, mContext);
    }


    @Override
    public AdapterDirectItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return viewType == ITEM_IN_TEAM ?
//                :

        return AdapterDirectItemHolder.create(LayoutInflater.from(parent.getContext()), parent);

    }

    @Override
    public int getItemViewType(int position) {
        if (getCursor().getString(getCursor().getColumnIndex(UsersRepository.FIELD_IN_TEAM)).equals("true")) // FIXME: 10.02.17 maybe there are another solution?
            return ITEM_IN_TEAM;
        else
            return ITEM_NOT_IN_TEAM;

    }

    public UserV2 getItem(int position) {
        if (!getCursor().moveToPosition(position)) {
            throw new IllegalArgumentException("Invalid item position requested");
        } else return new UserV2(getCursor());
    }
}
