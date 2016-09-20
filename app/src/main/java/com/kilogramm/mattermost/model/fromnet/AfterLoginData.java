package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.user.User;

import io.realm.RealmList;

/**
 * Created by Evgeny on 28.07.2016.
 */
public class AfterLoginData {

    @SerializedName("client_cfg")
    private ClientCfg clientCfg;
    @SerializedName("direct_profiles")
    private RealmList<User> directProfiles;
    @SerializedName("teams")
    private RealmList<Team> teams;



}
