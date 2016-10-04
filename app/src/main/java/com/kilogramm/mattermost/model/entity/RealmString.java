package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Evgeny on 02.09.2016.
 */
public class RealmString extends RealmObject implements Parcelable {

    private String string;

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.string);
    }

    protected RealmString(Parcel in) {
        this.string = in.readString();
    }

    public static final Parcelable.Creator<RealmString> CREATOR = new Parcelable.Creator<RealmString>() {
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
