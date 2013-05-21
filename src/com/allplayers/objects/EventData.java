package com.allplayers.objects;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EventData extends DataObject {
    private String uuid = "";
    private String title = "";
    private String description = "";
    private String category = "";
    private String start = "";
    private String end = "";
    private String longitude = "";
    private String latitude = "";
    private String zip = "";

    public EventData() {

    }

    /**
     * Check if this DataObject exists in an ArrayList list.
     *
     * @param list to look through.
     * @return if this is new to list.
     */
    @Override
    public boolean isNew(ArrayList <? extends DataObject > list) {
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                EventData object = (EventData) list.get(i);

                if (uuid.equals(object.getUUID())
                        && title.equals(object.getTitle())
                        && description.equals(object.getDescription())
                        && category.equals(object.getCategory())
                        && start.equals(object.getStart())
                        && end.equals(object.getEnd())
                        && longitude.equals(object.getLongitude())
                        && latitude.equals(object.getLatitude())
                        && zip.equals(object.getZip())) {
                    return false;
                }
            }
        }

        return true;
    }

    private Date parseDatetime(String datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(datetime, new ParsePosition(0));

        TimeZone timezone = TimeZone.getDefault();
        int offset = timezone.getOffset(date.getTime());
        date = new Date(date.getTime() + offset);
        return date;
    }

    public String getDateString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; //because calendar uses 0-11 instead of 1-12
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String AmPm = "AM";

        if (hour >= 12) {
            AmPm = "PM";

            if (hour > 12) {
                hour = hour - 12;
            }
        } else if (hour == 0) {
            hour = 12;
        }

        DecimalFormat df = new DecimalFormat("00");

        return "" + df.format(month) + "/" + df.format(day) + "/" + year + " " + df.format(hour) + ":" + df.format(minute) + AmPm;
    }

    public void setLatitude(String newLatitude) {
        latitude = newLatitude;
    }

    public void setLongitude(String newLongitude) {
        longitude = newLongitude;
    }

    public void setZip(String s) {
        zip = s;
    }

    public String getUUID() {
        return uuid;
    }

    public String getId() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getStart() {
        return start;
    }

    public Date getStartDate() {
        return parseDatetime(start);
    }

    public String getStartDateString() {
        return getDateString(parseDatetime(start));
    }

    public String getEnd() {
        return end;
    }

    public Date getEndDate() {
        return parseDatetime(end);
    }

    public String getEndDateString() {
        return getDateString(parseDatetime(end));
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getZip() {
        return zip;
    }
}