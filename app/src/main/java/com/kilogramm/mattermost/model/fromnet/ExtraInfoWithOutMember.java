package com.kilogramm.mattermost.model.fromnet;

import com.kilogramm.mattermost.model.entity.user.User;

import java.util.Map;

/**
 * Created by Evgeny on 09.01.2017.
 */
public class ExtraInfoWithOutMember {
    private ExtraInfo extraInfo;
    private Map<String, User> members;

    public ExtraInfoWithOutMember(ExtraInfo extraInfo, Map<String, User> members) {
        this.extraInfo = extraInfo;
        this.members = members;
    }

    public ExtraInfo getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(ExtraInfo extraInfo) {
        this.extraInfo = extraInfo;
    }

    public Map<String, User> getMembers() {
        return members;
    }

    public void setMembers(Map<String, User> members) {
        this.members = members;
    }
}
