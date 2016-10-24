package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AttachedFileLayoutBinding;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.squareup.picasso.Picasso;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by kepar on 7.10.16.
 */
public class AttachedFilesAdapter extends RealmRecyclerViewAdapter<FileToAttach, AttachedFilesAdapter.MyViewHolder> {
    private static final String TAG = "AttachedFilesAdapter";
    private Context context;

    private EmptyListListener emptyListListener;

    public AttachedFilesAdapter(Context context, RealmResults<FileToAttach> realmResults) {
        super(context, realmResults, true);
        this.context = context;
    }

    public AttachedFilesAdapter(Context context, RealmResults<FileToAttach> realmResults, EmptyListListener emptyListListener) {
        super(context, realmResults, true);
        this.context = context;
        this.emptyListListener = emptyListListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileToAttach fileToAttach = getData().get(position);

        /*Glide.with(context)
                .load(fileToAttach.getFilePath())
                .override(150, 150)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .error(context.getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .thumbnail(0.1f)
                .centerCrop()
                .into(holder.binding.imageView);*/

        Picasso.with(context)
                .load(fileToAttach.getFilePath())
                .resize(150, 150)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .error(context.getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .centerCrop()
                .into(holder.binding.imageView);

        if (fileToAttach.getProgress() < 100) {
            holder.binding.progressBar.setVisibility(View.VISIBLE);
            holder.binding.progressBar.setProgress(fileToAttach.getProgress());
            holder.binding.progressWait.setVisibility(View.GONE);
        } else {
            holder.binding.progressWait.setVisibility(View.GONE);
            holder.binding.progressBar.setVisibility(View.GONE);
            if (fileToAttach.getUploadState() == UploadState.UPLOADING) {
                holder.binding.progressWait.setVisibility(View.VISIBLE);
            } else if (fileToAttach.getUploadState() == UploadState.UPLOADED) {
                holder.binding.progressWait.setVisibility(View.GONE);
            }
        }
        holder.binding.close.setOnClickListener(v -> {
            if (fileToAttach.isValid()) {
                FileToAttachRepository.getInstance().remove(fileToAttach);
                if (emptyListListener != null && FileToAttachRepository.getInstance().getFilesForAttach().isEmpty()) {
                    emptyListListener.onEmptyList();
                }
            }
        });
    }

    public static class MyViewHolder extends RealmViewHolder {

        private AttachedFileLayoutBinding binding;

        private MyViewHolder(AttachedFileLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            AttachedFileLayoutBinding binding = AttachedFileLayoutBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }
    }

    public interface EmptyListListener {
        void onEmptyList();
    }

    public void setEmptyListListener(EmptyListListener emptyListListener) {
        this.emptyListListener = emptyListListener;
    }
}
