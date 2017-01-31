package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ItemDownloadsListBinding;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.tools.FileUtil;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by kepar on 30.01.17.
 */

public class DownloadsListAdapter extends RealmRecyclerViewAdapter<FileInfo, DownloadsListAdapter.MyViewHolder> {

    public DownloadsListAdapter(Context context, RealmResults<FileInfo> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileInfo fileInfo = getItem(position);
        if(fileInfo == null || !fileInfo.isValid()) return;
        holder.mItemDirectListBinding.textViewName.setText(fileInfo.getmName());
        holder.mItemDirectListBinding.textViewSize
                .setText(FileUtil.getInstance().convertFileSize(fileInfo.getmSize()));
    }

    public static class MyViewHolder extends RealmViewHolder{

        private ItemDownloadsListBinding mItemDirectListBinding;

        public MyViewHolder(ItemDownloadsListBinding binding) {
            super(binding.getRoot());
            this.mItemDirectListBinding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ItemDownloadsListBinding binding = ItemDownloadsListBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }
    }
}
