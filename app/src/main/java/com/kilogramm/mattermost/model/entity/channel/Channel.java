package com.kilogramm.mattermost.model.entity.channel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.user.User;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * Created by Evgeny on 03.08.2016.
 */
public class Channel extends RealmObject implements Parcelable {


    public static final String DIRECT = "D";
    public static final String OPEN = "O";

    @PrimaryKey
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

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The createAt
     */
    public Long getCreateAt() {
        return createAt;
    }

    /**
     *
     * @param createAt
     * The create_at
     */
    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    /**
     *
     * @return
     * The updateAt
     */
    public Long getUpdateAt() {
        return updateAt;
    }

    /**
     *
     * @return
     * The deleteAt
     */
    public Long getDeleteAt() {
        return deleteAt;
    }

    /**
     *
     * @return
     * The teamId
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     *
     * @param teamId
     * The team_id
     */
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The header
     */
    public String getHeader() {
        return header;
    }


    public void setHeader(String header) {
        this.header = header;
    }

    /**
     *
     * @return
     * The purpose
     */
    public String getPurpose() {
        return purpose;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return
     * The lastPostAt
     */
    public Long getLastPostAt() {
        return lastPostAt;
    }


    /**
     *
     * @return
     * The totalMsgCount
     */
    public Integer getTotalMsgCount() {
        return totalMsgCount;
    }



    /**
     *
     * @return
     * The extraUpdateAt
     */
    public Long getExtraUpdateAt() {
        return extraUpdateAt;
    }


    /**
     *
     * @return
     * The creatorId
     */
    public String getCreatorId() {
        return creatorId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public Channel(Channel channel) {
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

    public Channel() {
    }

    protected Channel(Parcel in) {
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

    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel source) {
            return new Channel(source);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };
}