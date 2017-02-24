package com.mikemiller.chomply.localdata;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;

import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.helper.Convenience;
import com.mikemiller.chomply.localdata.RestaurantContract.BrandEntry;
import com.mikemiller.chomply.objects.Restaurant;

public class SQLAccessor implements LocalAccessor {

	RestaurantDbHelper mRestaurantDbHelper;
	
	public SQLAccessor(Context context) {
		mRestaurantDbHelper = new RestaurantDbHelper(context);
	}

	@Override
	public ArrayList<Restaurant> getRestaurantHistory() {
		SQLiteDatabase db = mRestaurantDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { Info.BRAND_ID.databaseName,
				Info.BRAND_NAME.databaseName,
				BrandEntry.UPDATED_COLUMN_NAME,
				BrandEntry.LAST_LOADED_COLUMN_NAME};

		// Define 'where' part of query.
		//String selection = Info.BRAND_ID.databaseName + " = ?";

		// Specify arguments in placeholder order.
		String[] selectionArgs = { };

		Cursor c = db.query(BrandEntry.TABLE_NAME, // The table to query
				projection, // The columns to return
				null, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				BrandEntry.LAST_LOADED_COLUMN_NAME + " DESC" // The sort order
				);
		
		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>(c.getCount());
		while (c.moveToNext()) {
			Restaurant restaurant = new Restaurant(c);
			restaurants.add(restaurant);
		}
		return restaurants;
	}

	@Override
	public void updateRestaurantHistory(Restaurant restaurant) {

		SQLiteDatabase db = mRestaurantDbHelper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Info.BRAND_ID.databaseName, restaurant.getBrandID());
		values.put(Info.BRAND_NAME.databaseName, restaurant.getName());

		values.put(BrandEntry.LAST_LOADED_COLUMN_NAME, restaurant.getLastLoadedAt().getTime());
		values.put(BrandEntry.UPDATED_COLUMN_NAME, restaurant.getUpdatedAt().getTime());
		
		try {
			db.insertWithOnConflict(BrandEntry.TABLE_NAME, "null",
					values, SQLiteDatabase.CONFLICT_REPLACE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
