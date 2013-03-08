package com.mynameistodd.autovolume;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class MyArrayAdapter extends ArrayAdapter<Alarm> {

	public MyArrayAdapter(Context context, int textViewResourceId,
			List<Alarm> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.getView(position, convertView, parent);
	}

}
