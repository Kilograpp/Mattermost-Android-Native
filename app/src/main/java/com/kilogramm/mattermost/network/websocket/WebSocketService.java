package com.kilogramm.mattermost.network.websocket;

import android.content.Context;
import android.util.Log;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.tools.ObjectUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

/**
 * Created by Evgeny on 20.08.2016.
 */
public class WebSocketService {

    private static final String TAG = "Websocket";

    private static Thread thread = null;
    private static WebSocket webSocket = null;
    private static WebSocketFactory socketFactory;
    private static Context context;

    private static WebSocketService instance;

    public WebSocketService(Context context) {
        this.context = context;
        create(context);
    }

    public static WebSocketService with(Context context) {
        if(null == instance){
            instance = new WebSocketService(context);
        }
        return instance;
    }

    public void create(Context context) {
        Log.d(TAG, "create");

        try {
            List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
            Cookie cookie = cookies.get(0);
            socketFactory = new WebSocketFactory();
            webSocket = socketFactory.createSocket("wss://mattermost.kilograpp.com/api/v3/users/websocket");
            webSocket.addHeader("Cookie", cookie.name() + "=" + cookie.value());
            webSocket.addListener(new WebSocketListener() {
                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    switch (newState){
                        case CLOSING:
                            websocket.sendClose();
                    }
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.d(TAG, "onConnected");
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d(TAG, "onConnectError");
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    Log.d(TAG, "onDisconnected");
                    onDisconnct();
                }

                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onFrame");
                }

                @Override
                public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onContinuationFrame");
                }

                @Override
                public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onTextFrame");
                }

                @Override
                public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onBinaryFrame");
                }

                @Override
                public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onCloseFrame");
                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onPingFrame");
                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onPongFrame");
                }

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    String webMessage = new String(text);
                    Log.d(TAG, "onMessage " + webMessage);
                    try {
                        ObjectUtil.parseWebSocketObject(webMessage, context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                    Log.d(TAG, "onBinaryMessage");
                }

                @Override
                public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onSendingFrame");
                }

                @Override
                public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onFrameSent");
                }

                @Override
                public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onFrameUnsent");
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d(TAG, "onError");
                }

                @Override
                public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onFrameError");
                }

                @Override
                public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
                    Log.d(TAG, "onMessageError");
                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                    Log.d(TAG, "onMessageDecompressionError");
                }

                @Override
                public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
                    Log.d(TAG, "onTextMessageError");
                }

                @Override
                public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onSendError");
                }

                @Override
                public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d(TAG, "onUnexpectedError");
                }

                @Override
                public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                    Log.d(TAG, "handleCallbackError");
                }

                @Override
                public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
                    Log.d(TAG, "onSendingHandshake");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDisconnct() {
        if(!thread.isInterrupted()){
            thread.interrupt();
            webSocket.sendClose();
            Log.d(TAG,"interrupt");
        }
    }

    public void run() {

        thread = new Thread(() -> {
            try {
                connect();
                Log.d(TAG, "connect");
            } catch (WebSocketException e) {
                Log.d(TAG, "error");
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void connect() throws WebSocketException {
        if(webSocket!=null && (webSocket.getState() != WebSocketState.CREATED)){
            create(context);
        }
        webSocket.connect();
        Log.d(TAG, "try connect");
    }

    private boolean hasWebsocket(){
        return ((webSocket!=null)&&(!(webSocket.getState() == WebSocketState.CLOSED)))?true:false;
    }

    public void reconnect() throws WebSocketException {
        if(!hasWebsocket() && !webSocket.isOpen()){
            Log.d(TAG, "reconnect");
            run();
        }
    }
}
