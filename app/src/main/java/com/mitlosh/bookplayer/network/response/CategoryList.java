package com.mitlosh.bookplayer.network.response;

import com.mitlosh.bookplayer.model.Category;

import java.util.List;

public class CategoryList extends BaseData{

    private List<Category> categories;

    public List<Category> getCategories() {
        return categories;
    }
}
