package com.kilogramm.mattermost.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FilesItemLayoutBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.realmstring.RealmStringRepository;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.viewPhoto.ViewPagerWGesturesActivity;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

/**
 * Created by Evgeny on 01.09.2016.
 */
public class FilesView extends GridLayout {

    private static final String TAG = "FilesView";

    private List<FileInfo> fileList = new ArrayList<>();

    public FilesView(Context context) {
        super(context);
        init(context);
    }

    public FilesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FilesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public FilesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.file_view_layout, this);
    }

// region commented
    /*public void setItems(List<String> items) {
        Log.d(TAG, "items count: " + items.size());
        clearView();
        if (items != null && items.size() != 0) {
            fileList = items;
            for (String fileName : items) {
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.
                        from(getContext()), R.layout.files_item_layout, this, false);
                FileDownloadManager.FileDownloadListener fileDownloadListener =
                        createDownloadListener(binding);
                binding.downloadFileControls.setControlsClickListener(
                        createControlsClickListener(fileName, fileDownloadListener, binding)
                );

                File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator
                        + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));


                FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileName);
                if (fileToAttach != null &&
                        (fileToAttach.getUploadState() == UploadState.DOWNLOADING ||
                                fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD)) {
                    binding.downloadFileControls.showProgressControls();
                    FileDownloadManager.getInstance().addListener(fileName, fileDownloadListener);
                } else if (fileToAttach != null &&
                        fileToAttach.getUploadState() == UploadState.DOWNLOADED) {
                    setupFileClickListeners(binding, fileName);
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                } else if (fileToAttach != null && file.exists()) {
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                } else {
                    binding.downloadFileControls.hideProgressControls();
                }

                String extension = FileUtil.getInstance().getMimeType(fileName);
                if (extension != null && extension.contains("image")) {
                    binding.image.setVisibility(VISIBLE);
                    binding.circleFrame.setVisibility(GONE);

                    binding.title.setOnClickListener(v -> {
                        if (file.exists()) {
                            createDialog(fileName, binding);
                        } else {
                            downloadFile(fileName, createDownloadListener(binding));
                        }
                    });


                    initAndAddItem(binding, fileName);

                    binding.image.setOnClickListener(view ->
                            ViewPagerWGesturesActivity.start(getContext(),
                                    binding.title.getText().toString(),
                                    fileName,
                                    (ArrayList<String>) fileList)
                    );
                } else {
                    initAndAddItem(binding, fileName);
                }
            }
        } else {
            clearView();
        }
    }*/
    //endregion

    public void setFileForPost(String postId) {
        clearView();
        List<FileInfo> items = FileInfoRepository.getInstance().queryForPostId(postId);
        if (items != null && items.size() != 0) {
            fileList.clear();
            fileList.addAll(items);
            for (FileInfo fileInfo : items) {
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.
                        from(getContext()), R.layout.files_item_layout, this, false);
                FileDownloadManager.FileDownloadListener fileDownloadListener =
                        createDownloadListener(binding);
                binding.downloadFileControls.setControlsClickListener(
                        createControlsClickListener(fileInfo, fileDownloadListener, binding)
                );

                File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator
                        + fileInfo.getmName());

                if (fileInfo.getUploadState() == UploadState.DOWNLOADING ||
                        fileInfo.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD) {
                    binding.downloadFileControls.showProgressControls();
                    FileDownloadManager.getInstance().addListener(fileInfo, fileDownloadListener);
                } else if (fileInfo.getUploadState() == UploadState.DOWNLOADED) {
                    setupFileClickListeners(binding, fileInfo);
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                } else if (file.exists()) {
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                } else {
                    binding.downloadFileControls.hideProgressControls();
                }

                if (fileInfo.getmMimeType() != null && fileInfo.getmMimeType().contains("image")) {
                    binding.image.setVisibility(VISIBLE);
                    binding.circleFrame.setVisibility(GONE);

                    binding.title.setOnClickListener(v -> {
                        if (file.exists()) {
                            createDialog(fileInfo, binding);
                        } else {
                            downloadFile(fileInfo, createDownloadListener(binding));
                        }
                    });

                    initAndAddItem(binding, fileInfo);

                    binding.image.setOnClickListener(view ->
                            ViewPagerWGesturesActivity.start(getContext(),
                                    binding.title.getText().toString(),
                                    fileInfo,
                                    (ArrayList<FileInfo>) fileList)
                    );
                } else {
                    initAndAddItem(binding, fileInfo);
                }
            }
        } else {
            clearView();
        }
    }

    private void setupFileClickListeners(FilesItemLayoutBinding binding, FileInfo fileInfo) {
        OnClickListener fileClickListener = v -> {
            Intent intent = FileUtil.getInstance().createOpenFileIntent(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator + fileInfo.getmName());
            if (intent != null && intent.resolveActivityInfo(MattermostApp.getSingleton()
                    .getApplicationContext().getPackageManager(), 0) != null) {
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.no_suitable_app),
                        Toast.LENGTH_SHORT).show();
            }
        };

        binding.icDownloadedFile.setOnClickListener(fileClickListener);
        binding.title.setOnClickListener(fileClickListener);
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, FileInfo fileInfo) {
        binding.title.setText(fileInfo.getmName());
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageOnLoading(R.drawable.slices)
                .showImageOnFail(R.drawable.slices)
                .resetViewBeforeLoading(true)
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
        ImageLoader.getInstance().displayImage(thumb_url, binding.image, options);
        binding.fileSize.setText(FileUtil.getInstance().convertFileSize(fileInfo.getmSize()));

        this.addView(binding.getRoot());
    }

    private FileDownloadManager.FileDownloadListener createDownloadListener(FilesItemLayoutBinding binding) {
        return new FileDownloadManager.FileDownloadListener() {
            @Override
            public void onComplete(String fileId) {
                binding.downloadFileControls.post(() -> {
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                    FileInfo fileInfo = FileInfoRepository.getInstance().get(fileId);
                    if (fileInfo != null && fileInfo.isValid()) {
                        setupFileClickListeners(binding, fileInfo);
                    }
                });
            }

            @Override
            public void onProgress(int percantage) {
                binding.downloadFileControls.post(() ->
                        binding.downloadFileControls.setProgress(percantage));
            }

            @Override
            public void onError(String fileId) {
                binding.downloadFileControls.post(() -> Toast.makeText(getContext(),
                        getContext().getString(R.string.error_during_file_download),
                        Toast.LENGTH_SHORT).show());

            }
        };
    }

    private DownloadFileControls.ControlsClickListener createControlsClickListener(FileInfo fileInfo,
                                                                                   FileDownloadManager.FileDownloadListener fileDownloadListener,
                                                                                   FilesItemLayoutBinding binding) {
        return new DownloadFileControls.ControlsClickListener() {
            @Override
            public void onClickDownload() {
                File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator
                        + fileInfo.getmName());
                if (file.exists()) {
                    binding.downloadFileControls.post(() -> createDialog(fileInfo, binding));
                } else {
                    downloadFile(fileInfo, fileDownloadListener);
                }
                if (fileInfo.getUploadState() == UploadState.DOWNLOADING ||
                        fileInfo.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD) {
                    binding.downloadFileControls.showProgressControls();
                }
            }

            @Override
            public void onClickCancel() {
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), null);
                FileDownloadManager.getInstance().stopDownloadCurrentFile(fileInfo);
            }
        };
    }

    private void createDialog(FileInfo fileInfo, FilesItemLayoutBinding binding) {
        FileDownloadManager.FileDownloadListener fileDownloadListener = createDownloadListener(binding);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getContext().getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            binding.downloadFileControls.hideProgressControls();
            dialog.dismiss();
        });
        builder.setPositiveButton(R.string.replace, (dialog, which) ->{
                FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                + File.separator
                + fileInfo.getmName());
                downloadFile(fileInfo, fileDownloadListener);
        });
        builder.setNeutralButton(R.string.open_file, (dialog, which) -> {
            Intent intent = null;
            intent = FileUtil.getInstance().
                    createOpenFileIntent(
                            FileUtil.getInstance().getDownloadedFilesDir()
                                    + File.separator
                                    + fileInfo.getmName());
            if (intent != null && intent.resolveActivityInfo(MattermostApp.getSingleton()
                    .getApplicationContext().getPackageManager(), 0) != null) {
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.no_suitable_app),
                        Toast.LENGTH_SHORT).show();
            }
            binding.downloadFileControls.hideProgressControls();
        });
        builder.show();
    }

    private void downloadFile(FileInfo fileInfo, FileDownloadManager.FileDownloadListener fileDownloadListener) {
        FileDownloadManager.getInstance().addItem(fileInfo, fileDownloadListener);
    }

    private void clearView() {
        fileList.clear();
        this.removeAllViews();
    }

    public static class AuthDownloader extends BaseImageDownloader {

        public AuthDownloader(Context context) {
            super(context);
        }

        @Override
        protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
            HttpURLConnection conn = super.createConnection(url, extra);
            Map<String, String> headers = (Map<String, String>) extra;
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            return conn;
        }
    }
}
