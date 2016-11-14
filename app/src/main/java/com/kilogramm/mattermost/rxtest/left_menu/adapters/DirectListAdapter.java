package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.rxtest.left_menu.holders.DirectHolder;

import io.realm.RealmAD;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class DirectListAdapter extends RealmAD<Channel, DirectHolder> {

    private static final String TAG = "DIRECT_LIST_ADAPTER";
    private final Context context;

    private int mSelectedItem = -1;
    private LayoutInflater inflater;

    public DirectListAdapter(RealmResults<Channel> adapterData, Context context) {
        super(adapterData);
        this.context = MattermostApp.get(context);
        this.inflater = (LayoutInflater) this.context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public DirectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DirectHolder holder, int position) {

    }

}
