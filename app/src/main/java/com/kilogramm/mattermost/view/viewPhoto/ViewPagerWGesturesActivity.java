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

public class ViewPagerWGesturesActivity extends BaseActivity {

    public static final String IMAGE_URL = "image_url";
    public static final String TITLE = "title";
    public static final String PHOTO_LIST = "photo_list";

    private ActivityPhotoViewerBinding binding;
    private TouchImageAdapter adapter;

    private ArrayList<String> photosList;
    private String clickedImageUri;

    private Menu menu;

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

        setupToolbar(binding.toolbar, toolbarTitle, true);
        setColorScheme(R.color.black, R.color.black);

        adapter = new TouchImageAdapter(getSupportFragmentManager(), photosList);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(photosList.indexOf(clickedImageUri));
        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //String[] parsedName = photosList.get(position).split("/");
                //setupToolbar(parsedName[parsedName.length - 1], true);
            }

            @Override
            public void onPageSelected(int position) {
                String toolbarTitle = (position + 1) + " из " + photosList.size();
                setupToolbar(toolbarTitle, true);
 /*               FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(photosList.get(position));
                if(fileToAttach != null && fileToAttach.getUploadState() == UploadState.DOWNLOADED){
                    menu.findItem(R.id.action_download).setVisible(false);
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_download, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download:
                String fileId = photosList.get(binding.viewPager.getCurrentItem());
                File file = new File(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator
                        + FileUtil.getInstance().getFileNameFromIdDecoded(fileId));
                if (file.exists()) {
                    createDialog(fileId);
                } else {
                    downloadFile(fileId);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialog(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.file_exists));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.replace, (dialog, which) ->downloadFile(fileName));
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

    private void downloadFile(String fileName){
        findViewById(R.id.action_download).setVisibility(View.GONE);
        FileDownloadManager.getInstance().addItem(fileName);
    }
}
