package com.mikemiller.chomply.localdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.localdata.RestaurantContract.BrandEntry;

public class RestaurantDbHelper extends SQLiteOpenHelper {
	

    private static final String TEXT_TYPE = " TEXT";
    //private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String LONG_TYPE = " BIGINT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    private static String SQL_CREATE_ENTRIES;

    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + BrandEntry.TABLE_NAME;
    
    
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Brand.db";

    public RestaurantDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
        SQL_CREATE_ENTRIES =
                "CREATE TABLE " + BrandEntry.TABLE_NAME + " (" +         
                BrandEntry.PRIMARY_KEY + " TEXT PRIMARY KEY";
        

        for (int i = 0; i < BrandEntry.columns.length; ++i) {
        	String columnName = BrandEntry.columns[i];
        	Class<?> columnDataType = BrandEntry.columnDataTypes[i];
        	String dataTypeString = " UNKNOWN";
        	if (columnDataType == String.class){
        		dataTypeString = TEXT_TYPE;
        	} else if (columnDataType == long.class) {
        		dataTypeString = LONG_TYPE;
        	} else if (columnDataType == int.class) {
        		dataTypeString = INT_TYPE;
        	}
        	
    		SQL_CREATE_ENTRIES += COMMA_SEP + columnName + dataTypeString;
        }
        
        SQL_CREATE_ENTRIES += " )";
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}