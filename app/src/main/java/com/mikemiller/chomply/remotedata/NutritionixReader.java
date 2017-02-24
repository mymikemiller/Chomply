package com.mikemiller.chomply.remotedata;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.util.Log;

import com.joshdholtz.protocol.lib.ProtocolClient;
import com.joshdholtz.protocol.lib.requests.ParamsRequestData;
import com.joshdholtz.protocol.lib.responses.JSONResponseHandler;
import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.objects.Restaurant;

public class NutritionixReader extends RemoteReader {

	private static final String TAG_HITS = "hits";
	
	protected NutritionixReader(RemoteDataManager manager) {
		super(manager);
	}


	@Override
	public void fetchRestaurantsAsync(final String searchTerm) {
		ProtocolClient client = new ProtocolClient(
				"https://api.nutritionix.com/v1_1");
		client.setDebug(false);

		// Creates request with JSONObject as body
		ParamsRequestData requestData = new ParamsRequestData();
		requestData.addParam(Constants.TAG_APPLICATION_ID, Constants.VALUE_APPLICATION_ID);
		requestData.addParam(Constants.TAG_APPLICATION_KEY, Constants.VALUE_APPLICATION_KEY);
		requestData.addParam("query", searchTerm);
		requestData.addParam("min_score", "1");
		requestData.addParam("type", "1"); // Search only restaurants
		
		// Sends GET request
		client.doGet("/brand/search", requestData, new JSONResponseHandler() {
			@Override
			public void handleResponse(JSONObject jsonObject,
					JSONArray jsonArray) {
				
				ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
				
				try {
					JSONArray hitsArray = jsonObject.getJSONArray(TAG_HITS);

					if (hitsArray.length() == 0) {
						Restaurant none = new Restaurant("No restaurants found for \"" + searchTerm + "\"");
						restaurants.add(none);
					} else {
						for (int i = 0; i < hitsArray.length(); ++i) {
							JSONObject hitObj;
							try {
								hitObj = hitsArray.getJSONObject(i);
								
								Restaurant restaurant = new Restaurant(hitObj);
	
								if (restaurant.getName() == null) {
									Log.d("null", "null restaurant name");
								} else {
									restaurants.add(restaurant);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				mManager.restautantsFetched(restaurants);
			}

		});
	}

}
