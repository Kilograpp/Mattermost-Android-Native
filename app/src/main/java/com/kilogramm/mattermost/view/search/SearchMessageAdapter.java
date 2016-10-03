package com.kilogramm.mattermost.view.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ItemSearchResultBinding;
import com.kilogramm.mattermost.model.entity.SearchResult;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessageAdapter extends RealmRecyclerViewAdapter<SearchResult, SearchMessageAdapter.MyViewHolder> {



    public SearchMessageAdapter(Context context, @Nullable OrderedRealmCollection<SearchResult> data, boolean autoUpdate) {
        super(context, data, autoUpdate);
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }





    public static class MyViewHolder extends RealmViewHolder {

        private ItemSearchResultBinding binding;

        private MyViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            binding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ItemSearchResultBinding bindingSearchResult = ItemSearchResultBinding.inflate(inflater, parent, false);
            return new MyViewHolder(bindingSearchResult);
        }

        public void bindTo(SearchResult searchResult) {


        }

        public ItemSearchResultBinding getmBinding() {
            return binding;
        }
    }
}
