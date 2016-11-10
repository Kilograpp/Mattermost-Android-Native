package com.kilogramm.mattermost.view.viewPhoto;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melkshake on 08.11.16.
 */

public class TouchImageAdapter extends FragmentPagerAdapter {

    List<PhotoViewFragment> photoViewFragmentList;
    private ArrayList<String> photosList;

    public TouchImageAdapter(FragmentManager fm, ArrayList<String> photosList) {
        super(fm);
        this.photosList = photosList;
        photoViewFragmentList = new ArrayList<>();
        for (String photo : photosList) {
            PhotoViewFragment fragment = PhotoViewFragment.newInstance(photo);
            photoViewFragmentList.add(fragment);
        }
    }

    @Override
    public int getCount() {
        return this.photosList.size();
    }

    @Override
    public PhotoViewFragment getItem(int position) {
        return photoViewFragmentList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        FragmentManager manager = ((Fragment) object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();
    }
}