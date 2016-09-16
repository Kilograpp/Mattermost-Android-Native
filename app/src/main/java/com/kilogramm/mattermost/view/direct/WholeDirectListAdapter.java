package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;
import com.squareup.picasso.Picasso;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListAdapter extends RealmRecyclerViewAdapter<User, WholeDirectListAdapter.MyViewHolder> {
    private static final String TAG = "WholeDirectListAdapter";

    private Context context;
    private WholeDirectListActivity.OnDirectItemClickListener directItemClickListener;

//    public WholeDirectListAdapter(Context context, RealmResults<User> realmResults, boolean animateResults,
//                                  WholeDirectListActivity.OnDirectItemClickListener listener) {
//        super(context, realmResults, true);
//        this.context = context;
//        this.directItemClickListener = listener;
//    }
    public WholeDirectListAdapter(Context context, RealmResults<User> realmResults, boolean animateResults) {
        super(context, realmResults, true);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);
        holder.bindTo(user);

        holder.getmBinding().getRoot()
                .setOnClickListener(v -> {
                    Log.d(TAG, "onClickItem() holder");
                    if (directItemClickListener != null) {
                        directItemClickListener.onDirectClick(user.getId(), user.getUsername());
//                        ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
//                        setSelecteditem(holder.getAdapterPosition());
//                        onChangeSelected();
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

        public void bindTo(User user) {
            directBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            directBinding.directProfileName.setText(user.getUsername());

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
}
