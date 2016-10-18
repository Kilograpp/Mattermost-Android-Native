package com.kilogramm.mattermost.rxtest;

import com.kilogramm.mattermost.model.entity.post.Post;

/**
 * Created by Evgeny on 17.10.2016.
 */
public interface GetRootPost {
    Post getRootPost(Post post);
}
