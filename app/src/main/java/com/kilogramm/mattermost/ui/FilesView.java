package com.kilogramm.mattermost.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FilesItemLayoutBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.viewPhoto.ViewPagerWGesturesActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Evgeny on 01.09.2016.
 */
public class FilesView extends GridLayout {

    private static final String TAG = "FilesView";

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

    public void setFileForPost(String postId) {
        clearView();
        List<FileInfo> items = FileInfoRepository.getInstance().queryForPostId(postId);
        if (items != null && items.size() != 0) {
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

                binding.icDownloadedFile.setOnClickListener(getFileClickListener(fileInfo));

                if (fileInfo.getUploadState() == UploadState.DOWNLOADING ||
                        fileInfo.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD) {
                    binding.downloadFileControls.showProgressControls();
                    FileDownloadManager.getInstance().addListener(fileInfo, fileDownloadListener);
                } else if (fileInfo.getUploadState() == UploadState.DOWNLOADED) {
                    if (file.exists()) {
                        binding.title.setOnClickListener(getFileClickListener(fileInfo));
                        binding.downloadFileControls.setVisibility(GONE);
                        binding.icDownloadedFile.setVisibility(VISIBLE);
                    } else {
                        binding.downloadFileControls.hideProgressControls();
                    }
//                    setupFileClickListeners(binding, fileInfo);
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

                    binding.image.setOnClickListener(view -> {
                        ArrayList<String> fileIdList = new ArrayList<>();
                        FileInfo clicked = items.get(0);

                        for (FileInfo item : items) {
                            if (item.getmMimeType() != null
                                    && item.getmMimeType().contains("image")) {
                                fileIdList.add(item.getId());
                                if (fileInfo.getId() == item.getId())
                                    clicked = item;
                            }
                        }

                        int[] location = new int[2];
                        int[] size = new int[]{view.getWidth(), clicked.getHeight()};
                        binding.image.getLocationOnScreen(location);

                        ViewPagerWGesturesActivity.start(getContext(),
                                binding.title.getText().toString(),
                                fileInfo.getId(),
                                fileIdList,
                                location, size);
                    });
                } else {
                    initAndAddItem(binding, fileInfo);
                }
            }
        } else {
            clearView();
        }
    }

    private OnClickListener getFileClickListener(FileInfo fileInfo) {
        return v -> {
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
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, FileInfo fileInfo) {
        binding.title.setText(fileInfo.getmName());
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)// it's the default value, so it's not necessary to use it
                .imageScaleType(ImageScaleType.NONE)
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
        ImageLoader.getInstance().displayImage(thumb_url, binding.image, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                resizeImageView(loadedImage, binding.image);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
//        ImageLoader.getInstance().displayImage(thumb_url, binding.image, options);
        binding.fileSize.setText(FileUtil.getInstance().convertFileSize(fileInfo.getmSize()));

        this.addView(binding.getRoot());
    }

    private void resizeImageView(Bitmap image, ImageView imageView) {
        int displayWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int displayHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        float scale = getContext().getResources().getDisplayMetrics().density;
        int maxHeight = (int) (displayHeight * 0.6);
        int widthPaddings = (int) (115 * scale);

        int imageWidth = displayWidth - widthPaddings;
        int ratio = imageWidth / image.getWidth();
        int imageHeight = image.getHeight() * ratio;
        imageView.getLayoutParams().width = imageWidth;
        imageView.getLayoutParams().height = ((imageHeight > maxHeight) ? maxHeight : imageHeight);

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
                        binding.icDownloadedFile.setOnClickListener(getFileClickListener(fileInfo));
                        binding.title.setOnClickListener(getFileClickListener(fileInfo));
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
                    // TODO убрать, если не потребуется. Этот метод итак вызывается при клике на иконку
                    // загрузки в DownloadFilesControls
//                    binding.downloadFileControls.showProgressControls();
                }
            }

            @Override
            public void onClickCancel() {
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(),
                        UploadState.IN_LIST);
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

        builder.setPositiveButton(R.string.replace, (dialog, which) -> {
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

    private void downloadFile(FileInfo fileInfo,
                              FileDownloadManager.FileDownloadListener fileDownloadListener) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.no_premission_for_download),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            FileDownloadManager.getInstance().addItem(fileInfo, fileDownloadListener);
        }
    }

    private void clearView() {
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
