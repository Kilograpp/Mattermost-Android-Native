package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FilesItemLayoutBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.tools.FileUtils;
import com.kilogramm.mattermost.view.ImageViewerActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

/**
 * Created by Evgeny on 01.09.2016.
 */
public class FilesView extends GridLayout {

    private static final String TAG = "FilesView";

    private static final String PNG = "png";
    private static final String JPG = "jpg";

    private List<String> fileList = new ArrayList<>();

    public FilesView(Context context) {
        super(context);
        init(context, null);
    }

    public FilesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FilesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FilesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        inflate(context, R.layout.file_view_layout, this);
    }

    public void setItems(List<String> items) {
        clearView();
        if(items!=null && items.size()!=0) {
            fileList = items;
            for (String s : items) {
                FilesItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.files_item_layout, this,false);
                switch (FileUtils.getFileType(s)) {
                    case PNG:
                        initAndAddItem(binding,getImageUrl(s));
                        binding.image.setOnClickListener(view -> {
                            Toast.makeText(getContext(), "image open", Toast.LENGTH_SHORT).show();
                            ImageViewerActivity.start(getContext(),
                                    binding.image,
                                    binding.title.getText().toString(),
                                    getImageUrl(s));

                        });
                        break;
                    case JPG:
                        initAndAddItem(binding,getImageUrl(s));
                        binding.image.setOnClickListener(view -> {
                            ImageViewerActivity.start(getContext(),
                                    binding.image,
                                    binding.title.getText().toString(),
                                    getImageUrl(s));

                        });
                        break;
                    default:
                        initAndAddItem(binding,getImageUrl(s));
                        break;
                }
            };
        } else {
            clearView();
        }
    }

    private void clearView() {
        fileList.clear();
        this.removeAllViews();
    }

    private void initAndAddItem(FilesItemLayoutBinding binding, String url) {
        //Log.d(TAG, url);
        Pattern pattern = Pattern.compile(".*?([^\\/]*$)");
        Matcher matcher = pattern.matcher(url);
        String title = "";
        if(matcher.find()){
            title = matcher.group(1);
        }
        try {
            binding.title.setText(URLDecoder.decode(title,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            binding.title.setText(title);
        }
        Glide.with(getContext())
                .load(url)
                .override(150,150)
                .placeholder(R.drawable.ic_attachment_grey_24dp)
                .error(R.drawable.ic_attachment_grey_24dp)
                .thumbnail(0.1f)
                .into(binding.image);
        this.addView(binding.getRoot());
    }

    private String getImageUrl(String id){
        Realm realm = Realm.getDefaultInstance();
        String s = new String(realm.where(Team.class).findFirst().getId());
        realm.close();
        if(id!=null){
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/teams/"
                    + s
                    + "/files/get" +  id;
        } else {
            return "";
        }
    }

}
