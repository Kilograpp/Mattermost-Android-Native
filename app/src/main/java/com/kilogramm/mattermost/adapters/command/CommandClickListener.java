package com.kilogramm.mattermost.adapters.command;

import com.kilogramm.mattermost.model.entity.CommandObject;

/**
 * Created by Evgeny on 12.12.2016.
 */

public interface CommandClickListener {
    void onCommandClick(CommandObject command);
}
