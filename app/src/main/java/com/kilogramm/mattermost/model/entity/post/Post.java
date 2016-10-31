package com.kilogramm.mattermost.model.entity.post;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class Post extends RealmObject implements Parcelable {

    public static final long NO_UPDATE = -1;

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
    @SerializedName("filenames")
    @Expose
    private RealmList<RealmString> filenames = new RealmList<>();
    @SerializedName("pending_post_id")
    @Expose
    private String pendingPostId;
    private User user;

    private Boolean viewed = false;

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSystemMessage() {
        if (type != null)
            return type.contains("system");
        else
            return false;
    }

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

    public List<String> getFilenames() {
        List<String> list = new ArrayList<>();
        for (RealmString filename : this.filenames) {
            list.add(filename.getString());
        }
        return list;
    }

    public void setFilenames(List<String> filenames) {
        if (filenames == null) {
            return;
        }
        this.filenames = new RealmList<>();
        if (Build.VERSION.SDK_INT == 24) {
            filenames.forEach(s -> this.filenames.add(new RealmString(s)));
        } else {
            for (String filename : filenames) {
                this.filenames.add(new RealmString(filename));
            }
        }
    }

    public void setFilenames(RealmResults<FileToAttach> attachedFiles) {
        if (attachedFiles == null) {
            return;
        }
            for (FileToAttach attachedFile : attachedFiles) {
                this.filenames.add(new RealmString(attachedFile.getFileName()));
            }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.createAt);
        dest.writeValue(this.updateAt);
        dest.writeValue(this.deleteAt);
        dest.writeString(this.userId);
        dest.writeString(this.channelId);
        dest.writeString(this.rootId);
        dest.writeString(this.parentId);
        dest.writeString(this.originalId);
        dest.writeString(this.message);
        dest.writeString(this.type);
        dest.writeString(this.hashtags);
        dest.writeList(this.filenames);
        dest.writeString(this.pendingPostId);
        dest.writeParcelable(this.user, flags);
        dest.writeValue(this.viewed);
    }

    public Post() {
    }

    public Post(Post post){
        this.id=post.getId();
        this.createAt=post.getCreateAt();
        this.updateAt=post.getUpdateAt();
        this.deleteAt=post.getDeleteAt();
        this.userId=post.getUserId();
        this.channelId=post.getChannelId();
        this.rootId=post.getRootId();
        this.parentId=post.getParentId();
        this.originalId=post.getOriginalId();
        this.message=post.getMessage();
        this.type=post.getType();
        this.hashtags=post.getHashtags();
        this.filenames = new RealmList<>();
        for (String s : post.getFilenames()) {
            this.filenames.add(new RealmString(s));
        }
        this.pendingPostId=post.getPendingPostId();
        this.user=post.getUser();
        this.viewed=post.getViewed();
    }


    protected Post(Parcel in) {
        this.id = in.readString();
        this.createAt = (Long) in.readValue(Long.class.getClassLoader());
        this.updateAt = (Long) in.readValue(Long.class.getClassLoader());
        this.deleteAt = (Long) in.readValue(Long.class.getClassLoader());
        this.userId = in.readString();
        this.channelId = in.readString();
        this.rootId = in.readString();
        this.parentId = in.readString();
        this.originalId = in.readString();
        this.message = in.readString();
        this.type = in.readString();
        this.hashtags = in.readString();
        this.filenames = new RealmList<>();
        in.readList(this.filenames, RealmString.class.getClassLoader());
        this.pendingPostId = in.readString();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.viewed = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}