package com.iproject.tapstor.helper;

import android.content.Context;
import android.location.Location;

import com.iproject.tapstor.R;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Cat;
import com.iproject.tapstor.objects.Element;
import com.iproject.tapstor.objects.News;
import com.iproject.tapstor.objects.Rating;
import com.iproject.tapstor.objects.Results;
import com.iproject.tapstor.objects.SearchChoice;
import com.jwetherell.augmented_reality.ui.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Data used throughout the app
 *
 * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
 */
public class TapstorData {

    private static final Object lock = new Object();
    private static TapstorData data = null;
    public ArrayList<Integer> notificationIdsofReadList;
    public String north = "B";
    public String northEast = "BA";
    public String northWest = "BΔ";
    public String south = "N";
    public String southEast = "NA";
    public String southWest = "NΔ";
    public String east = "A";
    public String west = "Δ";
    public Marker centerMarker;
    private boolean menuStatusOpen = false;
    private boolean internet;
    private int tab;
    private Location location;
    private String userToken;
    private int selectionTab1 = -99;
    private int selectionTab2 = -99;
    private int selectedCategoryIdTab1 = -99;
    private int selectedCategoryIdTab2 = -99;
    private SearchChoice searchChoice1, searchChoice2;
    private Results selectedEnterprise;
    private Element selectedElement;
    private int unreadMessages;
    private List<News> newsList = new ArrayList<>();
    private List<Rating> ratingsList = new ArrayList<>();
    private List<Cat> cloneAll;
    private List<Cat> cloneFeatured;

    private TapstorData() {

    }

    public static void resetInstance() {
        data = null;
    }

    public static TapstorData getInstance() {

        if (data == null) {
            // Create the instance
            data = new TapstorData();
        }

        return data;
    }

    public void setCompassCharacters(Context context) {
        this.north = context.getResources().getString(R.string.north);
        this.northEast = context.getResources().getString(R.string.north_east);
        this.northWest = context.getResources().getString(R.string.north_west);
        this.south = context.getResources().getString(R.string.south);
        this.southEast = context.getResources().getString(R.string.south_east);
        this.southWest = context.getResources().getString(R.string.south_west);
        this.east = context.getResources().getString(R.string.east);
        this.west = context.getResources().getString(R.string.west);
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public boolean isMenuStatusOpen() {
        return menuStatusOpen;
    }

    public void setMenuStatusOpen(boolean menuStatusOpen) {
        this.menuStatusOpen = menuStatusOpen;
    }

    public boolean isInternet() {
        return internet;
    }

    public void setInternet(boolean internet) {
        this.internet = internet;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public double getLatitude() {
        synchronized (lock) {
            return location != null ? location.getLatitude() : 0d;
        }
    }

    public double getLongitude() {
        synchronized (lock) {
            return location != null ? location.getLongitude() : 0d;
        }
    }

    public void setLocation(Location location) {
        synchronized (lock) {
            this.location = location;
        }
    }


    public Results getSelectedEnterprise() {
        return selectedEnterprise;
    }

    public void setSelectedEnterprise(Results selectedEnterprise) {
        this.selectedEnterprise = selectedEnterprise;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }

    public List<Rating> getRatingsList() {
        return ratingsList;
    }

    public void setRatingsList(List<Rating> ratingsList) {
        this.ratingsList = ratingsList;
    }

    public int getSelection(int tab) {
        if (tab == 1) {
            return selectionTab1;
        } else {
            return selectionTab2;
        }

    }

    public void setSelection(int selection, int tabTop) {
        if (tabTop == 1) {
            Log.e("--set Selection--", "for tab: 1 :" + selection + " --> ");
            this.selectionTab1 = selection;
        } else if (tabTop == 2) {
            Log.e("--set Selection--", "for tab: 2 :" + selection + " --> ");
            this.selectionTab2 = selection;
        }

    }

    public int getSelectedCategoryId(int activeTopTab) {

        if (activeTopTab == 1) {
            Log.e("--Get--Selected--", "Get tab: " + activeTopTab + " --> "
                    + selectedCategoryIdTab1);
            return selectedCategoryIdTab1;
        } else {
            Log.e("--Get--Selected--", "Get tab: " + activeTopTab + " --> "
                    + selectedCategoryIdTab2);
            return selectedCategoryIdTab2;
        }
    }

    public void setSelectedCategoryId(int selectedCategoryId, int activeTab) {

        Log.e("--Set--Selected--", "Set tab: " + activeTab + " With value: "
                + selectedCategoryId);
        if (activeTab == 1) {
            this.selectedCategoryIdTab1 = selectedCategoryId;
        } else if (activeTab == 2) {
            this.selectedCategoryIdTab2 = selectedCategoryId;
        }

    }

    public List<Cat> getCloneFeatured() {
        if (cloneFeatured == null) {
            return new ArrayList<>();
        } else {
            return cloneFeatured;
        }
    }

    public void setCloneFeatured(List<Cat> cloneFeatured) {
        this.cloneFeatured = cloneFeatured;
    }

    public List<Cat> getCloneAll() {
        return cloneAll;
    }

    public void setCloneAll(List<Cat> cloneAll) {
        this.cloneAll = cloneAll;
    }

    public Element getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(Element selectedElement) {
        this.selectedElement = selectedElement;
    }

    public SearchChoice getSearchChoice(int tab) {
        if (tab == 1) {
            if (searchChoice1 == null) {
                searchChoice1 = new SearchChoice();
            }
            return searchChoice1;
        } else {
            if (searchChoice2 == null) {
                searchChoice2 = new SearchChoice();
            }
            return searchChoice2;
        }
    }

    public void setSearchChoice(SearchChoice searchChoice, int tab) {
        if (tab == 1) {
            this.searchChoice1 = searchChoice;
        } else {
            this.searchChoice2 = searchChoice;
        }
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

}
