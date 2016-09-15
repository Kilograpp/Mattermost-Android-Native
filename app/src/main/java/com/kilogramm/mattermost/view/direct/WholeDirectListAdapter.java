package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.ui.MRealmRecyclerView;
import com.kilogramm.mattermost.view.chat.NewChatListAdapter;

import java.util.List;

import co.moonmonkeylabs.realmrecyclerview.LoadMoreListItemView;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListAdapter extends RealmBasedRecyclerViewAdapter<User, WholeDirectListAdapter.MyViewHolder> {

    private Context context;
    private MRealmRecyclerView mRecyclerView;

    public WholeDirectListAdapter(Context context, RealmResults<User> realmResults,
                                  boolean animateResults,
                                  MRealmRecyclerView mRecyclerView) {
        super(context, realmResults, true, animateResults);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public MyViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
//        MyViewHolder holder = MyViewHolder.create(inflater, viewGroup);
//        return holder;
        return MyViewHolder.create(inflater, viewGroup);
    }

    @Override
    public void onBindRealmViewHolder(MyViewHolder myViewHolder, int i) {
        User user = realmResults.get(i);
        myViewHolder.bindTo(user);
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemDirectListBinding directBinding;

        private MyViewHolder(ItemDirectListBinding binding){
            super(binding.getRoot());
            directBinding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent){
            ItemDirectListBinding binding = ItemDirectListBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        public void bindTo(User user){
            directBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            directBinding.directProfileName.setText(user.getUsername());
        }
    }
}
