package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityInviteUserBinding;
import com.kilogramm.mattermost.model.fromnet.InviteObject;
import com.kilogramm.mattermost.model.fromnet.ListInviteObj;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 18.10.2016.
 */
@RequiresPresenter(InviteUserRxPresenter.class)
public class InviteUserRxActivity extends BaseActivity<InviteUserRxPresenter> {

    private ActivityInviteUserBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_invite_user);
        initView();
    }

    private void initView() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.invite:
                onClickInvite();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickInvite() {
        String email = mBinding.lInvite.editEmail.getText().toString();
        String firstName = mBinding.lInvite.editFirstName.getText().toString();
        String lastName = mBinding.lInvite.editFirstName.getText().toString();
        if(isValidEmail(email)){
            ListInviteObj obj = new ListInviteObj();
            obj.getInvites().add(new InviteObject(email,firstName,lastName));
            getPresenter().requestInvite(obj);
        } else {
            showError("Email is not valid.");
        }
    }

    private boolean isValidEmail(String email) {
        Pattern p = Patterns.EMAIL_ADDRESS;
        Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupToolbar("Invite",true);
        setColorScheme(R.color.colorPrimary,R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, InviteUserRxActivity.class);
        context.startActivity(starter);
    }

    public void showError(String s) {
        Toast.makeText(InviteUserRxActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    public void onOkInvite() {
        Toast.makeText(InviteUserRxActivity.this, "on invite ok notify", Toast.LENGTH_SHORT).show();
    }
}
