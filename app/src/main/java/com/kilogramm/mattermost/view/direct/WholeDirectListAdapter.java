package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListAdapter extends RealmRecyclerViewAdapter<User, WholeDirectListAdapter.MyViewHolder> {
    private static final String TAG = "WholeDirectListAdapter";

    static WholeDirectListPresenter mWholeDirectListPresenter;

    private WholeDirectListActivity.OnDirectItemClickListener directItemClickListener;
    private ArrayList<String> mUsersIds = new ArrayList<>();
    private ArrayList<String> usersStatuses = new ArrayList<>();

    public WholeDirectListAdapter(Context context, RealmResults<User> realmResults, ArrayList<String> usersIds,
                                  WholeDirectListPresenter wholeDirectListPresenter,
                                  WholeDirectListActivity.OnDirectItemClickListener listener) {
        super(context, realmResults, true);
        this.mUsersIds = usersIds;
        mWholeDirectListPresenter = wholeDirectListPresenter;
        this.directItemClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);

        usersStatuses = mWholeDirectListPresenter.getUsersStatuses(mUsersIds);
        Log.d(TAG, "onBindViewHolder ->" + usersStatuses.toString());

        // TODO проверка приавильности перенесения методов в Presenter
//            holder.bindTo(user, usersStatuses.get(position));
        holder.bindTo(user, Channel.AWAY);

        holder.getmBinding().getRoot()
                .setOnClickListener(v -> {
                    Log.d(TAG, "onClickItem() onBindViewHolder");
//                    Log.d(TAG, user.getNickname());
                    if (directItemClickListener != null) {
                        directItemClickListener.onDirectClick(position, user.getNickname());
                    }
                });
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemDirectListBinding directBinding;

        private MyViewHolder(ItemDirectListBinding binding) {
            super(binding.getRoot());
            directBinding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ItemDirectListBinding binding = ItemDirectListBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        public void bindTo(User user, String status) {
            directBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            directBinding.directProfileName.setText(user.getUsername());

            StringBuilder stringBuilder = new StringBuilder("(" + user.getEmail() + ")");
            directBinding.emailProfile.setText(stringBuilder);

            if(status != null){
                directBinding.status.setImageDrawable(mWholeDirectListPresenter.drawStatusIcon(status));
            }

            Picasso.with(directBinding.avatarDirect.getContext())
                    .load(mWholeDirectListPresenter.imageUrl(user.getId()))
                    .resize(60, 60)
                    .error(directBinding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(directBinding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(directBinding.avatarDirect);
        }

        public ItemDirectListBinding getmBinding() {
            return directBinding;
        }
    }
}
