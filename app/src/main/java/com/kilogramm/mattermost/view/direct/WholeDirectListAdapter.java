package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListAdapter extends RealmRecyclerViewAdapter<User, WholeDirectListAdapter.MyViewHolder> {
    private Context context;

    private WholeDirectListAdapter.OnDirectItemClickListener directItemClickListener;
    private ArrayList<String> mUsersIds;
    private RealmResults<UserStatus> userStatuses;

    public WholeDirectListAdapter(Context context, RealmResults<User> realmResults, ArrayList<String> usersIds,
                                  WholeDirectListAdapter.OnDirectItemClickListener listener,
                                  RealmResults<UserStatus> statusRealmResults) {
        super(context, realmResults, true);
        this.mUsersIds = usersIds;
        this.context = context;
        this.userStatuses = statusRealmResults;
        this.userStatuses.addChangeListener(element -> {
            notifyDataSetChanged();
        });
        this.directItemClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getData().get(position);

        holder.getmBinding().getRoot().setOnClickListener(view -> {
            if (directItemClickListener != null) {
                directItemClickListener.onDirectClick(user.getId(), user.getUsername());
            }
        });

        UserStatus userStatus = null;
        RealmQuery<UserStatus> byId = userStatuses.where().equalTo("id", user.getId());
        if(byId.count()!=0){
            userStatus = byId.findFirst();
        }

        holder.bindTo(context, user, userStatus);
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

        public void bindTo(Context context, User user, UserStatus userStatus) {

            directBinding.directProfileName.setText(user.getUsername());

            StringBuilder stringBuilder = new StringBuilder("(" + user.getEmail() + ")");
            directBinding.emailProfile.setText(stringBuilder);


            if (user.getStatus() != null) {
                directBinding.status.setImageDrawable(drawStatusIcon(context,userStatus));
            }

            Picasso.with(directBinding.avatarDirect.getContext())
                    .load(getImageUrl(user.getId()))
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

    public static Drawable drawStatusIcon(Context context, UserStatus status) {
        switch (status.getStatus()){
            case UserStatus.ONLINE:
                return context.getResources().getDrawable(R.drawable.status_online_drawable);
            case UserStatus.OFFLINE:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
            case UserStatus.AWAY:
                return context.getResources().getDrawable(R.drawable.status_away_drawable);
            case UserStatus.REFRESH:
                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
            default:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
        }
    }

    public static String getImageUrl(String userId) {
        if (userId != null) {
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/users/"
                    + userId
                    + "/image";
        } else {
            return "";
        }
    }

    public interface OnDirectItemClickListener {
        void onDirectClick(String itemId, String name);
    }
}
