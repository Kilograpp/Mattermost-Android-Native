package com.kilogramm.mattermost.view.viewPhoto;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by melkshake on 08.11.16.
 */

public class TouchImageAdapter extends FragmentPagerAdapter {

    private ArrayList<String> photosList;
    private VerticalSwipeListener listener;

    public TouchImageAdapter(FragmentManager fm, ArrayList<String> photosList, VerticalSwipeListener listener) {
        super(fm);
        this.photosList = photosList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return this.photosList.size();
    }

    @Override
    public PhotoViewFragment getItem(int position) {
        return PhotoViewFragment.newInstance(photosList.get(position), listener);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}