package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private static final String TAG = "EditProfileRxActivity";
    private static final int YOUR_SELECT_PICTURE_REQUEST_CODE = 1;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityEditProfileBinding mBinding;

    private User user;
    @State
    Uri outputFileUri;
    @State
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        mBinding.collapsingToolbar.setTitleEnabled(false);
        mBinding.toolbar.setTitle("title");

        this.user = UserRepository.query(new UserRepository.UserByIdSpecification(
                MattermostPreference.getInstance().getMyUserId())).first();
        this.user.addChangeListener(getUserListener());
        initView();

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        user.removeChangeListeners();
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
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {

                FileUtil.getInstance().getBitmap(outputFileUri, data)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<FileUtil.BitmapWithUri>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                showErrorText(e.getMessage());
                            }

                            @Override
                            public void onNext(FileUtil.BitmapWithUri bitmapWithUri) {
                                mBinding.headerPicture.setImageBitmap(bitmapWithUri.getBitmap());
                                selectedImageUri = bitmapWithUri.getUri();
                            }
                        });
            }
        } else if (resultCode == RESULT_CANCELED) {
        } else {
            // failed to capture image
            Toast.makeText(getApplicationContext(),
                    "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "mattermost" + File.separator);
        root.mkdir();
        final String fname = "img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntents.add(captureIntent);

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, YOUR_SELECT_PICTURE_REQUEST_CODE);
    }


    private RealmChangeListener<User> getUserListener() {
        return element -> invalidateView();
    }

    private void onClickSave() {
        boolean save = false;
        if(selectedImageUri != null){
            getPresenter().requestNewImage(selectedImageUri);
            save = true;
        }
        if (checkDifferences()) {
            User editedUser = new User(UserRepository.query(new UserRepository.UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first());
            editedUser.setUsername(mBinding.editUserName.getText().toString());
            editedUser.setNickname(mBinding.editNickName.getText().toString());
            editedUser.setFirstName(mBinding.editFirstName.getText().toString());
            editedUser.setLastName(mBinding.editLastName.getText().toString());
            getPresenter().requestSave(editedUser);
            save = true;
        }
        if(!save){
            Toast.makeText(this, "No changes.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkDifferences() {
        return (!mBinding.editFirstName.getText().toString().equals((user.getFirstName() != null) ? user.getFirstName() : "")
                || !mBinding.editLastName.getText().toString().equals((user.getLastName() != null) ? user.getLastName() : "")
                || !mBinding.editNickName.getText().toString().equals(user.getNickname())
                || !mBinding.editUserName.getText().toString().equals(user.getUsername()));
    }


    private void initView() {
        invalidateView();
        mBinding.notification.setOnClickListener(v -> NotificationActivity.start(this));

        mBinding.changeEmail.setOnClickListener(v -> EmailEditActivity.start(
                EditProfileRxActivity.this));

        mBinding.changePassword.setOnClickListener(v -> PasswordChangeActivity.start(this));

        mBinding.changeAvatar.setOnClickListener(v -> openImageIntent());

        Picasso.with(this)
                .load(getAvatarUrl())
                .error(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .placeholder(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .into(mBinding.headerPicture);
    }

    public void invalidateView() {
        mBinding.headerUsername.setText(user.getUsername());
        mBinding.headerName.setText(String.format("%s %s",
                (user.getFirstName() != null) ? user.getFirstName() : "",
                (user.getLastName() != null) ? user.getLastName() : ""));
        mBinding.editFirstName.setText(user.getFirstName());
        mBinding.editLastName.setText(user.getLastName());
        mBinding.editUserName.setText(user.getUsername());
        mBinding.editNickName.setText(user.getNickname());
    }

    public String getAvatarUrl() {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + MattermostPreference.getInstance().getMyUserId()
                + "/image?time="
                + user.getLastPictureUpdate();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, EditProfileRxActivity.class);
        context.startActivity(starter);
    }
}