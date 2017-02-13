package com.kilogramm.mattermost.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyProps;
import com.kilogramm.mattermost.model.entity.user.User;

/**
 * Created by melkshake on 09.02.17.
 */

public class ResponsedUser implements Parcelable {

    public static final Long SYSTEM_AT_DATA = 0L;

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("create_at")
    @Expose
    private Long createAt;
    @SerializedName("update_at")
    @Expose
    private Long updateAt;
    @SerializedName("delete_at")
    @Expose
    private Long deleteAt;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("auth_data")
    @Expose
    private String authData;
    @SerializedName("auth_service")
    @Expose
    private String authService;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("email_verified")
    @Expose
    private Boolean emailVerified;
    @SerializedName("nickname")
    @Expose
    private String nickname;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("roles")
    @Expose
    private String roles;
    @SerializedName("last_activity_at")
    @Expose
    private Long lastActivityAt;
    @SerializedName("last_ping_at")
    @Expose
    private Long lastPingAt;
    @SerializedName("allow_marketing")
    @Expose
    private Boolean allowMarketing;
    @SerializedName("notify_props")
    @Expose
    private NotifyProps notifyProps;
    @SerializedName("theme_props")
    @Expose
    private ThemeProps themeProps;
    @SerializedName("last_password_update")
    @Expose
    private Long lastPasswordUpdate;
    @SerializedName("last_picture_update")
    @Expose
    private Long lastPictureUpdate;
    @SerializedName("locale")
    @Expose
    private String locale;

    private String status = "offline";

    public ResponsedUser() {
    }

    public ResponsedUser(User user) {
        this.id = user.getId();
        this.createAt = user.getCreateAt();
        this.updateAt = user.getUpdateAt();
        this.deleteAt = user.getDeleteAt();
        this.username = user.getUsername();
        this.authData = user.getAuthData();
        this.authService = user.getAuthService();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles();
        this.lastActivityAt = user.getLastActivityAt();
        this.lastPingAt = user.getLastPingAt();
        this.allowMarketing = user.isAllowMarketing();
        this.notifyProps = user.getNotifyProps();
        this.themeProps = user.getThemeProps();
        this.lastPasswordUpdate = user.getLastPasswordUpdate();
        this.lastPictureUpdate = user.getLastPictureUpdate();
        this.locale = user.getLocale();
        this.status = user.getStatus();
    }

    public ResponsedUser(String id, String username, String firstName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.deleteAt = SYSTEM_AT_DATA;
    }

    public ResponsedUser(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    public Long getDeleteAt() {
        return deleteAt;
    }

    public void setDeleteAt(Long deleteAt) {
        this.deleteAt = deleteAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthData() {
        return authData;
    }

    public void setAuthData(String authData) {
        this.authData = authData;
    }

    public String getAuthService() {
        return authService;
    }

    public void setAuthService(String authService) {
        this.authService = authService;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Long getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Long getLastPingAt() {
        return lastPingAt;
    }

    public void setLastPingAt(Long lastPingAt) {
        this.lastPingAt = lastPingAt;
    }

    public Boolean isAllowMarketing() {
        return allowMarketing;
    }

    public void setAllowMarketing(boolean allowMarketing) {
        this.allowMarketing = allowMarketing;
    }

    public NotifyProps getNotifyProps() {
        return notifyProps;
    }

    public void setNotifyProps(NotifyProps notifyProps) {
        this.notifyProps = notifyProps;
    }

    public ThemeProps getThemeProps() {
        return themeProps;
    }

    public void setThemeProps(ThemeProps themeProps) {
        this.themeProps = themeProps;
    }

    public Long getLastPasswordUpdate() {
        return lastPasswordUpdate;
    }

    public void setLastPasswordUpdate(Long lastPasswordUpdate) {
        this.lastPasswordUpdate = lastPasswordUpdate;
    }

    public Long getLastPictureUpdate() {
        return lastPictureUpdate;
    }

    public void setLastPictureUpdate(Long lastPictureUpdate) {
        this.lastPictureUpdate = lastPictureUpdate;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.createAt);
        dest.writeValue(this.updateAt);
        dest.writeValue(this.deleteAt);
        dest.writeString(this.username);
        dest.writeString(this.authData);
        dest.writeString(this.authService);
        dest.writeString(this.email);
        dest.writeValue(this.emailVerified);
        dest.writeString(this.nickname);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.roles);
        dest.writeValue(this.lastActivityAt);
        dest.writeValue(this.lastPingAt);
        dest.writeValue(this.allowMarketing);
        dest.writeParcelable(this.notifyProps, flags);
        dest.writeParcelable(this.themeProps, flags);
        dest.writeValue(this.lastPasswordUpdate);
        dest.writeValue(this.lastPictureUpdate);
        dest.writeString(this.locale);
        dest.writeString(this.status);
    }

    protected ResponsedUser(Parcel in) {
        this.id = in.readString();
        this.createAt = (Long) in.readValue(Long.class.getClassLoader());
        this.updateAt = (Long) in.readValue(Long.class.getClassLoader());
        this.deleteAt = (Long) in.readValue(Long.class.getClassLoader());
        this.username = in.readString();
        this.authData = in.readString();
        this.authService = in.readString();
        this.email = in.readString();
        this.emailVerified = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.nickname = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.roles = in.readString();
        this.lastActivityAt = (Long) in.readValue(Long.class.getClassLoader());
        this.lastPingAt = (Long) in.readValue(Long.class.getClassLoader());
        this.allowMarketing = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.notifyProps = in.readParcelable(NotifyProps.class.getClassLoader());
        this.themeProps = in.readParcelable(ThemeProps.class.getClassLoader());
        this.lastPasswordUpdate = (Long) in.readValue(Long.class.getClassLoader());
        this.lastPictureUpdate = (Long) in.readValue(Long.class.getClassLoader());
        this.locale = in.readString();
        this.status = in.readString();
    }

    public static final Parcelable.Creator<ResponsedUser> CREATOR = new Parcelable.Creator<ResponsedUser>() {
        @Override
        public ResponsedUser createFromParcel(Parcel source) {
            return new ResponsedUser(source);
        }

        @Override
        public ResponsedUser[] newArray(int size) {
            return new ResponsedUser[size];
        }
    };
}
