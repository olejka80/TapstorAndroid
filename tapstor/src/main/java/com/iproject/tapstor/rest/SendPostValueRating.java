package com.iproject.tapstor.rest;

public class SendPostValueRating {

    private String type;
    private String id;
    private String rating;
    private String comment;
    private String token;

    /**
     * // {"type":"1","id":"453","rating":"4", //
     * "comment":"to sxolio mou einai auto vazo 4 asteria gia afto ton logo", //
     * "token":"76e4320862d0b301c806eaa5cdf651c3f566b3f9"}
     */
    public SendPostValueRating(String type, String id, String rating,
                               String comment, String token) {

        setComment(comment);
        setId(id);
        setRating(rating);
        setToken(token);
        setType(type);

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
