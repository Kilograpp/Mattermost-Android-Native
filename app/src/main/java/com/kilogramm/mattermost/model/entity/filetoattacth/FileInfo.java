package com.kilogramm.mattermost.model.entity.filetoattacth;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.UploadState;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by User on 26.12.2016.
 */

public class FileInfo extends RealmObject implements Parcelable {
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
    private String uploadState;

    public String getId() {
        return id;
    }

    public String getmMimeType() {
        return mMimeType;
    }

    public String getmName() {
        return mName;
    }

    public long getmSize() {
        return mSize;
    }

    public UploadState getUploadState() {
        return (uploadState != null) ? UploadState.valueOf(uploadState) : null;
    }

    public void setUploadState(UploadState uploadState) {
        if(uploadState != null) {
            this.uploadState = uploadState.toString();
        } else {
            this.uploadState = null;
        }
    }

    public void setmPostId(String mPostId) {
        this.mPostId = mPostId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.mUserId);
        dest.writeString(this.mPostId);
        dest.writeString(this.mCreatedAt);
        dest.writeString(this.mUpdatedAt);
        dest.writeString(this.mDeletedAt);
        dest.writeString(this.mName);
        dest.writeString(this.mExtension);
        dest.writeLong(this.mSize);
        dest.writeString(this.mMimeType);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        dest.writeByte(this.hasPreviewImage ? (byte) 1 : (byte) 0);
    }

    public FileInfo() {
    }

    protected FileInfo(Parcel in) {
        this.id = in.readString();
        this.mUserId = in.readString();
        this.mPostId = in.readString();
        this.mCreatedAt = in.readString();
        this.mUpdatedAt = in.readString();
        this.mDeletedAt = in.readString();
        this.mName = in.readString();
        this.mExtension = in.readString();
        this.mSize = in.readLong();
        this.mMimeType = in.readString();
        this.mWidth = in.readInt();
        this.mHeight = in.readInt();
        this.hasPreviewImage = in.readByte() != 0;
    }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
}
