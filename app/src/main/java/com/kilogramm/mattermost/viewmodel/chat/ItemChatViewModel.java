package com.kilogramm.mattermost.viewmodel.chat;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.ui.FilesView;
import com.kilogramm.mattermost.viewmodel.ViewModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ItemChatViewModel extends BaseObservable implements ViewModel {

    private static final String TAG = "ItemChatViewModel";

    private Post post;
    private ObservableInt titleVisibility;
    private ObservableInt controlMenuVisibility;
    private ObservableInt progressSendVisibility;
    private ObservableInt progressErrorSendVisibility;

    public ItemChatViewModel(Post post){
        this.post = post;
        this.titleVisibility = new ObservableInt(View.GONE);
        this.controlMenuVisibility = new ObservableInt(View.VISIBLE);
        this.progressSendVisibility = new ObservableInt(View.VISIBLE);
        this.progressErrorSendVisibility = new ObservableInt(View.VISIBLE);
    }

    public ItemChatViewModel(){

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
       return getUrl(post);
    }

    public String getUrl(Post post) {
        if(post.getUser()!=null && !post.isSystemMessage()){
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/users/"
                    + post.getUser().getId()
                    + "/image?time="
                    + post.getUser().getLastPictureUpdate();
        } else {
            return null;
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

    public ObservableInt getControlMenuVisibility() {
        if (post.getUpdateAt() != null && post.getUpdateAt() != Post.NO_UPDATE
                && post.getProps()== null)
            return post.isSystemMessage() ? new ObservableInt(View.GONE) : controlMenuVisibility;
        return new ObservableInt(View.GONE);
    }

    public ObservableInt getProgressSendVisibility() {//// TODO: 12.01.17
        if (post.getUpdateAt() == null)
            return progressSendVisibility;
        else
            return new ObservableInt(View.GONE);
    }

    public ObservableInt getProgressErrorSendVisibility() {
        Log.i("PRFIX", "SET STATE: TIME: " + System.currentTimeMillis());
        if (post.getUpdateAt() != null && post.getUpdateAt() == Post.NO_UPDATE)
            return progressErrorSendVisibility;
        else
            return new ObservableInt(View.GONE);

    }

    @BindingAdapter("bind:items")
    public static void setItems(FilesView v, Post post){
        if(post!=null) {
//            v.setItems(post.getFilenames());
            v.setFileForPost(post.getId());
        }
    }

    @Override
    public void destroy() {

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
        if (imageUrl != null) {
            view.setRotation(0);
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
        } else {
            view.setImageResource(R.drawable.ic_system_grey_24dp);
        }
    }

    public ObservableInt getTitleVis() {
        return titleVisibility;
    }

    public void setTitleVisibility(Integer titleVisibility) {
        this.titleVisibility.set(titleVisibility);
    }
}