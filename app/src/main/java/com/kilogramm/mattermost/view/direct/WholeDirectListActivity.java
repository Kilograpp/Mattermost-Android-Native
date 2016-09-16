package com.kilogramm.mattermost.view.direct;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityWholeDirectListBinding;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 14.09.16.
 */
@RequiresPresenter(WholeDirectListPresenter.class)
public class WholeDirectListActivity extends BaseActivity<WholeDirectListPresenter> {

    private ActivityWholeDirectListBinding binding;
    private WholeDirectListAdapter adapter;
    private OnDirectItemClickListener directItemClickListener;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.realm = Realm.getDefaultInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whole_direct_list);
        View view = binding.getRoot();
        init();
        setRecycleView();
    }

    private void init() {
        setupToolbar(getString(R.string.title_direct_list), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
        getPresenter().getProfilesForDirectMessage();
    }

    private void setRecycleView() {
        RealmResults<User> users = realm.where(User.class).isNotNull("id").findAllSorted("username");

        // TODO правильно провести логику нажатий и раскомментировать + конструктор в адаптере
        adapter = new WholeDirectListAdapter(this, users, true);
//                (itemId, name) -> {
//                    directItemClickListener.onDirectClick(itemId, name);
//                });

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        binding.recViewDirect.setLayoutManager(manager);
        binding.recViewDirect.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    //==========================MVP methods==================================================

    public void finishActivity() {
        finish();
    }

    //по нажатию на любого пользователя - открыть с ним диалог

    public void setDirectItemClickListener(OnDirectItemClickListener listener) {
        this.directItemClickListener = listener;
    }

    public interface OnDirectItemClickListener {
        void onDirectClick(String itemId, String name);
    }

}
