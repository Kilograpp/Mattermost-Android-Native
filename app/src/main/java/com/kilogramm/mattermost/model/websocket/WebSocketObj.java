package com.kilogramm.mattermost.model.websocket;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private String props;

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

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
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
        dest.writeString(this.props);
    }

    public WebSocketObj() {
    }

    protected WebSocketObj(Parcel in) {
        this.teamId = in.readString();
        this.channelId = in.readString();
        this.userId = in.readString();
        this.action = in.readString();
        this.props = in.readString();
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
}
