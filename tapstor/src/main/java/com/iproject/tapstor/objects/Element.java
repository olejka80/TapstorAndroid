package com.iproject.tapstor.objects;

import java.util.List;

public class Element {

    public int id;
    public String title;
    public String description;
    public int ratings;
    public String average_rating;
    public String phone;
    public String address;
    public String lat;
    public String lng;

    public int elevation;
    public int favorite;
    public List<Store> stores;
    public Item items;
    public String avatar;

    public Availability availability;

    public Rating my_rating;

    public Store closestStore;
    public float calculatedDistance;


    public String has_products;

    public boolean hasProducts() {
        return has_products.equals("1");
    }

}
