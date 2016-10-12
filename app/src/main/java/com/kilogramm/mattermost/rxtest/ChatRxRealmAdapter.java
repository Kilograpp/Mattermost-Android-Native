package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChatListItemBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.tools.HrSpannable;
import com.kilogramm.mattermost.tools.MattermostTagHandler;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.viewmodel.chat.ItemChatViewModel;
import com.vdurmont.emoji.EmojiParser;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.OrderedRealmCollection;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by Evgeny on 07.10.2016.
 */
public class ChatRxRealmAdapter<T extends RealmModel> extends RealmRecyclerViewAdapter<Post, ChatRxRealmAdapter.MyViewHolder> {

    private static final String TAG = "NewChatListAdapter";
    private OnItemClickListener<Post> listener;

    public ChatRxRealmAdapter(@NonNull Context context,
                              @Nullable OrderedRealmCollection data,
                              boolean autoUpdate,
                              OnItemClickListener<Post> listener) {
        super(context, data, autoUpdate);
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Post post = getData().get(position);
        Calendar curDate = Calendar.getInstance();
        Calendar preDate = Calendar.getInstance();
        Post prePost;
        Boolean isTitle = false;
        if(position-1 >= 0){
            prePost = getData().get(position-1);
            curDate.setTime(new Date(post.getCreateAt()));
            preDate.setTime(new Date(prePost.getCreateAt()));
            if(curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)){
                isTitle = true;
            }
        }
        holder.bindTo(post, context, isTitle, listener);
    }

    @Override
    public long getItemId(int index) {
        return super.getItem(index).getCreateAt();
        //return super.getItemId(index);
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ChatListItemBinding mBinding;

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ChatListItemBinding binding = ChatListItemBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        private MyViewHolder(ChatListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
        public void bindTo(Post post, Context context, Boolean isTitle, OnItemClickListener listener) {
            mBinding.controlMenu.setOnClickListener(view -> {
                if (listener != null)
                    listener.OnItemClick(mBinding.controlMenu, post);
            });
            mBinding.avatar.setTag(post);
            SpannableStringBuilder ssb = getSpannableStringBuilder(post, context);
            mBinding.message.setText(revertSpanned(ssb));
            mBinding.message.setMovementMethod(LinkMovementMethod.getInstance());
            if(mBinding.getViewModel() == null){
                mBinding.setViewModel(new ItemChatViewModel(context, post));
            } else {
                mBinding.getViewModel().setPost(post);

            }
            if(isTitle){
                mBinding.getViewModel().setTitleVisibility(View.VISIBLE);
            } else {
                mBinding.getViewModel().setTitleVisibility(View.GONE);
            }
               /* AnimatedVectorDrawableCompat animatedVectorDrawableCompat
                        = AnimatedVectorDrawableCompat.create(context, R.drawable.vector_test_anim);
                mBinding.sendStatus.setImageDrawable(animatedVectorDrawableCompat);


            if(mBinding.sendStatus.getDrawable() instanceof Animatable){
                ((Animatable) mBinding.sendStatus.getDrawable()).start();
            }*/
            mBinding.executePendingBindings();
        }
        @NonNull
        private static SpannableStringBuilder getSpannableStringBuilder(Post post, Context context) {
            Spanned spanned;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(EmojiParser.parseToUnicode(post.getMessage()),Html.FROM_HTML_MODE_LEGACY,null, new MattermostTagHandler());
            } else {
                spanned = Html.fromHtml(EmojiParser.parseToUnicode(post.getMessage()), null,new MattermostTagHandler());
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(spanned);
            Linkify.addLinks(ssb, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);

            Linkify.addLinks(ssb, Pattern.compile("\\B@([\\w|.]+)\\b"), null, (s, start, end) -> {
                ssb.setSpan(new ForegroundColorSpan(context.getResources ().getColor(R.color.colorPrimary)),
                        start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return true;
            }, null);

            Linkify.addLinks(ssb, Pattern.compile("<hr>.*<\\/hr>"), null, (charSequence, i, i1) -> {
                String s = charSequence.toString();
                StringBuilder builder = new StringBuilder();
                for (int k = i; k < i1; k++){
                    builder.append(' ');
                }
                ssb.replace(i,i1,builder.toString());
                ssb.setSpan(new HrSpannable(context.getResources().getColor(R.color.light_grey)), i, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return true;
            },null);
            return ssb;
        }

        static Spannable revertSpanned(Spanned stext) {
            Object[] spans = stext.getSpans(0, stext.length(), Object.class);
            Spannable ret = Spannable.Factory.getInstance().newSpannable(stext.toString());
            if (spans != null && spans.length > 0) {
                for(int i = spans.length - 1; i >= 0; --i) {
                    ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]));
                }
            }

            return ret;
        }
    }

}
