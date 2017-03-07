package com.mikemiller.chomply.objects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.joshdholtz.protocol.lib.ProtocolModel;
import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.helper.UnknownInfoException;

public class Item extends ProtocolModel implements Parcelable {

	private String mItemID;
	private String mName;
	private String mDescription;
	
    private static final String TAG_FIELDS = "fields";
    
    private static final String VALUE_LIMIT = "20";
    private static final String VALUE_OFFSET = "0";
    
    HashMap<Constants.Info, Double> values; // This will contain values for everything that can be converted to a double
    HashMap<Constants.Info, String> valueStrings; // This will contain all values a Strings, including the ones in 'values'.

    public Item(String name) {
    	mName = name;
    }
	
    //mikem: this should be moved into NutritionixReader
	public Item(JSONObject hitObj) {
		
		values = new HashMap<Constants.Info, Double>();
		valueStrings = new HashMap<Constants.Info, String>();

		JSONObject fieldsObj;
		try {
			mItemID = hitObj.getString(Constants.Info.ITEM_ID.databaseName);
			fieldsObj = hitObj.getJSONObject(TAG_FIELDS);
			mName = fieldsObj.getString(Constants.Info.ITEM_NAME.databaseName);
			mDescription = fieldsObj.optString(Constants.Info.ITEM_DESCRIPTION.databaseName);
			fillHashMap(mItemID, fieldsObj);
		} catch (JSONException e1) {
			e1.printStackTrace();
			String str = e1.getMessage();
		}
		
		if (getName() == null) {
			Log.d("null", "null itemn name");
		}
	}
	
	//mikem: this should be moved into SQLAccessor
	public Item(Cursor cursor) {
		values = new HashMap<Constants.Info, Double>();
		valueStrings = new HashMap<Constants.Info, String>();
		
		for (Info i : Info.values()) {
			if (!i.databaseName.isEmpty()) {
				if (i.dataType == double.class) {
					double val = cursor.getDouble(cursor.getColumnIndexOrThrow(i.databaseName));
					values.put(i, val);
					valueStrings.put(i, String.valueOf(val));
				} else {
					valueStrings.put(i, cursor.getString(cursor.getColumnIndexOrThrow(i.databaseName)));
				}
			}
		}
		mItemID = valueStrings.get(Info.ITEM_ID);
		mName = valueStrings.get(Info.ITEM_NAME);
		mDescription = valueStrings.get(Info.ITEM_DESCRIPTION);		
	}
	
//region Getters
	public String getItemID() { return mItemID; }
	public String getName() { return mName; }
	public String getDescription() { return mDescription; }
	public boolean hasDescription() { return !(mDescription.equals("null") || mDescription.isEmpty()); }
	public String getUsableDescription() { 
		return !hasDescription() ? mName : mDescription;
	}
	
	@Override
	public String toString() {
	    return mName;
	}
	
	
	// Returns the info value if it exists, otherwise returns NaN
	public double optGetDouble(Constants.Info info) {
		if (info == Info.CHOMPSCORE) {
			double chompScore = getChompScore();
			return chompScore;
		} else if (info == Info.NET_CARBS) {
			return getNetCarbs();
		} else if (values.containsKey(info) && info.dataType == double.class) {
			return values.get(info);
		} else if (info == Info.FIBER) {
			// If we don't have information for fiber, we assume 0
			return 0;
		} else {
			return Double.NaN;
		}
	}

