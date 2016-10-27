package com.kilogramm.mattermost.adapters;

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
import android.util.Log;
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

import io.realm.RealmAD;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 10.10.2016.
 */
public class AdapterPost extends RealmAD<Post, AdapterPost.MyViewHolder> {
    public static final String TAG = "AdapterPost";

    public static final int ITEM = -1;
    public static final int LOADING_TOP = -2;
    public static final int LOADING_BOTTOM = -3;

    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener<String> listener;

    private Boolean isTopLoading = false;
    private Boolean isBottomLoading = false;


    public AdapterPost(Context context, RealmResults adapterData, OnItemClickListener<String> listener) {
        super(adapterData);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isTopLoading) {
            Log.d(TAG, "Loading_Top = " + position);
            return LOADING_TOP;
        } else if (position == getItemCount() - 1 && isBottomLoading) {
            Log.d(TAG, "Loading_Bottom = " + (getItemCount() - 1));
            return LOADING_BOTTOM;
        } else {
            return ITEM;
        }
    }

    @Override
    public int getItemCount() {

        int count = super.getItemCount();

        if (isBottomLoading) {
            count++;
        }
        if (isTopLoading) {
            count++;
        }
        //Log.d(TAG,"super.getItemCount() = "+super.getItemCount() + "\n count = " + count);
        return count;
    }

    public void setLoadingTop(Boolean enabled) {
        Log.d(TAG, "setLoadingTop(" + enabled + ");");
        if (isTopLoading != enabled) {
            isTopLoading = enabled;
            getItemCount();
            if (enabled) {
                notifyItemInserted(0);
            } else {
                notifyItemRemoved(0);
            }
        }
    }

    public void setLoadingBottom(Boolean enabled) {
        Log.d(TAG, "setLoadingBottom(" + enabled + ");");
        if (isBottomLoading != enabled) {
            isBottomLoading = enabled;
            getItemCount();
            if (enabled) {
                notifyItemInserted(getItemCount());
            } else {
                notifyItemRemoved(getItemCount());
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                Log.d(TAG, "bindItem ");
                return MyViewHolder.createItem(inflater, parent);
            case LOADING_TOP:
                Log.d(TAG, "bindTop ");
                return MyViewHolder.createLoadingTop(inflater, parent);
            case LOADING_BOTTOM:
                Log.d(TAG, "bindBot ");
                return MyViewHolder.createLoadingBottom(inflater, parent);
            default:
                return MyViewHolder.createItem(inflater, parent);
        }
    }

    @Override
    public void onBindViewHolder(AdapterPost.MyViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM) {
            int pos = isTopLoading ? position - 1 : position;
            Post post = getData().get(isTopLoading ? position - 1 : position);
            Calendar curDate = Calendar.getInstance();
            Calendar preDate = Calendar.getInstance();
            Post prePost;
            Boolean isTitle = false;
            Post root = null;
            if (pos - 1 >= 0) {
                prePost = getData().get(pos - 1);
                curDate.setTime(new Date(post.getCreateAt()));
                preDate.setTime(new Date(prePost.getCreateAt()));
                if (curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)) {
                    isTitle = true;
                }
                if (post.getRootId() != null
                        && post.getRootId().length() > 0
                        && getData().where().equalTo("id", post.getRootId()).findAll().size() != 0) {
                    root = getData().where().equalTo("id", post.getRootId()).findFirst();
                }
            }
            if(pos-1==-1){
                isTitle = true;
            }
            holder.bindToItem(post, context, isTitle, root, listener);
        } else {
            holder.bindToLoadingBottom();
        }
    }

    public Boolean getTopLoading() {
        return isTopLoading;
    }

    public Boolean getBottomLoading() {
        return isBottomLoading;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mBinding;

        public static MyViewHolder createItem(LayoutInflater inflater, ViewGroup parent) {
            ChatListItemBinding binding = ChatListItemBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        public static MyViewHolder createLoadingTop(LayoutInflater inflater, ViewGroup parent) {
            com.kilogramm.mattermost.databinding.LoadMoreLayoutBinding binding = com.kilogramm.mattermost.databinding.LoadMoreLayoutBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        public static MyViewHolder createLoadingBottom(LayoutInflater inflater, ViewGroup parent) {
            com.kilogramm.mattermost.databinding.LoadMoreLayoutBinding binding = com.kilogramm.mattermost.databinding.LoadMoreLayoutBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        private MyViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bindToItem(Post post, Context context, Boolean isTitle, Post root, OnItemClickListener listener) {
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
            SpannableStringBuilder ssb = getSpannableStringBuilder(post, context);
            ((ChatListItemBinding) mBinding).message.setText(revertSpanned(ssb));
            ((ChatListItemBinding) mBinding).message.setMovementMethod(LinkMovementMethod.getInstance());
            if (((ChatListItemBinding) mBinding).getViewModel() == null) {
                ((ChatListItemBinding) mBinding).setViewModel(new ItemChatViewModel(post));
            } else {
                ((ChatListItemBinding) mBinding).getViewModel().setPost(post);

            }
            if (root != null)
                setRootMassage(root);
            else
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

        private void setRootMassage(Post root) {
            if (root != null) {
                ((ChatListItemBinding) mBinding).filesViewRoot.setBackgroundColorComment();
                ((ChatListItemBinding) mBinding).filesViewRoot.setItems(root.getFilenames());
                ((ChatListItemBinding) mBinding).linearLayoutRootPost.setVisibility(View.VISIBLE);
                ((ChatListItemBinding) mBinding).nickRootPost.setText(root.getUser().getUsername());
                ((ChatListItemBinding) mBinding).getViewModel().loadImage(((ChatListItemBinding) mBinding).avatarRootPost, ((ChatListItemBinding) mBinding).getViewModel().getUrl(root));
                ((ChatListItemBinding) mBinding).messageRootPost.setText(revertSpanned(getSpannableStringBuilder(root, (mBinding).getRoot().getContext())).toString().trim());
            }
        }

        @NonNull
        private static SpannableStringBuilder getSpannableStringBuilder(Post post, Context context) {
            Spanned spanned;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(EmojiParser.parseToUnicode(post.getMessage()), Html.FROM_HTML_MODE_LEGACY, null, new MattermostTagHandler());
            } else {
                spanned = Html.fromHtml(EmojiParser.parseToUnicode(post.getMessage()), null, new MattermostTagHandler());
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
    }

    public int getPositionById(String id) {
        int count = super.getPositionById(id);

        if (isTopLoading) {
            count++;
        }
        //Log.d(TAG,"super.getItemCount() = "+super.getItemCount() + "\n count = " + count);
        return count;
    }
}
