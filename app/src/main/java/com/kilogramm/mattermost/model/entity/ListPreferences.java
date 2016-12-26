package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.kilogramm.mattermost.model.entity.Preference.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 13.10.2016.
 */
public class ListPreferences implements Parcelable {

    private List<Preferences> preferencesList = new ArrayList<>();

    public List<Preferences> getmSaveData() {
        return preferencesList;
    }

    public void setPreferencesList(List<Preferences> preferencesList) {
        this.preferencesList = preferencesList;
    }

    public ListPreferences() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.preferencesList);
    }

    protected ListPreferences(Parcel in) {
        this.preferencesList = in.createTypedArrayList(Preferences.CREATOR);
    }

    public static final Creator<ListPreferences> CREATOR = new Creator<ListPreferences>() {
        @Override
        public ListPreferences createFromParcel(Parcel source) {
            return new ListPreferences(source);
        }

        @Override
        public ListPreferences[] newArray(int size) {
            return new ListPreferences[size];
        }
    };
}
