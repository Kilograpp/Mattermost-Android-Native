package com.kilogramm.mattermost.model.fromnet;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.List;

/**
 * Created by Evgeny on 22.08.2016.
 */
public class ExtraInfo implements Parcelable {
    @SerializedName("member_count")
    @Expose
    private String member_count;
    @SerializedName("members")
    @Expose
    private List<User> members;

    public String getMember_count() {
        return member_count;
    }

    public void setMember_count(String member_count) {
        this.member_count = member_count;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public ExtraInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.member_count);
        dest.writeTypedList(this.members);
    }

    protected ExtraInfo(Parcel in) {
        this.member_count = in.readString();
        this.members = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<ExtraInfo> CREATOR = new Creator<ExtraInfo>() {
        @Override
        public ExtraInfo createFromParcel(Parcel source) {
            return new ExtraInfo(source);
        }

        @Override
        public ExtraInfo[] newArray(int size) {
            return new ExtraInfo[size];
        }
    };
}
