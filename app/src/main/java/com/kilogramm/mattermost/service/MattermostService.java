package com.kilogramm.mattermost.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.service.websocket.WebSocketManager;

import org.json.JSONException;

/**
 * Created by kraftu on 16.09.16.
 */
public class MattermostService extends Service implements WebSocketManager.WebSocketMessage {

    public static String SERVICE_ACTION_START_WEB_SOCKET = "ru.com.kilogramm.mattermost.SERVICE_ACTION_START_WEB_SOCKET";
    public static String UPDATE_USER_STATUS = "ru.com.kilogramm.mattermost.SERVICE_ACTION_START_WEB_SOCKET.UPDATE_USER_STATUS";

    private static String TAG = "MattermostService";

    private WebSocketManager mWebSocketManager;

    private ManagerBroadcast managerBroadcast;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        mWebSocketManager = new WebSocketManager(this);
        managerBroadcast = new ManagerBroadcast(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWebSocketManager!=null) mWebSocketManager.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) return Service.START_STICKY;
        Log.d(TAG,"onStartCommand action:"+intent.getAction());
        if(SERVICE_ACTION_START_WEB_SOCKET.equals(intent.getAction())){
            mWebSocketManager.start();
        }

        if(UPDATE_USER_STATUS.equals(intent.getAction())){
            mWebSocketManager.updateUserStatusNow();
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void receiveMessage(String message) {
        managerBroadcast.praseMessage(message);
    }

    public static class Helper{

        private Context mContext;

        public Helper(Context mContext) {
            this.mContext = mContext;
        }

        public static Helper create(Context c){
            return new Helper(c);
        }

        public Helper startService(){
            Intent intent = new Intent(mContext,MattermostService.class);
            mContext.startService(intent);
            return this;
        }

        public Helper stopService(){
            Intent intent = new Intent(mContext,MattermostService.class);
            mContext.stopService(intent);
            return this;
        }

        public Helper startWebSocket(){
            Intent intent = new Intent(mContext,MattermostService.class);
            intent.setAction(SERVICE_ACTION_START_WEB_SOCKET);
            mContext.startService(intent);
            return this;
        }

        public Helper updateUserStatusNow(){
            Intent intent = new Intent(mContext,MattermostService.class);
            intent.setAction(UPDATE_USER_STATUS);
            mContext.startService(intent);
            return this;
        }
    }
}
