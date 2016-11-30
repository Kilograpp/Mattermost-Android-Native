package com.kilogramm.mattermost.view.viewPhoto;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.BaseActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by melkshake on 08.11.16.
 */

public class ViewPagerWGesturesActivity extends BaseActivity implements FileDownloadManager.FileDownloadListener {

    public static final String IMAGE_URL = "image_url";
    public static final String TITLE = "title";
    public static final String PHOTO_LIST = "photo_list";

    private ActivityPhotoViewerBinding binding;
    private TouchImageAdapter adapter;

    private ArrayList<String> photosList;
    private String clickedImageUri;

    Toast errorToast;

    public static void start(Context context, String title, String imageUrl, ArrayList<String> photoList) {
        Intent starter = new Intent(context, ViewPagerWGesturesActivity.class);
        starter
                .putExtra(TITLE, title)
                .putExtra(IMAGE_URL, imageUrl)
                .putExtra(PHOTO_LIST, photoList);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_viewer);

        photosList = getIntent().getStringArrayListExtra(PHOTO_LIST);
        clickedImageUri = getIntent().getStringExtra(IMAGE_URL);

        String toolbarTitle = (photosList.indexOf(clickedImageUri) + 1)
                + " из " + photosList.size();

        setupToolbar(toolbarTitle, true);
        setColorScheme(R.color.black, R.color.black);

        adapter = new TouchImageAdapter(getSupportFragmentManager(), photosList);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(photosList.indexOf(clickedImageUri));
        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                String toolbarTitle = (position + 1) + " из " + photosList.size();
                setupToolbar(toolbarTitle, true);
                setupDownloadIcon(photosList.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupDownloadIcon(null);
        findViewById(R.id.action_download).setOnClickListener(v -> {
            String fileId = photosList.get(binding.viewPager.getCurrentItem());
            File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator
                    + FileUtil.getInstance().getFileNameFromIdDecoded(fileId));
            if (file.exists()) {
                createDialog(fileId);
            } else {
                downloadFile(fileId);
            }
        });
    }

    private void setupDownloadIcon(String fileName) {
        String workingFileUri = fileName != null ? fileName : clickedImageUri;
        FileToAttach fileToAttach = FileToAttachRepository.getInstance().
                get(workingFileUri);
        File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                + File.separator
                + FileUtil.getInstance().getFileNameFromIdDecoded(workingFileUri));

        if(fileToAttach != null && fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD){
            findViewById(R.id.action_download).setVisibility(View.GONE);
        } else if(fileToAttach != null && file.exists()){
            findViewById(R.id.action_download).setVisibility(View.GONE);
        } else {
            FileToAttachRepository.getInstance().updateUploadStatus(workingFileUri,
                    UploadState.WAITING_FOR_DOWNLOAD);
            findViewById(R.id.action_download).setVisibility(View.VISIBLE);
        }
/*
        if(fileToAttach != null && fileToAttach.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD
                || fileToAttach != null && file.exists()) {
            findViewById(R.id.action_download).setVisibility(View.GONE);
        } else {
            FileToAttachRepository.getInstance().updateUploadStatus(workingFileUri,
                    UploadState.WAITING_FOR_DOWNLOAD);
            findViewById(R.id.action_download).setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialog(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.replace, (dialog, which) -> downloadFile(fileName));
        builder.setNeutralButton(R.string.open_file, (dialog, which) -> {
            Intent intent = null;
            intent = FileUtil.getInstance().
                    createOpenFileIntent(
                            FileUtil.getInstance().getDownloadedFilesDir()
                                    + File.separator
                                    + FileUtil.getInstance().getFileNameFromIdDecoded(fileName));
            if (intent != null && intent.resolveActivityInfo(MattermostApp.getSingleton()
                    .getApplicationContext().getPackageManager(), 0) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        getString(R.string.no_suitable_app),
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void downloadFile(String fileName) {
        findViewById(R.id.action_download).setVisibility(View.GONE);
//        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        FileDownloadManager.getInstance().addItem(fileName, this);
    }

    @Override
    public void onComplete(String fileId) {
    }

    @Override
    public void onProgress(int percantage) {
    }

    @Override
    public void onError(String fileId) {
        binding.viewPager.post(() -> {
            findViewById(R.id.action_download).setVisibility(View.VISIBLE);
            if(errorToast != null) errorToast.cancel();
            errorToast = Toast.makeText(this,
                    this.getString(R.string.error_during_file_download),
                    Toast.LENGTH_SHORT);
            errorToast.show();
        });
    }
}
