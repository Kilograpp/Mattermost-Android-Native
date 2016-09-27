package com.kilogramm.mattermost.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kilogramm.mattermost.R;

import java.util.ArrayList;

/**
 * Created by melkshake on 26.09.16.
 */
public class BottomToolbar extends LinearLayout {

    private ImageView btnWriteText;
    private ImageView btnMakePhoto;
    private ImageView btnGallery;
    private ImageView btnToolbarMore;

    private BottomtoolbarChatListener bottomToolbarListener;

    private final ArrayList<BottomtoolbarChatListener> mOnItemTouchListeners = new ArrayList<>();

    public BottomToolbar(Context context) {
        super(context);
    }

    public BottomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        btnWriteText = (ImageView) findViewById(R.id.writeText);
        btnMakePhoto = (ImageView) findViewById(R.id.makePhoto);
        btnGallery = (ImageView) findViewById(R.id.addExistedPhoto);
        btnToolbarMore = (ImageView) findViewById(R.id.addDocs);

        btnWriteText.setOnClickListener(view -> {
                if (bottomToolbarListener != null) bottomToolbarListener.onClickWriteText();
        });
        btnMakePhoto.setOnClickListener(view -> {
                if (bottomToolbarListener != null) bottomToolbarListener.onClickMakePhoto();
        });
        btnGallery.setOnClickListener(view -> {
                if (bottomToolbarListener != null) bottomToolbarListener.onClickGallery();
        });
        btnToolbarMore.setOnClickListener(view -> {
                if (bottomToolbarListener != null) bottomToolbarListener.onClickToolbarMore();
        });
    }

    public void setBottomToolbarListener(BottomtoolbarChatListener bottomToolbarListener) {
        this.bottomToolbarListener = bottomToolbarListener;
    }

    public interface BottomtoolbarChatListener {
        void onClickWriteText();
        void onClickMakePhoto();
        void onClickGallery();
        void onClickToolbarMore();
    }
}
