package com.iproject.tapstor.objects;

import com.google.gson.annotations.SerializedName;
import com.iproject.tapstor.rest.ArResults;

public class Results {

    public String avatar;
    public String id;
    public String name;
    public String stamp;

    public String company;
    public String featured;
    public ImagesTapstor images;
    public Offer offer;

    public String lat;
    public String lng;
    public int elevation;

    public float distance;

    public int news;

    @SerializedName("new")
    public int isNew; // 0 not new, 1 is new

    public int has_offers;// 0 no offers, 1 has offers

    public void setResultFromArResult(ArResults ar) {

        avatar = ar.avatar;
        id = ar.id;
        name = ar.title;
        lat = ar.lat;
        lng = ar.lng;

    }

}

// "id": "444",
// "title": "iProject",
// "description": "Test\r\ntest",
// "products": "6",
// "offers": "1",
// "has_offers": 1,
// "avatar": "http://www.tapstorbusiness.com/echo/images/users/444l.jpg",
// "lat": "37.926969",
// "lng": "23.736121000000026",
// "news": "0",
// "elevation": "93"

//
// "id": "1319",
// "news": "2",
// "name": "Tapstor",
// "new": "1",
// "avatar": "http://www.tapstorbusiness.com/echo/images/users/1319l.jpg",
// "counter": "19"

// "id": "133",
// "name": "1 neo proion",
// "company": "444",
// "featured": false,
// "images": {
// "company": "http://www.tapstorbusiness.com/echo/images/users/444m.jpg",
// "small": "http://www.tapstorbusiness.com/echo/files/33.jpg"
// },
// "offer": {
// "sell_price": "200.00",
// "offer_price": "145.00",
// "deal_from": "2014-07-01 00:00:00",
// "deal_to": "2014-08-07 00:00:00"
// }