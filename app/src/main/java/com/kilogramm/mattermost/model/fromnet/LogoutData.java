package com.kilogramm.mattermost.model.fromnet;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class LogoutData implements Parcelable {

    @SerializedName("user_id")
    @Expose
    private String userId;

    public LogoutData(String userId) {
        this.userId = userId;
    }

    public LogoutData() {
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

    protected LogoutData(Parcel in) {
        this.userId = in.readString();
    }

    public static final Parcelable.Creator<LogoutData> CREATOR = new Parcelable.Creator<LogoutData>() {
        @Override
        public LogoutData createFromParcel(Parcel source) {
            return new LogoutData(source);
        }

        @Override
        public LogoutData[] newArray(int size) {
            return new LogoutData[size];
        }
    };
}
