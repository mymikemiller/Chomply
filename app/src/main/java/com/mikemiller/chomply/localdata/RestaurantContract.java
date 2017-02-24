package com.mikemiller.chomply.localdata;

import com.mikemiller.chomply.Constants.Info;

import android.provider.BaseColumns;

public final class RestaurantContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RestaurantContract() {}

    /* Inner class that defines the table contents */
    public static abstract class BrandEntry implements BaseColumns {
        public static final String TABLE_NAME = "brand";
        public static final String UPDATED_COLUMN_NAME = "updated_time";
        public static final String LAST_LOADED_COLUMN_NAME = "last_loaded_time";
        
        public static final String PRIMARY_KEY = Info.BRAND_ID.databaseName;
        
        public static final String[] columns = {
            Info.BRAND_NAME.databaseName,
            UPDATED_COLUMN_NAME,
            LAST_LOADED_COLUMN_NAME
        };
        
        public static final Class<?>[] columnDataTypes = { 
            Info.BRAND_NAME.dataType,
            long.class,
            long.class
        };   
        
    }
}