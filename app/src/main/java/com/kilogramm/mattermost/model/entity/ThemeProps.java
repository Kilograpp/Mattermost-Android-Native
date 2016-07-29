package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class ThemeProps extends RealmObject implements Parcelable {

    @PrimaryKey
    private long id;
    @SerializedName("awayIndicator")
    @Expose
    private String awayIndicator;
    @SerializedName("buttonBg")
    @Expose
    private String buttonBg;
    @SerializedName("buttonColor")
    @Expose
    private String buttonColor;
    @SerializedName("centerChannelBg")
    @Expose
    private String centerChannelBg;
    @SerializedName("centerChannelColor")
    @Expose
    private String centerChannelColor;
    @SerializedName("codeTheme")
    @Expose
    private String codeTheme;
    @SerializedName("linkColor")
    @Expose
    private String linkColor;
    @SerializedName("mentionBj")
    @Expose
    private String mentionBj;
    @SerializedName("mentionColor")
    @Expose
    private String mentionColor;
    @SerializedName("mentionHighlightBg")
    @Expose
    private String mentionHighlightBg;
    @SerializedName("mentionHighlightLink")
    @Expose
    private String mentionHighlightLink;
    @SerializedName("newMessageSeparator")
    @Expose
    private String newMessageSeparator;
    @SerializedName("onlineIndicator")
    @Expose
    private String onlineIndicator;
    @SerializedName("sidebarBg")
    @Expose
    private String sidebarBg;
    @SerializedName("sidebarHeaderBg")
    @Expose
    private String sidebarHeaderBg;
    @SerializedName("sidebarHeaderTextColor")
    @Expose
    private String sidebarHeaderTextColor;
    @SerializedName("sidebarText")
    @Expose
    private String sidebarText;
    @SerializedName("sidebarTextActiveBorder")
    @Expose
    private String sidebarTextActiveBorder;
    @SerializedName("sidebarTextActiveColor")
    @Expose
    private String sidebarTextActiveColor;
    @SerializedName("sidebarTextHoverBg")
    @Expose
    private String sidebarTextHoverBg;
    @SerializedName("sidebarUnreadText")
    @Expose
    private String sidebarUnreadText;
    @SerializedName("type")
    @Expose
    private String type;

    /**
     *
     * @return
     * The awayIndicator
     */
    public String getAwayIndicator() {
        return awayIndicator;
    }

    /**
     *
     * @param awayIndicator
     * The awayIndicator
     */
    public void setAwayIndicator(String awayIndicator) {
        this.awayIndicator = awayIndicator;
    }

    /**
     *
     * @return
     * The buttonBg
     */
    public String getButtonBg() {
        return buttonBg;
    }

    /**
     *
     * @param buttonBg
     * The buttonBg
     */
    public void setButtonBg(String buttonBg) {
        this.buttonBg = buttonBg;
    }

    /**
     *
     * @return
     * The buttonColor
     */
    public String getButtonColor() {
        return buttonColor;
    }

    /**
     *
     * @param buttonColor
     * The buttonColor
     */
    public void setButtonColor(String buttonColor) {
        this.buttonColor = buttonColor;
    }

    /**
     *
     * @return
     * The centerChannelBg
     */
    public String getCenterChannelBg() {
        return centerChannelBg;
    }

    /**
     *
     * @param centerChannelBg
     * The centerChannelBg
     */
    public void setCenterChannelBg(String centerChannelBg) {
        this.centerChannelBg = centerChannelBg;
    }

    /**
     *
     * @return
     * The centerChannelColor
     */
    public String getCenterChannelColor() {
        return centerChannelColor;
    }

    /**
     *
     * @param centerChannelColor
     * The centerChannelColor
     */
    public void setCenterChannelColor(String centerChannelColor) {
        this.centerChannelColor = centerChannelColor;
    }

    /**
     *
     * @return
     * The codeTheme
     */
    public String getCodeTheme() {
        return codeTheme;
    }

    /**
     *
     * @param codeTheme
     * The codeTheme
     */
    public void setCodeTheme(String codeTheme) {
        this.codeTheme = codeTheme;
    }

    /**
     *
     * @return
     * The linkColor
     */
    public String getLinkColor() {
        return linkColor;
    }

    /**
     *
     * @param linkColor
     * The linkColor
     */
    public void setLinkColor(String linkColor) {
        this.linkColor = linkColor;
    }

    /**
     *
     * @return
     * The mentionBj
     */
    public String getMentionBj() {
        return mentionBj;
    }

    /**
     *
     * @param mentionBj
     * The mentionBj
     */
    public void setMentionBj(String mentionBj) {
        this.mentionBj = mentionBj;
    }

    /**
     *
     * @return
     * The mentionColor
     */
    public String getMentionColor() {
        return mentionColor;
    }

    /**
     *
     * @param mentionColor
     * The mentionColor
     */
    public void setMentionColor(String mentionColor) {
        this.mentionColor = mentionColor;
    }

    /**
     *
     * @return
     * The mentionHighlightBg
     */
    public String getMentionHighlightBg() {
        return mentionHighlightBg;
    }

    /**
     *
     * @param mentionHighlightBg
     * The mentionHighlightBg
     */
    public void setMentionHighlightBg(String mentionHighlightBg) {
        this.mentionHighlightBg = mentionHighlightBg;
    }

    /**
     *
     * @return
     * The mentionHighlightLink
     */
    public String getMentionHighlightLink() {
        return mentionHighlightLink;
    }

    /**
     *
     * @param mentionHighlightLink
     * The mentionHighlightLink
     */
    public void setMentionHighlightLink(String mentionHighlightLink) {
        this.mentionHighlightLink = mentionHighlightLink;
    }

    /**
     *
     * @return
     * The newMessageSeparator
     */
    public String getNewMessageSeparator() {
        return newMessageSeparator;
    }

    /**
     *
     * @param newMessageSeparator
     * The newMessageSeparator
     */
    public void setNewMessageSeparator(String newMessageSeparator) {
        this.newMessageSeparator = newMessageSeparator;
    }

    /**
     *
     * @return
     * The onlineIndicator
     */
    public String getOnlineIndicator() {
        return onlineIndicator;
    }

    /**
     *
     * @param onlineIndicator
     * The onlineIndicator
     */
    public void setOnlineIndicator(String onlineIndicator) {
        this.onlineIndicator = onlineIndicator;
    }

    /**
     *
     * @return
     * The sidebarBg
     */
    public String getSidebarBg() {
        return sidebarBg;
    }

    /**
     *
     * @param sidebarBg
     * The sidebarBg
     */
    public void setSidebarBg(String sidebarBg) {
        this.sidebarBg = sidebarBg;
    }

    /**
     *
     * @return
     * The sidebarHeaderBg
     */
    public String getSidebarHeaderBg() {
        return sidebarHeaderBg;
    }

    /**
     *
     * @param sidebarHeaderBg
     * The sidebarHeaderBg
     */
    public void setSidebarHeaderBg(String sidebarHeaderBg) {
        this.sidebarHeaderBg = sidebarHeaderBg;
    }

    /**
     *
     * @return
     * The sidebarHeaderTextColor
     */
    public String getSidebarHeaderTextColor() {
        return sidebarHeaderTextColor;
    }

    /**
     *
     * @param sidebarHeaderTextColor
     * The sidebarHeaderTextColor
     */
    public void setSidebarHeaderTextColor(String sidebarHeaderTextColor) {
        this.sidebarHeaderTextColor = sidebarHeaderTextColor;
    }

    /**
     *
     * @return
     * The sidebarText
     */
    public String getSidebarText() {
        return sidebarText;
    }

    /**
     *
     * @param sidebarText
     * The sidebarText
     */
    public void setSidebarText(String sidebarText) {
        this.sidebarText = sidebarText;
    }

    /**
     *
     * @return
     * The sidebarTextActiveBorder
     */
    public String getSidebarTextActiveBorder() {
        return sidebarTextActiveBorder;
    }

    /**
     *
     * @param sidebarTextActiveBorder
     * The sidebarTextActiveBorder
     */
    public void setSidebarTextActiveBorder(String sidebarTextActiveBorder) {
        this.sidebarTextActiveBorder = sidebarTextActiveBorder;
    }

    /**
     *
     * @return
     * The sidebarTextActiveColor
     */
    public String getSidebarTextActiveColor() {
        return sidebarTextActiveColor;
    }

    /**
     *
     * @param sidebarTextActiveColor
     * The sidebarTextActiveColor
     */
    public void setSidebarTextActiveColor(String sidebarTextActiveColor) {
        this.sidebarTextActiveColor = sidebarTextActiveColor;
    }

    /**
     *
     * @return
     * The sidebarTextHoverBg
     */
    public String getSidebarTextHoverBg() {
        return sidebarTextHoverBg;
    }

    /**
     *
     * @param sidebarTextHoverBg
     * The sidebarTextHoverBg
     */
    public void setSidebarTextHoverBg(String sidebarTextHoverBg) {
        this.sidebarTextHoverBg = sidebarTextHoverBg;
    }

    /**
     *
     * @return
     * The sidebarUnreadText
     */
    public String getSidebarUnreadText() {
        return sidebarUnreadText;
    }

    /**
     *
     * @param sidebarUnreadText
     * The sidebarUnreadText
     */
    public void setSidebarUnreadText(String sidebarUnreadText) {
        this.sidebarUnreadText = sidebarUnreadText;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.awayIndicator);
        dest.writeString(this.buttonBg);
        dest.writeString(this.buttonColor);
        dest.writeString(this.centerChannelBg);
        dest.writeString(this.centerChannelColor);
        dest.writeString(this.codeTheme);
        dest.writeString(this.linkColor);
        dest.writeString(this.mentionBj);
        dest.writeString(this.mentionColor);
        dest.writeString(this.mentionHighlightBg);
        dest.writeString(this.mentionHighlightLink);
        dest.writeString(this.newMessageSeparator);
        dest.writeString(this.onlineIndicator);
        dest.writeString(this.sidebarBg);
        dest.writeString(this.sidebarHeaderBg);
        dest.writeString(this.sidebarHeaderTextColor);
        dest.writeString(this.sidebarText);
        dest.writeString(this.sidebarTextActiveBorder);
        dest.writeString(this.sidebarTextActiveColor);
        dest.writeString(this.sidebarTextHoverBg);
        dest.writeString(this.sidebarUnreadText);
        dest.writeString(this.type);
    }

    public ThemeProps() {
    }

    protected ThemeProps(Parcel in) {
        this.awayIndicator = in.readString();
        this.buttonBg = in.readString();
        this.buttonColor = in.readString();
        this.centerChannelBg = in.readString();
        this.centerChannelColor = in.readString();
        this.codeTheme = in.readString();
        this.linkColor = in.readString();
        this.mentionBj = in.readString();
        this.mentionColor = in.readString();
        this.mentionHighlightBg = in.readString();
        this.mentionHighlightLink = in.readString();
        this.newMessageSeparator = in.readString();
        this.onlineIndicator = in.readString();
        this.sidebarBg = in.readString();
        this.sidebarHeaderBg = in.readString();
        this.sidebarHeaderTextColor = in.readString();
        this.sidebarText = in.readString();
        this.sidebarTextActiveBorder = in.readString();
        this.sidebarTextActiveColor = in.readString();
        this.sidebarTextHoverBg = in.readString();
        this.sidebarUnreadText = in.readString();
        this.type = in.readString();
    }

    public static final Parcelable.Creator<ThemeProps> CREATOR = new Parcelable.Creator<ThemeProps>() {
        @Override
        public ThemeProps createFromParcel(Parcel source) {
            return new ThemeProps(source);
        }

        @Override
        public ThemeProps[] newArray(int size) {
            return new ThemeProps[size];
        }
    };
}