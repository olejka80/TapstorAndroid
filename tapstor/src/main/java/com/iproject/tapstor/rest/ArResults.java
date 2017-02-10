package com.iproject.tapstor.rest;

import com.iproject.tapstor.objects.Store;

public class ArResults {

    public String id;
    public String title;
    public String description;
    public int products;
    public int offers;
    public String address;
    public String phone;
    public int has_offers;
    public String avatar;
    public String lat;
    public String lng;
    public int news;
    public int elevation;
    public float distance;

    public void createfromStore(Store store, String avatar) {
        this.id = "";
        this.title = store.title;
        this.description = store.title;

        this.address = store.address;
        this.phone = store.phone;

        this.avatar = avatar;
        this.lat = store.lat;
        this.lng = store.lng;

    }

}

// "id": "444",
// "title": "iProject",
// "description": "Test\r\ntest",
// "products": "6",
// "offers": "0",
// "has_offers": 0,
// "avatar": "http://www.tapstorbusiness.com/echo/images/users/444l.jpg",
// "lat": "37.926969",
// "lng": "23.736121000000026",
// "address": "",
// "distance": "0.01754385667789324",
// "phone": null,
// "loc_id": "444",
// "news": "0",
// "elevation": "93"
