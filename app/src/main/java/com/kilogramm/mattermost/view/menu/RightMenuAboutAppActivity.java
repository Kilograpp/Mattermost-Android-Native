package com.kilogramm.mattermost.view.menu;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityRightMenuAboutAppBinding;
import com.kilogramm.mattermost.view.BaseActivity;

/**
 * Created by melkshake on 02.11.16.
 */

public class RightMenuAboutAppActivity extends BaseActivity {

    private ActivityRightMenuAboutAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_right_menu_about_app);

        setupToolbar("About Mattermost", true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        binding.mattermostOrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.mattermost.org/"));
                startActivity(intent);
            }
        });

        binding.kilograppTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://kilograpp.com/"));
                startActivity(intent);
            }
        });
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, RightMenuAboutAppActivity.class);
        context.startActivity(starter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
