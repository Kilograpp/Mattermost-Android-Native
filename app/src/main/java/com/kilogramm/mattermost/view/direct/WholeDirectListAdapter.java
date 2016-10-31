package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

import java.util.ArrayList;

import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListAdapter extends RealmRecyclerViewAdapter<User, WholeDirectListHolder> {

    private WholeDirectListAdapter.OnDirectItemClickListener directItemClickListener;
    private ArrayList<String> mUsersIds;
    private RealmResults<UserStatus> userStatuses;

    public WholeDirectListAdapter(Context context,
                                  RealmResults<User> realmResults,
                                  ArrayList<String> usersIds,
                                  WholeDirectListAdapter.OnDirectItemClickListener listener,
                                  RealmResults<UserStatus> statusRealmResults) {
        super(context, realmResults, true);
        this.mUsersIds = usersIds;
        this.userStatuses = statusRealmResults;
        this.userStatuses.addChangeListener(element -> notifyDataSetChanged());
        this.directItemClickListener = listener;
    }

    @Override
    public WholeDirectListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return WholeDirectListHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(WholeDirectListHolder holder, int position) {
        User user = getData().get(position);

        UserStatus userStatus = null;
        RealmQuery<UserStatus> byId = userStatuses.where().equalTo("id", user.getId());
        if(byId.count()!=0){
            userStatus = byId.findFirst();
        }

        holder.getmBinding().getRoot().setOnClickListener(view -> {
            if (directItemClickListener != null) {
                directItemClickListener.onDirectClick(user.getId(), user.getUsername());
            }
        });

        holder.bindTo(context, user, userStatus);
    }

    public interface OnDirectItemClickListener {
        void onDirectClick(String userTalkToId, String name);
    }


}
