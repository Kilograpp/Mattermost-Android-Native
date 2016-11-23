package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by ngers on 22.11.16.
 */

public class Prop extends RealmObject implements Parcelable {
    @NonNull
    @SerializedName("from_webhook")
    @Expose
    private String from_webhook;
    @SerializedName("override_username")
    @Expose
    @NonNull
    private String override_username;
    @SerializedName("attachments")
    @Expose
    @NonNull
    private RealmList<Attachments> attachments;

    public String getFrom_webhook() {
        return from_webhook;
    }

    public void setFrom_webhook(String from_webhook) {
        this.from_webhook = from_webhook;
    }

    public String getOverride_username() {
        return override_username;
    }

    public void setOverride_username(String override_username) {
        this.override_username = override_username;
    }

//    public RealmList<Attachments> getAttachments() {
//        return attachments;
//    }
//
//    public void setAttachments(RealmList<Attachments> attachments) {
//        this.attachments = attachments;
//    }

    public Prop() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.from_webhook);
        dest.writeString(this.override_username);
        dest.writeTypedList(this.attachments);
    }

    protected Prop(Parcel in) {
        this.from_webhook = in.readString();
        this.override_username = in.readString();
        this.attachments = new RealmList<>();
        in.readList(this.attachments, Attachments.class.getClassLoader());
    }

    public static final Creator<Prop> CREATOR = new Creator<Prop>() {
        @Override
        public Prop createFromParcel(Parcel source) {
            return new Prop(source);
        }

        @Override
        public Prop[] newArray(int size) {
            return new Prop[size];
        }
    };
}
