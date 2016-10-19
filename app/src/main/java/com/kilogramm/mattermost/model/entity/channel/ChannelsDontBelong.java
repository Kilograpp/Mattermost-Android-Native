package com.kilogramm.mattermost.model.entity.channel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by melkshake on 19.10.16.
 */

public class ChannelsDontBelong extends RealmObject {
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

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    public Long getDeleteAt() {
        return deleteAt;
    }

    public void setDeleteAt(Long deleteAt) {
        this.deleteAt = deleteAt;
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Long getLastPostAt() {
        return lastPostAt;
    }

    public void setLastPostAt(Long lastPostAt) {
        this.lastPostAt = lastPostAt;
    }

    public Integer getTotalMsgCount() {
        return totalMsgCount;
    }

    public void setTotalMsgCount(Integer totalMsgCount) {
        this.totalMsgCount = totalMsgCount;
    }

    public Long getExtraUpdateAt() {
        return extraUpdateAt;
    }

    public void setExtraUpdateAt(Long extraUpdateAt) {
        this.extraUpdateAt = extraUpdateAt;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
