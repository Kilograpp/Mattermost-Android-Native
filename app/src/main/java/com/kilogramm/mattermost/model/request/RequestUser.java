package com.kilogramm.mattermost.model.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by melkshake on 09.02.17.
 */

public class RequestUser implements Parcelable {

    @SerializedName("user_id")
    @Expose
    private String userId;

    public RequestUser(String userId) {
        this.userId = userId;
    }

    public RequestUser() {
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
    }

    protected RequestUser(Parcel in) {
        this.userId = in.readString();
    }

    public static final Parcelable.Creator<RequestUser> CREATOR = new Parcelable.Creator<RequestUser>() {
        @Override
        public RequestUser createFromParcel(Parcel source) {
            return new RequestUser(source);
        }

        @Override
        public RequestUser[] newArray(int size) {
            return new RequestUser[size];
        }
    };
}
