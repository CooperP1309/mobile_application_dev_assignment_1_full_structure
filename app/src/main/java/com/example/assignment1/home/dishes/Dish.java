package com.example.assignment1.home.dishes;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.assignment1.home.Model;

public class Dish {

    private int dishID;
    private String dishName, dishType, ingredients, image;
    private Double price;

    // constructor for addRecord use
    public Dish(String theDishName, String theDishType, String theIngredients
            , Double thePrice, String theImage) {
        dishName = theDishName;
        dishType = theDishType;
        ingredients = theIngredients;
        price = thePrice;
        image = theImage;
    }

    // constructor for updateRecord use (ID to reference record in db)
    public Dish(int theDishID, String theDishName, String theDishType, String theIngredients
            , Double thePrice, String theImage) {
        dishID = theDishID;
        dishName = theDishName;
        dishType = theDishType;
        ingredients = theIngredients;
        price = thePrice;
        image = theImage;
    }

    // constructor for reprocessing retrieved dish rows from db
    public Dish(String string) {
        //String[] dishArgs = string.trim().split("\\s+"); // Handles multiple spaces and trims
        String[] dishArgs = string.split(",");

        Log.i("Dish Conversion", "Converting the following... \n" +
                "Original string:\n" +
                string + "\n" +
                "Arg0: " + dishArgs[0] + "\n" +
                "Arg1: " + dishArgs[1] + "\n" +
                "Arg2: " + dishArgs[2] + "\n" +
                "Arg3: " + dishArgs[3] + "\n" +
                "Arg4: " + dishArgs[4] + "\n" +
                "Arg5: " + dishArgs[5]);

        dishID = Integer.parseInt(dishArgs[0]);
        dishName = dishArgs[1];
        dishType = dishArgs[2];
        ingredients = dishArgs[3];
        price = Double.parseDouble(dishArgs[4]);
        image = dishArgs[5];
    }

    public int getDishID() {
        return dishID;
    }

    public String getDishName() {
        return dishName;
    }

    public String getDishType() {
        return dishType;
    }

    public String getIngredients() {
        return ingredients;
    }

    public Double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setImage(String image){
        this.image = image;
    }
}
