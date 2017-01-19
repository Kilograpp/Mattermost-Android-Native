package com.kilogramm.mattermost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Broadcast;
import com.kilogramm.mattermost.model.entity.BroadcastBilder;
import com.kilogramm.mattermost.model.entity.Data;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.ChatRxFragment;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.tools.NetworkUtil;
import com.kilogramm.mattermost.view.chat.PostViewHolder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.RealmList;
import rx.schedulers.Schedulers;

import static com.kilogramm.mattermost.view.direct.WholeDirectListHolder.getImageUrl;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ManagerBroadcast {
    public static final String TAG = "ObjectUtil";
    public static final String CHANNEL_ID = "sCHANNEL_ID";
    public static final String CHANNEL_NAME = "sCHANNEL_NAME";
    public static final String CHANNEL_TYPE = "CHANNEL_TYPE";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String CLOSE_NOTIFICATION = "CLOSE_NOTIFICATION";

    private static final int NOTIFY_ID = 1;

    private Context mContext;
    private ApiMethod service;

    private Gson gson;

    public ManagerBroadcast(Context mContext) {
        this.mContext = mContext;
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        gson = NetworkUtil.createGson();
    }

    public WebSocketObj parseMessage(String message) {
        try {
            return parseWebSocketObject(message, mContext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private WebSocketObj parseWebSocketObject(String json, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJSON = jsonObject.getJSONObject(WebSocketObj.DATA);
        JSONObject broadcastJSON = null;
        if (jsonObject.has(WebSocketObj.BROADCAST))
            broadcastJSON = jsonObject.getJSONObject(WebSocketObj.BROADCAST);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setDataJSON(jsonObject.getString(WebSocketObj.DATA));
        if (jsonObject.has(WebSocketObj.BROADCAST))
            webSocketObj.setBroadcastJSON(jsonObject.getString(WebSocketObj.BROADCAST));
        else {
            webSocketObj.setBroadcast(null);
        }
        Log.d(TAG, jsonObject.toString());
        webSocketObj = new Gson().fromJson(jsonObject.toString(), WebSocketObj.class);
        if (webSocketObj.getSeqReplay() != null) {
            webSocketObj.setEvent(WebSocketObj.ALL_USER_STATUS);
        }
        String event = webSocketObj.getEvent();
        Data data = null;
        Broadcast broadcast = null;
        switch (event) {
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                data = new WebSocketObj.BuilderData()
                        .setChannelId(dataJSON.getString(WebSocketObj.CHANNEL_ID))
                        .build();
                break;
            case WebSocketObj.EVENT_POSTED:
                String mentions = null;
                if (dataJSON.has(WebSocketObj.MENTIONS))
                    mentions = dataJSON.getString(WebSocketObj.MENTIONS);
                broadcast = new BroadcastBilder()
                        .setChannelId(broadcastJSON.getString(WebSocketObj.CHANNEL_ID))
                        .setTeamId(broadcastJSON.getString(WebSocketObj.USER_ID))
                        .setUserID(broadcastJSON.getString(WebSocketObj.TEAM_ID))
                        .build();
                data = new WebSocketObj.BuilderData()
                        .setChannelDisplayName(dataJSON.getString(WebSocketObj.CHANNEL_DISPLAY_NAME))
                        .setChannelType(dataJSON.getString(WebSocketObj.CHANNEL_TYPE))
                        .setMentions((mentions != null) ? mentions : "")
                        .setSenderName(dataJSON.getString(WebSocketObj.SENDER_NAME))
                        .setTeamId(dataJSON.getString(WebSocketObj.TEAM_ID))
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class))
                        .build();

                Post post = data.getPost();
                getFileInfoAndSavePost(post);

                String MuUserId = MattermostPreference.getInstance().getMyUserId();
                if (data.getMentions().length() > 0
                        && data.getMentions().equals("[\"" + MuUserId + "\"]")
                        && !data.getPost().getChannelId().equals(
                        MattermostPreference.getInstance().getLastChannelId())
                        && !data.getPost().getUserId().equals(MuUserId)) {

                    createNotificationNEW(data.getPost(), context);
                }
                Log.d(TAG, data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_TYPING:
                broadcast = new BroadcastBilder()
                        .setChannelId(broadcastJSON.getString(WebSocketObj.CHANNEL_ID))
                        .setTeamId(broadcastJSON.getString(WebSocketObj.TEAM_ID))
                        .setUserID(broadcastJSON.getString(WebSocketObj.USER_ID))
                        .build();
                data = new WebSocketObj.BuilderData()
                        .setParentId(dataJSON.getString(WebSocketObj.PARENT_ID))
                        .setTeamId(webSocketObj.getTeamId())
                        .setUser(dataJSON.getString(WebSocketObj.USER_ID))
                        .build();
                break;
            case WebSocketObj.EVENT_USER_ADDED:
                ExtroInfoRepository.updateAddUser(jsonObject.getString(WebSocketObj.CHANNEL_ID),
                        jsonObject.getString(WebSocketObj.USER_ID));
                break;
            case WebSocketObj.EVENT_USER_REMOVE:
                ExtroInfoRepository.updateRemoveUser(jsonObject.getString(WebSocketObj.CHANNEL_ID),
                        jsonObject.getString(WebSocketObj.USER_ID));
                break;
            case WebSocketObj.EVENT_NEW_USER:
                String ids = jsonObject.getString(WebSocketObj.USER_ID);
                service.getTeamUsers(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(stringUserMap ->
                                UserRepository.add(stringUserMap.get(ids)));
                break;
            case WebSocketObj.EVENT_CHANNEL_DELETED:
                ChannelRepository.remove(new ChannelRepository.ChannelByIdSpecification(
                        jsonObject.getString(WebSocketObj.CHANNEL_ID)));
                ExtroInfoRepository.remove(new ExtroInfoRepository.ExtroInfoByIdSpecification(
                        jsonObject.getString(WebSocketObj.CHANNEL_ID)));
                break;
            case WebSocketObj.EVENT_DIRECT_ADDED:
                ServerMethod.getInstance()
                        .extraInfo(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(channelsWithMembers -> {
                            RealmList<Channel> channelsList = new RealmList<>();
                            channelsList.addAll(channelsWithMembers.getChannels());
                            ChannelRepository.prepareDirectAndChannelAdd(channelsWithMembers.getChannels());
                            MembersRepository.update(channelsWithMembers.getMembers());
                        });
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                data = getPost(dataJSON);
                UserRepository.updateUserMessage(data.getPost().getId(), data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                data = getPost(dataJSON);
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                data = getStatus(dataJSON);
                break;
            case WebSocketObj.ALL_USER_STATUS:
                data = getMapStatus(dataJSON);
                break;
            case WebSocketObj.EVENT_PREFERENCE_CHANGED:
                data = getPreferenceData(dataJSON);
        }
        webSocketObj.setData(data);
        webSocketObj.setBroadcast(broadcast);
        return webSocketObj;
    }

    private void getFileInfoAndSavePost(Post post){
        ServerMethod.getInstance().getFileInfo(MattermostPreference.getInstance().getTeamId(),
                post.getChannelId(), post.getId())
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                    savePost(post);
                })
                .subscribe(fileInfos -> {
                    for (FileInfo fileInfo : fileInfos) {
                        Log.d(TAG, "onNext: " + fileInfo.getId());
                        FileInfoRepository.getInstance().add(fileInfo);
                    }
                    savePost(post);
                });
    }

    private Data getPreferenceData(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setPreference((new Gson()).fromJson(dataJSON.getString(WebSocketObj.PREFERENCES), Preferences.class))
                .build();
    }

    private Data getMapStatus(JSONObject dataJSON) {
        return new WebSocketObj.BuilderData()
                .setMapUserStatus((new Gson()).fromJson(dataJSON.toString(), new TypeToken<HashMap<String, Object>>() {
                }.getType()))
                .build();
    }

    private Data getStatus(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setStatus(dataJSON.getString(WebSocketObj.STATUS))
                .build();
    }

    private Data getPost(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class))
                .build();
    }

    private Data getUser(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setUser(dataJSON.getString(WebSocketObj.USER_ID))
                .build();
    }

    private void createNotificationNEW(Post post, Context context) {
        if (ChatRxFragment.active && MattermostPreference.getInstance().getLastChannelId()
                .equals(post.getChannelId())) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Channel channel = ChannelRepository.query(
                new ChannelRepository.ChannelByIdSpecification(post.getChannelId())).first();

        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                openDialogIntent(context, channel), PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 0,
                closeNotificationIntent(context, post.getChannelId().hashCode()), 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        remoteViews.setTextViewText(R.id.title, setNotificationTitle(channel, post.getUserId()));
        remoteViews.setTextViewText(R.id.text, displayedMessage(post, context));
        remoteViews.setImageViewResource(R.id.closeNotification, R.drawable.ic_close_notification);
        remoteViews.setOnClickPendingIntent(R.id.closeNotification, pendingIntentClose);

        Notification.Builder notificationBuilder = showNotification(context);
        notificationBuilder.setContent(remoteViews);
        notificationBuilder.setContentIntent(pIntent);
        notificationBuilder.build().flags = Notification.FLAG_AUTO_CANCEL;

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() ->
                Picasso.with(context.getApplicationContext())
                        .load(getImageUrl(post.getUserId()))
                        .transform(new RoundTransformation(90, 0))
                        .into(remoteViews, R.id.avatar, post.getChannelId().hashCode(), notificationBuilder.build()));

        notificationManager.notify(post.getChannelId().hashCode(), notificationBuilder.build());
    }

    private static Notification.Builder showNotification(Context context) {
        return new Notification.Builder(context)
                .setSmallIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                        R.mipmap.icon : R.drawable.ic_mm)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);
    }

    private static String setNotificationTitle(Channel channel, String userId) {
        String userName = UserRepository.query(new UserRepository.UserByIdSpecification(userId))
                .first()
                .getUsername();

        if (channel.getType().equals(Channel.DIRECT)) {
            return "Direct message from " + channel.getUsername();
        } else {
            return userName + " in " + channel.getDisplayName();
        }
    }

    private static CharSequence displayedMessage(Post post, Context context) {
        if (post.getProps() != null && post.getProps().getAttachments() != null) {
            return context.getResources().getString(R.string.notification_sent_attachment);
        } else {
            if (post.getFilenames().size() != 0) {
                String fileType = FileUtil.getInstance().getMimeType(post.getFilenames().get(0));
                if (fileType.contains("image")) {
                    return context.getResources().getString(R.string.notification_sent_pic);
                } else {
                    return context.getResources().getString(R.string.notification_sent_file);
                }
            } else {
                return PostViewHolder.getMarkdownPost(post.getMessage(), context);
            }
        }
    }

    private static Intent openDialogIntent(Context context, Channel channel) {
        Intent intent = new Intent(context, GeneralRxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CHANNEL_ID, channel.getId());
        if (Objects.equals(channel.getType(), Channel.DIRECT)) {
            intent.putExtra(CHANNEL_NAME, channel.getUsername());
        } else {
            intent.putExtra(CHANNEL_NAME, channel.getDisplayName());
        }
        intent.putExtra(CHANNEL_TYPE, channel.getType());
        return intent;
    }

    private static Intent closeNotificationIntent(Context context, int id) {
        Intent intent = new Intent(context.getApplicationContext(), CloseButtonReceiver.class);
        intent.putExtra(NOTIFICATION_ID, NOTIFY_ID);
        intent.setAction(CLOSE_NOTIFICATION);
        return intent;
    }

    private static void savePost(Post post) {
        String pendingPostId = post.getPendingPostId();
        Log.d(TAG, "savePost() called with: post = [" + pendingPostId + "]");
        if (PostRepository.query(pendingPostId) != null) {
            Log.d(TAG, "savePost: merge from ws");
            PostRepository.merge(post);
        } else {
            Log.d(TAG, "savePost: add new from ws");
            PostRepository.prepareAndAddPost(post);
        }
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static class CloseButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.cancel(notificationId);
            manager.cancelAll();
        }
    }

    private static class RoundTransformation implements com.squareup.picasso.Transformation {

        private final int radius;
        private final int margin;

        public RoundTransformation(final int radius, final int margin) {
            this.radius = radius;
            this.margin = margin;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

            if (source != output) {
                source.recycle();
            }
            return output;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }
}