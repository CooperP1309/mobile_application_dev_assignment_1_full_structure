package com.example.assignment1.home.dishes;

import android.graphics.Bitmap;
import android.net.Uri;

public class DishModel {
    private String text;
    private Uri image;
    private boolean isSelected = false;
    private boolean isSelectable = false;
    private boolean containsImage;
    public DishModel(String text, boolean selected) {
        this.text = text;
        isSelected = selected;
        isSelectable = true;
        containsImage = false;
    }

    // in fragment, sortModelList(); the uri from the db will be converted into
    // a bitmap and stored in this class object
    public DishModel(String text, boolean selected, Uri image) {
        this.text = text;
        this.image = image;
        isSelected = selected;
        isSelectable = true;
        containsImage = true;
    }

    // constructor for dish rows that act as lables (cant be selectable)
    public DishModel(String text, boolean selected, boolean selectable) {
        this.text = text;
        isSelected = false;
        isSelectable = selectable;
        containsImage = false;
    }

    public String getText() {
        return text;
    }
    public Uri getImage() { return image; }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public boolean isSelectable() { return isSelectable; }
    public boolean hasImage() { return containsImage; }
}