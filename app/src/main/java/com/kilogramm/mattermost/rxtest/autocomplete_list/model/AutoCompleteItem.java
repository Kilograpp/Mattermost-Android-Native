package com.kilogramm.mattermost.rxtest.autocomplete_list.model;

import com.kilogramm.mattermost.model.entity.user.User;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class AutoCompleteItem extends AutoCompleteListItem {

    private User user;

    @Override
    public int getType() {
        return TYPE_ITEM;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
