package com.mikemiller.chomply.remotedata;

import java.util.ArrayList;

import com.mikemiller.chomply.objects.Restaurant;

public class RemoteDataManager {

	private static RemoteDataManager sInstance;
	private NutritionixReader mReader;
	

	public interface RemoteReadListener {
        public void onRestaurantsFetched(ArrayList<Restaurant> restaurants);
    }

	// Use this instance of the interface to deliver action events
	RemoteReadListener mListener;
	
	private RemoteDataManager() {
	}
	
	private void setListener(RemoteReadListener listener) {
		mListener = listener;
	}
	
	private static RemoteDataManager getInstance()
    {
        if (sInstance == null)
        {
        	sInstance = new RemoteDataManager();
    		sInstance.mReader = new NutritionixReader(sInstance);
        }
        return sInstance;
    }
	
	public static void fetchRestaurantsAsync(String searchTerm, RemoteReadListener listener) {
		RemoteDataManager manager = getInstance();
		manager.setListener(listener);
		manager.doFetchRestaurants(searchTerm);
	}
	
	private void doFetchRestaurants(String searchTerm) {
		mReader.fetchRestaurantsAsync(searchTerm);
	}
	
	protected void restautantsFetched(ArrayList<Restaurant> restaurants) {
		mListener.onRestaurantsFetched(restaurants);
	}
}