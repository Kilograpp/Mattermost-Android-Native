package com.kilogramm.mattermost.view.search;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemSearchResultBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

import static com.kilogramm.mattermost.view.direct.WholeDirectListAdapter.getImageUrl;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessageAdapter extends RealmRecyclerViewAdapter<Post, SearchMessageAdapter.MyViewHolder> {

    private OnJumpClickListener jumpClickListener;

    public SearchMessageAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Post> data,
                                boolean autoUpdate, OnJumpClickListener listener) {
        super(context, data, autoUpdate);
        this.jumpClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindTo(getData().get(position));

        String messageId = getData().get(position).getId();
        String channelId = getData().get(position).getChannelId();
        Realm realm = Realm.getDefaultInstance();
        String channelName = realm.where(Channel.class).equalTo("id", channelId).findFirst().getName();

        holder.getmBinding().getRoot().setOnClickListener(v -> {
            if (jumpClickListener != null) {
                jumpClickListener.onJumpClick(messageId, channelId, channelName, holder.isChannel());
            }
        });
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemSearchResultBinding binding;
        private Realm realm;
        private boolean isChannel;

        private MyViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.realm = Realm.getDefaultInstance();
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            return new MyViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_search_result, parent, false));
        }

        void bindTo(Post post) {
            RealmResults<User> user = realm.where(User.class).equalTo("id", post.getUserId()).findAll();
            RealmResults<Channel> channel = realm.where(Channel.class).equalTo("id", post.getChannelId()).findAll();

            binding.postedDate.setText(DateOrTimeConvert(post.getCreateAt(), false));

            String chName = channel.first().getName();

            if (Pattern.matches(".+\\w[_].+\\w", chName)) {
                binding.chatName.setText(this.getChatName(chName));
                isChannel = false;
            } else {
                binding.chatName.setText(chName);
                isChannel = true;
            }

            binding.chatName.setText(Pattern.matches(".+\\w[_].+\\w", chName) ? this.getChatName(chName) : chName);

            binding.userName.setText(user.first().getUsername());
            binding.userName.setText(user.first().getUsername());
            binding.postedTime.setText(DateOrTimeConvert(post.getCreateAt(), true));
            binding.foundMessage.setText(post.getMessage());
            Picasso.with(binding.avatarDirect.getContext())
                    .load(getImageUrl(user.first().getId()))
                    .resize(60, 60)
                    .error(binding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(binding.avatarDirect.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(binding.avatarDirect);
        }

        public boolean isChannel() {
            return isChannel;
        }

        public ItemSearchResultBinding getmBinding() {
            return binding;
        }

        public String getChatName(String rawName) {
            String[] channelNameParsed = rawName.split("__");
            String myId = realm.where(User.class).findFirst().getId();
            if (channelNameParsed[0] == myId) {
                return realm.where(User.class).equalTo("id", channelNameParsed[1]).findFirst().getUsername();
            } else {
                return realm.where(User.class).equalTo("id", channelNameParsed[0]).findFirst().getUsername();
            }
        }
    }

    public static String DateOrTimeConvert(Long createAt, boolean isTime) {
        Date dateTime = new Date(createAt);
        if (isTime) {
            return new SimpleDateFormat("hh:mm").format(dateTime);
        } else {
            return new SimpleDateFormat("dd.MM.yyyy").format(dateTime);
        }
    }

    public interface OnJumpClickListener {
        void onJumpClick(String messageId, String channelId, String channelName, boolean isChannel);
    }
}
