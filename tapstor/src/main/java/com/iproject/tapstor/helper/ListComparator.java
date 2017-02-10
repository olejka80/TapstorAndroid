package com.iproject.tapstor.helper;

import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.News;
import com.iproject.tapstor.objects.Rating;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ListComparator implements Comparator<Object> {

    private static final String TAG = "ListComparator";

    @Override
    public int compare(Object obj1, Object obj2) {
        // 2014-10-31 14:24:46
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1 = new Date();
        Date date2 = new Date();
        try {
            if (obj1 instanceof News) {

                date1 = sdf.parse(((News) obj1).stamp);
                date2 = sdf.parse(((News) obj2).stamp);

            } else {
                date1 = sdf.parse(((Rating) obj1).stamp);
                date2 = sdf.parse(((Rating) obj2).stamp);
            }

            if (date1.compareTo(date2) > 0) {
                return -1;
            } else if (date1.compareTo(date2) < 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            Log.e(TAG, e);
            return 0;
        }
    }
}
