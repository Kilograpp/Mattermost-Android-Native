package com.kilogramm.mattermost.viewmodel;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Evgeny on 06.09.2016.
 */
public class ImageViewerViewModel implements ViewModel {

    private Context context;
    private String imageUrl;


    public ImageViewerViewModel(Context context, String imageUrl){
        this.context = context;
        this.imageUrl = imageUrl;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }
}
