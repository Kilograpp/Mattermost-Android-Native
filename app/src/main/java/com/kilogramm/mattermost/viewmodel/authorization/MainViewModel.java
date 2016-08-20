package com.kilogramm.mattermost.viewmodel.authorization;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.authorization.LoginActivity;
import com.kilogramm.mattermost.view.authorization.MainActivity;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class MainViewModel implements ViewModel {

    private static final String TAG = "MainViewModel";

    private Realm mRealm;
    private Context context;
    private Subscription subscription;

    private String editTextUrl;
    private ObservableInt isVisibleNextButton;
    private ObservableInt isVisibleProgress;


    public MainViewModel(Context context) {
        this.context = context;
        mRealm = Realm.getDefaultInstance();
        isVisibleNextButton = new ObservableInt(View.GONE);
        isVisibleProgress = new ObservableInt(View.GONE);
    }

    public void onClickNext(View view){
        checkTeamAndNext(editTextUrl);
    }

    private void checkTeamAndNext(String editTextUrl){

        try {
            URL url = new URL(editTextUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        MattermostPreference.getInstance().setBaseUrl(editTextUrl);
        MattermostApplication.get(context).refreshMattermostRetrofitService();

        if(subscription!=null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }

        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        hideKeyboard(((MainActivity) context));
        isVisibleProgress.set(View.VISIBLE);
        subscription = service.initLoad()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<InitObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete");
                        isVisibleProgress.set(View.GONE);
                        LoginActivity.startActivity(context);
                    }
                    @Override
                    public void onError(Throwable e) {
                        isVisibleProgress.set(View.GONE);
                        handleErrorLogin(e);
                    }
                    @Override
                    public void onNext(InitObject initObject) {
                        mRealm.executeTransaction(realm -> {
                            RealmResults<ClientCfg> results = realm.where(ClientCfg.class).findAll();
                            results.deleteAllFromRealm();
                        });
                        mRealm.executeTransaction(realm1 -> {
                            ClientCfg cfg = realm1.copyToRealm(initObject.getClientCfg());
                            realm1.copyToRealm(cfg);
                        });
                    }
                });
    }

    public TextWatcher getUrlTextWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editTextUrl = s.toString();
                isVisibleNextButton.set(isValidUrl(s.toString()) ? View.VISIBLE : View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }


    @Override
    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = null;
        context = null;
        mRealm.close();
    }

    private void handleErrorLogin(Throwable e) {
        if(e instanceof HttpException){
            HttpError error;
            try {
                error = new Gson()
                        .fromJson((((HttpException) e)
                                .response()
                                .errorBody()
                                .string()), HttpError.class);
                Log.d(TAG, error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IOException e1) {
                Log.d(TAG, "Message not has body.");
                e1.printStackTrace();
            }
        } else {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SystemException, stackTrace: \n");
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    private boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url);
        return m.matches();
    }

    public ObservableInt getIsVisibleNextButton() {
        return isVisibleNextButton;
    }
    public ObservableInt getIsVisibleProgress() {
        return isVisibleProgress;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
