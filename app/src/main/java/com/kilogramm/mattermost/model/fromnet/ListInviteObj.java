package com.kilogramm.mattermost.model.fromnet;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 18.10.2016.
 */
public class ListInviteObj implements Parcelable {
    @SerializedName("invites")
    @Expose
    private List<InviteObject> invites = new ArrayList<>();

    public List<InviteObject> getInvites() {
        return invites;
    }

    public void setInvites(List<InviteObject> invites) {
        this.invites = invites;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.invites);
    }

    public ListInviteObj() {
    }

    protected ListInviteObj(Parcel in) {
        this.invites = in.createTypedArrayList(InviteObject.CREATOR);
    }

    public static final Parcelable.Creator<ListInviteObj> CREATOR = new Parcelable.Creator<ListInviteObj>() {
        @Override
        public ListInviteObj createFromParcel(Parcel source) {
            return new ListInviteObj(source);
        }

        @Override
        public ListInviteObj[] newArray(int size) {
            return new ListInviteObj[size];
        }
    };
}
