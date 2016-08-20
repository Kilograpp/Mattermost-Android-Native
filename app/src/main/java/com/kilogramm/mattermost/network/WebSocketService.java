package com.kilogramm.mattermost.network;

import android.util.Log;

import com.kilogramm.mattermost.MattermostPreference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

/**
 * Created by Evgeny on 20.08.2016.
 */
public class WebSocketService {

    private static WebSocketService instance;
    private static WebSocketCall mWebSocketCall;

    public static void create() {
        OkHttpClient clientWebS = new OkHttpClient.Builder()
                .readTimeout(60L*1000, TimeUnit.MILLISECONDS)
                .connectTimeout(60L*1000, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token;
                    if((token = MattermostPreference.getInstance().getAuthToken())!=null){
                        Request request = original.newBuilder()
                                .addHeader("Authorization","Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    } else {
                        return chain.proceed(original);
                    }
                })
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        MattermostPreference.getInstance().saveCookies(cookies);
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();

        MattermostPreference.getInstance().getBaseUrl();

        Request request = new Request.Builder()
                .url("wss://mattermost.kilograpp.com/api/v3/users/websocket")
                .build();

        mWebSocketCall = WebSocketCall.create(clientWebS,request);
        mWebSocketCall.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("Websocket", "Opened");
            }

            @Override
            public void onFailure(IOException e, Response response) {
                Log.d("Websocket", "onFailure" +e.toString());
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                Log.d("Websocket", "onMessage " + message.string());

            }

            @Override
            public void onPong(Buffer payload) {
                Log.d("Websocket", "onPong");
            }

            @Override
            public void onClose(int code, String reason) {
                Log.d("Websocket", "onClose");
            }
        });
    }
}
