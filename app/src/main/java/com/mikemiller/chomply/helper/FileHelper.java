package com.mikemiller.chomply.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHelper {

	public static void writeToFile(Activity a, String data, String filename) {
	    try {
	    	File sdCard = Environment.getExternalStorageDirectory();
	    	File dir = new File ("/storage/extSdCard/ChomplyTest/");
	    	dir.mkdirs();
	    	String d = dir.getPath();
	    	File file = new File(dir, filename);
	    	
	    	

	    	FileOutputStream f = new FileOutputStream(file);
	    	
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(f);
	        outputStreamWriter.write(data);
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    } 
	}


	public static String readFromFile(Activity a, String filename) {

	    String ret = "";

	    try {
	        InputStream inputStream = a.openFileInput(filename);

	        if ( inputStream != null ) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            StringBuilder stringBuilder = new StringBuilder();

	            while ( (receiveString = bufferedReader.readLine()) != null ) {
	                stringBuilder.append(receiveString);
	            }

	            inputStream.close();
	            ret = stringBuilder.toString();
	        }
	    }
	    catch (FileNotFoundException e) {
	        Log.e("login activity", "File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e("login activity", "Can not read file: " + e.toString());
	    }

	    return ret;
	}
}
