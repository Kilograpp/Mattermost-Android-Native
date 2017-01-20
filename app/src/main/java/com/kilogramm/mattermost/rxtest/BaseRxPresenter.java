package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.error.HttpError;

import java.io.IOException;
import java.net.UnknownHostException;

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
    public static final String DELETE_PREFERENCES = "DELETE_PREFERENCES";
    public static final String CREATE_CHANNEL = "CREATE_CHANNEL";
    public static final String CHANNELS_FOR_USER = "CHANNELS_FOR_USER";
    public static final String CHANNELS_MORE = "CHANNELS_MORE";
    public static final String CHANNELS_MEMBERS = "CHANNELS_MEMBERS";
    public static final String GET_A_CHANNEL = "GET_A_CHANNEL";
    public static final String GET_CHANNEL_STATS = "GET_CHANNEL_STATS";
    public static final String GET_CHANNEL_MEMBER = "GET_CHANNEL_MEMBER";
    public static final String GET_A_FILE = "GET_A_FILE";
    public static final String GET_THUMBNAIL = "GET_THUMBNAIL";
    public static final String GET_PREVIEW = "GET_PREVIEW";
    public static final String GET_METADATA = "GET_METADATA";
    public static final String GET_LINK = "GET_LINK";
    public static final String LOGIN = "LOGIN";
    public static final String SAVE_PREFERENCES = "SAVE_PREFERENCES";
    public static final String SPECIFIC_PREFERENCE = "SPECIFIC_PREFERENCE";
    public static final String USER_PREFERENCES = "USER_PREFERENCES";
    public static final String UPLOAD_A_FILE = "UPLOAD_A_FILE";
    public static final String UPDATE_CHANNEL = "UPDATE_CHANNEL";
    public static final String NO_NETWORK = "NO_NETWORK";

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

    protected <T> Observable<Delivery<ViewType, T>> createTemplateObservable(T obj) {
        return Observable.just(obj)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(deliverFirst());
    }

    public String getError(Throwable e) {
        if (e instanceof HttpException) {
            try {
                HttpError error = new Gson().fromJson(((HttpException) e).response()
                        .errorBody()
                        .string(), HttpError.class);

                if (error != null && error.getStatusCode() != null
                        && error.getStatusCode() == 500) {
                    return "Internal server error, please try later";
                }

                return (error != null) ?
                        (error.getMessage() != null) ? error.getMessage() : error.getError() :
                        e.getMessage();
            } catch (IOException e1) {
                return e.getMessage();
            }
        } else if (e instanceof UnknownHostException) {
            return "Couldn't find existing team matching this URL";
        } else {
            return e.getMessage();
        }
    }

    /*******************************/

    // TODO вот альтернативный метод для обработки ошибок, посмотрите плз

    /**
     * @param requestTag may be any string as you want,
     * but it's desirable to use constants defined above */
    public String parceError(Throwable e, String requestTag) {
//        if (e == null && requestTag.equals(NO_NETWORK)) {
        if (!isNetworkAvailable()) {
            return "No connection to the network";
        } else if (e instanceof HttpException) {
            try {
                HttpError error = new Gson().fromJson(((HttpException) e).response()
                        .errorBody()
                        .string(), HttpError.class);

                if (requestTag != null && error != null) {
                    if (error.getStatusCode() != null) {
                        int statusCode = error.getStatusCode();

                        switch (requestTag) {
                            case LOGIN:
                                if (statusCode == 400) {
                                    return "Invalid login";
                                }
                                if (statusCode == 401) {
                                    return "Invalid password";
                                }
                                break;
                            case CREATE_CHANNEL:
                            case UPDATE_CHANNEL:
                            case CHANNELS_FOR_USER:
                            case CHANNELS_MORE:
                            case CHANNELS_MEMBERS:
                                if (statusCode == 403) {
                                    return "You are not from the team";
                                }
                                if (statusCode == 500 && requestTag.equals(CREATE_CHANNEL)) {
                                    //return "Name must be 2 or more lowercase alphanumeric characters";
                                    return "A channel with that URL already exists";
                                } else {
                                    return "There is no channels for you";
                                }
                            case GET_CHANNEL_STATS:
                                if (statusCode == 500) {
                                    return " Could not retrieve the channel stats";
                                }
                                break;
                            case GET_A_CHANNEL:
                            case GET_CHANNEL_MEMBER:
                                if (statusCode == 500) {
                                    return "You are not from this channel or channel is not from this team";
                                }
                                break;
                            case UPLOAD_A_FILE:
                                if (statusCode == 400) {
                                    return "Invalid file type or invalid image dimensions";
                                } else if (statusCode == 401) {
                                    return "You are not logged in";
                                } else if (statusCode == 403) {
                                    return "You do not have permission to upload file here";
                                } else if (statusCode == 413) {
                                    return "Uploaded file is too large";
                                } else if (statusCode == 501) {
                                    return "File storage is disabled";
                                } else {
//                                    return null;
                                    return error.getMessage();
                                }
                            case GET_A_FILE:
                            case GET_THUMBNAIL:
                            case GET_PREVIEW:
                            case GET_METADATA:
                                if (statusCode == 400 && requestTag.equals(GET_A_FILE)
                                        || requestTag.equals(GET_METADATA)) {
                                    return "The file is owned by another user and is not attached to a post";
                                }
                                if (statusCode == 400 && requestTag.equals(GET_THUMBNAIL)
                                        || requestTag.equals(GET_PREVIEW)) {
                                    return "The file is not an image or thumbnail is lost";
                                }
                                if (statusCode == 403) {
                                    return "You do not have permission to view the file";
                                }
                                if (statusCode == 404) {
                                    return "The file cannot be found on the file system";
                                }
                                if (statusCode == 501) {
                                    return "File storage is disabled";
                                }
                                break;
                            case GET_LINK:
                                if (statusCode == 400) {
                                    return "The fil is not attached to the post";
                                }
                                if (statusCode == 403) {
                                    return "You do not have permission to view the file";
                                }
                                if (statusCode == 501) {
                                    return "The file was deleted";
                                }
                                break;
                            case SAVE_PREFERENCES:
                            case DELETE_PREFERENCES:
                            case USER_PREFERENCES:
                            case SPECIFIC_PREFERENCE:
                                if (statusCode == 400) {
                                    return "Cannot save data";
                                }
                                if (statusCode == 401) {
                                    return "You are not logged in";
                                }
                                if (statusCode == 403) {
                                    return "Some preferences do not match your account";
                                }
                                if (statusCode == 500) {
                                    return "There is not such preferences for your account";
                                }
                                break;
                        }
                    }
                } else {
                    return e.getMessage();
                }
                return (error != null) ?
                        (error.getMessage() != null) ? error.getMessage() : error.getError() :
                        e.getMessage();
            } catch (IOException e1) {
                return e.getMessage();
            }
        } else if (e instanceof UnknownHostException) {
            return "Couldn't find existing team matching this URL";
        } else if(requestTag != null){
            return requestTag;
        } else {
            return e.getMessage();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MattermostApp.getSingleton()
                        .getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        return !(ni == null || !ni.isConnectedOrConnecting());
    }
}
