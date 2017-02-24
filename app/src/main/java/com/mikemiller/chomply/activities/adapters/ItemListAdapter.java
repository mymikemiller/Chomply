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

import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.R.id;
import com.mikemiller.chomply.helper.FilterableAdapter;
import com.mikemiller.chomply.objects.Item;

public class ItemListAdapter extends FilterableAdapter<Item, String> {
	
	private Constants.Info mSortOn = Constants.Info.CALORIES;
	private int layoutResourceId;
	private static final String LOG_TAG = "MenuItemListAdapter";
	

	public ItemListAdapter(Context context, int resourceId,
			List<Item> objects) {
		super(context, resourceId, objects);
		
		layoutResourceId = resourceId;
	}
	
	public void setSortOn(Info sortOn) { mSortOn = sortOn; }
	/*
	public void refreshView(int position) {
		View v = getView(position, null, null);

        TextView sort_value = (TextView) v.findViewById(R.id.sort_value);

        MenuItem menuItem = getItem(position);
        double sortValue;
		try {
			sortValue = menuItem.getDouble(mSortOn);
	        sort_value.setText(String.valueOf(sortValue));
		} catch (UnknownInfoException e) {
	        sort_value.setText("-");
		}
	}
	*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    try {
	        Item menuItem = getItem(position);
	        View v = null;
	        if (convertView == null) {
	            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	            v = inflater.inflate(layoutResourceId, null);
	
	        } else {
	            v = convertView;
	        }
	
	        TextView item_name = (TextView) v.findViewById(R.id.item_name);
	        TextView sort_value = (TextView) v.findViewById(R.id.sort_value);
	
	        // We use fromHtml here to properly display things like the registered symbol for &Reg;
	        item_name.setText(Html.fromHtml(menuItem.getName()));  
	        sort_value.setText(menuItem.getDisplayString(mSortOn));
	        
	        v.setTag(menuItem);
	
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
    protected boolean passesFilter(Item object, String searchTerm) {
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