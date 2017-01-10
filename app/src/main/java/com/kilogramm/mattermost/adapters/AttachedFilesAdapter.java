package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.AttachedFileLayoutBinding;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.IOException;

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

    private EmptyListListener mEmptyListListener;

    public AttachedFilesAdapter(Context context, RealmResults<FileToAttach> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileToAttach fileToAttach = getItem(holder.getAdapterPosition());
        if (fileToAttach == null) return;

        holder.binding.fileName.setText(FileUtil.getInstance().getFileNameFromIdDecoded(fileToAttach.getFileName()));

        String mimeType = FileUtil.getInstance().getMimeType(fileToAttach.getFilePath());

        if (mimeType != null && mimeType.contains("image")) {
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
                            ExifInterface exif;
                            try {
                                exif = new ExifInterface(fileToAttach.getFilePath());
                                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                                Log.d("ORIENTATION", "" + orientation);
                                Matrix matrix = new Matrix();
                                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                                    matrix.postRotate(90);
                                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                                    matrix.postRotate(180);
                                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                    matrix.postRotate(270);
                                }
                                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(),
                                        myBitmap.getHeight(), matrix, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            holder.binding.imageView.setImageBitmap(myBitmap);
                        }
                    });
        } else {
            holder.binding.imageView.setVisibility(View.GONE);
        }
        if (fileToAttach.getProgress() > 0 && fileToAttach.getProgress() < 100) {
            holder.binding.progressBar.setVisibility(VISIBLE);
            holder.binding.progressBar.setProgress(fileToAttach.getProgress());
            holder.binding.progressWait.setVisibility(View.GONE);
        } else {
            holder.binding.progressBar.setVisibility(View.GONE);
            if (fileToAttach.getUploadState() == UploadState.UPLOADING
                    || fileToAttach.getUploadState() == UploadState.WAITING_FOR_UPLOAD) {
                holder.binding.progressWait.setVisibility(VISIBLE);
            } else {
                holder.binding.progressWait.setVisibility(View.GONE);
            }
        }
        holder.binding.close.setOnClickListener(v -> {
            FileToAttach fileToAttach1 = getItem(position);
            if (fileToAttach1 != null && fileToAttach1.isValid()) {
                FileToAttachRepository.getInstance().remove(getItem(holder.getAdapterPosition()));
                if (mEmptyListListener != null && FileToAttachRepository.getInstance().getFilesForAttach().isEmpty()) {
                    mEmptyListListener.onEmptyList();
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
        this.mEmptyListListener = emptyListListener;
    }
}
