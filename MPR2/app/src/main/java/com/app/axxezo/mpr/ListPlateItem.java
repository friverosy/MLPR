package com.app.axxezo.mpr;

/**
 * Created by nicolasmartin on 24-01-17.
 */

public class ListPlateItem {

    private String number;
    private String date;
    private String id;

    public ListPlateItem() {
        super();
    }

    public ListPlateItem(String number, String date, String id) {
        super();
        this.number = number;
        this.date = date;
        this.id = id;
    }


    public String get_number() {
        return number;
    }

    public void set_number(String number) {
        this.number = number;
    }

    public String get_date() {
        return date;
    }

    public void set_date(String date) {
        this.date = date;
    }

    public String get_id() {
        return id;
    }

    public void set_id(String id) {
        this.id = id;
    }
}
