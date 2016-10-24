package com.kilogramm.mattermost.service;

import android.util.Log;

import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.userstatus.AllRemove;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;

/**
 * Created by Evgeny on 23.09.2016.
 */
public class MattermostNotificationManager {

    private static final String TAG = "Websocket";

    private MattermostService service;


    public MattermostNotificationManager(MattermostService service) {
        this.service = service;
    }

    public void handleSocket(WebSocketObj webSocketObj){
        switch (webSocketObj.getEvent()){
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                PostRepository.remove(webSocketObj.getData().getPost());
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                break;
            case WebSocketObj.EVENT_POSTED:
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                Log.d(TAG, "EVENT_STATUS_CHANGE: useid = "+ webSocketObj.getUserId() + "\n" +
                        "status = " + webSocketObj.getData().getStatus());
                UserStatusRepository.add(new UserStatus(webSocketObj.getUserId(), webSocketObj.getData().getStatus()));
                //userRepository.updateUserStatus(webSocketObj.getUserId(), webSocketObj.getData().getStatus());
                break;
            case WebSocketObj.EVENT_TYPING:
                break;
            case WebSocketObj.ALL_USER_STATUS:
                UserStatusRepository.remove(new AllRemove());
                for (String s : webSocketObj.getData().getStatusMap().keySet()) {
                    Log.d(TAG, "EVENT_ALL_USER_STATUS: useid = "+ s + "\n" +
                            "status = " + webSocketObj.getData().getStatusMap().get(s));
                    UserStatusRepository.add(new UserStatus(s,webSocketObj.getData().getStatusMap().get(s)));
                    //userRepository.updateUserStatus(s,webSocketObj.getData().getStatusMap().get(s));
                }
        }
    }
}
