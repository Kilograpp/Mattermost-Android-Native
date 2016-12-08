package com.kilogramm.mattermost.view.chat;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
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
import com.kilogramm.mattermost.databinding.LoadMoreLayoutBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.tools.HrSpannable;
import com.kilogramm.mattermost.tools.MattermostTagHandler;
import com.kilogramm.mattermost.viewmodel.chat.ItemChatViewModel;
import com.vdurmont.emoji.EmojiParser;

import java.util.regex.Pattern;

import in.uncod.android.bypass.Bypass;

/**
 * Created by Evgeny on 31.10.2016.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    private Post mPost;


    private ViewDataBinding mBinding;

    private PostViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static PostViewHolder createItem(LayoutInflater inflater, ViewGroup parent) {
        ChatListItemBinding binding = ChatListItemBinding.inflate(inflater, parent, false);
        return new PostViewHolder(binding);
    }

    public static PostViewHolder createLoadingTop(LayoutInflater inflater, ViewGroup parent) {
        LoadMoreLayoutBinding binding = LoadMoreLayoutBinding.inflate(inflater, parent, false);
        return new PostViewHolder(binding);
    }

    public static PostViewHolder createLoadingBottom(LayoutInflater inflater, ViewGroup parent) {
        LoadMoreLayoutBinding binding = LoadMoreLayoutBinding.inflate(inflater, parent, false);
        return new PostViewHolder(binding);
    }

    public void bindToItem(Post post, Context context, Boolean isTitle, Post root, OnItemClickListener listener) {
        this.mPost = post;

        if (post.getUpdateAt() != null && post.getUpdateAt() == Post.NO_UPDATE) {
            ((ChatListItemBinding) mBinding).sendStatusError.setOnClickListener(view -> {
                if (listener != null)
                    listener.OnItemClick(((ChatListItemBinding) mBinding).sendStatusError, post.getId());
            });
        }
        ((ChatListItemBinding) mBinding).controlMenu.setOnClickListener(view -> {
            if (listener != null) {
                listener.OnItemClick(((ChatListItemBinding) mBinding).controlMenu, post.getId());
            }
        });
        if (!post.isSystemMessage()) {
            ((ChatListItemBinding) mBinding).avatar.setOnClickListener(view -> {
                if (listener != null) {
                    listener.OnItemClick(((ChatListItemBinding) mBinding).avatar, post.getId());
                }
            });
        } else {
            ((ChatListItemBinding) mBinding).avatar.setOnClickListener(null);
        }
        ((ChatListItemBinding) mBinding).avatar.setTag(post);

        if (post.getMessage() != null && post.getMessage().length() > 0 || post.isSystemMessage()) {
            ((ChatListItemBinding) mBinding).message.setVisibility(View.VISIBLE);

//            CharSequence string = new Bypass().markdownToSpannable(EmojiParser.parseToUnicode(post.getMessage()));
//            Spannable spannable = Spannable.Factory.getInstance().newSpannable(string);
//            Linkify.addLinks(spannable, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
//
//            Linkify.addLinks(spannable, Pattern.compile("\\B@([\\w|.]+)\\b"), null, (s, start, end) -> {
//                spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
//                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                return true;
//            }, null);

            ((ChatListItemBinding) mBinding).message.setText(getMarkdownPost(
                    post.getMessage(), context));
            //SpannableStringBuilder ssb = getSpannableStringBuilder(post, context);
            //((ChatListItemBinding) mBinding).message.setText(revertSpanned(ssb));
            ((ChatListItemBinding) mBinding).message.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            ((ChatListItemBinding) mBinding).message.setVisibility(View.GONE);
        }
        if (((ChatListItemBinding) mBinding).getViewModel() == null) {
            ((ChatListItemBinding) mBinding).setViewModel(new ItemChatViewModel(post));
        } else {
            ((ChatListItemBinding) mBinding).getViewModel().setPost(post);
        }
        if (root != null)
            setRootMassage(root);

        if (post.getProps() != null)
            setPropMassage(post);

        if (root == null && post.getProps() == null)
            ((ChatListItemBinding) mBinding).linearLayoutRootPost.setVisibility(View.GONE);

        if (isTitle) {
            ((ChatListItemBinding) mBinding).getViewModel().setTitleVisibility(View.VISIBLE);
        } else {
            ((ChatListItemBinding) mBinding).getViewModel().setTitleVisibility(View.GONE);
        }
               /* AnimatedVectorDrawableCompat animatedVectorDrawableCompat
                        = AnimatedVectorDrawableCompat.create(context, R.drawable.vector_test_anim);
                mBinding.sendStatus.setImageDrawable(animatedVectorDrawableCompat);


            if(mBinding.sendStatus.getDrawable() instanceof Animatable){
                ((Animatable) mBinding.sendStatus.getDrawable()).start();
            }*/
        mBinding.executePendingBindings();
    }

    public void bindToLoadingTop() {
    }

    public void bindToLoadingBottom() {
    }

    public ViewDataBinding getmBinding() {
        return mBinding;
    }

    private void setRootMassage(Post post) {
        ((ChatListItemBinding) mBinding).filesViewRoot.setItems(post.getFilenames());
        ((ChatListItemBinding) mBinding).linearLayoutRootPost.setVisibility(View.VISIBLE);
        ((ChatListItemBinding) mBinding).nickRootPost.setText(post.getUser().getUsername());
        ((ChatListItemBinding) mBinding).getViewModel()
                .loadImage(((ChatListItemBinding) mBinding).avatarRootPost,
                        ((ChatListItemBinding) mBinding).getViewModel().getUrl(post));
        ((ChatListItemBinding) mBinding).messageRootPost.setText(getMarkdownPost(post.getMessage(), mBinding.getRoot().getContext()));
        ((ChatListItemBinding) mBinding).messageRootPost.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setPropMassage(Post post) {
        if (post != null) {
            ((ChatListItemBinding) mBinding).linearLayoutRootPost.setVisibility(View.VISIBLE);
            ((ChatListItemBinding) mBinding).layUser.setVisibility(View.GONE);
            if (post.getProps().getAttachments().get(0).getColor().equals("good"))
                ((ChatListItemBinding) mBinding)
                        .line.setBackgroundColor(
                        mBinding.getRoot().getContext()
                                .getResources().getColor(R.color.green_send_massage));
            else
                ((ChatListItemBinding) mBinding)
                        .line.setBackgroundColor(
                        mBinding.getRoot().getContext()
                                .getResources().getColor(R.color.red_error_send_massage));
            ((ChatListItemBinding) mBinding).messageRootPost.setText(
                    getMarkdownPost(
                            post.getProps().getAttachments().get(0).getText(), mBinding.getRoot().getContext())
            );
            ((ChatListItemBinding) mBinding).messageRootPost.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @NonNull
    public static Spannable getMarkdownPost(String postMessage, Context context) {
        if (postMessage != null) {
            CharSequence string = new Bypass().markdownToSpannable(EmojiParser.parseToUnicode(postMessage));
            Spannable spannable = Spannable.Factory.getInstance().newSpannable(string);

            Linkify.addLinks(spannable, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            Linkify.addLinks(spannable, Pattern.compile("\\B@([\\w|.]+)\\b"), null, (s, start, end) -> {
                spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return true;
            }, null);

            return spannable;
        } else {
            return Spannable.Factory.getInstance().newSpannable("");
        }
    }

    // TODO этот метод не используется, он нужен?
    @NonNull
    public static SpannableStringBuilder getSpannableStringBuilder(Post post, Context context, boolean isProp, boolean isComment) {
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(EmojiParser.parseToUnicode(
                    isProp ? post.getProps().getAttachments().get(0).getText().trim() : post.getMessage()),
                    Html.FROM_HTML_MODE_LEGACY, null, new MattermostTagHandler());
        } else {
            spanned = Html.fromHtml(EmojiParser.parseToUnicode(
                    isProp ? post.getProps().getAttachments().get(0).getText().trim() : post.getMessage()),
                    null, new MattermostTagHandler());
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(spanned);
        Linkify.addLinks(ssb, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);

        Linkify.addLinks(ssb, Pattern.compile("\\B@([\\w|.]+)\\b"), null, (s, start, end) -> {
            ssb.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return true;
        }, null);

        Linkify.addLinks(ssb, Pattern.compile("<hr>.*<\\/hr>"), null, (charSequence, i, i1) -> {
            String s = charSequence.toString();
            StringBuilder builder = new StringBuilder();
            for (int k = i; k < i1; k++) {
                builder.append(' ');
            }
            ssb.replace(i, i1, builder.toString());
            ssb.setSpan(new HrSpannable(context.getResources().getColor(R.color.light_grey)), i, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return true;
        }, null);
        return ssb;
    }

    static Spannable revertSpanned(Spanned stext) {
        Object[] spans = stext.getSpans(0, stext.length(), Object.class);
        Spannable ret = Spannable.Factory.getInstance().newSpannable(stext.toString());
        if (spans != null && spans.length > 0) {
            for (int i = spans.length - 1; i >= 0; --i) {
                ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]));
            }
        }

        return ret;
    }

//    public void changeChatItemBackground(Context context, boolean isHighlighted, String searchMessageId) {
//        if ( mPost.getId().equals(searchMessageId)) {
//            ((ChatListItemBinding) mBinding).chatItem.setBackgroundColor(
//                    isHighlighted ?
//                            context.getResources().getColor(R.color.color_highlight) :
//                            context.getResources().getColor(R.color.white));
//        }
//    }
}
