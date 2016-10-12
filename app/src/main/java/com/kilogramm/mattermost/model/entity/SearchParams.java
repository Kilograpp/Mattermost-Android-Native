package com.kilogramm.mattermost.model.entity;

/**
 * Created by melkshake on 05.10.16.
 */

public class SearchParams {
    private String terms;
    private boolean is_or_search;

    public SearchParams(String terms, boolean is_or_search) {
        this.terms = terms;
        this.is_or_search = is_or_search;
    }

    public String getTerms() {
        return terms;
    }

    public boolean is_or_search() {
        return is_or_search;
    }
}
