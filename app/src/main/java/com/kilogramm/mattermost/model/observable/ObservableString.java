package com.kilogramm.mattermost.model.observable;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Evgeny on 26.07.2016.
 */
// TODO класс не используется
public class ObservableString extends BaseObservable implements Parcelable, Serializable{

    static final long serialVersionUID = 1L;
    private String mValue;


    public ObservableString(String mValue) {
        this.mValue = mValue;
    }

    public String get() {
        return this.mValue;
    }

    public void set(String value) {
        if (!equals(value, this.mValue)) {
            this.mValue = value;
            this.notifyChange();
        }

    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public boolean isEmpty() {
        return mValue == null || mValue.isEmpty();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mValue);
    }

    public ObservableString() {
    }

    // TODO заменил модификатор доступа с protected на private, потому что студия рекомендовала (Kepar)
    private ObservableString(Parcel in) {
        this.mValue = in.readString();
    }

    public static final Creator<ObservableString> CREATOR = new Creator<ObservableString>() {
        @Override
        public ObservableString createFromParcel(Parcel source) {
            return new ObservableString(source);
        }

        @Override
        public ObservableString[] newArray(int size) {
            return new ObservableString[size];
        }
    };
}
