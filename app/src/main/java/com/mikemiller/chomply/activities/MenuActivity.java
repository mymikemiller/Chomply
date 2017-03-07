package com.mikemiller.chomply.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.joshdholtz.protocol.lib.ProtocolClient;
import com.joshdholtz.protocol.lib.requests.JSONRequestData;
import com.joshdholtz.protocol.lib.responses.JSONResponseHandler;
import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.Constants.SortOrder;
import com.mikemiller.chomply.R.drawable;
import com.mikemiller.chomply.R.id;
import com.mikemiller.chomply.R.layout;
import com.mikemiller.chomply.R.menu;
import com.mikemiller.chomply.activities.SortOptionDialogFragment.SortOptionsDialogListener;
import com.mikemiller.chomply.activities.adapters.ItemListAdapter;
import com.mikemiller.chomply.helper.AlertDialogManager;
import com.mikemiller.chomply.helper.ConnectionDetector;
import com.mikemiller.chomply.helper.UnknownInfoException;
import com.mikemiller.chomply.helper.ConnectionDetector.OnlineMode;
import com.mikemiller.chomply.helper.FileHelper;
import com.mikemiller.chomply.helper.JSONParser;
import com.mikemiller.chomply.localdata.ItemDbHelper;
import com.mikemiller.chomply.localdata.RestaurantContract.BrandEntry;
import com.mikemiller.chomply.localdata.ItemContract.MenuItemEntry;
import com.mikemiller.chomply.objects.Item;
import com.mikemiller.chomply.objects.Restaurant;

