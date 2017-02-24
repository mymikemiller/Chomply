package com.mikemiller.chomply.localdata;

import java.util.ArrayList;

import android.content.Context;

import com.mikemiller.chomply.objects.Restaurant;
import com.mikemiller.chomply.remotedata.NutritionixReader;
import com.mikemiller.chomply.remotedata.RemoteDataManager;


public class LocalDataManager {

	private static LocalDataManager sInstance;
	private SQLAccessor mAccessor;

	private LocalDataManager() {
	}
	
	private static LocalDataManager getInstance(Context context)
    {
        if (sInstance == null)
        {
        	sInstance = new LocalDataManager();
    		sInstance.mAccessor = new SQLAccessor(context);
        }
        return sInstance;
    }
	
	public static void updateRestaurantHistory(Restaurant restaurant, Context context) {
		getInstance(context).doUpdateRestaurantHistory(restaurant, context);
	}
	public void doUpdateRestaurantHistory(Restaurant restaurant, Context context) {
		mAccessor.updateRestaurantHistory(restaurant);
	}
	
	
	
	public static ArrayList<Restaurant> getRestaurantHistory(Context context) {
		return getInstance(context).doGetRestaurantHistory();
	}
	
	private ArrayList<Restaurant> doGetRestaurantHistory() {
		return mAccessor.getRestaurantHistory();
	}
}
