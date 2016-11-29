package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by ngers on 23.11.16.
 */

public class Attachments extends RealmObject implements Parcelable {
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("fallback")
    @Expose
    private String fallback;
    //        @SerializedName("fields")
//        @Expose
//        private RealmList<RealmString> fields;
//        @SerializedName("mrkdw_in")
//        @Expose
//        private RealmList<RealmString> mrkdw_in;
    @SerializedName("text")
    @Expose
    private String text;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Attachments() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.color);
        dest.writeString(this.fallback);
        dest.writeString(this.text);
    }

    protected Attachments(Parcel in) {
        this.color = in.readString();
        this.fallback = in.readString();
        this.text = in.readString();
    }

    public static final Creator<Attachments> CREATOR = new Creator<Attachments>() {
        @Override
        public Attachments createFromParcel(Parcel source) {
            return new Attachments(source);
        }

        @Override
        public Attachments[] newArray(int size) {
            return new Attachments[size];
        }
    };
}
