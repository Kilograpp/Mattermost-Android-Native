package com.kilogramm.mattermost.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AttachedFileLayoutBinding;
import com.kilogramm.mattermost.presenter.AttachedFilesPresenter;

import java.io.File;

import nucleus.presenter.Presenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */

public class AttachedFilesLayout extends NucleusLayout<AttachedFilesPresenter> {

    AttachedFileLayoutBinding binding;

    public AttachedFilesLayout(Context context) {
        super(context);
        init(context);
    }

    public AttachedFilesLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AttachedFilesLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /*@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AttachedFilesLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }*/

    private void init(Context context) {
        inflate(context, R.layout.attached_files_layout, this);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.attached_file_layout, this,false);
    }

    public void addItem(Presenter presenter){

    }
}
