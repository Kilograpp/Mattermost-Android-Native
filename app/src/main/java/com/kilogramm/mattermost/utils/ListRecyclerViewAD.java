package com.kilogramm.mattermost.utils;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by ngers on 02.11.16.
 */

public abstract class ListRecyclerViewAD <T, VH extends RecyclerView.ViewHolder > extends  RecyclerView.Adapter<VH> {

    protected final LayoutInflater inflater;
    @Nullable
    private List<T> adapterData;
    @Nullable
    Context context;

    public ListRecyclerViewAD(Context context, List<T> adapterData) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }
        this.adapterData = adapterData;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public <T extends ViewDataBinding> T createBinding(ViewGroup parent, int layoutId){
       return   DataBindingUtil.inflate(
                inflater,
                layoutId,
                parent,
                false);
    }

    @Nullable
    public List<T> getrData() {
        return adapterData;
    }

    public void setData(@Nullable List<T> adapterData) {
        this.adapterData = adapterData;
    }

    public void updateData(@Nullable List<T> data) {
        this.adapterData = data;
        notifyDataSetChanged();
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }

    @Nullable
    public T getItem(int index) {
        //noinspection ConstantConditions
        return isDataValid() ? adapterData.get(index) : null;
    }

    @Override
    public int getItemCount() {
        return adapterData.size();
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.size() > 0;
    }
}
