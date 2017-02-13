package com.kilogramm.mattermost.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.User;

import io.realm.annotations.PrimaryKey;

/**
 * Created by melkshake on 09.02.17.
 */

public class ResponsedChannel implements Parcelable{
    public static final String DIRECT = "D";
    public static final String OPEN = "O";
    public static final String PRIVATE = "P";

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("create_at")
    @Expose
    private Long createAt;
    @SerializedName("update_at")
    @Expose
    private Long updateAt;
    @SerializedName("delete_at")
    @Expose
    private Long deleteAt;
    @SerializedName("team_id")
    @Expose
    private String teamId;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("purpose")
    @Expose
    private String purpose;
    @SerializedName("last_post_at")
    @Expose
    private Long lastPostAt;
    @SerializedName("total_msg_count")
    @Expose
    private Integer totalMsgCount;
    @SerializedName("extra_update_at")
    @Expose
    private Long extraUpdateAt;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;
    @SerializedName("username")
    @Expose
    private String username;

    private User user;

    private Integer unreadedMessage = 0;

    public void setTotalMsgCount(Integer totalMsgCount) {
        this.totalMsgCount = totalMsgCount;
    }

    public Integer getUnreadedMessage() {
        return unreadedMessage;
    }

    public void setUnreadedMessage(Integer unreadedMessage) {
        this.unreadedMessage = unreadedMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public Long getDeleteAt() {
        return deleteAt;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public String getPurpose() {
        return purpose;
    }

    public Long getLastPostAt() {
        return lastPostAt;
    }

    public Integer getTotalMsgCount() {
        return totalMsgCount;
    }

    public Long getExtraUpdateAt() {
        return extraUpdateAt;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setAttributesToCreate(String name, String displayName,
                                      String purpose, String header, String type) {
        setName(name);
        setDisplayName(displayName);
        setPurpose(purpose);
        setHeader(header);
        setType(type);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public ResponsedChannel(Channel channel) {
        this.id = channel.getId();
        this.createAt = channel.getCreateAt();
        this.updateAt = channel.getUpdateAt();
        this.deleteAt = channel.getDeleteAt();
        this.teamId = channel.getTeamId();
        this.type = channel.getType();
        this.displayName = channel.getDisplayName();
        this.name = channel.getName();
        this.header = channel.getHeader();
        this.purpose = channel.getPurpose();
        this.lastPostAt = channel.getLastPostAt();
        this.totalMsgCount = channel.getTotalMsgCount();
        this.extraUpdateAt = channel.getExtraUpdateAt();
        this.creatorId = channel.getCreatorId();
        this.username = channel.getUsername();
        this.user = channel.getUser();
        this.unreadedMessage = channel.getUnreadedMessage();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.createAt);
        dest.writeValue(this.updateAt);
        dest.writeValue(this.deleteAt);
        dest.writeString(this.teamId);
        dest.writeString(this.type);
        dest.writeString(this.displayName);
        dest.writeString(this.name);
        dest.writeString(this.header);
        dest.writeString(this.purpose);
        dest.writeValue(this.lastPostAt);
        dest.writeValue(this.totalMsgCount);
        dest.writeValue(this.extraUpdateAt);
        dest.writeString(this.creatorId);
        dest.writeString(this.username);
        dest.writeParcelable(this.user, flags);
        dest.writeValue(this.unreadedMessage);
    }

    public ResponsedChannel() {
    }

    public ResponsedChannel(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    protected ResponsedChannel(Parcel in) {
        this.id = in.readString();
        this.createAt = (Long) in.readValue(Long.class.getClassLoader());
        this.updateAt = (Long) in.readValue(Long.class.getClassLoader());
        this.deleteAt = (Long) in.readValue(Long.class.getClassLoader());
        this.teamId = in.readString();
        this.type = in.readString();
        this.displayName = in.readString();
        this.name = in.readString();
        this.header = in.readString();
        this.purpose = in.readString();
        this.lastPostAt = (Long) in.readValue(Long.class.getClassLoader());
        this.totalMsgCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.extraUpdateAt = (Long) in.readValue(Long.class.getClassLoader());
        this.creatorId = in.readString();
        this.username = in.readString();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.unreadedMessage = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<ResponsedChannel> CREATOR = new Parcelable.Creator<ResponsedChannel>() {
        @Override
        public ResponsedChannel createFromParcel(Parcel source) {
            return new ResponsedChannel(source);
        }

        @Override
        public ResponsedChannel[] newArray(int size) {
            return new ResponsedChannel[size];
        }
    };

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                ", deleteAt=" + deleteAt +
                ", teamId='" + teamId + '\'' +
                ", type='" + type + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                ", header='" + header + '\'' +
                ", purpose='" + purpose + '\'' +
                ", lastPostAt=" + lastPostAt +
                ", totalMsgCount=" + totalMsgCount +
                ", extraUpdateAt=" + extraUpdateAt +
                ", creatorId='" + creatorId + '\'' +
                ", username='" + username + '\'' +
                ", user=" + user +
                ", unreadedMessage=" + unreadedMessage +
                '}';
    }
}