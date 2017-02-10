package com.iproject.tapstor.objects;

/*
 * @param cat category id
 * 
 * @param sorting 1:percentage 2:price_difference 3:price_range 4:near_me
 * 
 * @param sorting_val price range ex.90,00:100,00
 * 
 * @param keyword text to search
 */

/**
 * @param cat         category id
 * @param sorting     1:percentage 2:price_difference 3:price_range 4:near_me
 * @param sorting_val price range ex.90,00:100,00
 * @param keyword     text to search
 * @author Konstantinos Grassos
 */
public class SearchChoice {

    private String cat;
    private String sorting;
    private String sorting_val;
    private String keyword;

    /*
     * @param cat category id
     *
     * @param sorting 1:percentage 2:price_difference 3:price_range 4:near_me
     *
     * @param sorting_val price range ex.90,00:100,00
     *
     * @param keyword text to search
     */
    public SearchChoice() {

        setCat("");
        setKeyword("");
        setSorting("");
        setSorting_val("");

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


}
