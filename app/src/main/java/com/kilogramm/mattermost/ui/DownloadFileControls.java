package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.kilogramm.mattermost.R;

/**
 * Created by kepar on 29.9.16.
 */
//@RequiresPresenter(DownLoadFilePresenter.class)
public class DownloadFileControls extends FrameLayout {

    public static final String TAG = "AttachedFilesLayout";

    private View iconActionDownload;
    private ProgressBar progressBar;
    private View progressWait;
    private View viewClose;

    private ControlsClickListener controlsClickListener;

    public DownloadFileControls(Context context) {
        super(context);
        init(context);
    }

    public DownloadFileControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownloadFileControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        inflate(context, R.layout.download_controls, this);

        iconActionDownload = findViewById(R.id.iconActionDownload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressWait = findViewById(R.id.progressWait);
        viewClose = findViewById(R.id.close);

        iconActionDownload.setOnClickListener(v -> {
            showProgressControls();
            if(controlsClickListener != null) controlsClickListener.onClickDownload();
        });

        viewClose.setOnClickListener(v -> {
            hideProgressControls();
            if(controlsClickListener != null) controlsClickListener.onClickCancel();
        });
    }

    public void setControlsClickListener(ControlsClickListener controlsClickListener) {
        this.controlsClickListener = controlsClickListener;
    }

    public void showProgressControls() {
        iconActionDownload.setVisibility(INVISIBLE);
        progressWait.setVisibility(VISIBLE);
        viewClose.setVisibility(VISIBLE);
    }

    public void hideProgressControls() {
        iconActionDownload.setVisibility(VISIBLE);
        progressBar.setVisibility(INVISIBLE);
        progressWait.setVisibility(INVISIBLE);
        viewClose.setVisibility(INVISIBLE);
    }

    private void showProgress(){
        progressBar.setVisibility(VISIBLE);
        progressWait.setVisibility(INVISIBLE);
    }

    public void setProgress(int percantage){
        showProgress();
        progressBar.setProgress(percantage);
    }

    public interface ControlsClickListener {
        void onClickDownload();

        void onClickCancel();
    }
}
