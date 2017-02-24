package com.mikemiller.chomply.localdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.localdata.ItemContract.MenuItemEntry;

public class ItemDbHelper extends SQLiteOpenHelper {
	

    private static final String TEXT_TYPE = " TEXT";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String COMMA_SEP = ",";
    private static String SQL_CREATE_ENTRIES;

    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + MenuItemEntry.TABLE_NAME;
    
    
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "MenuItem.db";

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
        SQL_CREATE_ENTRIES =
                "CREATE TABLE " + MenuItemEntry.TABLE_NAME + " (" +        
        		MenuItemEntry.PRIMARY_KEY.databaseName + " TEXT PRIMARY KEY";
        
        for(Info info : MenuItemEntry.columns) { 
    		if (!info.databaseName.isEmpty()) {
        		String dataType = info.dataType == String.class ? TEXT_TYPE : DOUBLE_TYPE;
        		SQL_CREATE_ENTRIES += COMMA_SEP + info.databaseName + dataType;
        	}
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