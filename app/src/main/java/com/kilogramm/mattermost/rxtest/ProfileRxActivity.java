package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySettingsBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.view.BaseActivity;
import com.squareup.picasso.Picasso;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 19.10.2016.
 */
@RequiresPresenter(ProfileRxPresenter.class)
public class ProfileRxActivity extends BaseActivity<ProfileRxPresenter> {

    private static final String USER_ID = "user_id";

    private ActivitySettingsBinding mBinding;

    private String userId;

    public ProfileRxActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        userId = getIntent().getStringExtra(USER_ID);

        initView();

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

    }

    private void initView() {
        User user = UserRepository.query(new UserRepository.UserByIdSpecification(userId)).first();
        mBinding.headerUsername.setText(user.getUsername());
        mBinding.headerName.setText(String.format("%s %s",
                (user.getFirstName()!=null)?user.getFirstName():"",
                (user.getLastName()!=null)?user.getLastName():""));

        mBinding.name.setText(String.format("%s %s",
                (user.getFirstName()!=null)?user.getFirstName():"",
                (user.getLastName()!=null)?user.getLastName():""));
        mBinding.userName.setText(user.getUsername());
        mBinding.nickName.setText(user.getNickname());

        mBinding.email.setText(user.getEmail());
        Picasso.with(this)
                .load(getAvatarUrl())
                .error(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .placeholder(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .into(mBinding.headerPicture);
    }

    public String getAvatarUrl() {
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/users/"
                    + userId
                    + "/image";
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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

    private void onClickSave() {
        Snackbar.make(mBinding.getRoot(),"save click",Snackbar.LENGTH_SHORT).show();
    }

    public static void start(Context context, String userId) {
        Intent starter = new Intent(context, ProfileRxActivity.class);
        starter.putExtra(USER_ID, userId);
        context.startActivity(starter);
    }
}