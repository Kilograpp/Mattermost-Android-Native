package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by melkshake on 05.10.16.
 */

public class SearchParams {

    @SerializedName("terms")
    @Expose
    private String terms;
    @SerializedName("is_or_search")
    @Expose
    private Boolean is_or_search;

    public SearchParams(String terms, Boolean is_or_search) {
        this.terms = terms;
        this.is_or_search = is_or_search;
    }

    public SearchParams(String terms) {
        this.terms = terms;
        this.is_or_search = null;
    }

    public String getTerms() {
        return terms;
    }

    public boolean is_or_search() {
        return is_or_search;
    }

    @Override
    public String toString() {
        return "SearchParams{" +
                "terms='" + terms + '\'' +
                ", is_or_search=" + is_or_search +
                '}';
    }
}
