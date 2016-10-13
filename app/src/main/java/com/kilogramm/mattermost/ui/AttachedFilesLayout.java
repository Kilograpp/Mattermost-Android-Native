package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.presenter.AttachedFilesPresenter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */
@RequiresPresenter(AttachedFilesPresenter.class)
public class AttachedFilesLayout extends NucleusLayout<AttachedFilesPresenter> {

    AttachedFilesAdapter attachedFilesAdapter;

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
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        attachedFilesAdapter = new AttachedFilesAdapter(getContext(), FileToAttachRepository.getInstance().query());
        recyclerView.setAdapter(attachedFilesAdapter);
    }

    public void addItem(List<Uri> uriList, String teamId, String channelId){
        for (Uri uri : uriList) {
            getPresenter().uploadFileToServer(getActivity(), teamId, channelId, uri);
        }
    }

    public List<String> getAttachedFiles(){
        // lambda requires min API level 24, so use old method
        List<String> fileNames = new ArrayList<>();
        for (FileToAttach fileToAttach : attachedFilesAdapter.getData()) {
            fileNames.add(fileToAttach.getFileName());
        }
        return fileNames;
    }

    public void setEmptyListListener(AttachedFilesAdapter.EmptyListListener emptyListListener) {
        attachedFilesAdapter.setEmptyListListener(emptyListListener);
    }
}
