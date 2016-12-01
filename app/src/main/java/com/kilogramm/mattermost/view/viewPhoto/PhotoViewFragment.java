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
import com.kilogramm.mattermost.tools.FileUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
        Picasso.with(getContext())
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
                });

        photoBinding.image.setVerticalSwipeListener(() -> getActivity().finish());
    }

    @Override
    public void onSwipe() {
    }
}
