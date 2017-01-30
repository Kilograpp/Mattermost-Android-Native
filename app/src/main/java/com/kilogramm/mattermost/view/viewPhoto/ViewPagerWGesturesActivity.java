package com.kilogramm.mattermost.view.viewPhoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityPhotoViewerBinding;
import com.kilogramm.mattermost.model.FileDownloadManager;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by melkshake on 08.11.16.
 */

public class ViewPagerWGesturesActivity extends BaseActivity implements FileDownloadManager.FileDownloadListener {

    public static final String OPENED_FILE = "OPENED_FILE";
    public static final String TITLE = "title";
    public static final String PHOTO_LIST = "photo_list";
    private static final String TAG = "ViewPagerWGesturesAct";
    private static final String ANIMDATA = "animdata";
    private static final int ANIM_DURATION = 300;

    private ActivityPhotoViewerBinding binding;
    private TouchImageAdapter adapter;

    private ArrayList<FileInfo> photosList = new ArrayList<>();
    private FileInfo mOpenedFile;
    private ImageView mIconDownload;


    ImageViewerAnimator animator;

    Toast errorToast;

    public static void start(Context context, String title, String openedFile, ArrayList<String> photoList, int[] screenLocation, int[] size) {
        Intent starter = new Intent(context, ViewPagerWGesturesActivity.class);

        int orientation = context.getResources().getConfiguration().orientation;
        starter.putExtra(TITLE, title)
                .putExtra(OPENED_FILE, openedFile)
                .putStringArrayListExtra(PHOTO_LIST, photoList)
                .putExtra(ANIMDATA + ".orientation", orientation)
                .putExtra(ANIMDATA + ".left", screenLocation[0])
                .putExtra(ANIMDATA + ".top", screenLocation[1])
                .putExtra(ANIMDATA + ".width", size[0])
                .putExtra(ANIMDATA + ".height", size[1]);

        context.startActivity(starter);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_viewer);
        mIconDownload = (ImageView) findViewById(R.id.action_download);

        animator = new ImageViewerAnimator(this, getIntent().getExtras(),
                binding.viewPager, binding.background, mIconDownload);

        if(savedInstanceState == null) animator.startAnimation(() -> animateToolabar(getToolbarTitle()));


        List<String> photosIdList = getIntent().getStringArrayListExtra(PHOTO_LIST);
        for (String photoId : photosIdList) {
            photosList.add(FileInfoRepository.getInstance().get(photoId));
        }
        String mOpenedFileId = getIntent().getStringExtra(OPENED_FILE);
        mOpenedFile = FileInfoRepository.getInstance().get(mOpenedFileId);

        adapter = new TouchImageAdapter(getSupportFragmentManager(), photosList);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(photosList.indexOf(mOpenedFile));


        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                String toolbarTitle = (position + 1) + " of " + photosList.size();
                setupToolbar(toolbarTitle, true);
                setupDownloadIcon(photosList.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupDownloadIcon(mOpenedFile);
        findViewById(R.id.action_download).setOnClickListener(v -> {
            FileInfo fileInfo = photosList.get(binding.viewPager.getCurrentItem());
            File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator
                    + fileInfo.getmName());
            if (file.exists()) {
                createDialog(fileInfo);
            } else {
                downloadFile(fileInfo);
            }
        });
    }

    private String getToolbarTitle(){
        String toolbarTitle = "";
        if (mOpenedFile != null) {
            int index = 0;
            for (FileInfo fileInfo : photosList) {
                ++index;
                if (fileInfo.getId().equals(mOpenedFile.getId())) {
                    toolbarTitle = index + " of " + photosList.size();
//                    binding.viewPager.setCurrentItem(index - 1);
                }
            }
        }
        return toolbarTitle;
    }
    private void animateToolabar(String title){
//        setTransparentActionBar(0);
        if(title == null) title = "";
        setupToolbar(title, true);
//        setColorScheme(R.color.black, R.color.black);

    }

    private void setupDownloadIcon(FileInfo fileInfo) {
        if (fileInfo.getUploadState() == UploadState.WAITING_FOR_DOWNLOAD
                || fileInfo.getUploadState() == UploadState.DOWNLOADING) {
            mIconDownload.setVisibility(View.GONE);
        } else {
            mIconDownload.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void finish() {
        animator.finishWithAnimation(super::finish);
    }

    private void createDialog(FileInfo fileInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.replace, (dialog, which) -> {
            FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator
                    + fileInfo);
            downloadFile(fileInfo);
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
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        getString(R.string.no_suitable_app),
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void downloadFile(FileInfo fileInfo) {
        findViewById(R.id.action_download).setVisibility(View.GONE);
        FileDownloadManager.getInstance().addItem(fileInfo, this);
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
            if (errorToast != null) errorToast.cancel();
            errorToast = Toast.makeText(this,
                    this.getString(R.string.error_during_file_download),
                    Toast.LENGTH_SHORT);
            errorToast.show();
        });
    }

    public void setBackgroundAlpha(float alpha){
        animator.setBackgroundAlpha(alpha);
    }

    public void setBackgroundAlpha(float alpha, int duration){
        animator.setBackgroundAlpha(alpha, duration);
    }

//    public void setTransparent(float v) {
////        getWindow().getDecorView().setAlpha(1);
////        binding.getRoot().setAlpha(Math.abs(1 - v / 100));
////        binding.getRoot().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
////        binding.getRoot().setAlpha(v);
//    }

}

