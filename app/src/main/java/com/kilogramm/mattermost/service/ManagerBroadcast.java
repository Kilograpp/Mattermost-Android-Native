package com.kilogramm.mattermost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Data;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ManagerBroadcast {

    public static final String TAG = "ObjectUtil";

    public Context mContext;

    private UserRepository userRepository;

    private Gson gson;

    public ManagerBroadcast(Context mContext) {
        this.mContext = mContext;
        this.userRepository = new UserRepository();
        gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {}.getType(), new TypeAdapter<RealmList<RealmString>>() {

                    @Override
                    public void write(JsonWriter out, RealmList<RealmString> value) throws IOException {
                        out.beginArray();
                        for (RealmString realmString : value) {
                            out.value(realmString.getString());
                        }
                        out.endArray();
                    }

                    @Override
                    public RealmList<RealmString> read(JsonReader in) throws IOException {
                        RealmList<RealmString> list = new RealmList<>();
                        in.beginArray();
                        while (in.hasNext()) {
                            list.add(new RealmString(in.nextString()));
                        }
                        in.endArray();
                        return list;
                    }
                })
                .setLenient()
                .create();
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
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJSON = jsonObject.getJSONObject(WebSocketObj.DATA);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setDataJSON(jsonObject.getString(WebSocketObj.DATA));
        if(jsonObject.has(WebSocketObj.TEAM_ID)){
            webSocketObj.setTeamId(jsonObject.getString(WebSocketObj.TEAM_ID));
        }
        if(jsonObject.has(WebSocketObj.USER_ID)){
            webSocketObj.setUserId(jsonObject.getString(WebSocketObj.USER_ID));
        }
        if(jsonObject.has(WebSocketObj.CHANNEL_ID)){
            webSocketObj.setChannelId(jsonObject.getString(WebSocketObj.CHANNEL_ID));
        }
        if(jsonObject.has(WebSocketObj.EVENT)){
            webSocketObj.setEvent(jsonObject.getString(WebSocketObj.EVENT));
        }
        if(jsonObject.has(WebSocketObj.SEQ_REPLAY)){
            webSocketObj.setSeqReplay(jsonObject.getInt(WebSocketObj.SEQ_REPLAY));
        }
        if(webSocketObj.getSeqReplay()!=null){
            webSocketObj.setEvent(WebSocketObj.ALL_USER_STATUS);
        }
        String event = webSocketObj.getEvent();
        Data data = null;
        switch (event){
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                break;
            case WebSocketObj.EVENT_POSTED:
                String mentions = null;
                if(dataJSON.has(WebSocketObj.MENTIONS))
                    mentions = dataJSON.getString(WebSocketObj.MENTIONS);
                data = new WebSocketObj.BuilderData()
                                .setChannelDisplayName(dataJSON.getString(WebSocketObj.CHANNEL_DISPLAY_NAME))
                                .setChannelType(dataJSON.getString(WebSocketObj.CHANNEL_TYPE))
                                .setMentions((mentions!=null)
                                        ?mentions
                                        :"")
                        .setSenderName(dataJSON.getString(WebSocketObj.SENDER_NAME))
                        .setTeamId(dataJSON.getString(WebSocketObj.TEAM_ID))
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();

                savePost(data.getPost());
                if(!data.getPost().getUserId().equals(MattermostPreference.getInstance().getMyUserId())){
                    createNotification(data.getPost(), context);
                }
                Log.d(TAG, data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_TYPING:
                data = new WebSocketObj.BuilderData()
                        .setParentId(dataJSON.getString(WebSocketObj.PARENT_ID))
                        .setTeamId(webSocketObj.getTeamId())
                        .build();
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                data = new WebSocketObj.BuilderData()
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();
                userRepository.updateUserMessage(data.getPost().getId(), data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                data = new WebSocketObj.BuilderData()
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class),
                                webSocketObj.getUserId())
                        .build();
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                data = new WebSocketObj.BuilderData()
                        .setStatus(dataJSON.getString(WebSocketObj.STATUS))
                        .build();
                break;
            case WebSocketObj.ALL_USER_STATUS:
                data = new WebSocketObj.BuilderData()
                        .setMapUserStatus((new Gson()).fromJson(dataJSON.toString(),  new TypeToken<HashMap<String, Object>>() {}.getType()))
                        .build();
                break;
        }
        webSocketObj.setData(data);
        return webSocketObj;
    }


    private static void createNotification(Post post, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("New message from " + post.getUser().getUsername())
                .setContentText(post.getMessage())
                .setSmallIcon(R.mipmap.icon);
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

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
