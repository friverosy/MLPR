package com.app.axxezo.mpr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmartin on 24-01-17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "axxezo_mpr";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // SQL statement to create PlateData table
        String CREATE_PLATEDATA_TABLE = "CREATE TABLE PLATEDATA ( " +
                "platedata_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "platedata_number TEXT, "+
                "platedata_date TEXT" +")";

        db.execSQL(CREATE_PLATEDATA_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older tables if it existed
        db.execSQL("DROP TABLE IF EXISTS platedata");

        //create fresh tables
        this.onCreate(db);

    }

    /**
     * CRUD operations (create "add", read "get", update, delete)
     */

    //Table names
    private static final String TABLE_PLATEDATA = "platedata";


    //PlateData table columns names
    private static final String PLATEDATA_ID = "platedata_id";
    private static final String PLATEDATA_NUMBER = "platedata_number";
    private static final String PLATEDATA_DATE = "platedata_date";

    private static final String[] PLATEDATA_COLUMNS = {PLATEDATA_ID,PLATEDATA_NUMBER,PLATEDATA_DATE};


    //PlateData
    public void add_platedata(PlateData platedata){

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(PLATEDATA_NUMBER, platedata.get_platedata_number());
        values.put(PLATEDATA_DATE, platedata.get_platedata_date());



        // 3. insert
        db.insert(TABLE_PLATEDATA, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }


    public PlateData get_platedata(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_PLATEDATA, // a. table
                        PLATEDATA_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build object
        PlateData platedata = new PlateData();
        platedata.set_platedata_id(Integer.parseInt(cursor.getString(0)));
        platedata.set_platedata_number(cursor.getString(1));
        platedata.set_platedata_date(cursor.getString(2));

        db.close();

        // 5. return
        return platedata;
    }

    // Updating a single PlateData
    public int update_platedata(PlateData user) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();


        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(PLATEDATA_NUMBER, user.get_platedata_number());
        values.put(PLATEDATA_DATE, user.get_platedata_date());


        // 3. updating row
        int i = db.update(TABLE_PLATEDATA, //table
                values, // column/value
                PLATEDATA_ID+" = ?", // selections
                new String[] { String.valueOf(user.get_platedata_id()) }); //selection args

        // 4. close
        db.close();

        return i;

    }


    // Deleting a single PlateData
    public void delete_platedata(int id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_PLATEDATA,
                PLATEDATA_ID+" = ?",
                new String[] { String.valueOf(id) });

        // 3. close
        db.close();

    }

    public int platedata_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM platedata",null);
        int i = cursor.getCount();
        db.close();

        return i;
    }

    public ArrayList<ListPlateItem> get_platedata_all(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<ListPlateItem> lp = new ArrayList<ListPlateItem>();
        Cursor cursor = db.rawQuery("SELECT * FROM platedata",null);

        if(cursor!=null && cursor.getCount()>0)
        {
            cursor.moveToFirst();
            do {
                Log.d("ADDDB_GET", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2));
                lp.add(new ListPlateItem(cursor.getString(1),cursor.getString(2),cursor.getString(0)));

            } while (cursor.moveToNext());
        }
        db.close();
        return lp;
    }




}
