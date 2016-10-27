package com.kilogramm.mattermost.view.search;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemSearchResultBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
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
    private String terms;
    private Context context;

    public SearchMessageAdapter(@NonNull Context context,
                                @Nullable OrderedRealmCollection<Post> data,
                                boolean autoUpdate,
                                OnJumpClickListener listener,
                                String terms) {
        super(context, data, autoUpdate);
        this.jumpClickListener = listener;
        this.terms = terms;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindTo(getData().get(position), context, terms);

        String messageId = getData().get(position).getId();
        String channelId = getData().get(position).getChannelId();

        holder.getmBinding().getRoot().setOnClickListener(v -> {
            if (jumpClickListener != null) {
                jumpClickListener.onJumpClick(
                        messageId,
                        channelId,
                        holder.getmBinding().chatName.getText().toString(),
                        holder.getTypeChannel());
            }
        });
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemSearchResultBinding binding;
        private String typeChannel;

        private MyViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            return new MyViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_search_result, parent, false));
        }

        void bindTo(Post post, Context context, String terms) {
            RealmResults<User> user = UserRepository.query(new UserRepository.UserByIdSpecification(post.getUserId()));
            RealmResults<Channel> channel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(post.getChannelId()));

            binding.postedDate.setText(DateOrTimeConvert(post.getCreateAt(), false));

            String chName = channel.first().getName();

            if (Pattern.matches(".+\\w[_].+\\w", chName)) {
                binding.chatName.setText(this.getChatName(chName));
                typeChannel = channel.first().getType();
            } else {
                binding.chatName.setText(chName);
                typeChannel = channel.first().getType();
            }

            binding.userName.setText(user.first().getUsername());
            binding.postedTime.setText(DateOrTimeConvert(post.getCreateAt(), true));
            binding.foundMessage.setText(this.setTextWithHighlight(post.getMessage(), terms, context));
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

        public String getTypeChannel() {
            return typeChannel;
        }

        public ItemSearchResultBinding getmBinding() {
            return binding;
        }

        public String getChatName(String rawName) {
            String[] channelNameParsed = rawName.split("__");
            String myId = MattermostPreference.getInstance().getMyUserId();
            if (channelNameParsed[0] == myId) {
                return UserRepository.query(new UserRepository.UserByIdSpecification(channelNameParsed[1])).get(0).getUsername();
            } else {
                return UserRepository.query(new UserRepository.UserByIdSpecification(channelNameParsed[0])).get(0).getUsername();
            }
        }

        public Spannable setTextWithHighlight(String message, String terms, Context context) {
            Spannable spannableText = new SpannableStringBuilder(message);
            Pattern mTerm = Pattern.compile(terms.toLowerCase());
            Matcher findWord = mTerm.matcher(message.toLowerCase());

            while (findWord.find()) {
                spannableText.setSpan(new BackgroundColorSpan(
                                context.getResources().getColor(R.color.color_highlight)),
                        findWord.start(0),
                        findWord.end(0),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return spannableText;
        }
    }

    public static String DateOrTimeConvert(Long createAt, boolean isTime) {
        Date dateTime = new Date(createAt);
        if (isTime) {
            return new SimpleDateFormat("hh:mm a").format(dateTime);
        } else {
            return new SimpleDateFormat("dd.MM.yyyy").format(dateTime);
        }
    }

    public interface OnJumpClickListener {
        void onJumpClick(String messageId, String channelId, String channelName, String type);
    }
}
