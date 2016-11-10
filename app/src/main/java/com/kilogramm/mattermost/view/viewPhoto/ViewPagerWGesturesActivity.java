package com.kilogramm.mattermost.view.viewPhoto;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.ArrayList;

/**
 * Created by melkshake on 08.11.16.
 */

public class ViewPagerWGesturesActivity extends BaseActivity {

    public static final String IMAGE_URL = "image_url";
    public static final String TITLE = "title";
    public static final String PHOTO_LIST = "photo_list";

    private ActivityPhotoViewerBinding binding;
    private TouchImageAdapter adapter;

    private ArrayList<String> photosList;
    private String clickedImageUri;

    public static void start(Context context, String title, String imageUrl, ArrayList<String> photoList) {
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

        setupToolbar(binding.toolbar, getIntent().getStringExtra(TITLE), true);
        setColorScheme(R.color.black, R.color.black);

        photosList = getIntent().getStringArrayListExtra(PHOTO_LIST);
        clickedImageUri = getIntent().getStringExtra(IMAGE_URL);

        adapter = new TouchImageAdapter(getSupportFragmentManager(), photosList);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(photosList.indexOf(clickedImageUri));
        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                String[] parsedName = photosList.get(position).split("/");
                setupToolbar(parsedName[parsedName.length - 1], true);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
}
