package com.kilogramm.mattermost.model.entity.channel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.user.User;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * Created by Evgeny on 03.08.2016.
 */
public class Channel extends RealmObject {


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

    /**
     *
     * @return
     * The purpose
     */
    public String getPurpose() {
        return purpose;
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



}