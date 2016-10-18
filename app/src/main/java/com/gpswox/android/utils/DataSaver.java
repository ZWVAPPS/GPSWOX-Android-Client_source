package com.gpswox.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class DataSaver
{
	private static DataSaver instance;
	public static DataSaver getInstance(Context context)
	{
		if(instance == null)
			instance = new DataSaver(context);
		return instance;
	}
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private static final String PREFERENCES_NAME = "savedata";
	public DataSaver(Context context)
	{
		prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
		editor = prefs.edit();
	}
	public String getPreferencesName()
	{
		return PREFERENCES_NAME;
	}
	public void save(String key, Object object)
	{
		String string = new Gson().toJson(object);
		editor.putString(key, string);
		editor.commit();
	}
	public Object load(String key)
	{
		String string = prefs.getString(key, "");
		Object object = new Gson().fromJson(string, Object.class);
		return object;
	}
	public Object load(String key, Type type)
	{
		String string = prefs.getString(key, "");
		//Log.d("DataSaver", key + ":" + string);
		Object object = new Gson().fromJson(string, type);
		return object;
	}
}
