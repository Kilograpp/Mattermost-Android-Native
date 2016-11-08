package com.kilogramm.mattermost.view.viewPhoto;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by melkshake on 08.11.16.
 */

public class TouchImageAdapter extends PagerAdapter {

    private ArrayList<String> photosList;
    private ActivityPhotoViewerBinding binding;

    public TouchImageAdapter(ActivityPhotoViewerBinding binding, ArrayList<String> photoList) {
        this.photosList = photoList;
        this.binding = binding;
    }

    @Override
    public int getCount() {
        return this.photosList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView img = new TouchImageView(container.getContext());

        binding.viewPager.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        Picasso.with(container.getContext())
                .load(photosList.get(position))
                .error(container.getContext().getResources().getDrawable(R.drawable.ic_error_red_24dp))
                .placeholder(container.getContext().getResources().getDrawable(R.drawable.circular_white_progress_bar))
                .into(img, new ImageLoadedCallback(binding.progressBar) {
                    @Override
                    public void onSuccess() {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.viewPager.setVisibility(View.VISIBLE);
                        }
                    }
                });

        container.addView(img, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);

        return img;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private class ImageLoadedCallback implements Callback {
        private MaterialProgressBar progressBar = null;

        public ImageLoadedCallback(MaterialProgressBar progBar) {
            this.progressBar = progBar;
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onError() {
        }
    }
}
