package com.kilogramm.mattermost.view.viewPhoto;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentPhotoViewBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
        photoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_view, container, false);
        return photoBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Picasso.with(getContext())
                .load(imageUri)
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
                });

        photoBinding.image.setVerticalSwipeListener(() -> {
            if (verticalSwipeListener != null) {
                verticalSwipeListener.onSwipe();
            }
        });
    }
}
