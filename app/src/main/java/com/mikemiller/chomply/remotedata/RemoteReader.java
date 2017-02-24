package com.mikemiller.chomply.remotedata;

import java.util.ArrayList;

import android.app.DialogFragment;

import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.objects.Restaurant;

abstract class RemoteReader {
	
	RemoteDataManager mManager;
	
	RemoteReader(RemoteDataManager manager) {
		mManager = manager;
	}
	
	abstract void fetchRestaurantsAsync(String searchTerm);
}
