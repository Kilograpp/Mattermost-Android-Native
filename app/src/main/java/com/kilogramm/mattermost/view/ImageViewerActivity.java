package com.kilogramm.mattermost.view;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.bumptech.glide.Glide;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityImageViewerBinding;
import com.kilogramm.mattermost.viewmodel.ImageViewerViewModel;

/**
 * Created by Evgeny on 05.09.2016.
 */
public class ImageViewerActivity extends BaseActivity {

    public static final String COLOR = "color";
    public static final String TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String PACKAGE = "package";
    private static final int ANIM_DURATION = 200;

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    ColorDrawable mBackground;
    int mLeftDelta;
    int mTopDelta;
    float mWidthScale;
    float mHeightScale;
    private String imageUrl;
    private ActivityImageViewerBinding binding;
    private ImageViewerViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_viewer);
        imageUrl = getIntent().getStringExtra(IMAGE_URL);
        viewModel = new ImageViewerViewModel(this, imageUrl);
        binding.setViewModel(viewModel);

        Glide.with(getApplicationContext())
                .load(imageUrl)
                .placeholder(Color.RED)
                .into(binding.image);
        String title = getIntent().getStringExtra(TITLE);
        setupToolbar(title, true);

        final int thumbnailTop = getIntent().getExtras().getInt(PACKAGE + ".top");
        final int thumbnailLeft = getIntent().getExtras().getInt(PACKAGE + ".left");
        final int thumbnailWidth = getIntent().getExtras().getInt(PACKAGE + ".width");
        final int thumbnailHeight = getIntent().getExtras().getInt(PACKAGE + ".height");

        mBackground = new ColorDrawable(Color.BLACK);
        binding.topLayout.setBackground(mBackground);


        if (savedInstanceState == null) {
            ViewTreeObserver observer = binding.image.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    binding.image.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    binding.image.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) thumbnailWidth / binding.image.getWidth();
                    mHeightScale = (float) thumbnailHeight / binding.image.getHeight();

                    runEnterAnimation();

                    return true;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        runExitAnimation(() -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }

    public static void startActivity(Context context, View view, String title, String imageUrl){
        Intent intent = new Intent(context,ImageViewerActivity.class);
        int colour = context.getResources().getColor(R.color.black);
        intent.putExtra(COLOR, colour);
        intent.putExtra(TITLE, title);
        intent.putExtra(IMAGE_URL, imageUrl);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(colour);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(view,bitmap,0,0);
        context.startActivity(intent, optionsCompat.toBundle());
    }

    public static final void start(Context context, View view, String title, String imageUrl){
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        Intent subActivity = new Intent(context,
                ImageViewerActivity.class);
        int orientation = context.getResources().getConfiguration().orientation;
        subActivity.
                putExtra(PACKAGE + ".orientation", orientation).
                putExtra(PACKAGE + ".left", screenLocation[0]).
                putExtra(PACKAGE + ".top", screenLocation[1]).
                putExtra(PACKAGE + ".width", view.getWidth()).
                putExtra(PACKAGE + ".height", view.getHeight()).
                putExtra(TITLE, title).
                putExtra(IMAGE_URL, imageUrl);
        context.startActivity(subActivity);

        // Override transitions: we don't want the normal window animation in addition
        // to our custom one
        ((Activity) context).overridePendingTransition(0, 0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setColorScheme(R.color.transparent, R.color.black);
    }

    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * 1);

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        binding.image.setPivotX(0);
        binding.image.setPivotY(0);
        binding.image.setScaleX(mWidthScale);
        binding.image.setScaleY(mHeightScale);
        binding.image.setTranslationX(mLeftDelta);
        binding.image.setTranslationY(mTopDelta);

        // We'll fade the text in later
        //mTextView.setAlpha(0);

        // Animate scale and translation to go from thumbnail to full size
        binding.image.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator).
                withEndAction(() -> {

                });

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer = ObjectAnimator.ofFloat(ImageViewerActivity.this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();

        // Animate a drop-shadow of the image
        ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(binding.frame, "shadowDepth", 0, 1);
        shadowAnim.setDuration(duration);
        shadowAnim.start();
    }

    public void runExitAnimation(final Runnable endAction) {
        final long duration = (long) (ANIM_DURATION * 1);

        // No need to set initial values for the reverse animation; the image is at the
        // starting size/location that we want to start from. Just animate to the
        // thumbnail size/location that we retrieved earlier

        // Caveat: configuration change invalidates thumbnail positions; just animate
        // the scale around the center. Also, fade it out since it won't match up with
        // whatever's actually in the center
        final boolean fadeOut;
        fadeOut = false;

        // First, slide/fade text out of the way

        binding.image.animate().setDuration(duration).
                scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta).
                withEndAction(endAction);
        if (fadeOut) {
            binding.image.animate().alpha(0);
        }
        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate the shadow of the image
        ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(binding.frame,
                "shadowDepth", 1, 0);
        shadowAnim.setDuration(duration);
        shadowAnim.start();
        // Animate a color filter to take the image back to grayscale,
        // in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer =
                ObjectAnimator.ofFloat(ImageViewerActivity.this, "saturation", 1, 0);
        colorizer.setDuration(duration);
        colorizer.start();

    }
}
