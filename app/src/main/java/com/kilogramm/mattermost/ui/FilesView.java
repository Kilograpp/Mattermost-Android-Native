package com.kilogramm.mattermost.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FilesItemLayoutBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.ImageViewerActivity;

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

    private static final String TAG = "FilesView";

    private static final String PNG = "png";
    private static final String JPG = "jpg";

    private List<String> fileList = new ArrayList<>();
    private Drawable backgroundColorId;

    public FilesView(Context context) {
        super(context);
        init(context, null);
    }

    public FilesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FilesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public FilesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.file_view_layout, this);
    }

    public void setBackgroundColorComment() {
        backgroundColorId = getResources().getDrawable(R.drawable.files_item_background_comment);
    }

    public void setItems(List<String> items) {
        clearView();
        if (items != null && items.size() != 0) {
            fileList = items;
            for (String s : items) {
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.files_item_layout, this, false);
                FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(s);
                if(fileToAttach != null) {
                    if (fileToAttach.getUploadState() == UploadState.DOWNLOADING) {
                        binding.downloadFileControls.showProgressControls();
                    } else if (fileToAttach.getUploadState() == UploadState.DOWNLOADED) {
                        binding.downloadFileControls.setVisibility(GONE);
                    }
                }
                binding.downloadFileControls.setControlsClickListener(new DownloadFileControls.ControlsClickListener() {
                    @Override
                    public void onClickDownload() {
                        FileDownloadManager.getInstance().addItem(s, new FileDownloadManager.FileDownloadListener() {
                            @Override
                            public void onComplete(String fileId) {
                                binding.downloadFileControls.post(() -> binding.downloadFileControls.setVisibility(GONE));
                            }

                            @Override
                            public void onError(String fileId) {

                            }
                        });
                    }

                    @Override
                    public void onClickCancel() {

                    }
                });
                switch (FileUtil.getInstance().getFileType(s)) {
                    case PNG:
                        initAndAddItem(binding, getImageUrl(s));
                        binding.image.setOnClickListener(view -> {
                            Toast.makeText(getContext(), "image open", Toast.LENGTH_SHORT).show();
                            ImageViewerActivity.start(getContext(),
                                    binding.image,
                                    binding.title.getText().toString(),
                                    getImageUrl(s));

                        });
                        break;
                    case JPG:
                        initAndAddItem(binding, getImageUrl(s));
                        binding.image.setOnClickListener(view -> {
                            ImageViewerActivity.start(getContext(),
                                    binding.image,
                                    binding.title.getText().toString(),
                                    getImageUrl(s));

                        });
                        break;
                    default:
                        initAndAddItem(binding, getImageUrl(s));
                        break;
                }
            }
        } else {
            clearView();
        }
    }

    private void clearView() {
        fileList.clear();
        this.removeAllViews();
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, String url) {
        //Log.d(TAG, url);
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
        Glide.with(getContext())
                .load(url)
                .override(150, 150)
                .placeholder(R.drawable.ic_attachment_grey_24dp)
                .error(R.drawable.ic_attachment_grey_24dp)
                .thumbnail(0.1f)
                .into(binding.image);
        this.addView(binding.getRoot());
    }

    private String getImageUrl(String id) {
        Realm realm = Realm.getDefaultInstance();
        String s = new String(realm.where(Team.class).findFirst().getId());
        realm.close();
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
