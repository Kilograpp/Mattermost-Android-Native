package com.kilogramm.mattermost.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.realmstring.RealmStringRepository;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.ImageViewerActivity;
import com.kilogramm.mattermost.view.viewPhoto.ViewPagerWGesturesActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

import static com.kilogramm.mattermost.view.ImageViewerActivity.startActivity;

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
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.
                        from(getContext()), R.layout.files_item_layout, this, false);
                FileDownloadManager.FileDownloadListener fileDownloadListener =
                        createDownloadListener(binding);
                binding.downloadFileControls.setControlsClickListener(
                        createControlsClickListener(fileName, fileDownloadListener, binding)
                );

                FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileName);
                if (fileToAttach != null &&
                        (fileToAttach.getUploadState() == UploadState.DOWNLOADING ||
                                fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD)) {
                    binding.downloadFileControls.showProgressControls();
                    FileDownloadManager.getInstance().addListener(fileName, fileDownloadListener);
                } else if (fileToAttach != null &&
                        fileToAttach.getUploadState() == UploadState.DOWNLOADED) {
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                }

                ArrayList<String> photoUriList = new ArrayList<>();
                for (String fileItem : fileList) {
                    photoUriList.add(getImageUrl(fileItem));
                }

                switch (FileUtil.getInstance().getFileType(fileName)) {
                    case PNG:
                    case JPG:
                        binding.image.setVisibility(VISIBLE);
                        binding.circleFrame.setVisibility(GONE);
                        initAndAddItem(binding, fileName);

                        binding.image.setOnClickListener(view ->
//                            ImageViewerActivity.start(getContext(),
//                                    binding.image,
//                                    binding.title.getText().toString(),
//                                    getImageUrl(fileName));

                            ViewPagerWGesturesActivity.start(getContext(),
                                    binding.title.getText().toString(),
                                    fileName,
                                    (ArrayList<String>) fileList)

                        );
                        break;
                    default:
                        initAndAddItem(binding, fileName);
                        break;
                }
            }
        } else {
            clearView();
        }
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, String fileName) {
        if (backgroundColorId != null)
            binding.root.setBackground(backgroundColorId);
        String url = getImageUrl(fileName);
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
                .resize(300, 300)
                .centerCrop()
                .placeholder(getContext().getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .error(getContext().getResources().getDrawable(R.drawable.ic_attachment_grey_24dp))
                .into(binding.image);
        this.addView(binding.getRoot());

        RealmString realmString = RealmStringRepository.getInstance().get(fileName);
        if(realmString != null) {
            if(realmString.getFileSize() <= 0) {
                new Thread(() -> {
                    long fileSize = getRemoteFileSize(url);
                    Log.d(TAG, String.valueOf(fileSize));
                    if (fileSize > 0) {
                        binding.fileSize.post(() -> {
                            binding.materialProgressBar.setVisibility(GONE);
                            binding.fileSize.setText(FileUtil.getInstance()
                                .convertFileSize(fileSize));
                            RealmStringRepository.getInstance().updateFileSize(fileName, fileSize);
                        });
                    }
                }).start();
            } else {
                binding.materialProgressBar.setVisibility(GONE);
                binding.fileSize.setText(FileUtil.getInstance().convertFileSize(realmString.getFileSize()));
            }
        }
    }

    private FileDownloadManager.FileDownloadListener createDownloadListener(FilesItemLayoutBinding binding) {
        return new FileDownloadManager.FileDownloadListener() {
            @Override
            public void onComplete(String fileId) {
                binding.downloadFileControls.post(() -> {
                    binding.downloadFileControls.setVisibility(GONE);
                    binding.icDownloadedFile.setVisibility(VISIBLE);
                });
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
        };
    }

    private DownloadFileControls.ControlsClickListener createControlsClickListener(String fileName,
                                                                                   FileDownloadManager.FileDownloadListener fileDownloadListener,
                                                                                   FilesItemLayoutBinding binding) {
        return new DownloadFileControls.ControlsClickListener() {
            @Override
            public void onClickDownload() {
                File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator
                        + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));
                if (file.exists()) {
                    binding.downloadFileControls.post(() -> createDialog(fileName, binding));
                } else {
                    downloadFile(fileName, fileDownloadListener);
                }
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
                FileToAttachRepository.getInstance().remove(fileName);
                FileDownloadManager.getInstance().stopDownloadCurrentFile(fileName);
            }
        };
    }

    private void createDialog(String fileName, FilesItemLayoutBinding binding) {

        FileDownloadManager.FileDownloadListener fileDownloadListener = createDownloadListener(binding);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getContext().getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            binding.downloadFileControls.hideProgressControls();
            dialog.dismiss();
        });
        builder.setPositiveButton(R.string.replace, (dialog, which) ->
                downloadFile(fileName, fileDownloadListener));
        builder.setNeutralButton(R.string.open_file, (dialog, which) -> {
            Intent intent = null;
            intent = FileUtil.getInstance().
                    createOpenFileIntent(
                            FileUtil.getInstance().getDownloadedFilesDir()
                                    + File.separator
                                    + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));
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

    private long getRemoteFileSize(String fileUrl) {
        try {
            Log.d(TAG, fileUrl);
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
            Log.d(TAG, urlConnection.getResponseMessage());
            final long file_size = Long.parseLong(urlConnection.getHeaderField("Content-Length"));
            urlConnection.disconnect();
            return file_size;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0L;
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private void downloadFile(String fileName, FileDownloadManager.FileDownloadListener fileDownloadListener) {
        FileDownloadManager.getInstance().addItem(fileName, fileDownloadListener);
    }

    private void clearView() {
        fileList.clear();
        this.removeAllViews();
    }
}
