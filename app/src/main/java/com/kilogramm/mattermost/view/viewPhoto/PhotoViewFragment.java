package com.kilogramm.mattermost.view.viewPhoto;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentPhotoViewBinding;
import com.kilogramm.mattermost.tools.FileUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by melkshake on 09.11.16.
 */

public class PhotoViewFragment extends Fragment implements VerticalSwipeListener {

    public static final String IMAGE_URI = "image_uri";

    private FragmentPhotoViewBinding photoBinding;
    private String imageUri;

    static PhotoViewFragment newInstance(String imageUri) {
        PhotoViewFragment photoViewFragment = new PhotoViewFragment();

        Bundle arguments = new Bundle();
        arguments.putString(IMAGE_URI, imageUri);
        photoViewFragment.setArguments(arguments);

        return photoViewFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.imageUri = getArguments().getString(IMAGE_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        photoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_view, container, false);
        return photoBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*Picasso.with(getContext())
                .load(FileUtil.getInstance().getImageUrl(imageUri))
                .error(getContext().getResources().getDrawable(R.drawable.ic_error_red_24dp))
                .into(photoBinding.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (photoBinding.progressBar != null) {
                            photoBinding.progressBar.setVisibility(View.GONE);
                            photoBinding.image.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError() {
                    }
                });*/
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.slices)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().loadImage(FileUtil.getInstance().getImageUrl(imageUri), options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(photoBinding.image!=null) photoBinding.image.setImageBitmap(loadedImage);
                if (photoBinding.progressBar != null) {
                    photoBinding.progressBar.setVisibility(View.GONE);
                    photoBinding.image.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });


        photoBinding.image.setVerticalSwipeListener(() -> getActivity().finish());
    }

    @Override
    public void onSwipe() {
    }
}
