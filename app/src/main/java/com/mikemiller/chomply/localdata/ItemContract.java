package com.mikemiller.chomply.localdata;

import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.Constants.Info;

import android.provider.BaseColumns;

public final class ItemContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ItemContract() {}

    /* Inner class that defines the table contents */
    public static abstract class MenuItemEntry implements BaseColumns {
    	
    	public static final String TABLE_NAME = "menu_item";
    	
    	public static final Info PRIMARY_KEY = Info.ITEM_ID;
    	
    	public static final Info[] columns = {
	        Info.BRAND_ID,
	        Info.BRAND_NAME,
	        Info.ITEM_NAME,
	        Info.ITEM_DESCRIPTION,
	        Info.UPDATED_AT,
	        Info.SERVING_SIZE_QTY,
	        Info.SERVING_SIZE_UNIT,
	        
	        Info.CALORIES,
	        Info.FAT_CALS,
	        Info.CARBS,
	        Info.FAT,
	        Info.SATURATED_FAT,
	        Info.CONTAINS_GLUTEN,
	        Info.TRANS_FAT,
	        Info.POLYUNSATURATED_FAT,
	        Info.MONOUNSATURATED_FAT,
	        Info.CHOLESTEROL,
	        Info.SODIUM,
	        Info.FIBER,
	        Info.SUGARS,
	        Info.PROTEIN,
	        Info.VITAMIN_A,
	        Info.VITAMIN_C,
	        Info.CALCIUM,
	        Info.IRON };
    }
}