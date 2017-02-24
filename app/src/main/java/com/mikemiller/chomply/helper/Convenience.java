package com.mikemiller.chomply.helper;

import java.util.Date;

import android.text.format.Time;

public class Convenience {
	public static Date getNow() {
		Time now = new Time();
		now.setToNow();
		return new Date(now.toMillis(true));
	}

}
