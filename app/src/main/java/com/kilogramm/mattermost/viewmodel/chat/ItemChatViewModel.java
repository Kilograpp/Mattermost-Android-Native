package com.kilogramm.mattermost.viewmodel.chat;

import android.content.Context;
import android.databinding.BaseObservable;
import android.os.Bundle;

import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.viewmodel.ViewModel;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ItemChatViewModel extends BaseObservable implements ViewModel {

    private Post post;
    private Context context;

    public ItemChatViewModel(Context context, Post post){
        this.context = context;
        this.post = post;
    }


    public String getMessage() {
        return post.getMessage();}


    @Override
    public void destroy() {
        context = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void setPost(Post post) {
        this.post = post;
        notifyChange();
    }

}