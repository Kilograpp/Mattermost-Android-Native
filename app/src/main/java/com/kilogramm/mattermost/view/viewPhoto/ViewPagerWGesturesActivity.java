package com.kilogramm.mattermost.view.viewPhoto;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.ArrayList;

/**
 * Created by melkshake on 08.11.16.
 */

public class ViewPagerWGesturesActivity extends BaseActivity implements VerticalSwipeListener{

    public static final String IMAGE_URL = "image_url";
    public static final String TITLE = "title";
    public static final String PHOTO_LIST = "photo_list";

    private ActivityPhotoViewerBinding binding;
    private TouchImageAdapter adapter;

    public static void start(Context context, View view, String title, String imageUrl, ArrayList<String> photoList) {
        Intent starter = new Intent(context, ViewPagerWGesturesActivity.class);
        starter
                .putExtra(TITLE, title)
                .putExtra(IMAGE_URL, imageUrl)
                .putExtra(PHOTO_LIST, photoList);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_viewer);

        setupToolbar(getIntent().getStringExtra(TITLE), true);
        setColorScheme(R.color.black, R.color.black);

        adapter = new TouchImageAdapter(
                binding,
                //getIntent().getStringExtra(IMAGE_URL),
                getIntent().getStringArrayListExtra(PHOTO_LIST));

        binding.viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSwipe() {
        this.finish();
    }
}
