package com.mikemiller.chomply;

import android.app.Application;

import com.joshdholtz.protocol.lib.ProtocolModelFormats;
import com.joshdholtz.protocol.lib.ProtocolModelFormats.MapFormat;

public class ChomplyApplication extends Application {

	@Override
	public void onCreate() {
		ProtocolModelFormats.set("TernaryFormat", new MapFormat() {

			@Override
			public Object format(Object value) {
				if (value == null) {
					return -1;
				}
				String str = (String)value;
				if (str.equals("true")) {
					return 1;
				} else if (str.equals("false")) {
					return 2;
				} else {
					return 3;
				}
			} 
			
		});
		
	}
}
