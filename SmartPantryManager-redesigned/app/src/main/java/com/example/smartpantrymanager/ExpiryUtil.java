package com.example.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Helper for working out how close an ingredient is to its expiry date. */
public class ExpiryUtil {

    /** Items expiring within this many days are flagged as "expiring soon". */
    public static final int SOON_DAYS = 3;

    /**
     * Days from today until the given yyyy-MM-dd date.
     * Negative means already expired. Returns Long.MAX_VALUE if the date is invalid.
     */
    public static long daysUntil(String isoDate) {
        if (isoDate == null) {
            return Long.MAX_VALUE;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(isoDate.trim());

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            double diff = expiry.getTime() - today.getTimeInMillis();
            return Math.round(diff / (24.0 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }
}