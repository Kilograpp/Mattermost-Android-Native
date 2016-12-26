package com.kilogramm.mattermost.model.entity.filetoattacth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by User on 26.12.2016.
 */

public class FileInfo extends RealmObject {
    @SerializedName("id") @Expose @PrimaryKey String id;
    @SerializedName("user_id") @Expose String mUserId;
    @SerializedName("post_id") @Expose String mPostId;
    @SerializedName("create_at") @Expose String mCreatedAt;
    @SerializedName("update_at") @Expose String mUpdatedAt;
    @SerializedName("delete_at") @Expose String mDeletedAt;
    @SerializedName("name") @Expose String mName;
    @SerializedName("extension") @Expose String mExtension;
    @SerializedName("size") @Expose long mSize;
    @SerializedName("mime_type") @Expose String mMimeType;
    @SerializedName("width") @Expose int mWidth;
    @SerializedName("height") @Expose int mHeight;
    @SerializedName("has_preview_image") @Expose boolean hasPreviewImage;

    public String getId() {
        return id;
    }
}
