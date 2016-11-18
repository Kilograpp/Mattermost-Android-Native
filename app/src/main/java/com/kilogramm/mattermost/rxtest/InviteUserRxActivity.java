package com.kilogramm.mattermost.rxtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.InviteUserAdapter;
import com.kilogramm.mattermost.databinding.ActivityInviteUserBinding;
import com.kilogramm.mattermost.model.fromnet.InviteObject;
import com.kilogramm.mattermost.model.fromnet.ListInviteObj;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 18.10.2016.
 */
@RequiresPresenter(InviteUserRxPresenter.class)
public class InviteUserRxActivity extends BaseActivity<InviteUserRxPresenter> {

    private ActivityInviteUserBinding mBinding;
    private RecyclerView mRecyclerView;
    private InviteUserAdapter mAdapter;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_invite_user);
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new InviteUserAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(new InviteObject());

        View view = getLayoutInflater().inflate(R.layout.item_invite_list_footer, null);
        view.setOnClickListener(v -> {
                    mAdapter.setShouldCheckNullFields(false);
                    mAdapter.add(new InviteObject());
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                }
        );
        mAdapter.addFooter(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        int position = mAdapter.isAllValid();
        if (position < 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.data_processing_progress_layout);

            ListInviteObj obj = new ListInviteObj();
            obj.getInvites().addAll(mAdapter.getData());
            getPresenter().requestInvite(obj);
        } else {
            if(position == (mAdapter.getItemCount() - mAdapter.getFooterItemCount() - 1)){
                position += 1;
            }
            mRecyclerView.smoothScrollToPosition(position);
            showError(getString(R.string.fields_invalid));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupToolbar("Invite", true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, InviteUserRxActivity.class);
        context.startActivity(starter);
    }

    public void showError(String s) {
        Toast.makeText(InviteUserRxActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    public void onOkInvite() {
        if(progressDialog != null) progressDialog.dismiss();
        Toast.makeText(InviteUserRxActivity.this, getString(R.string.invitations_were_sended), Toast.LENGTH_SHORT).show();
        finish();
    }
}
