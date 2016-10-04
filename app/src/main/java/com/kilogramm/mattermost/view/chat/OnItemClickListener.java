package com.kilogramm.mattermost.view.chat;

import android.view.View;

import com.kilogramm.mattermost.model.entity.post.Post;

/**
 * Created by Evgeny on 21.09.2016.
 */
public interface OnItemClickListener<T>{
    void OnItemClick(View view, T item);
}
