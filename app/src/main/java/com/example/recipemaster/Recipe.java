package com.example.recipemaster;

import java.util.List;

public class Recipe {
    private String title;
    private String description;
    private List<String> ingredients;
    private List<String> preparing;
    private List<String> imgs;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getPreparing() {
        return preparing;
    }

    public List<String> getImgs() {
        return imgs;
    }
}
