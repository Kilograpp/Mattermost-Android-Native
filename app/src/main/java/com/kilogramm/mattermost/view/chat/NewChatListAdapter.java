package com.kilogramm.mattermost.view.chat;

import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChatListItemBinding;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.tools.MarkDownConfig;
import com.kilogramm.mattermost.ui.MRealmRecyclerView;
import com.kilogramm.mattermost.viewmodel.chat.ItemChatViewModel;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.TextFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class NewChatListAdapter extends RealmBasedRecyclerViewAdapter<Post, NewChatListAdapter.MyViewHolder> {

    private static final String TAG = "NewChatListAdapter";

    private Context context;
    private MRealmRecyclerView mRecyclerView;

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, String animateExtraColumnName,
                              MRealmRecyclerView mRecyclerView) {
        super(context, realmResults, true, animateResults, animateExtraColumnName);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults,MRealmRecyclerView mRecyclerView) {
        super(context, realmResults, true, animateResults);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, boolean addSectionHeaders,
                              String headerColumnName,MRealmRecyclerView mRecyclerView) {
        super(context, realmResults, true, animateResults, addSectionHeaders, headerColumnName);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    public NewChatListAdapter(Context context, RealmResults<Post> realmResults,
                              boolean animateResults, boolean addSectionHeaders,
                              String headerColumnName, String animateExtraColumnName,
                              MRealmRecyclerView mRecyclerView) {
        super(context, realmResults, true, animateResults, addSectionHeaders, headerColumnName, animateExtraColumnName);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public MyViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        MyViewHolder holder = MyViewHolder.create(inflater, viewGroup);
        return holder;
    }

    @Override
    public void onBindRealmViewHolder(MyViewHolder myViewHolder, int i) {
        Post post = realmResults.get(i);
        Calendar curDate = Calendar.getInstance();
        Calendar preDate = Calendar.getInstance();
        Post prePost = null;
        Boolean isTitle = false;
        if(i-1 >= 0){
            prePost = realmResults.get(i-1);
            curDate.setTime(new Date(post.getCreateAt()));
            preDate.setTime(new Date(prePost.getCreateAt()));
            if(curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)){
                isTitle = true;
            }
        }
        myViewHolder.bindTo(post, context, isTitle);
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
        public void bindTo(Post post, Context context, Boolean isTitle) {
            mBinding.getRoot().setOnLongClickListener(view -> {
                Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show();
                return true;
            });
            mBinding.avatar.setTag(post);
            Spannable spannable = new Spannable.Factory().newSpannable(post.getMessage());
            Linkify.addLinks(spannable, Pattern.compile("\\B@([\\w|.]+)\\b"), null, (s, start, end) -> {
                spannable.setSpan(new ForegroundColorSpan(context.getResources ().getColor(R.color.colorPrimary)),
                        start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return false;
            }, null);
            Linkify.addLinks(spannable,Linkify.EMAIL_ADDRESSES);
            Linkify.addLinks(spannable,Linkify.WEB_URLS);
            mBinding.message.setText(spannable);
            RxMarkdown.with(post.getMessage(), context)
                    .config(MarkDownConfig.getRxMDConfiguration(context))
                    .factory(TextFactory.create())
                    .intoObservable()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(charSequence -> mBinding.message.setText(charSequence, TextView.BufferType.SPANNABLE));
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

            mBinding.executePendingBindings();
        }

        public ChatListItemBinding getmBinding() {
            return mBinding;
        }
    }
}
