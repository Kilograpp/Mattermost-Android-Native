package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AttachedFileLayoutBinding;
import com.kilogramm.mattermost.databinding.AttachedFilesLayoutBinding;
import com.kilogramm.mattermost.presenter.AttachedFilesPresenter;
import com.kilogramm.mattermost.view.NucleusLinearLayout;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */
@RequiresPresenter(AttachedFilesPresenter.class)
public class AttachedFilesLayout extends NucleusLayout<AttachedFilesPresenter> {

    private static final String TAG = "AttachedFilesLayout";

    AttachedFilesLayoutBinding bindingRoot;

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        inflate(context, R.layout.attached_files_layout, this);
        bindingRoot = DataBindingUtil.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                R.layout.attached_files_layout,
                null,false);
    }

    public void addItem(Uri uri, String teamId, String channelId){
        showFile(uri);
        getPresenter().uploadFileToServer(getActivity(), teamId, channelId, uri);
    }

    private void showFile(Uri uri){
        AttachedFileLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.attached_file_layout, this,false);
        Glide.with(getContext())
                .load(uri)
                .override(150,150)
                .placeholder(R.drawable.ic_attachment_grey_24dp)
                .error(R.drawable.ic_attachment_grey_24dp)
                .thumbnail(0.1f)
                .into(binding.imageView);
        bindingRoot.attachedFilesLayout.addView(binding.getRoot());
    }
}
