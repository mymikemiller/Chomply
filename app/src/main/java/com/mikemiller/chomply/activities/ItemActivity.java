package com.mikemiller.chomply.activities;
 
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.R.id;
import com.mikemiller.chomply.R.layout;
import com.mikemiller.chomply.helper.AlertDialogManager;
import com.mikemiller.chomply.helper.ConnectionDetector;
import com.mikemiller.chomply.objects.Item;
 
public class ItemActivity extends Activity {
    // Connection detector
    ConnectionDetector cd;
     
    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();
     
    // Progress Dialog
    private ProgressDialog pDialog;
 
    // Creating JSON Parser object
    //JSONParser jsonParser = new JSONParser();
 
    // tracks JSONArray
    //JSONArray albums = null;

    Item mMenuItem;
 
    // single song JSON url
    // GET parameters album, song
    //private static final String URL_SONG = "http://api.androidhive.info/songs/track.php";
 
    // ALL JSON node names
    //private static final String TAG_NAME = "name";
    //private static final String TAG_DURATION = "duration";
    //private static final String TAG_ALBUM = "album";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
         
        //cd = new ConnectionDetector(getApplicationContext());
          
        // Check if Internet present
        /* mikem: This activity doesn't yet require internet
        if (!cd.isConnectingToInternet() && !IsDebugOfflineMode()) {
            // Internet Connection is not present
            alert.showAlertDialog(ItemActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
        */
         
        Intent i = getIntent();
        mMenuItem = i.getParcelableExtra("menuItem");
        
        TextView txt_item_description = (TextView) findViewById(R.id.item_description);
        
        String description = mMenuItem.getDescription();
        txt_item_description.setText(description);
        

        TextView txt_item_points_plus = (TextView) findViewById(R.id.item_points_plus);
        txt_item_points_plus.setText("ChompScore: " + mMenuItem.getDisplayString(Info.CHOMPSCORE));

        TextView txt_item_net_carbs = (TextView) findViewById(R.id.item_net_carbs);
        txt_item_net_carbs.setText("Net Carbs: " + String.valueOf(mMenuItem.getDisplayString(Info.NET_CARBS)));
        

        
        setNutritionalLabel();
         
        // Change Activity Title with the item name
        setTitle(mMenuItem.getName());
         
        // calling background thread
        //new LoadSingleTrack().execute();
    }
    
    private void setNutritionalLabel() {

        WebView web_nutritional_label = (WebView) findViewById(R.id.nutritionalLabelView);
        
        String params = mMenuItem.getNutritionLabelParams();
        
        AssetManager am = getApplicationContext().getAssets();
        String html = "";
        InputStream is;
		try {
			is = am.open("nutrition_label_html");
			java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		    html = s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		html = html.replace("PARAMS", params);
        
        String mime = "text/html";
        String encoding = "utf-8";
        web_nutritional_label.getSettings().setJavaScriptEnabled(true);
        web_nutritional_label.getSettings().setBuiltInZoomControls(true);
        web_nutritional_label.getSettings().setSupportZoom(true); 
        web_nutritional_label.getSettings().setUseWideViewPort(true);
        web_nutritional_label.loadDataWithBaseURL(null, html, mime, encoding, null);
        
    }
     
    /**
     * Background Async Task to get single song information
     * */
    class LoadSingleTrack extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
    	
        @Override
        protected void onPreExecute() {
        	/*
            super.onPreExecute();
            pDialog = new ProgressDialog(ItemActivity.this);
            pDialog.setMessage("Loading details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
            */
            
        }
 
        /**
         * getting song json and parsing
         * */
        protected String doInBackground(String... args) {
        	/*
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
             
            // post album id, song id as GET parameters
            params.add(new BasicNameValuePair("album", album_id));
            params.add(new BasicNameValuePair("song", song_id));
 
            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_SONG, "GET",
                    params);
 
            // Check your log cat for JSON reponse
            Log.d("Single Track JSON: ", json);
 
            try {
                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    song_name = jObj.getString(TAG_NAME);
                    album_name = jObj.getString(TAG_ALBUM);
                    duration = jObj.getString(TAG_DURATION);                    
                }           
 
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            */
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
        	/*
            // dismiss the dialog after getting song information
            pDialog.dismiss();
             
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                     
                    TextView txt_item_name = (TextView) findViewById(R.id.item_name);
                     
                    // displaying song data in view
                    txt_item_name.setText(song_name);
                    //txt_album_name.setText(Html.fromHtml("<b>Album:</b> " + album_name));
                    //txt_duration.setText(Html.fromHtml("<b>Duration:</b> " + duration));
                     
                    // Change Activity Title with Song title
                    setTitle(song_name);
                }
            });
 */
        }
 
    }
}