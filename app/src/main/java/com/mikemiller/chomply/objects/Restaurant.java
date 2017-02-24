package com.mikemiller.chomply.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.joshdholtz.protocol.lib.ProtocolModel;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.localdata.RestaurantContract.BrandEntry;

public class Restaurant implements Parcelable {

	private final String mBrandID; // This is final because it is used in hashCode() and we wouldn't want the hash code to change if this Restaurant is used in a hash map
	private String mName;
	private Date mUpdatedAt; // The date/time that the list of items were last fetched from nutritionix for this restaurant
	private Date mLastLoadedAt; // This effects the order the restaurants are displayed in the history list
	private boolean mIsValid;
	// When more members are added, don't forget to modify the Parcelable methods below.
	
    private static final String TAG_FIELDS = "fields";
    
    private static final String VALUE_LIMIT = "20";
    private static final String VALUE_OFFSET = "0";
    
    // Creates an invalid restaurant with a custom name for display in restaurant lists
    public Restaurant(String name) {
    	mName = name;
    	mBrandID = "";
    	mUpdatedAt = new Date(0);
    	mLastLoadedAt = new Date(0);
    	mIsValid = false;
    }
    
    public Restaurant(String name, String brandID, Date updatedAt, Date lastLoadedAt) {
    	mName = name;
    	mBrandID = brandID;
    	mUpdatedAt = updatedAt;
    	mLastLoadedAt = lastLoadedAt;
    	mIsValid = true;
    }
	
	public Restaurant(JSONObject hitObj) {
		String brandID = "";
		JSONObject fieldsObj;
		try {
			brandID = hitObj.getString("_id");
			fieldsObj = hitObj.getJSONObject(TAG_FIELDS);
			mName = fieldsObj.getString("name");
		} catch (JSONException e1) {
			e1.printStackTrace();
			String str = e1.getMessage();
		}
		
		if (getName() == null) {
			Log.d("null", "null brand name");
		}
		
		mBrandID = brandID;
		mUpdatedAt = new Date(0);
		mLastLoadedAt = new Date(0);
		mIsValid = true;
	}
	

	public Restaurant(Cursor cursor) {
		mBrandID = cursor.getString(cursor
				.getColumnIndexOrThrow(Info.BRAND_ID.databaseName));
		mName = cursor.getString(cursor
				.getColumnIndexOrThrow(Info.BRAND_NAME.databaseName));
		mUpdatedAt = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(BrandEntry.UPDATED_COLUMN_NAME)));
		mLastLoadedAt = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(BrandEntry.LAST_LOADED_COLUMN_NAME)));
		mIsValid = true;
		
	}
	
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(mBrandID).
            toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Restaurant))
            return false;

        Restaurant rhs = (Restaurant) obj;
        // Note that this will cause all "invalid" restaurants to be equal because they have a blank BrandID. Might need to change that at some point.
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(mBrandID, rhs.mBrandID).
            isEquals();
    }
	
	
//region Getters
	public String getBrandID() { return mBrandID; }
	public String getName() { return mName; }
	public Date getUpdatedAt() { return mUpdatedAt; }
	public void setUpdatedAt(Date value) { mUpdatedAt = value; }
	public Date getLastLoadedAt() { return mLastLoadedAt; }
	public void setLastLoadedAt(Date value) { mLastLoadedAt = value; }
	public boolean isValid() { return mIsValid; }
	
	@Override
	public String toString() {
	    return mName;
	}
	

	//Returns true if this restaurant needs to update its cache of items from Nutritionix.
    public boolean needsFetch() {
		Calendar cal = Calendar.getInstance(); // This is initialized to the
												// current date
		// cal.setTime(dateInstance);
		cal.add(Calendar.DATE, -1);
		Date allowedCacheDate = cal.getTime();
		long allowedCacheTime = allowedCacheDate.getTime();

		if (getUpdatedAt().getTime() < allowedCacheTime) {
			return true;
		}
		return false;
    }
    
    public boolean hasNeverFetched() {
    	return getUpdatedAt().equals(new Date(0));
    }
	
	// 99.9% of the time you can just ignore this
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(mBrandID);
    	out.writeString(mName);
    	out.writeLong(mUpdatedAt.getTime());
    	out.writeLong(mLastLoadedAt.getTime());
    	out.writeValue(mIsValid);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Restaurant> CREATOR = new Parcelable.Creator<Restaurant>() {
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with its values
    private Restaurant(Parcel in) {
    	mBrandID = in.readString();
    	mName = in.readString();
    	mUpdatedAt = new Date(in.readLong());
    	mLastLoadedAt = new Date(in.readLong());
    	mIsValid = (Boolean)in.readValue(null);
    }
    
//endregion
    
}