public class MenuActivity extends FragmentActivity implements
		SortOptionsDialogListener {

	private ItemListAdapter mAdapter;
	private ArrayList<Item> mMenuItems = new ArrayList<Item>();

	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	// ArrayList<MenuItem> menuItemList; // We now rely on the adapter to store
	// the list of items
	ArrayList<Item> newMenuItemList; // we can't update the contents of the
											// ListView adapter (menuItems)
											// from a background thread, so we
											// update this one then switch them
											// out when we get back to the UI
											// thread.

	int total_hits_to_receive = 0;
	int hits_received = 0;
	int current_query_offset = 0;

	ArrayList<JSONArray> queryResponses;

	//String brand_id, brand_name;
	Restaurant mRestaurant;

	private static final String TAG = "ItemListActivity";

	// menu item JSON url
	// id - should be posted as GET params to get track list (ex: id = 5)
	private static final String URL_MENU_ITEMS = "https://api.nutritionix.com/v1_1/search/";

	// ALL JSON node names
	private static final String TAG_OFFSET = "offset";
	private static final String TAG_LIMIT = "limit";
	private static final String TAG_BRAND_ID = "brand_id";
	private static final String TAG_BRAND_NAME = "brand_name";
	private static final String TAG_HITS = "hits";
	private static final String TAG_TOTAL = "total";

	// Static JSON values
	private static final int VALUE_LIMIT = 50;

	Info mSortOn = Info.CALORIES;

	private ListView mItemList;

	ItemDbHelper mMenuItemDbHelper;
	
	boolean mUpdateLocalCacheOnLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_menu);

		mMenuItemDbHelper = new ItemDbHelper(getBaseContext());

		mItemList = (ListView) findViewById(R.id.item_list);

		ImageView attribution = new ImageView(this);
		attribution.setImageResource(R.drawable.nutritionix_attribution);
		mItemList.addFooterView(attribution);
		attribution.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("http://www.nutritionix.com/api"));
				startActivity(intent);
			}
		});

		mItemList.setAdapter(mAdapter = new ItemListAdapter(this,
				R.layout.list_item_for_menu, mMenuItems));

		// Get album id
		Intent i = getIntent();
		/*brand_id = i.getStringExtra(TAG_BRAND_ID);
		brand_name = i.getStringExtra(TAG_BRAND_NAME);*/
		mRestaurant = i.getParcelableExtra("restaurant");
		mUpdateLocalCacheOnLoad = i.getBooleanExtra("updateLocalCacheOnLoad", true);

		setTitle(mRestaurant.getName());

		if (savedInstanceState != null) {
			//mUpdateLocalCacheOnLoad = savedInstanceState.getBoolean("updateLocalCacheOnLoad");
			newMenuItemList = (ArrayList<Item>) savedInstanceState
					.getSerializable("menuItems");
			mMenuItems.clear();
			mMenuItems.addAll(newMenuItemList);
			mAdapter.notifyDataSetChanged();
			setSort((Info) savedInstanceState.getSerializable("sortOn"));

			setAdapterForMenuItemList();
		} else { // Bundle is empty so you should initialize the myList variable


			// menuItemList = new ArrayList<MenuItem>();
			newMenuItemList = new ArrayList<Item>();

			setAdapterForMenuItemList();

			SharedPreferences settings = getSharedPreferences("sortOn", 0);
			try {
				mSortOn = Info.getInfoFromDatabaseName(settings.getString(
						"sortOn", Info.CALORIES.databaseName));
			} catch (UnknownInfoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setSort(mSortOn);

			// Loading tracks in Background Thread
			// new LoadMenuItems().execute();
			LoadMenuItemsInForeground();

		}

		/**
		 * Listview on item click listener ItemActivity will be launched by
		 * passing brand_id, item_id
		 * */
		/* Disable the ItemActivity because the nutrition label isn't working
		mItemList
				.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int arg2, long arg3) {
						// On selecting single track get song information
						Intent i = new Intent(getApplicationContext(),
								ItemActivity.class);

						// to get song information
						// both album id and song is needed
						Item menuItem = (Item) view.getTag();

						i.putExtra("menuItem", (Parcelable) menuItem);

						startActivity(i);
					}
				});*/

		EditText filterText = (EditText) findViewById(R.id.filter_query);
		filterText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				String searchTerm = s.toString();
				Log.d("search", searchTerm);
				mAdapter.getFilter().filter(searchTerm);
				// adapter.notifyDataSetChanged();
				/*
				 * ArrayList<MenuItem> items = new ArrayList<MenuItem>(); for
				 * (int i = 0; i < adapter.getCount(); ++i) {
				 * items.add(adapter.getItem(i)); } Log.d("hi", "count: " +
				 * String.valueOf(items.size()));
				 */
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

	}


	private void setAdapterForMenuItemList() {
		/*
		 * MenuItemListAdapter adapter = new MenuItemListAdapter(this,
		 * R.layout.list_item_items, new ArrayList<MenuItem>());
		 * adapter.setNotifyOnChange(true); setListAdapter(adapter);
		 */
	}

	private ItemListAdapter getListViewAdapter() {
		return ((ItemListAdapter) ((android.widget.HeaderViewListAdapter) mItemList
				.getAdapter()).getWrappedAdapter());

	}

	/**
	 * This method is called when the user clicks the device's Menu button the
	 * first time for this Activity. Android passes in a Menu object that is
	 * populated with items.
	 * 
	 * Builds the menus for editing and inserting, and adds in alternative
	 * actions that registered themselves to handle the MIME types for this
	 * application.
	 * 
	 * @param menu
	 *            A Menu object to which items should be added.
	 * @return True to display the menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_item_list_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.menu_sort_option:
			SortOptionDialogFragment dialog = new SortOptionDialogFragment();
			dialog.setSelectedSortOption(mSortOn);
			dialog.show(getFragmentManager(), "SortOptionDialogFragment");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setSort(final Info sortParam) {

		mSortOn = sortParam;

		SharedPreferences settings = getSharedPreferences("sortOn", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("sortOn", mSortOn.databaseName);
		editor.commit();

		TextView sortParamName = (TextView) findViewById(R.id.sort_param_name);
		sortParamName.setText(mSortOn.readableName);

		sortMenuItems(mMenuItems, sortParam); // setSort is always called from
												// the UI thread, so we can just
												// send in mMenuItems here

		mAdapter.setSortOn(sortParam);
		mAdapter.notifyDataSetChanged();

	}

	// Copies menuItemList into newMenuItemList then sorts it. newMenuItemList
	// needs to be copied back into menuItemList in the UI thread.
	private void sortMenuItems(List<Item> menuItems, final Info sortParam) {

		Collections.sort(menuItems, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				if (sortParam.dataType == double.class
						|| sortParam.dataType == int.class) {
					double it1 = 0;
					double it2 = 0;
					try {
						it1 = item1.getDouble(sortParam);
						it2 = item2.getDouble(sortParam);
					} catch (UnknownInfoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int it1int = (int) (it1 * 10000);
					int it2int = (int) (it2 * 10000);
					if (sortParam.sortOrder == SortOrder.ASCENDING) {
						return it1int - it2int;
					} else {
						return it2int - it1int;
					}
				} else if (sortParam.dataType == String.class) {
					String it1 = "";
					String it2 = "";
					try {
						it1 = item1.getString(sortParam);
						it2 = item2.getString(sortParam);
					} catch (UnknownInfoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (sortParam.sortOrder == SortOrder.ASCENDING) {
						return it1.compareToIgnoreCase(it2);
					} else {
						return it2.compareToIgnoreCase(it1);
					}

				} else {
					Log.d("error", "Unknown sortParam dataType");
				}
				return 0;
			}
		});

	}

	// Fetches from Nutritionix MAX_HITS menu item results, filling in
	// menuItemList, returning total_hits
	private void fetchMenuItems(int lowerBound) {

		if (ConnectionDetector.getOnlineMode(getApplicationContext()) != OnlineMode.DISALLOW_OFFLINE
				&& !mUpdateLocalCacheOnLoad) {
			Log.d(TAG, "Using cached results");

			// Load the menu items from the local sqlite database.
			getMenuItemsFromDatabase();
			if (!newMenuItemList.isEmpty()) {
				PostAllMenuItemsLoaded();
				return;
			}
		}

		// else, we continue on to use nutritionix to make our cache

		ProtocolClient client = new ProtocolClient(
				"https://api.nutritionix.com/v1_1");
		client.setDebug(false);

		JSONObject obj = new JSONObject();
		try {
			obj.put(Constants.TAG_APPLICATION_ID, Constants.VALUE_APPLICATION_ID);
			obj.put(Constants.TAG_APPLICATION_KEY, Constants.VALUE_APPLICATION_KEY);
			obj.put(TAG_OFFSET, current_query_offset);
			obj.put(TAG_LIMIT, VALUE_LIMIT);

			JSONArray fields = new JSONArray();

			fields.put("old_api_id");
			fields.put("item_id");
			fields.put("item_name");
			fields.put("brand_id");
			fields.put("brand_name");
			fields.put("item_description");
			fields.put("updated_at");
			fields.put("nf_ingredient_statement");
			fields.put("nf_water_grams");
			fields.put("nf_calories");
			fields.put("nf_calories_from_fat");
			fields.put("nf_total_fat");
			fields.put("nf_saturated_fat");
			fields.put("nf_trans_fatty_acid");
			fields.put("nf_polyunsaturated_fat");
			fields.put("nf_monounsaturated_fat");
			fields.put("nf_cholesterol");
			fields.put("nf_sodium");
			fields.put("nf_total_carbohydrate");
			fields.put("nf_dietary_fiber");
			fields.put("nf_sugars");
			fields.put("nf_protein");
			fields.put("nf_vitamin_a_dv");
			fields.put("nf_vitamin_c_dv");
			fields.put("nf_calcium_dv");
			fields.put("nf_iron_dv");
			fields.put("nf_refuse_pct");
			fields.put("nf_servings_per_container");
			fields.put("nf_serving_size_qty");
			fields.put("nf_serving_size_unit");
			fields.put("nf_serving_weight_grams");
			fields.put("allergen_contains_milk");
			fields.put("allergen_contains_eggs");
			fields.put("allergen_contains_fish");
			fields.put("allergen_contains_shellfish");
			fields.put("allergen_contains_tree_nuts");
			fields.put("allergen_contains_peanuts");
			fields.put("allergen_contains_wheat");
			fields.put("allergen_contains_soybeans");
			fields.put("allergen_contains_gluten");

			obj.put("fields", fields);

			// Sort results alphabetically so we get items in a consistent order
			JSONObject sort = new JSONObject();
			sort.put("field", "item_name.sortable_na");
			sort.put("order", "asc");
			obj.put("sort", sort);

			JSONObject filters = new JSONObject();
			filters.put("brand_id", mRestaurant.getBrandID());
			obj.put("filters", filters);
		} catch (Exception e) {
		}
		;

		// Creates request with JSONObject as body
		JSONRequestData requestData = new JSONRequestData(obj);
		requestData.addHeader("Content-Type", "application/json");

		// Sends POST request
		client.doPost("/search", requestData, new JSONResponseHandler() {

			@Override
			public void handleResponse(JSONObject jsonObject,
					JSONArray jsonArray) {
				addSingleQueryResult(jsonObject);
			}

		});

	}

	private void addSingleQueryResult(JSONObject jsonObject) {

		try {
			total_hits_to_receive = jsonObject.getInt(TAG_TOTAL);
			JSONArray hitsArray = jsonObject.getJSONArray(TAG_HITS);
			hits_received += hitsArray.length();
			queryResponses.add(hitsArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (hits_received < total_hits_to_receive) {
			current_query_offset += VALUE_LIMIT;
			fetchMenuItems(current_query_offset);
		} else {
			// cacheFullResult(jsonObject, queryResponses);
			// This function knows to save the results to our local database
			// only from this code path, where we got the results from JSON
			PostAllMenuItemsLoaded();
		}
	}

	private void cacheFullResult(JSONObject jsonObject,
			ArrayList<JSONArray> queryResponses) {

		try {
			// First add all queryResponses to the one jsonObject
			JSONArray hitsArray = new JSONArray();

			for (JSONArray arr : queryResponses) {
				for (int i = 0; i < arr.length(); ++i) {
					JSONObject obj = arr.getJSONObject(i);
					hitsArray.put(obj);
				}
			}

			jsonObject.remove(TAG_HITS);
			Log.d("results", String.valueOf(hitsArray.length()));
			jsonObject.put(TAG_HITS, hitsArray);

			FileHelper.writeToFile(MenuActivity.this,
					jsonObject.toString(), "request.txt");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void parseQueryResponses() {
		newMenuItemList.clear();
		for (JSONArray hitsArr : queryResponses) {
			for (int i = 0; i < hitsArr.length(); ++i) {
				JSONObject hitObj;
				try {
					hitObj = hitsArr.getJSONObject(i);
					Item item = new Item(hitObj);

					if (item.getName() == null) {
						Log.d("null", "null itemn name");
					}

					// Check for duplicates (...this shouldn't be necessary. See
					// https://developer.nutritionix.com/forum/topics/duplicate-items-returned
					boolean insert = true;
					for (int j = newMenuItemList.size() - 1; j >= 0; j--) {
						Item existingMenuItem = newMenuItemList.get(j);
						if (existingMenuItem.getItemID().equals(
								item.getItemID())) {
							insert = false;
						} else if (existingMenuItem.getName() == null) {
							Log.d("null", "null itemn name");
						} else if (existingMenuItem.getName().equals(
								item.getName())) {
							if (!existingMenuItem.hasDescription()
									&& item.hasDescription()) {
								// The existing item is incomplete. Remove it
								// and insert this item (below) instead.
								newMenuItemList.remove(j);
							} else if (!existingMenuItem.hasDescription()
									&& !item.hasDescription()) {
								// This item is incomplete. Do not insert it.
								insert = false;
							} else {
								Log.d("", "unknown duplicate");
							}
						}
					}
					if (insert) {
						newMenuItemList.add(item);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void LoadMenuItemsInForeground() {
		pDialog = new ProgressDialog(MenuActivity.this);
		pDialog.setMessage("Loading menu...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();

		queryResponses = new ArrayList<JSONArray>();
		hits_received = 0;
		fetchMenuItems(0);
	}

	private void PostAllMenuItemsLoaded() {
		// Loading menu items in Background Thread and updating our local cache
		new ProcessQueryResults().execute();
	}

	// Update the brand database to let it know what date/time we fetched menu
	// items for this brand from nutritionix
	private void updateBrandDatabase(boolean fetchedFromNutritionix) {
		Intent intent = new Intent("restaurant-loaded");
		intent.putExtra("restaurant", mRestaurant);
		intent.putExtra("fetchedFromNutritionix", fetchedFromNutritionix);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
	}

	protected void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable("menuItems", mMenuItems);
		bundle.putSerializable("sortOn", mSortOn);
		super.onSaveInstanceState(bundle);
	}

	/**
	 * Background Async Task to Load all tracks under one album
	 * */
	class ProcessQueryResults extends AsyncTask<String, String, String> {

		private boolean mGotItemsFromLocalDatabase;

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground(String... args) {
			// If we already filled in newMenuItemList from the local database,
			// we don't have to parse any JSON
			if (newMenuItemList.isEmpty()) {
				parseQueryResponses();
			} else {
				mGotItemsFromLocalDatabase = true;
			}
			sortMenuItems(newMenuItemList, mSortOn);
			// Update the brand database so it knows that we loaded a restaurant's menu 
			// (for history tracking purposes) and whether or not we fetched up to date data
			// from nutritionix at this timestamp
			updateBrandDatabase(!mGotItemsFromLocalDatabase);
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					// Save the items to our local cache
					if (!mGotItemsFromLocalDatabase) {
						saveMenuItemsToDatabase(newMenuItemList);
					}
					// dismiss the dialog after getting all data
					pDialog.dismiss();
					// mikem: Should just notify that underlying data has
					// changed, but I don't know how to do that and this works
					// for now.
					// setAdapterForMenuItemList();
					mMenuItems.clear();
					mMenuItems.addAll(newMenuItemList);
					mAdapter.notifyDataSetChanged();
				}
			});

		}

	}

	@Override
	public void onSortOptionSelected(DialogFragment dialog,
			Info selectedSortOption) {
		setSort(selectedSortOption);

	}

	private void saveMenuItemsToDatabase(List<Item> menuItems) {
		// Gets the data repository in write mode
		SQLiteDatabase db = mMenuItemDbHelper.getWritableDatabase();
		for (Item item : menuItems) {

			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			// Info[] valuesToPut = {Info.ITEM_ID, Info.BRAND_ID,
			// Info.ITEM_NAME, Info.ITEM_DESCRIPTION};
			for (Info info : Info.values()) {
				if (!info.databaseName.isEmpty()) {
					if (info.dataType == String.class) {
						values.put(info.databaseName, item.optGetString(info));
					} else if (info.dataType == double.class) {
						values.put(info.databaseName, item.optGetDouble(info));
					}
				}
			}

			// Insert the new row, returning the primary key value of the new
			// row
			long newRowId = 0;
			try {
				newRowId = db.insertWithOnConflict(MenuItemEntry.TABLE_NAME,
						"null", values, db.CONFLICT_REPLACE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.d("insert", "done");
	}

	private void getMenuItemsFromDatabase() {
		SQLiteDatabase db = mMenuItemDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		Info[] infos = Info.values();
		String[] projection = new String[infos.length];
		for (int i = 0; i < infos.length; ++i) {
			if (!infos[i].databaseName.isEmpty()) {
				projection[i] = infos[i].databaseName;
			}
		}

		// Define 'where' part of query.
		String selection = Info.BRAND_ID.databaseName + " = ?";
		// Specify arguments in placeholder order.
		String[] selectionArgs = { mRestaurant.getBrandID() };

		// How you want the results sorted in the resulting Cursor
		String sortOrder = Info.ITEM_NAME.databaseName + " DESC";

		Cursor c = db.query(MenuItemEntry.TABLE_NAME, // The table to query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		newMenuItemList.clear();
		while (c.moveToNext()) {
			Item item = new Item(c);
			newMenuItemList.add(item);
		}
	}
}