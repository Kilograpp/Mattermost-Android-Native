package com.kilogramm.mattermost.rxtest;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityEditProfileBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.settings.EmailEditActivity;
import com.kilogramm.mattermost.view.settings.NotificationActivity;
import com.kilogramm.mattermost.view.settings.PasswordChangeActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import icepick.State;
import io.realm.RealmChangeListener;
import nucleus.factory.RequiresPresenter;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 31.10.2016.
 */

@RequiresPresenter(EditProfileRxPresenter.class)
public class EditProfileRxActivity extends BaseActivity<EditProfileRxPresenter> {

    private static final int REQUEST_CODE_YOUR_SELECT_PICTURE = 1;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 7;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityEditProfileBinding mBinding;
    private ProgressDialog mProgressDialog;
    private MenuItem mMenuItem;

    private User mUser;
    @State
    Uri outputFileUri;
    @State
    Uri selectedImageUri;
    @State
    boolean isCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        mBinding.collapsingToolbar.setTitleEnabled(false);
        mBinding.toolbar.setTitle("title");

        this.mUser = UserRepository.query(new UserRepository.UserByIdSpecification(
                MattermostPreference.getInstance().getMyUserId())).first();
        this.mUser.addChangeListener(getUserListener());
        initView();

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.edit_profile));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUser.removeChangeListeners();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (title != null && !title.toString().isEmpty()) {
            mBinding.collapsingToolbar.setTitle(title.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                mMenuItem = item;
                onClickSave();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_YOUR_SELECT_PICTURE) {
                    boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null || data.getData() == null;
                    }
                    Uri uri = isCamera ? outputFileUri : data.getData();
                    setAvatar(uri);
                    selectedImageUri = uri;
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Failed to capture image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showDialog();
                }
                break;
        }
    }

    public void invalidateView() {
        mBinding.headerUsername.setText(mUser.getUsername());
        mBinding.headerName.setText(String.format("%s %s",
                (mUser.getFirstName() != null) ? mUser.getFirstName() : "",
                (mUser.getLastName() != null) ? mUser.getLastName() : ""));
        mBinding.editFirstName.setText(mUser.getFirstName());
        mBinding.editLastName.setText(mUser.getLastName());
        mBinding.editUserName.setText(mUser.getUsername());
        mBinding.editNickName.setText(mUser.getNickname());
    }

    public String getAvatarUrl() {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + MattermostPreference.getInstance().getMyUserId()
                + "/image?time="
                + mUser.getLastPictureUpdate();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, EditProfileRxActivity.class);
        context.startActivity(starter);
    }

    public void onUpdateComplete() {
        hideProgressBar();
    }

    private void showProgressBar() {
        BaseActivity.hideKeyboard(this);
        mMenuItem.setVisible(false);
        mBinding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mMenuItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.GONE);
    }

    private void setAvatar(final Uri bitmapUri) {
        FileUtil.getInstance().getBitmap(FileUtil.getInstance().getFileByUri(bitmapUri), 16)
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
                            exif = new ExifInterface(FileUtil.getInstance()
                                    .getFileByUri(bitmapUri));
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                            boolean wasRotation = false;
                            Matrix matrix = new Matrix();
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                                matrix.postRotate(90);
                                wasRotation = true;
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                                matrix.postRotate(180);
                                wasRotation = true;
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                matrix.postRotate(270);
                                wasRotation = true;
                            }
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(),
                                    myBitmap.getHeight(), matrix, true);
                            // TODO Проверить с другими файлами, мб тоже их надо руками переворачивать
                            if (wasRotation) {
                                writeBitmapToFile(myBitmap);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mBinding.headerPicture.setImageBitmap(myBitmap);
                    }
                });
    }

    private void writeBitmapToFile(final Bitmap bitmap) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.data_processing_progress_layout);
        new Thread(() -> {
            FileOutputStream out = null;
            try {
                final File root = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + "/Mattermost");
                root.mkdir();
                final String fname = "img_" + System.currentTimeMillis() + ".jpg";
                final File sdImageMainDirectory = new File(root, fname);
                out = new FileOutputStream(sdImageMainDirectory);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                selectedImageUri = Uri.fromFile(sdImageMainDirectory);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mBinding.changeAvatar.post(() -> {
                    if (mProgressDialog != null) mProgressDialog.cancel();
                });
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_buttom_sheet, null);

        final Dialog mBottomSheetDialog = new Dialog(this, R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

        view.findViewById(R.id.layCamera).setOnClickListener(v -> {
            isCamera = true;
            final File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "mattermost" + File.separator);
            root.mkdir();
            final String fname = "img_" + System.currentTimeMillis() + ".jpg";
            final File sdImageMainDirectory = new File(root, fname);
            outputFileUri = Uri.fromFile(sdImageMainDirectory);

            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(captureIntent, REQUEST_CODE_YOUR_SELECT_PICTURE);
            mBottomSheetDialog.cancel();
        });

        view.findViewById(R.id.layGallery).setOnClickListener(v -> {
            isCamera = false;
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_CODE_YOUR_SELECT_PICTURE);
            mBottomSheetDialog.cancel();
        });

        view.findViewById(R.id.layFile).setOnClickListener(v -> {
            isCamera = false;
            final Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
            startActivityForResult(chooserIntent, REQUEST_CODE_YOUR_SELECT_PICTURE);
            mBottomSheetDialog.cancel();
        });
    }

    private RealmChangeListener<User> getUserListener() {
        return element -> invalidateView();
    }

    private void onClickSave() {
        showProgressBar();
        boolean save = false;
        if (selectedImageUri != null) {
            getPresenter().requestNewImage(selectedImageUri);
            save = true;
        }
        if (checkDifferences()) {
            User editedUser = new User(UserRepository.query(new UserRepository
                    .UserByIdSpecification(MattermostPreference.getInstance().getMyUserId()))
                    .first());
            editedUser.setUsername(mBinding.editUserName.getText().toString());
            editedUser.setNickname(mBinding.editNickName.getText().toString());
            editedUser.setFirstName(mBinding.editFirstName.getText().toString());
            editedUser.setLastName(mBinding.editLastName.getText().toString());
            getPresenter().requestSave(editedUser);
            save = true;
        }
        if (!save) {
            Toast.makeText(this, "No changes", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    private boolean checkDifferences() {
        return (!mBinding.editFirstName.getText().toString().equals((mUser.getFirstName() != null)
                ? mUser.getFirstName() : "")
                || !mBinding.editLastName.getText().toString().equals((mUser.getLastName() != null)
                ? mUser.getLastName() : "")
                || !mBinding.editNickName.getText().toString().equals(mUser.getNickname())
                || !mBinding.editUserName.getText().toString().equals(mUser.getUsername()));
    }


    private void initView() {
        invalidateView();
        mBinding.notification.setOnClickListener(v -> NotificationActivity.start(this));

        mBinding.changeEmail.setOnClickListener(v -> EmailEditActivity.start(
                EditProfileRxActivity.this));

        mBinding.changePassword.setOnClickListener(v -> PasswordChangeActivity.start(this));

        mBinding.changeAvatar.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_CAMERA_PERMISSION);
                } else {
                    showDialog();
                }
            } else {
                showDialog();
            }
        });

        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .showImageOnFail(R.drawable.ic_person_grey_24dp)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().displayImage(getAvatarUrl(), mBinding.headerPicture, options);
    }
}