package com.gpswox.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class AwesomeAdapter<T> extends ArrayAdapter<T>
{
	public AwesomeAdapter(Context context)
	{
		super(context, 0);
	}

    public AwesomeAdapter(Context context, ArrayList<T> array)
    {
        super(context, 0);
        setArray(array);
    }


	public void setArray(ArrayList<T> array)
	{
		clear();
		for(T object : array)
			add(object);
	}
	public ArrayList<T> getArray()
	{
		ArrayList<T> array = new ArrayList<T>();
		for(int i = 0; i < getCount(); i++)
			array.add(getItem(i));
		return array;
	}

	private LayoutInflater inflater;
	protected LayoutInflater getLayoutInflater()
	{
		if(inflater == null)
			inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater;
	}

	protected String getString(int resId)
	{
		return getContext().getString(resId);
	}
}
