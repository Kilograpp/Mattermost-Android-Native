package com.kilogramm.mattermost.view.viewPhoto;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.view.BaseActivity;

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
    private ColorDrawable mBackground;
    private Context context;
    private ImageView mIconDownload;

    private View imageView;
    private View background;
    private final String TAG = ImageViewerAnimator.class.getSimpleName();

    public ImageViewerAnimator(Context context, Bundle bundle, View imageView, View background, ImageView downloadIcon) {
        this.context = context;

        this.imageView = imageView;
        this.background = background;
        this.mIconDownload = downloadIcon;
        mBackground = new ColorDrawable(Color.BLACK);
        background.setBackground(mBackground);


        thumbnailTop = bundle.getInt(ANIMDATA + ".top");
        thumbnailLeft = bundle.getInt(ANIMDATA + ".left");
        thumbnailWidth = bundle.getInt(ANIMDATA + ".width");
        thumbnailHeight = bundle.getInt(ANIMDATA + ".height");
        mOriginalOrientation = bundle.getInt(ANIMDATA + ".orientation");

        sDecelerator = new DecelerateInterpolator();
    }

    public void startAnimation(Runnable onFinishAnimation) {
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
//                mHeightScale = (float) thumbnailHeight / imageView.getHeight();//

                runEnterAnimation(onFinishAnimation);

                return true;
            }
        });
    }

    public void runEnterAnimation(Runnable onFinishAnimation) {
        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        imageView.setPivotX(imageView.getWidth() / 2);
        imageView.setPivotY(imageView.getHeight() / 2);
        imageView.setScaleX(mWidthScale);
        imageView.setScaleY(mWidthScale);
        imageView.setTranslationX(0);
        imageView.setTranslationY(0);
        imageView.setAlpha(0.f);

        // Animate scale and translation to go from thumbnail to full size
        imageView.animate().setDuration(ANIM_DURATION)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .alpha(1)
                .setInterpolator(sDecelerator)
        ;

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();

        // Animating status & progress bar
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(1, 255);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(valueAnimator1 ->
                setStatusBarAndToolbarAlpha((int) valueAnimator1.getAnimatedValue()));

        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();

        if (onFinishAnimation != null) onFinishAnimation.run();

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

        imageView.setPivotX(imageView.getWidth() / 2);
        imageView.setPivotY(imageView.getHeight() / 2);
        mLeftDelta = 0;
        mTopDelta = 0;


        //animate toolbar and status bar
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(255, 1);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(valueAnimator1 -> setStatusBarAndToolbarAlpha((int) valueAnimator1.getAnimatedValue()));
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();

        // Animate image back to thumbnail size/location
        imageView.animate().setDuration(ANIM_DURATION)
                .scaleX(mWidthScale).scaleY(mWidthScale)
                .translationX(mLeftDelta).translationY(mTopDelta)
                .alpha(0.f)
                .withEndAction(endAction);
//
        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();

    }

    public void finishWithAnimation(Runnable action) {
        runExitAnimation(() -> {
            action.run();
            ((Activity) context).overridePendingTransition(0, 0);
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarAndToolbarAlpha(int alpha) {
        ActionBar actionBar = ((BaseActivity) context).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(Color.argb(alpha, 0, 0, 0)));

            Spannable title = new SpannableString(actionBar.getTitle());
            title.setSpan(new ForegroundColorSpan(Color.argb(alpha, 255, 255, 255)),
                    0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            actionBar.setTitle(title);
            mIconDownload.setAlpha(alpha);
            Drawable buttonBack = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white_24dp);
            buttonBack.setAlpha(alpha);
            actionBar.setHomeAsUpIndicator(buttonBack);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((BaseActivity) context).getWindow().setStatusBarColor(Color.argb(alpha, 0, 0, 0));
            }
        }
    }

    public void setBackgroundAlpha(float backgroundAlpha) {
        background.setAlpha(backgroundAlpha);
        imageView.setAlpha(1); // FIXME: 27.01.17
    }

    public void setBackgroundAlpha(float alpha, int duration){
        background.animate().alpha(alpha).setDuration(duration);
    }
}
