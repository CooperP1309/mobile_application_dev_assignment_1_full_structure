package com.example.assignment1.home.orders;

import android.util.Log;

import com.example.assignment1.home.dishes.BitmapConvert;

public class Order {

    int orderId, tableNo;
    String diningOption, dishes;
    double price;

    public Order(int theOrderId, int theTableNo,
                 String theDiningOption, String theDishes, double thePrice) {
        orderId = theOrderId;
        diningOption = theDiningOption;
        tableNo = theTableNo;
        dishes = theDishes;
        price = thePrice;
    }

    // constructor for reprocessing retrieved dish rows from db
    public Order(String string) {

        string = string.replaceFirst("\\.", ",");
        String[] orderArgs = string.split(",");

        // strip all args of whitespaces
        for (int i=0; i<5; i++) {
            orderArgs[i] = orderArgs[i].replaceAll("\\s+","");
        }

        Log.i("Order Constructing", "Converting the following... \n" +
                "Original string:\n" +
                string + "\n" +
                "Arg0: " + orderArgs[0] + "\n" +
                "Arg1: " + orderArgs[1] + "\n" +
                "Arg2: " + orderArgs[2] + "\n" +
                "Arg3: " + orderArgs[3] + "\n" +
                "Arg4: " + orderArgs[4]);

        // assign to class properties
        orderId = Integer.parseInt(orderArgs[0]);
        diningOption = orderArgs[1];
        tableNo = Integer.parseInt(orderArgs[2]);
        dishes = orderArgs[3];
        price = Double.parseDouble(orderArgs[4]);
    }

    public int getOrderId() {
        return orderId;
    }

    public String getDiningOption() {
        return diningOption;
    }

    public int getTableNo() {
        return tableNo;
    }

    public String getDishes() {
        return dishes;
    }

    public double getPrice() {
        return price;
    }
}
