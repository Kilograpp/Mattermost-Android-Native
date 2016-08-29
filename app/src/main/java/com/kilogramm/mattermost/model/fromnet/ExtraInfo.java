package com.kilogramm.mattermost.model.fromnet;

import com.kilogramm.mattermost.model.entity.User;

import java.util.List;

/**
 * Created by Evgeny on 22.08.2016.
 */
public class ExtraInfo {

    private List<User> members;

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
