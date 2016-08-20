package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class Posts {
    @SerializedName("posts")
    Map<String,Post> posts;

    public Map<String, Post> getPosts() {
        return posts;
    }

    public void setPosts(Map<String, Post> posts) {
        this.posts = posts;
    }
}
