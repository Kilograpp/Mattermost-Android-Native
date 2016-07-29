package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 26.07.2016.
 */
public class LoginData {
    @SerializedName("login_id")
    private String login;
    @SerializedName("password")
    private String password;
    @SerializedName("token")
    private String token;

    public LoginData(String login, String password, String token) {
        this.login = login;
        this.password = password;
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
