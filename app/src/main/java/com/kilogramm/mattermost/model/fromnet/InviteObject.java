package com.kilogramm.mattermost.model.fromnet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Evgeny on 18.10.2016.
 */
public class InviteObject implements Parcelable {
    private String email;
    private String firstName;
    private String lastName;



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
    }

    public InviteObject(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public InviteObject() {
    }

    protected InviteObject(Parcel in) {
        this.email = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
    }

    public static final Parcelable.Creator<InviteObject> CREATOR = new Parcelable.Creator<InviteObject>() {
        @Override
        public InviteObject createFromParcel(Parcel source) {
            return new InviteObject(source);
        }

        @Override
        public InviteObject[] newArray(int size) {
            return new InviteObject[size];
        }
    };
}
