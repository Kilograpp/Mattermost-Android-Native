package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.realm.RealmModel;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchResult implements RealmModel{

    @SerializedName("order")
    ArrayList<String> order;
    @SerializedName("posts")
    ArrayList<Posts> posts;

    public ArrayList<String> getOrder() {
        return order;
    }

    public void setOrder(ArrayList<String> order) {
        this.order = order;
    }

    public ArrayList<Posts> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Posts> posts) {
        this.posts = posts;
    }
}
