package com.app.axxezo.mpr;

/**
 * Created by nicolasmartin on 24-01-17.
 */

public class PlateData {

    int platedata_id;
    String platedata_number;
    String platedata_date;


    //Constructors
    public PlateData(){

    }

    public PlateData(int platedata_id,String platedata_number,String platedata_date){

        this.platedata_id = platedata_id;
        this.platedata_number = platedata_number;
        this.platedata_date = platedata_date;


    }

    public PlateData(String platedata_number,String platedata_date){

        this.platedata_number = platedata_number;
        this.platedata_date = platedata_date;

    }

    //Set
    public void set_platedata_id(int platedata_id) {
        this.platedata_id = platedata_id;
    }

    public void set_platedata_number(String platedata_number) {
        this.platedata_number = platedata_number;
    }

    public void set_platedata_date(String platedata_date) {
        this.platedata_date = platedata_date;
    }

    //Get
    public int get_platedata_id() {
        return this.platedata_id;
    }

    public String get_platedata_number() {
        return this.platedata_number;
    }

    public String get_platedata_date() {
        return this.platedata_date;
    }


    @Override
    public String toString() {
        return "Platedata [id=" + platedata_id + ", number=" + platedata_number +
                ", date=" + platedata_date + "]";
    }
}
