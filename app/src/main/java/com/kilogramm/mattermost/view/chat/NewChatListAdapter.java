package com.kilogramm.mattermost.view.chat;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChatListItemBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.tools.HrSpannable;
import com.kilogramm.mattermost.tools.MattermostTagHandler;
import com.kilogramm.mattermost.viewmodel.chat.ItemChatViewModel;
import com.vdurmont.emoji.EmojiParser;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class NewChatListAdapter extends RealmBasedRecyclerViewAdapter<Post, NewChatListAdapter.MyViewHolder> {

    private static final String TAG = "NewChatListAdapter";

    private Context context;

    private OnItemClickListener<Post> listener;

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, String animateExtraColumnName,
                              OnItemClickListener<Post> listener) {
        super(context, realmResults, true, animateResults, animateExtraColumnName);
        this.context = context;
        this.listener = listener;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, OnItemClickListener<Post> listener) {
        super(context, realmResults, true, animateResults);
        this.context = context;
        this.listener = listener;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, boolean addSectionHeaders,
                              String headerColumnName, OnItemClickListener<Post> listener) {
        super(context, realmResults, true, animateResults, addSectionHeaders, headerColumnName);
        this.context = context;
        this.listener = listener;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, boolean addSectionHeaders,
                              String headerColumnName, String animateExtraColumnName,
                              OnItemClickListener<Post> listener) {
        super(context, realmResults, true, animateResults, addSectionHeaders, headerColumnName, animateExtraColumnName);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        return MyViewHolder.create(inflater, viewGroup);
    }

    @Override
    public void onBindRealmViewHolder(MyViewHolder myViewHolder, int i) {
        Post post = realmResults.get(i);
        Calendar curDate = Calendar.getInstance();
        Calendar preDate = Calendar.getInstance();
        Post prePost;
        Boolean isTitle = false;
        if(i-1 >= 0){
            prePost = realmResults.get(i-1);
            curDate.setTime(new Date(post.getCreateAt()));
            preDate.setTime(new Date(prePost.getCreateAt()));
            if(curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)){
                isTitle = true;
            }

            if (post.getRootId() != null && post.getRootId().length() > 0 &&
                    !prePost.getId().equals(post.getRootId())) {
                myViewHolder.setRootMassage(post.getRootId());
            }

        }
        myViewHolder.bindTo(post, context, isTitle, listener);
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

            if(AppCompatDelegate.isCompatVectorFromResourcesEnabled()){
                Log.d(TAG,"isCompatVectorFromResourcesEnabled");
                /*AnimatedVectorDrawableCompat animatedVectorDrawableCompat
                        = AnimatedVectorDrawableCompat.create(context, R.drawable.vector_test_anim);
                mBinding.sendStatus.setImageDrawable(animatedVectorDrawableCompat);*/
               // mBinding.sendStatus.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.vector_test_anim));
              //  mBinding.sendStatus.setImageResource(R.drawable.vector_test_anim);
            } else {
                Log.d(TAG,"NOT isCompatVectorFromResourcesEnabled");
            }

            if(mBinding.sendStatus.getDrawable() instanceof Animatable){
                ((Animatable) mBinding.sendStatus.getDrawable()).start();
            }
            mBinding.executePendingBindings();
        }

        private void setRootMassage(String rootId) {
            Realm realm = Realm.getDefaultInstance();
            Post rootPost = realm.where(Post.class)
                    .equalTo("id", rootId).findFirst();
            mBinding.linearLayoutRootPost.setVisibility(View.VISIBLE);
            mBinding.nickRootPost.setText(rootPost.getUser().getUsername());
            mBinding.avatarRootPost.setTag(rootPost);
            mBinding.messageRootPost.setText(revertSpanned(getPostLinkifyMessage(rootPost)).toString().trim());
        }
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
