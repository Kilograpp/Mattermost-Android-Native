package com.kilogramm.mattermost.view.viewPhoto;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.HashMap;

/**
 * Created by ivan on 25.01.17.
 */

public class ImageViewerAnimator {
    private static final int ANIM_DURATION = 300;
    private static final String ANIMDATA = "animdata";
//    ActivityPhotoViewerBinding binding;
    private float mWidthScale;
    private float mHeightScale;
    private float mLeftDelta;
    private float mTopDelta;

    final int thumbnailTop;
    final int thumbnailLeft;
    final int thumbnailWidth;
    final int thumbnailHeight;

    private int mOriginalOrientation;
    private TimeInterpolator sDecelerator;
    private TimeInterpolator sAccelerator;
    private ColorDrawable mBackground;
    private Context context;

    private View imageView;
    private View background;

    public ImageViewerAnimator(Context context, HashMap<String, Integer> params, ActivityPhotoViewerBinding binding){
        this.context = context;

        sDecelerator = new DecelerateInterpolator();
        sAccelerator = new AccelerateInterpolator();

        thumbnailTop = params.get(ANIMDATA + ".top");
        thumbnailLeft = params.get(ANIMDATA + ".left");
        thumbnailWidth = params.get(ANIMDATA + ".width");
        thumbnailHeight = params.get(ANIMDATA + ".height");
        mOriginalOrientation = params.get(ANIMDATA + ".orientation");

        imageView = binding.viewPager;
        background = binding.background;


        mBackground = new ColorDrawable(Color.BLACK);
        background.setBackground(mBackground);// TODO: 24.01.17 viewpager -> background

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)

    }

    public void startAnimation(){
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);

                // Figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);
                mLeftDelta = thumbnailLeft - screenLocation[0];
                mTopDelta = thumbnailTop - screenLocation[1];

                // Scale factors to make the large version the same size as the thumbnail
                mWidthScale = (float) thumbnailWidth / imageView.getWidth();
                mHeightScale = (float) thumbnailHeight / imageView.getHeight();

                runEnterAnimation();

                return true;
            }
        });
    }

    public void runEnterAnimation() {
        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        imageView.setPivotX(0);
        imageView.setPivotY(0);
        imageView.setScaleX(mWidthScale);
        imageView.setScaleY(mHeightScale);
        imageView.setTranslationX(mLeftDelta);
        imageView.setTranslationY(mTopDelta);
        imageView.setAlpha(0.2f);

        // Animate scale and translation to go from thumbnail to full size
        imageView.animate().setDuration(ANIM_DURATION)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .alpha(1)
                .setInterpolator(sDecelerator);


        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();


        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(1, 255);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(valueAnimator1 ->
                ((BaseActivity)context).setStatusbarAndToolbarAlpha((int) valueAnimator1.getAnimatedValue()));
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();



    }


    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {

        // No need to set initial values for the reverse animation; the image is at the
        // starting size/location that we want to start from. Just animate to the
        // thumbnail size/location that we retrieved earlier

        // Caveat: configuration change invalidates thumbnail positions; just animate
        // the scale around the center. Also, fade it out since it won't match up with
        // whatever's actually in the center
        final boolean fadeOut;
        if (context.getResources().getConfiguration().orientation != mOriginalOrientation) {
            imageView.setPivotX(imageView.getWidth() / 2);
            imageView.setPivotY(imageView.getHeight() / 2);
            mLeftDelta = 0;
            mTopDelta = 0;
            fadeOut = true;
        } else {
            fadeOut = false;
        }

        //animate toolbar and status bar
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(255, 1);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(valueAnimator1 ->
                ((BaseActivity)context).setStatusbarAndToolbarAlpha((int) valueAnimator1.getAnimatedValue()));
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();

        // Animate image back to thumbnail size/location
        imageView.animate().setDuration(ANIM_DURATION)
                .scaleX(mWidthScale).scaleY(mHeightScale)
                .translationX(mLeftDelta).translationY(mTopDelta)
                .alpha(0.2f)
                .withEndAction(endAction);
//
        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();

    }
    /***/

    public void finishWithAnimation(Runnable action){
        runExitAnimation(() -> {
            action.run();
            ((Activity)context).overridePendingTransition(0, 0);
        });
    }
}
