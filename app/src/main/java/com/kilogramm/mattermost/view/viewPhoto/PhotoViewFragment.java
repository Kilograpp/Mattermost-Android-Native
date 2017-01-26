package com.kilogramm.mattermost.view.viewPhoto;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentPhotoViewBinding;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.ui.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by melkshake on 09.11.16.
 */

public class PhotoViewFragment extends Fragment {

    public static final String IMAGE_URI = "image_uri";
    public static final String TAG = PhotoViewFragment.class.getSimpleName();
    private FragmentPhotoViewBinding photoBinding;
    private FileInfo mFileInfo;

    static PhotoViewFragment newInstance(FileInfo fileInfo) {
        PhotoViewFragment photoViewFragment = new PhotoViewFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(IMAGE_URI, fileInfo);
        photoViewFragment.setArguments(arguments);

        return photoViewFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mFileInfo = getArguments().getParcelable(IMAGE_URI);
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
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.drawable.ic_error_red_24dp)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .considerExifParams(true)
                .build();

        String preview_url = "https://mattermost.kilograpp.com/api/v3/files/"
                + mFileInfo.getId()
                + "/get_preview";

        String thumb_url = "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/files/"
                + mFileInfo.getId()
                + "/get_thumbnail";

        ImageLoader.getInstance().loadImage(preview_url, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Bitmap bmp = BitmapFactory.decodeFile(ImageLoader.getInstance().getDiskCache().get(thumb_url).getPath());
                if (bmp != null) {
                    photoBinding.image.setImageBitmap(bmp);
                    photoBinding.image.setVisibility(View.VISIBLE);
                    photoBinding.progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (photoBinding.progressBar != null) {
                    photoBinding.progressBar.setVisibility(View.GONE);
                }
                photoBinding.image.setVisibility(View.VISIBLE);
                photoBinding.errorText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (photoBinding.image != null) photoBinding.image.setImageBitmap(loadedImage);
                if (photoBinding.progressBar != null) {
                    photoBinding.progressBar.setVisibility(View.GONE);
                    photoBinding.image.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {


            }
        });
        photoBinding.image.setOnTouchListener(new View.OnTouchListener() {
            float oldPosition;
            @Override
            public boolean onTouch(View view1, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE: {
                        if(photoBinding.image.getCurrentZoom() != 1)
                            return false;
                        if(event.getPointerCount() == 1)
                        view1.setY(event.getRawY() - oldPosition);

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view1.animate().translationY(0).setInterpolator(new OvershootInterpolator()).setDuration(300);
                        ((TouchImageView)view1).setZoomEnable(true);
                        if(photoBinding.image.getCurrentZoom() == 1 && Math.abs(view1.getY()) >
                                getActivity().getResources().getDisplayMetrics().heightPixels/4)
                            getActivity().finish();

                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        oldPosition = event.getRawY();
                        Log.i(TAG, "onTouch: ACTION_DOWN: " + event.getRawY());
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_DOWN: {
                        break;
                    }
                }
                return true;
            }
        });
        photoBinding.image.setVerticalSwipeListener(new VerticalSwipeListener() {
            @Override
            public void onSwipe() {
                getActivity().finish();
            }

            @Override
            public void swipe(float beginY, float endY) {

                //Log.d("SWIPE____", "swipe: {\n  beginY = " + beginY + "\n   endY = " + endY + "\n}");
                // ((ViewPagerWGesturesActivity) getActivity()).setTransparent(Math.abs(endY-beginY)/(1.5f));
            }
        });
    }

}
