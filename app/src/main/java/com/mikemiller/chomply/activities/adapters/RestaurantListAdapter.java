package com.mikemiller.chomply.activities.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.R.id;
import com.mikemiller.chomply.helper.FilterableAdapter;
import com.mikemiller.chomply.objects.Restaurant;

public class RestaurantListAdapter extends FilterableAdapter<Restaurant, String> {
	
	private int layoutResourceId;
	private static final String LOG_TAG = "RestaurantListAdapter";
	

	public RestaurantListAdapter(Context context, int resourceId,
			List<Restaurant> objects) {
		super(context, resourceId, objects);
		
		layoutResourceId = resourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    try {
	        Restaurant restaurant = getItem(position);
	        View v = null;
	        if (convertView == null) {
	            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	            v = inflater.inflate(layoutResourceId, null);
	
	        } else {
	            v = convertView;
	        }
	
	        TextView brand_name = (TextView) v.findViewById(R.id.brand_name);
	        TextView brand_id = (TextView) v.findViewById(R.id.brand_id);
	
	        // We use fromHtml here to properly display things like the registered symbol for &Reg;
	        brand_name.setText(Html.fromHtml(restaurant.getName()));
	        brand_id.setText(restaurant.getBrandID());
	        
	        v.setTag(restaurant);
	
	        return v;
	    } catch (Exception ex) {
	        Log.e(LOG_TAG, "error", ex);
	        return null;
	    }
	}
	

	@Override
    protected String prepareFilter(CharSequence seq) {
 
        /* The object we return here will be passed to passesFilter() as constraint.
        ** This method is called only once per filter run. The same constraint is
        ** then used to decide upon all objects in the data set.
        */
 
        return seq.toString().toLowerCase();
    }
 
    @Override
    protected boolean passesFilter(Restaurant object, String searchTerm) {
        String itemName = object.toString().toLowerCase();
         
        if (itemName.startsWith(searchTerm))
            return true;
         
        else {
            final String[] words = searchTerm.split(" ");
             
            for (int i = 0; i < words.length; i++) {
                if (itemName.contains(words[i]))
                    return true;
            }
        }
         
        return false;
    }
}