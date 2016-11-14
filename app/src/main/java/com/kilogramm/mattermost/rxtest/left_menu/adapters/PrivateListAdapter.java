package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.rxtest.left_menu.holders.PrivateHolder;

import io.realm.RealmAD;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class PrivateListAdapter extends RealmAD<Channel, PrivateHolder>{

    private static final String TAG = "PRIVATE_LIST_ADAPTER";

    private int selectedItem = -1;

    public PrivateListAdapter(RealmResults<Channel> adapterData) {
        super(adapterData);
    }

    @Override
    public PrivateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(PrivateHolder holder, int position) {

    }

}
