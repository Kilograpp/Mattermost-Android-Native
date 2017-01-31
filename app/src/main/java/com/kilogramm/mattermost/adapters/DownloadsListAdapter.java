package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDownloadsListBinding;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;

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
        if (fileInfo == null || !fileInfo.isValid()) return;
        holder.mBinding.textViewName.setText(fileInfo.getmName());
        holder.mBinding.textViewSize
                .setText(FileUtil.getInstance().convertFileSize(fileInfo.getmSize()));

        holder.mBinding.getRoot().setOnClickListener(getFileClickListener(fileInfo));
    }

    private View.OnClickListener getFileClickListener(FileInfo fileInfo) {
        return v -> {
            Intent intent = FileUtil.getInstance().createOpenFileIntent(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator + fileInfo.getmName());
            if (intent != null && intent.resolveActivityInfo(MattermostApp.getSingleton()
                    .getApplicationContext().getPackageManager(), 0) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context,
                        context.getString(R.string.no_suitable_app),
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    public static class MyViewHolder extends RealmViewHolder {

        private ItemDownloadsListBinding mBinding;

        public MyViewHolder(ItemDownloadsListBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ItemDownloadsListBinding binding = ItemDownloadsListBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }
    }
}
