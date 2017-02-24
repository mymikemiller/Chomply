package com.mikemiller.chomply.localdata;

import java.util.ArrayList;

import com.mikemiller.chomply.objects.Restaurant;

public interface LocalAccessor {
	public ArrayList<Restaurant> getRestaurantHistory();
	public void updateRestaurantHistory(Restaurant restaurant);

}
