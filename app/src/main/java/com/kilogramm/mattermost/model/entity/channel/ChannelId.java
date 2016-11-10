package com.kilogramm.mattermost.model.entity.channel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by ngers on 02.11.16.
 */

public class ChannelId implements Parcelable {
    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
    }

    public ChannelId() {
    }

    protected ChannelId(Parcel in) {
        this.id = in.readString();
    }

    public static final Parcelable.Creator<ChannelId> CREATOR = new Parcelable.Creator<ChannelId>() {
        @Override
        public ChannelId createFromParcel(Parcel source) {
            return new ChannelId(source);
        }

        @Override
        public ChannelId[] newArray(int size) {
            return new ChannelId[size];
        }
    };
}
