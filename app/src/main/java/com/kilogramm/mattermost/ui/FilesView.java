package com.kilogramm.mattermost.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FilesItemLayoutBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.ImageViewerActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

/**
 * Created by Evgeny on 01.09.2016.
 */
public class FilesView extends GridLayout {

    private static final String TAG = "FileDownloadManager";

    private static final String PNG = "png";
    private static final String JPG = "jpg";

    private List<String> fileList = new ArrayList<>();
    private Drawable backgroundColorId;

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

    public void setBackgroundColorComment() {
        backgroundColorId = getResources().getDrawable(R.drawable.files_item_background_comment);
    }

    public void setItems(List<String> items) {
        clearView();
        if (items != null && items.size() != 0) {
            fileList = items;
            for (String fileName : items) {
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.files_item_layout, this, false);

                binding.downloadFileControls.setControlsClickListener(new DownloadFileControls.ControlsClickListener() {
                    @Override
                    public void onClickDownload() {
                        try {
                            File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                                    + File.separator
                                    + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));
                            if (file.exists()) {
                                binding.downloadFileControls.post(() -> createDialog(fileName, binding));
                            } else {
                                downloadFile(fileName, binding);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            downloadFile(fileName, binding);
                        }
//                        downloadFile(fileName, binding);

                        FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileName);
                        if (fileToAttach != null) {
                            if (fileToAttach.getUploadState() == UploadState.DOWNLOADING ||
                                    fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD) {
                                binding.downloadFileControls.showProgressControls();
                            }
                        }
                    }

                    @Override
                    public void onClickCancel() {
                        FileToAttachRepository.getInstance().updateUploadStatus(fileName, UploadState.IN_LIST);
                        FileDownloadManager.getInstance().stopDownloadCurrentFile(fileName);
                    }
                });

                FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileName);
                if (fileToAttach != null) {
                    if (fileToAttach.getUploadState() == UploadState.DOWNLOADING ||
                            fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD) {
                        binding.downloadFileControls.showProgressControls();
                    }
                }

                switch (FileUtil.getInstance().getFileType(fileName)) {
                    case PNG:
                        initAndAddItem(binding, getImageUrl(fileName));
                        binding.image.setOnClickListener(view -> {
                            Toast.makeText(getContext(), "image open", Toast.LENGTH_SHORT).show();
                            ImageViewerActivity.start(getContext(),
                                    binding.image,
                                    binding.title.getText().toString(),
                                    getImageUrl(fileName));

                        });
                        break;
                    case JPG:
                        initAndAddItem(binding, getImageUrl(fileName));
                        binding.image.setOnClickListener(view -> ImageViewerActivity.start(getContext(),
                                binding.image,
                                binding.title.getText().toString(),
                                getImageUrl(fileName)));
                        break;
                    default:
                        initAndAddItem(binding, getImageUrl(fileName));
                        break;
                }
            }
        } else {
            clearView();
        }
    }

    private void downloadFile(String fileName, FilesItemLayoutBinding binding) {
        FileDownloadManager.getInstance().addItem(fileName, new FileDownloadManager.FileDownloadListener() {
            @Override
            public void onComplete(String fileId) {
                binding.downloadFileControls.post(() ->
                        binding.downloadFileControls.setVisibility(GONE));
            }

            @Override
            public void onProgress(int percantage) {
                binding.downloadFileControls.post(() ->
                        binding.downloadFileControls.setProgress(percantage));
            }

            @Override
            public void onError(String fileId) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.error_during_file_download),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDialog(String fileName, FilesItemLayoutBinding binding) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getContext().getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            binding.downloadFileControls.hideProgressControls();
            dialog.dismiss();
        });
        builder.setPositiveButton(R.string.replace, (dialog, which) ->
                downloadFile(fileName, binding));
        builder.setNeutralButton(R.string.open_file, (dialog, which) -> {
            Intent intent = null;
            try {
                intent = FileUtil.getInstance().
                        createOpenFileIntent(
                                FileUtil.getInstance().getDownloadedFilesDir()
                                        + File.separator
                                        + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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

    private void clearView() {
        fileList.clear();
        this.removeAllViews();
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, String url) {
        if (backgroundColorId != null)
            binding.root.setBackground(backgroundColorId);
        Pattern pattern = Pattern.compile(".*?([^\\/]*$)");
        Matcher matcher = pattern.matcher(url);
        String title = "";
        if (matcher.find()) {
            title = matcher.group(1);
        }
        try {
            binding.title.setText(URLDecoder.decode(title, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            binding.title.setText(title);
        }
        Picasso.with(getContext())
                .load(url)
                .resize(150, 150).centerCrop()
                .placeholder(getContext().getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .error(getContext().getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .into(binding.image);
        this.addView(binding.getRoot());
    }

    private String getImageUrl(String id) {
        Realm realm = Realm.getDefaultInstance();
        String s = realm.where(Team.class).findFirst().getId();
        if (id != null) {
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/teams/"
                    + s
                    + "/files/get" + id;
        } else {
            return "";
        }
    }
}
