package com.iproject.tapstor.rest;

/**
 * @author Grassos Konstantinos
 */
public class SendPostValueGetResults {

    private String token;
    private String type;
    private String tab;
    private String page;
    private String cat;
    private String sorting;
    private String sorting_val;
    private String keyword;
    private String lat;
    private String lng;

    /**
     * @param token       (do_login token)
     * @param type        1:companies 2:products 3:services
     * @param tab         1:popular 2:near_me 3:new
     * @param page        paging number
     * @param cat         category id
     * @param sorting     1:percentage 2:price_difference 3:price_range 4:near_me
     * @param sorting_val price range ex.90,00:100,00
     * @param keyword     text to search
     * @param lat         latitude
     * @param lng         longitude
     */
    public SendPostValueGetResults(String token, String type, String tab,
                                   String page, String cat, String sorting, String sorting_val,
                                   String keyword, String lat, String lng) {

        setCat(cat);
        setKeyword(keyword);
        setLat(lat);
        setLng(lng);
        setPage(page);
        setSorting(sorting);
        setSorting_val(sorting_val);
        setTab(tab);
        setToken(token);
        setType(type);

    }


    // type:
    // 1:companies
    // 2:products
    // 3:services
    // tab:
    // 1:popular
    // 2:near_me
    // 3:new
    // sorting:
    // 1:percentage
    // 2:price_difference
    // 3:price_range
    // 4:near_me

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getSorting_val() {
        return sorting_val;
    }

    public void setSorting_val(String sorting_val) {
        this.sorting_val = sorting_val;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

}

// {"tab":"3","type":"1","page":"0","sorting":"","sorting_val":"","token":"","cat":"","lat":"","lng":"","keyword":""}

// {
// "token":"8278344262131bcecb7cc49bc1790258a5139154", (do_login token)
// "type":"1", (1:companies,2:products,3:services)
// "tab":"2", (1:popular,2:near_me,3:new)
// "page":"0",
// "cat":"", (category id)
// "sorting":"1", (1:percentage,2:price_difference,3:price_range)
// "sorting_val":"90,00:100,00"
// "keyword":"test test1 test3.",
// "lat":"38",
// "lng":"27"
// } 