package com.kilogramm.mattermost.model.websocket;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Props;
import com.kilogramm.mattermost.model.entity.User;

import io.realm.annotations.Ignore;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class WebSocketObj implements Parcelable {

    public static final String TEAM_ID = "team_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String USER_ID = "user_id";
    public static final String ACTION = "action";
    public static final String PROPS = "props";

    public static final String ACTION_POSTED = "posted";
    public static final String ACTION_CHANNEL_VIEWED = "channel_viewed";
    public static final String ACTION_TYPING = "typing";
    public static final String ACTION_POST_EDITED = "post_edited";
    public static final String ACTION_POST_DELETED = "post_deleted";


    //Posted
    public static final String CHANNEL_DISPLAY_NAME = "channel_display_name";
    public static final String CHANNEL_TYPE = "channel_type";
    public static final String CHANNEL_POST = "post";
    public static final String SENDER_NAME = "sender_name";
    public static final String MENTIONS = "mentions";

    //Typing
    public static final String PARENT_ID = "parent_id";
    public static final String STATE = "state";

    @SerializedName("team_id")
    @Expose
    private String teamId;
    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("props")
    @Expose
    private String propsJSON;
    @Ignore
    private Props props;

    //Posted
    @SerializedName("channel_display_name")
    @Expose
    private String channelDisplayName;
    @SerializedName("channel_type")
    @Expose
    private String channelType;
    @SerializedName("mentions")
    @Expose
    private String mentions;
    @SerializedName("post")
    @Expose
    private Post post;
    @SerializedName("sender_name")
    @Expose
    private String senderName;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPropsJSON() {
        return propsJSON;
    }

    public void setPropsJSON(String propsJSON) {
        this.propsJSON = propsJSON;
    }

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public void setChannelDisplayName(String channelDisplayName) {
        this.channelDisplayName = channelDisplayName;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getMentions() {
        return mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.teamId);
        dest.writeString(this.channelId);
        dest.writeString(this.userId);
        dest.writeString(this.action);
        dest.writeString(this.propsJSON);
    }

    public WebSocketObj() {
    }

    protected WebSocketObj(Parcel in) {
        this.teamId = in.readString();
        this.channelId = in.readString();
        this.userId = in.readString();
        this.action = in.readString();
        this.propsJSON = in.readString();
    }

    public static final Parcelable.Creator<WebSocketObj> CREATOR = new Parcelable.Creator<WebSocketObj>() {
        @Override
        public WebSocketObj createFromParcel(Parcel source) {
            return new WebSocketObj(source);
        }

        @Override
        public WebSocketObj[] newArray(int size) {
            return new WebSocketObj[size];
        }
    };

    public static class BuilderProps {
        private String channelDisplayName;
        private String channelType;
        private String mentions;
        private Post post;
        private String senderName;
        private String teamId;
        private String parentId;

        public BuilderProps setChannelDisplayName(String channelDisplayName) {
            this.channelDisplayName = channelDisplayName;
            return this;
        }

        public BuilderProps setChannelType(String channelType) {
            this.channelType = channelType;
            return this;
        }

        public BuilderProps setMentions(String mentions) {
            this.mentions = mentions;
            return this;
        }

        public BuilderProps setPost(Post post, String userId) {
            User user = new User();
            user.setId(userId);
            user.setUsername(senderName);
            post.setUser(user);
            this.post = post;
            return this;
        }

        public BuilderProps setSenderName(String senderName) {
            this.senderName = senderName;
            return this;
        }

        public BuilderProps setTeamId(String teamId){
            this.teamId = teamId;
            return this;
        }

        public BuilderProps setParentId(String parentId){
            this.parentId = parentId;
            return this;
        }

        public Props build(){
            return new Props(channelDisplayName, channelType, mentions, post, senderName, teamId);
        }
    }
}
