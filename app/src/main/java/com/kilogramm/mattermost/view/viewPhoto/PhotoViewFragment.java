package com.kilogramm.mattermost.view.viewPhoto;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentPhotoViewBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by melkshake on 09.11.16.
 */

public class PhotoViewFragment extends Fragment {

    public static final String IMAGE_URI = "image_uri";
    public static final String LISTENER = "listener";

    private FragmentPhotoViewBinding photoBinding;
    private String imageUri;
    private VerticalSwipeListener verticalSwipeListener;

    static PhotoViewFragment newInstance(String imageUri, VerticalSwipeListener listener) {
        PhotoViewFragment photoViewFragment = new PhotoViewFragment();
        Bundle arguments = new Bundle();
        arguments.putString(IMAGE_URI, imageUri);
        arguments.putSerializable(LISTENER, listener);
        photoViewFragment.setArguments(arguments);
        return photoViewFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.imageUri = getArguments().getString(IMAGE_URI);
            this.verticalSwipeListener = (VerticalSwipeListener) getArguments().getSerializable(LISTENER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TouchImageView img = new TouchImageView(container.getContext());
        img.setVisibility(View.GONE);

        photoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_view, container, false);
        photoBinding.progressBar.setVisibility(View.VISIBLE);

        Picasso.with(container.getContext())
                .load(imageUri)
                .error(container.getContext().getResources().getDrawable(R.drawable.ic_error_red_24dp))
                .placeholder(container.getContext().getResources().getDrawable(R.drawable.circular_white_progress_bar))
                .into(img, new PhotoViewFragment.ImageLoadedCallback(photoBinding.progressBar) {
                    @Override
                    public void onSuccess() {
                        if (photoBinding.progressBar != null) {
                            photoBinding.progressBar.setVisibility(View.GONE);
                            img.setVisibility(View.VISIBLE);
                        }
                    }
                });

        img.setVerticalSwipeListener(() -> {
            if (verticalSwipeListener != null) {
                verticalSwipeListener.onSwipe();
            }
        });

        container.addView(img, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);

        return img;
    }

    private class ImageLoadedCallback implements Callback {
        MaterialProgressBar progressBar = null;

        public ImageLoadedCallback(MaterialProgressBar progBar) {
            progressBar = progBar;
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onError() {
        }
    }
}
