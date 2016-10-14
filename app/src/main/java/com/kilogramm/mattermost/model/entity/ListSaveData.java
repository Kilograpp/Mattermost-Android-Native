package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 13.10.2016.
 */
public class ListSaveData implements Parcelable {

    private List<SaveData> mSaveData = new ArrayList<>();

    public List<SaveData> getmSaveData() {
        return mSaveData;
    }

    public void setmSaveData(List<SaveData> mSaveData) {
        this.mSaveData = mSaveData;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mSaveData);
    }

    public ListSaveData() {
    }

    protected ListSaveData(Parcel in) {
        this.mSaveData = in.createTypedArrayList(SaveData.CREATOR);
    }

    public static final Parcelable.Creator<ListSaveData> CREATOR = new Parcelable.Creator<ListSaveData>() {
        @Override
        public ListSaveData createFromParcel(Parcel source) {
            return new ListSaveData(source);
        }

        @Override
        public ListSaveData[] newArray(int size) {
            return new ListSaveData[size];
        }
    };
}
