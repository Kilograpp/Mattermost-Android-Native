package com.kilogramm.mattermost.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kilogramm.mattermost.model.error.HttpError;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by kraftu on 13.09.16.
 */
public abstract class MattermostHttpSubscriber<T> extends Subscriber<T> {
    @Override
    public void onError(Throwable e) {
        HttpError error = null;
        if(e instanceof HttpException){
            try {
                error = new Gson()
                        .fromJson((((HttpException) e)
                                .response()
                                .errorBody()
                                .string()), HttpError.class);
            } catch (IOException e1) {
                e1.printStackTrace();
                error = new HttpError();
                error.setMessage(e.getMessage());
            }
        } else if(e instanceof JsonSyntaxException) {
            e.printStackTrace();
            error = new HttpError();
            error.setMessage(e.getMessage());
        } else {
            e.printStackTrace();
            error = new HttpError();
            error.setMessage(e.getMessage());
        }


        onErrorMattermost(error,e);
    }


    public abstract void onErrorMattermost(HttpError httpError, Throwable e);
}
