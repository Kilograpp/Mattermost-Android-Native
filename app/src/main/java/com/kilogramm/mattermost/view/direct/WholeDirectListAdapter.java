package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by melkshake on 14.09.16.
 */

public class WholeDirectListAdapter extends RealmRecyclerViewAdapter<User, WholeDirectListHolder> {

    private RealmResults<UserStatus> mUserStatuses;
    private RealmResults<Preferences> mPreferences;
    private Map<String, Boolean> mChangesMap;

    public WholeDirectListAdapter(Context context,
                                  RealmResults<UserStatus> statusRealmResults,
                                  RealmResults<Preferences> preferences) {
        super(context, null, true);
        this.mUserStatuses = statusRealmResults;
        this.mUserStatuses.addChangeListener(element -> notifyDataSetChanged());
        this.mChangesMap = new HashMap<>();
        this.mPreferences = preferences;
    }

    @Override
    public WholeDirectListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return WholeDirectListHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(WholeDirectListHolder holder, int position) {
        User user = getData().get(position);
        boolean isShow = false;
        RealmQuery<Preferences> preferencesRealmQuery = mPreferences.where()
                .equalTo("name", user.getId());

        if (preferencesRealmQuery.count() > 0) {
            if (preferencesRealmQuery.findFirst().getValue().equals("true")) isShow = true;
        }

        holder.getmBinding().getRoot().setOnClickListener(view -> {
                holder.getmBinding().selectDirect.setChecked(
                        !holder.getmBinding().selectDirect.isChecked());
                setItemChangeMap(user.getId(), holder.getmBinding().selectDirect.isChecked());
        });

        holder.getmBinding().selectDirect.setOnClickListener(view ->
                setItemChangeMap(user.getId(), ((CheckBox) view).isChecked())
        );
        holder.bindTo(user, isShow, mChangesMap.get(user.getId()));
    }

    public Map<String, Boolean> getmChangesMap() {
        return mChangesMap;
    }

    public void setItemChangeMap(String id, boolean value) {
        mChangesMap.put(id, value);
    }
}
