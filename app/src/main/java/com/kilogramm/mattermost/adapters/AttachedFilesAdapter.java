package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AttachedFileLayoutBinding;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.View.VISIBLE;

/**
 * Created by kepar on 7.10.16.
 */
public class AttachedFilesAdapter extends RealmRecyclerViewAdapter<FileToAttach, AttachedFilesAdapter.MyViewHolder> {

    private EmptyListListener emptyListListener;
    Context context;

    public AttachedFilesAdapter(Context context, RealmResults<FileToAttach> realmResults) {
        super(context, realmResults, true);
        this.context = context;
    }

    public AttachedFilesAdapter(Context context, RealmResults<FileToAttach> realmResults, EmptyListListener emptyListListener) {
        super(context, realmResults, true);
        this.emptyListListener = emptyListListener;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileToAttach fileToAttach = getData().get(position);
        holder.binding.fileName.setText(FileUtil.getInstance().getFileNameFromIdDecoded(fileToAttach.getFileName()));
        switch (FileUtil.getInstance().getFileType(fileToAttach.getFileName())) {
            case FileUtil.PNG:
            case FileUtil.JPG:
                holder.binding.imageView.setVisibility(VISIBLE);

                FileUtil.getInstance().getBitmap(fileToAttach.getFilePath(), 16)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Bitmap>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Bitmap myBitmap) {
                                holder.binding.imageView.setImageBitmap(myBitmap);
                            }
                        });

                break;
            default:
                holder.binding.imageView.setVisibility(View.GONE);
                break;
        }


        if (fileToAttach.getProgress() < 100) {
            holder.binding.progressBar.setVisibility(VISIBLE);
            holder.binding.progressBar.setProgress(fileToAttach.getProgress());
            holder.binding.progressWait.setVisibility(View.GONE);
        } else {
            holder.binding.progressWait.setVisibility(View.GONE);
            holder.binding.progressBar.setVisibility(View.GONE);
            if (fileToAttach.getUploadState() == UploadState.UPLOADING) {
                holder.binding.progressWait.setVisibility(VISIBLE);
//                holder.binding.progressWait.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
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
