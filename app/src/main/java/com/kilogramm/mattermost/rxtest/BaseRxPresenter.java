package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;

import com.google.gson.Gson;
import com.kilogramm.mattermost.model.error.HttpError;

import java.io.IOException;

import icepick.Icepick;
import nucleus.presenter.RxPresenter;
import nucleus.presenter.delivery.Delivery;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 03.10.2016.
 */
public class BaseRxPresenter<ViewType> extends RxPresenter<ViewType> {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Icepick.restoreInstanceState(this, savedState);
    }

    @Override
    protected void onSave(Bundle state) {
        super.onSave(state);
        Icepick.saveInstanceState(this, state);
    }

    public <T> Observable<Delivery<ViewType, T>> createTemplateObservable(T obj){
        return Observable.just(obj)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(deliverFirst());
    }

    public String getError(Throwable e){
        if(e instanceof HttpException){
            try {
                return new Gson().fromJson(((HttpException) e).response()
                        .errorBody()
                        .string(),HttpError.class).getError();
            } catch (IOException e1) {
                return e.getMessage();
            }
        } else {
            return e.getMessage();
        }
    }
}
