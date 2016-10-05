package com.kilogramm.mattermost.view.search;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemSearchResultBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.squareup.picasso.Picasso;

import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

import static com.kilogramm.mattermost.view.direct.WholeDirectListAdapter.getImageUrl;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessageAdapter extends RealmRecyclerViewAdapter<Post, SearchMessageAdapter.MyViewHolder> {
    private static final String TAG = "SearchMessageAdapter";

//    private ArrayList<String> foundMessagesIds;

    public SearchMessageAdapter(Context context, RealmList<Post> realmResults/*,
                                ArrayList<String> foundMessagesIds*/) {
        super(context, realmResults, true);
//        this.foundMessagesIds = foundMessagesIds;
        Log.d(TAG, "CONSTRUCTOR");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Post post = getData().get(position);
        holder.bindTo(post);
        Log.d(TAG, "onBindViewHolder");
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemSearchResultBinding binding;

        private MyViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ItemSearchResultBinding bindingSearchResult = ItemSearchResultBinding.inflate(inflater, parent, false);
            return new MyViewHolder(bindingSearchResult);
        }

        public void bindTo(Post post) {
            Log.d(TAG, "bindTo");
            binding.userName.setText(post.getUser().getUsername());
            binding.postedTime.setText(post.getCreateAt().toString());
            binding.foundMessage.setText(post.getMessage());

            Picasso.with(binding.avatarDirect.getContext())
                    .load(getImageUrl(post.getUserId()))
                    .resize(60, 60)
                    .error(binding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(binding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(binding.avatarDirect);
        }

        public ItemSearchResultBinding getmBinding() {
            return binding;
        }
    }
}
