package com.echo.allscenarioapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Pref {

	private static SharedPreferences sharedPreferences = null;

	public static void openPref(Context context) {

		sharedPreferences = context.getSharedPreferences(Const.PREF_FILE, Context.MODE_PRIVATE);

	}
 /*Fore String Value Store*/
	public static String getStringValue(Context context, String key, String defaultValue) {
		Pref.openPref(context);
		String result = Pref.sharedPreferences.getString(key, defaultValue);
		Pref.sharedPreferences = null;
		return result;
	}
	
	public static void setStringValue(Context context, String key, String value) {
		Pref.openPref(context);
		Editor prefsPrivateEditor = Pref.sharedPreferences.edit();
		prefsPrivateEditor.putString(key, value);
		prefsPrivateEditor.commit();
		prefsPrivateEditor = null;
		Pref.sharedPreferences = null;
	}


 /*For Integer Value*/
	
	public static void setIntValue(Context context, String key, int value) {
		Pref.openPref(context);
		Editor prefsPrivateEditor = Pref.sharedPreferences.edit();
		prefsPrivateEditor.putInt(key, value);
		prefsPrivateEditor.commit();
		prefsPrivateEditor = null;
		Pref.sharedPreferences = null;
	}
	
	public static int getIntValue(Context context, String key, int defaultValue) {
		Pref.openPref(context);
		int result = Pref.sharedPreferences.getInt(key, defaultValue);
		Pref.sharedPreferences = null;
		return result;
	}
	
	
	 /*For boolean Value Store*/
	 
	public static boolean getBooleanValue(Context context, String key, boolean defaultValue) {
		Pref.openPref(context);
		boolean result = Pref.sharedPreferences.getBoolean(key, defaultValue);
		Pref.sharedPreferences = null;
		return result;
	}
	public static void setBooleanValue(Context context, String key, boolean value) {
		Pref.openPref(context);
		Editor prefsPrivateEditor = Pref.sharedPreferences.edit();
		prefsPrivateEditor.putBoolean(key, value);
		prefsPrivateEditor.commit();
		prefsPrivateEditor = null;
		Pref.sharedPreferences = null;
	}
 /*For boolean Value Store*/

	public static Long getLongValue(Context context, String key, long defaultValue) {
		Pref.openPref(context);
		long result = Pref.sharedPreferences.getLong(key, defaultValue);
		Pref.sharedPreferences = null;
		return result;
	}
	public static void setLongValue(Context context, String key, long value) {
		Pref.openPref(context);
		Editor prefsPrivateEditor = Pref.sharedPreferences.edit();
		prefsPrivateEditor.putLong(key, value);
		prefsPrivateEditor.commit();
		prefsPrivateEditor = null;
		Pref.sharedPreferences = null;
	}

	
	/*For Remove variable from pref*/
	
	public static void removeValue(Context context, String key) {
		Pref.openPref(context);
		Editor prefsPrivateEditor = Pref.sharedPreferences.edit();
		prefsPrivateEditor.remove(key);
		prefsPrivateEditor.commit();
		prefsPrivateEditor = null;
		Pref.sharedPreferences = null;
	}
	
	
	

}
