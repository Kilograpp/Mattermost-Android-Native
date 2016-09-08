package com.kilogramm.mattermost.viewmodel.chat;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.ui.FilesView;
import com.kilogramm.mattermost.viewmodel.ViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ItemChatViewModel extends BaseObservable implements ViewModel {

    private static final String TAG = "ItemChatViewModel";

    private Post post;
    private Context context;
    private ImageView targetImageView;
    private ObservableInt titleVisibility;

    public ItemChatViewModel(Context context, Post post){
        this.context = context;
        this.post = post;
        this.titleVisibility = new ObservableInt(View.GONE);
    }


    public String getMessage() {
        return post.getMessage();
    }

    public String getNick() {
        if(post.getUser()!=null)
            return post.getUser().getUsername();
        else
            return "";
    }

    public String getImageUrl(){
        if(post.getUser()!=null){
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/users/"
                    + post.getUser().getId()
                    + "/image";
        } else {
            return "";
        }
    }

    public String getTime(){
        if(post != null){
            Date postDate = new Date(post.getCreateAt());
            SimpleDateFormat format = new SimpleDateFormat("h:mm a");
            return format.format(postDate);
        } else {
            return "";
        }
    }

    public String getTitle(){
        if(post!=null){
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
            return format.format(post.getCreateAt());
        } else {
            titleVisibility.set(View.GONE);
            return  "";
        }
    }

    @BindingAdapter("bind:items")
    public static void setItems(FilesView v, Post post){
        for (String s : post.getFilenames()) {
            Log.d(TAG, "post "+ post.getMessage() +"\n"+ post.getFilenames());
        }
        v.setItems(post.getFilenames());
    }


    @Override
    public void destroy() {
        context = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void setPost(Post post) {
        this.post = post;
        notifyChange();
    }

    public Post getPost() {
        return post;
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        if(imageUrl!=null && imageUrl!="") {
            Picasso.with(view.getContext())
                    .load(imageUrl)
                    .resize(60, 60)
                    .error(view.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(view.getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(view);
        }
    }


    public ObservableInt getTitleVis() {
        return titleVisibility;
    }

    public void setTitleVisibility(Integer titleVisibility) {
        this.titleVisibility.set(titleVisibility);
    }
}