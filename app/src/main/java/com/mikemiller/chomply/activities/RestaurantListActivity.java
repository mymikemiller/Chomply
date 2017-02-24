package com.mikemiller.chomply.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.activities.adapters.RestaurantListAdapter;
import com.mikemiller.chomply.helper.ConnectionDetector.OnlineMode;
import com.mikemiller.chomply.helper.AlertDialogManager;
import com.mikemiller.chomply.helper.ConnectionDetector;
import com.mikemiller.chomply.helper.Convenience;
import com.mikemiller.chomply.localdata.LocalDataManager;
import com.mikemiller.chomply.localdata.RestaurantDbHelper;
import com.mikemiller.chomply.localdata.RestaurantContract.BrandEntry;
import com.mikemiller.chomply.objects.Restaurant;
import com.mikemiller.chomply.remotedata.RemoteDataManager;
import com.mikemiller.chomply.remotedata.RemoteDataManager.RemoteReadListener;
 
public class RestaurantListActivity extends ListActivity implements RemoteReadListener {
    // Progress Dialog
    private ProgressDialog pDialog;
    
	private RestaurantListAdapter mAdapter;
	private ArrayList<Restaurant> mRestaurantsDisplayed = new ArrayList<Restaurant>();
	private ArrayList<Restaurant> mRestaurantsHistory = new ArrayList<Restaurant>();
	
	private EditText mFilterText;
	
	RestaurantDbHelper mBrandDbHelper;

 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        
		mBrandDbHelper = new RestaurantDbHelper(getBaseContext());

		// Register mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
		    new IntentFilter("restaurant-loaded"));
                
        ListView lv = getListView();
		lv.setAdapter(mAdapter = new RestaurantListAdapter(this,
				R.layout.list_items_restaurant_list, mRestaurantsDisplayed));
		
        // Load the set of saved restaurants
        LoadRestaurantHistory();
        
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                    long arg3) {
                Restaurant restaurant = (Restaurant)view.getTag();
            	restaurantClicked(restaurant);

            }
        });     
        
        mFilterText = (EditText) findViewById(R.id.search_query);
		mFilterText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				String text = s.toString();
				filterTextChanged(text);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		mFilterText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-up event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_UP)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					runSearch();
					return true;
				}
				return false;
			}
		});
		
		Button searchButton = (Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		        runSearch();
		    }
		});
    }
    
    private void restaurantClicked(Restaurant restaurant) {
        Intent i = new Intent(getApplicationContext(), MenuActivity.class);                             	    	
        if (restaurant.isValid()) {
			// Check if Internet is present (if necessary)
        	OnlineMode onlineMode = ConnectionDetector.getOnlineMode(getApplicationContext());
        	if (restaurant.hasNeverFetched() && onlineMode == OnlineMode.FORCE_OFFLINE) {
        		new AlertDialogManager().showAlertDialog(RestaurantListActivity.this, "Internet Connection Error",
                        "Offline mode is FORCE_OFFLINE, but this restaurant has never fetched and would be empty. Please switch OfflineMode to allow fetching.", false);
        		return;
        	}
        	boolean internetIsNecessary = onlineMode == OnlineMode.DISALLOW_OFFLINE ||
        			onlineMode != onlineMode.DISALLOW_OFFLINE && restaurant.needsFetch();
        	if (internetIsNecessary) {
    	    	if (!ConnectionDetector.IsConnected(getApplicationContext(), RestaurantListActivity.this, internetIsNecessary)) {
    	    		if (internetIsNecessary) {
    	    			return;
    	    		}
    	    	}
        	}
            i.putExtra("restaurant", restaurant);
            i.putExtra("updateLocalCacheOnLoad", restaurant.needsFetch());           
            startActivity(i);
        }
    }
    
    private void filterTextChanged(String text) {
		if (text.length() == 0) {
			// The user cleared the text box. Reinstate their restaurant history.
			refreshRestaurantHistory();
			mAdapter.getFilter().filter("");
		} else {
			mAdapter.getFilter().filter(text);
		}
    }
    
    private void refreshRestaurantHistory() {
		mRestaurantsDisplayed.clear();
		mRestaurantsDisplayed.addAll(mRestaurantsHistory);
		mAdapter.notifyDataSetChanged();
    }
    
    protected void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable("restaurantsHistory", mRestaurantsHistory);
		super.onSaveInstanceState(bundle);
	}
    
    private void addRestaurantToHistory(Restaurant r) {
    	mRestaurantsHistory.remove(r);
        mRestaurantsHistory.add(r);
    }
    
    private void runSearch() {
    	// Check for internet connection
    	if (!ConnectionDetector.IsConnected(getApplicationContext(), this, true)) {
    		return;
    	}

		pDialog = new ProgressDialog(RestaurantListActivity.this);
		pDialog.setMessage("Searching...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
		
		mRestaurantsDisplayed.clear();
    	mAdapter.getFilter().filter("");
    	
    	String searchTerm = mFilterText.getText().toString();
		RemoteDataManager.fetchRestaurantsAsync(searchTerm, this);
		
    }
    
    private void LoadRestaurantHistory() {
    	mRestaurantsHistory.clear();
		for (Restaurant r : LocalDataManager.getRestaurantHistory(getBaseContext())) {
			mRestaurantsHistory.add(r);
		}
		refreshRestaurantHistory();
    }
    
    private void sortRestaurantHistory() {
    	Collections.sort(mRestaurantsHistory, new Comparator<Restaurant>() {
			@Override
			public int compare(Restaurant restaurant1, Restaurant restaurant2) {
				return -(restaurant1.getLastLoadedAt().compareTo(restaurant2.getLastLoadedAt()));
			}
		});

    	refreshRestaurantHistory();
    }
 
    // handler for received Intents for the "restaurant-updated" event 
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        // Extract data included in the Intent
        Restaurant restaurant = intent.getParcelableExtra("restaurant");
        // If we aren't able to find the restaurant in the loop below, we must have fetched it from Nutritionix
        boolean fetchedFromNutritionix = true;
        
        // Find the restaurant with the matching BrandId. We don't just use the restaurant passed through the Intent 
        // because we need to modify the restaurant stored in mRestaurants, and passing through an intent 
        // creates a new restaurant using Parcelable
        for (Restaurant r : mRestaurantsHistory) {
        	// If we don't find one, that means the restaurant doesn't exist yet in our history, so it's ok to add the restaurant we got from the Parcelable above
        	if (r.equals(restaurant)) {
                fetchedFromNutritionix = intent.getBooleanExtra("fetchedFromNutritionix", false);
                restaurant = r;
                break;
        	}
        }
        
        restaurantLoaded(restaurant, fetchedFromNutritionix);
      }
    };
    
    @Override
    protected void onDestroy() {
      // Unregister since the activity is about to be closed.
      LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
      super.onDestroy();
    }
    
    private void restaurantLoaded(Restaurant restaurant, boolean fetchedFromNutritionix) {
    	if (fetchedFromNutritionix) {
    		restaurant.setLastLoadedAt(Convenience.getNow());
    	}
    	LocalDataManager.updateRestaurantHistory(restaurant, getBaseContext());

		addRestaurantToHistory(restaurant);
		sortRestaurantHistory();
    }

	@Override
	public void onRestaurantsFetched(ArrayList<Restaurant> restaurants) {
		
		mRestaurantsDisplayed.clear();
		
		for (Restaurant r : restaurants) {
			mRestaurantsDisplayed.add(r);
		}

		mAdapter.notifyDataSetChanged();
		pDialog.dismiss();
	}
    
}