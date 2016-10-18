package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by melkshake on 22.09.16.
 */
public class SaveData implements Parcelable {

    private String category;
    private String name;
    private String user_id;
    private String value;

    public SaveData(String name, String user_id, Boolean value, String category) {
        this.name = name;
        this.user_id = user_id;
        this.value = value.toString();
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeString(this.name);
        dest.writeString(this.user_id);
        dest.writeString(this.value);
    }

    protected SaveData(Parcel in) {
        this.category = in.readString();
        this.name = in.readString();
        this.user_id = in.readString();
        this.value = in.readString();
    }

    public static final Creator<SaveData> CREATOR = new Creator<SaveData>() {
        @Override
        public SaveData createFromParcel(Parcel source) {
            return new SaveData(source);
        }

        @Override
        public SaveData[] newArray(int size) {
            return new SaveData[size];
        }
    };
}
