package com.kilogramm.mattermost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.Props;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ManagerBroadcast {

    public static final String TAG = "ObjectUtil";

    public Context mContext;

    public ManagerBroadcast(Context mContext) {
        this.mContext = mContext;
    }

    public WebSocketObj praseMessage(String message){
        try {
            return parseWebSocketObject(message, mContext);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private WebSocketObj parseWebSocketObject(String json, Context context) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(json);
        JSONObject propsJSON = jsonObject.getJSONObject(WebSocketObj.PROPS);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setTeamId(jsonObject.getString(WebSocketObj.TEAM_ID));
        webSocketObj.setUserId(jsonObject.getString(WebSocketObj.USER_ID));
        webSocketObj.setChannelId(jsonObject.getString(WebSocketObj.CHANNEL_ID));
        webSocketObj.setPropsJSON(jsonObject.getString(WebSocketObj.PROPS));
        webSocketObj.setAction(jsonObject.getString(WebSocketObj.ACTION));
        String action = webSocketObj.getAction();
        Props props = null;
        switch (action){
            case WebSocketObj.ACTION_CHANNEL_VIEWED:
                break;
            case WebSocketObj.ACTION_POSTED:
                props = new WebSocketObj.BuilderProps()
                        .setChannelDisplayName(propsJSON.getString(WebSocketObj.CHANNEL_DISPLAY_NAME))
                        .setChannelType(propsJSON.getString(WebSocketObj.CHANNEL_TYPE))
                        .setMentions(propsJSON.getString(WebSocketObj.MENTIONS))
                        .setSenderName(propsJSON.getString(WebSocketObj.SENDER_NAME))
                        .setTeamId(propsJSON.getString(WebSocketObj.TEAM_ID))
                        .setPost(gson.fromJson(propsJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();

                savePost(props.getPost());
                if(!props.getPost().getUserId().equals(MattermostPreference.getInstance().getMyUserId())){
                    createNotification(props.getPost(), context);
                }
                Log.d(TAG, props.getPost().getMessage());
                break;
            case WebSocketObj.ACTION_TYPING:
                props = new WebSocketObj.BuilderProps()
                        .setParentId(propsJSON.getString(WebSocketObj.PARENT_ID))
                        .setTeamId(webSocketObj.getTeamId())
                        .build();
                break;
            case WebSocketObj.ACTION_POST_EDITED:
                props = new WebSocketObj.BuilderProps()
                        .setPost(gson.fromJson(propsJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();
                break;
            case WebSocketObj.ACTION_POST_DELETED:
                props = new WebSocketObj.BuilderProps()
                        .setPost(gson.fromJson(propsJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();
                break;
        }
        webSocketObj.setProps(props);
        return webSocketObj;
    }


    private static void createNotification(Post post, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("New message from " + post.getUser().getUsername())
                .setContentText(post.getMessage())
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }


    public static void savePost(Post post){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(post);
        realm.commitTransaction();
        realm.close();
    }

}
