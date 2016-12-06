package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Evgeny on 02.09.2016.
 */
public class RealmString extends RealmObject implements Parcelable {

    private String string;
    private long fileSize;

    public RealmString(String s) {
        super();
        this.string = s;
    }

    public RealmString(){
        super();
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.string);
        dest.writeLong(this.fileSize);
    }

    protected RealmString(Parcel in) {
        this.string = in.readString();
        this.fileSize = in.readLong();
    }

    public static final Creator<RealmString> CREATOR = new Creator<RealmString>() {
        @Override
        public RealmString createFromParcel(Parcel source) {
            return new RealmString(source);
        }

        @Override
        public RealmString[] newArray(int size) {
            return new RealmString[size];
        }
    };
}
