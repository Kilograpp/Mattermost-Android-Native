package com.kilogramm.mattermost.model.entity.Preference;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ngers on 16.11.16.
 */

public class Preferences extends RealmObject implements Parcelable {
    @SerializedName("category")
    @Expose
    private String category;
    @PrimaryKey
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("user_id")
    @Expose
    private String user_id;
    @SerializedName("value")
    @Expose
    private String value;

    public Preferences(String name, String user_id, Boolean value, String category) {
        this.category = category;
        this.name = name;
        this.user_id = user_id;
        this.value = value.toString();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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

    public void setValue(String value) {
        this.value = value;
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

    public Preferences() {
    }

    protected Preferences(Parcel in) {
        this.category = in.readString();
        this.name = in.readString();
        this.user_id = in.readString();
        this.value = in.readString();
    }

    public static final Parcelable.Creator<Preferences> CREATOR = new Parcelable.Creator<Preferences>() {
        @Override
        public Preferences createFromParcel(Parcel source) {
            return new Preferences(source);
        }

        @Override
        public Preferences[] newArray(int size) {
            return new Preferences[size];
        }
    };
}
