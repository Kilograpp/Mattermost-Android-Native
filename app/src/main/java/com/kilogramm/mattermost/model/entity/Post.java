package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class Post extends RealmObject{

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
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @SerializedName("root_id")
    @Expose
    private String rootId;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("original_id")
    @Expose
    private String originalId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("hashtags")
    @Expose
    private String hashtags;
    /*@SerializedName("filenames")
    @Expose
    private List<Object> filenames = new ArrayList<Object>();*/
    @SerializedName("pending_post_id")
    @Expose
    private String pendingPostId;

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The createAt
     */
    public Long getCreateAt() {
        return createAt;
    }

    /**
     * @param createAt The create_at
     */
    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    /**
     * @return The updateAt
     */
    public Long getUpdateAt() {
        return updateAt;
    }

    /**
     * @param updateAt The update_at
     */
    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    /**
     * @return The deleteAt
     */
    public Long getDeleteAt() {
        return deleteAt;
    }

    /**
     * @param deleteAt The delete_at
     */
    public void setDeleteAt(Long deleteAt) {
        this.deleteAt = deleteAt;
    }

    /**
     * @return The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The channelId
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @param channelId The channel_id
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * @return The rootId
     */
    public String getRootId() {
        return rootId;
    }

    /**
     * @param rootId The root_id
     */
    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    /**
     * @return The parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId The parent_id
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return The originalId
     */
    public String getOriginalId() {
        return originalId;
    }

    /**
     * @param originalId The original_id
     */
    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    /**
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * @return The hashtags
     */
    public String getHashtags() {
        return hashtags;
    }

    /**
     * @param hashtags The hashtags
     */
    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }


    /**
     * @return The pendingPostId
     */
    public String getPendingPostId() {
        return pendingPostId;
    }

    /**
     * @param pendingPostId The pending_post_id
     */
    public void setPendingPostId(String pendingPostId) {
        this.pendingPostId = pendingPostId;
    }
}