package com.mikemiller.chomply.helper;

import com.mikemiller.chomply.activities.RestaurantListActivity;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
  
public class ConnectionDetector {

	public enum OnlineMode {
		NORMAL, FORCE_OFFLINE, DISALLOW_OFFLINE
	}
	
	private static ConnectionDetector mInstance;
  
    private Context mContext;
    private OnlineMode mOnlineMode = OnlineMode.NORMAL;
    AlertDialogManager mAlert;
  
    private ConnectionDetector(Context context){
        this.mContext = context;

        // Alert dialog manager
        mAlert = new AlertDialogManager();
    }
    
    private void showAlert(Activity activity) {
    	//mContext used to be RestaurantListActivity.this
        mAlert.showAlertDialog(activity, "Internet Connection Error",
                "Please connect to working Internet connection", false);
    }
    
    private static ConnectionDetector getInstance(Context context)
    {
        if (mInstance == null)
        {
        	mInstance = new ConnectionDetector(context);
        }
        return mInstance;
    }
    
    public static boolean IsConnected(Context context, Activity activity, boolean notifyUserIfNotConnected) {
    	ConnectionDetector cd = getInstance(context);
    	boolean isConnected = cd.isConnectingToInternet();
    	if (!isConnected && notifyUserIfNotConnected) {
    		cd.showAlert(activity);
    	}
    	return isConnected;    	
    }
  
    /**
     * Checking for all possible internet providers
     * **/
    private boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
  
          }
          return false;
    }
    
    public static OnlineMode getOnlineMode(Context context) {
    	return getInstance(context).mOnlineMode;
    }
    public static void setOnlineMode(Context context, OnlineMode onlineMode) {
    	getInstance(context).mOnlineMode = onlineMode;
    }
}