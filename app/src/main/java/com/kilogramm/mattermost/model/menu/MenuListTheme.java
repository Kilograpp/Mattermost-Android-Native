package com.kilogramm.mattermost.model.menu;

import android.databinding.BaseObservable;

/**
 * Created by Evgeny on 24.08.2016.
 */
public class MenuListTheme extends BaseObservable {

    private Integer textColor;
    private Integer backgroundColor;

    public MenuListTheme(Integer textColor, Integer backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public void setTextColor(Integer textColor) {
        this.textColor = textColor;
        notifyChange();
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Integer backgroundColor) {
        this.backgroundColor = backgroundColor;
        notifyChange();
    }
}
