package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDownloadsListBinding;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.tools.FileUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

        if(fileInfo.getmMimeType().contains("image")) {
            holder.mBinding.icDownloadedFile.setVisibility(View.GONE);
            holder.mBinding.imageViewImage.setVisibility(View.VISIBLE);

            Map<String, String> headers = new HashMap();
            headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .showImageOnLoading(R.drawable.slices)
                    .showImageOnFail(R.drawable.slices)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .extraForDownloader(headers)
                    .considerExifParams(true)
                    .build();

            String thumb_url = "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/files/"
                    + fileInfo.getId()
                    + "/get_thumbnail";

            ImageLoader.getInstance().displayImage(thumb_url, holder.mBinding.imageViewImage, options);
        } else {
            holder.mBinding.icDownloadedFile.setVisibility(View.VISIBLE);
            holder.mBinding.imageViewImage.setVisibility(View.GONE);
        }



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