	private void fillHashMap(String itemId, JSONObject fieldsObj) {
		
		valueStrings.put(Info.ITEM_ID, itemId);
		
		Iterator<?> keys = fieldsObj.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
        	Constants.Info infoKey;
			try {
				infoKey = Constants.Info.getInfoFromDatabaseName(key);
								
				// Save the string representation of the value for all keys
	        	valueStrings.put(infoKey, fieldsObj.optString(key));
	        	
	        	// Save the double value for types that can be converted to double
	            double d = fieldsObj.optDouble(key);
	            if (!Double.isNaN(d)) {
	            	values.put(infoKey, d);
	            }
			} catch (UnknownInfoException e) {
				e.printStackTrace();
			}
        }
	}
	
	// mikem: fieldName matches SortParams defined in ItemListActivity. We should really be passed the enum here.
	public double getDouble(Constants.Info info) throws UnknownInfoException {
		if (info != Info.NET_CARBS && info != Info.CHOMPSCORE && !values.containsKey(info)) {
			throw new UnknownInfoException("Info is not known for this Item: " + info.databaseName);
		}
		return optGetDouble(info);
	}
	
	public String getString(Constants.Info info) throws UnknownInfoException {
		if (valueStrings.containsKey(info)) {
			return valueStrings.get(info);
		} else {
			throw new UnknownInfoException("Info is not known or is not a String: " + info.databaseName);
		}
	}
	
	public String optGetString(Constants.Info info) {
		if (valueStrings.containsKey(info)) {
			return valueStrings.get(info);
		} else {
			return "";
		}
	}
	
	public String getDisplayString(Info info) {
		if (info.dataType == String.class) {
			if (info == Info.ITEM_NAME) {
				// The display name for ITEM_NAME is blank so we don't end up repeating this value by displaying it in the sort column
				return "";
			}
			return optGetString(info);
		} else if (info.dataType == double.class) {
			double val = optGetDouble(info);

			if (Double.isNaN(val)) {
				return "?";
			}
			
			if (info == Info.CHOMPSCORE) {
				int chompScoreInt = (int) Math.round(val);
		    	chompScoreInt = (int) Math.max(val, 0);
		    	val = chompScoreInt;
			}
			return String.valueOf((int) Math.round(optGetDouble(info)));
		}
		return ""; // This should never be hit.
	}
	
	public boolean hasInfo(Constants.Info info) {
		return valueStrings.containsKey(info);
	}
	
	public String getNutritionLabelParams() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("showPolyFat", false);
			obj.put("showMonoFat", false);
			obj.put("showIngredients", false);
			obj.put("showServingUnitQuantityTextbox", false);
			obj.put("itemName", mName);
			obj.put("showItemName", false);
			obj.put("showServingsPerContainer", false);
			obj.put("showAmountPerServing", false);
			//obj.put(Constants.Info.SERVING_SIZE_UNIT.nutritionalLabelName, getString(Constants.Info.SERVING_SIZE_UNIT));
			
			obj.put("showVitaminA", hasInfo(Info.VITAMIN_A));
			obj.put("showVitaminC", hasInfo(Info.VITAMIN_C));
			obj.put("showCalcium", hasInfo(Info.CALCIUM));
			obj.put("showIron", hasInfo(Info.IRON));
				
			for(Constants.Info i : Constants.Info.values()) {
				
				String key = i.nutritionalLabelName;
				if (i.dataType == double.class && !key.isEmpty()) {
					double val;
					try {
						val = getDouble(i);
						obj.put(key, val);
					} catch (UnknownInfoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String str = obj.toString();
		return str;
	}

//region Parcelable implementation
	
	// 99.9% of the time you can just ignore this
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(mItemID);
    	out.writeString(mName);
    	out.writeString(mDescription);
    	
    	// Write the 'values' map
    	int n = values.size();
        out.writeInt(n);
        if (n > 0) {
            for (Entry<Info, Double> entry : values.entrySet()) {
                out.writeString(entry.getKey().databaseName);
                double dat = entry.getValue();
                out.writeDouble(dat);
            }
        }
        
        // Write the valueStrings map
        n = valueStrings.size();
        out.writeInt(n);
        if (n > 0) {
            for (Entry<Info, String> entry : valueStrings.entrySet()) {
                out.writeString(entry.getKey().databaseName);
                String dat = entry.getValue();
                out.writeString(dat);
            }
        }
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    private Item(Parcel in) {
    	mItemID = in.readString();
    	mName = in.readString();
    	mDescription = in.readString();
    	
    	int n = in.readInt();

		values = new HashMap<Constants.Info, Double>(n);
    	
    	// Read the 'values' map
        for (int i=0; i<n; i++) {
            String key = in.readString();
            double dat = in.readDouble();
            Info infoKey;
			try {
				infoKey = Constants.Info.getInfoFromDatabaseName(key);
	            values.put(infoKey, dat);
	            Log.d("tet","two");
			} catch (UnknownInfoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        n = in.readInt();

		valueStrings = new HashMap<Constants.Info, String>(n);
        
        
        // Read the 'valueStrings' map
        for (int i=0; i<n; i++) {
            String key = in.readString();
            String dat = in.readString();
            Info infoKey;
			try {
				infoKey = Constants.Info.getInfoFromDatabaseName(key);
	            valueStrings.put(infoKey, dat);
			} catch (UnknownInfoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
//endregion
    
    public double getChompScore() {
    	
    	double protein = optGetDouble(Info.PROTEIN);
		double carbs = optGetDouble(Info.CARBS);
		double fat = optGetDouble(Info.FAT);
		double fiber = optGetDouble(Info.FIBER);
		if (Double.isNaN(protein) || 
			Double.isNaN(carbs) || 
			Double.isNaN(fat) || 
			Double.isNaN(fiber)) {
			return Double.NaN;
		}
    	
    	// This equation is from http://en.wikipedia.org/wiki/Weight_Watchers#PointsPlus
    	double chompScore = ((16 * protein)+
							(19 * carbs) +
							(45 * fat) -
							(14 * fiber)) / 175;
		   	
    	return chompScore;
    }
    
    // Returns NaN if the net carbs can't be calculated
    public double getNetCarbs() {
    	double carbs = optGetDouble(Info.CARBS);
    	double fiber = optGetDouble(Info.FIBER);
    	if (Double.isNaN(carbs) || Double.isNaN(fiber)) {
    		return Double.NaN;
    	}
		return carbs - fiber;
    }
}
